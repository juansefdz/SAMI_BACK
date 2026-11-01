package com.sami.app.emergency;

import java.util.EnumSet;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sami.app.account.Account;
import com.sami.app.account.Role;
import com.sami.app.ambulance.Ambulance;
import com.sami.app.ambulance.AmbulanceService;
import com.sami.app.ambulance.AmbulanceStatus;
import com.sami.app.common.exception.BadRequestException;
import com.sami.app.common.exception.ConflictException;
import com.sami.app.common.exception.ForbiddenException;
import com.sami.app.common.exception.ResourceNotFoundException;
import com.sami.app.common.web.PageResponse;
import com.sami.app.emergency.dto.AnamnesisRequest;
import com.sami.app.emergency.dto.EmergencyAssignRequest;
import com.sami.app.emergency.dto.EmergencyCreateRequest;
import com.sami.app.emergency.dto.EmergencyResponse;
import com.sami.app.hospital.HospitalService;
import com.sami.app.realtime.EmergencyEvent;
import com.sami.app.user.User;
import com.sami.app.user.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmergencyService {

    private static final EnumSet<EmergencyStatus> OPEN_STATUSES =
            EnumSet.of(EmergencyStatus.PENDING, EmergencyStatus.ASSIGNED, EmergencyStatus.IN_PROGRESS);

    private final EmergencyRepository emergencyRepository;
    private final EmergencyMapper emergencyMapper;
    private final UserService userService;
    private final AmbulanceService ambulanceService;
    private final HospitalService hospitalService;
    private final ApplicationEventPublisher events;

    // ---------- Consultas ----------

    public PageResponse<EmergencyResponse> findAll(EmergencyStatus status, Pageable pageable) {
        Page<Emergency> page = status == null
                ? emergencyRepository.findAllBy(pageable)
                : emergencyRepository.findByStatus(status, pageable);
        return PageResponse.of(page, emergencyMapper::toResponse);
    }

    public PageResponse<EmergencyResponse> findMine(Account account, Pageable pageable) {
        User client = userService.getByAccount(account.getId());
        return PageResponse.of(emergencyRepository.findByClient_Id(client.getId(), pageable),
                emergencyMapper::toResponse);
    }

    public EmergencyResponse findById(Long id, Account account) {
        return emergencyMapper.toResponse(getAccessible(id, account));
    }

    /** Emergencia activa (asignada o en curso) de la ambulancia del tripulante. */
    public Optional<EmergencyResponse> findCurrentForCrew(Account account) {
        return ambulanceService.getByCrewAccount(account.getId())
                .flatMap(a -> emergencyRepository.findFirstByAmbulance_IdAndStatusInOrderByCreatedAtDesc(
                        a.getId(), EmergencyStatus.ACTIVE))
                .map(emergencyMapper::toResponse);
    }

    /**
     * Devuelve la emergencia si el usuario puede verla: el administrador, el
     * cliente que la creó o la tripulación de la ambulancia asignada.
     */
    public Emergency getAccessible(Long id, Account account) {
        Emergency emergency = get(id);
        if (account.getRole() == Role.ADMIN) {
            return emergency;
        }
        Long userId = userService.getByAccount(account.getId()).getId();
        boolean isOwner = emergency.getClient().getId().equals(userId);
        boolean isCrew = emergency.getAmbulance() != null && emergency.getAmbulance().hasCrewMember(userId);
        if (!isOwner && !isCrew) {
            throw new ForbiddenException("No tiene acceso a esta emergencia");
        }
        return emergency;
    }

    // ---------- Comandos ----------

    @Transactional
    public EmergencyResponse create(EmergencyCreateRequest request, Account account) {
        User client = userService.getByAccount(account.getId());
        if (emergencyRepository.existsByClient_IdAndStatusIn(client.getId(), OPEN_STATUSES)) {
            throw new ConflictException("Ya tiene una emergencia en curso");
        }
        Emergency emergency = Emergency.builder()
                .latitude(request.latitude())
                .longitude(request.longitude())
                .address(request.address())
                .description(request.description().trim())
                .patientsCount(request.patientsCount())
                .severity(request.severity())
                .status(EmergencyStatus.PENDING)
                .client(client)
                .build();
        Emergency saved = emergencyRepository.save(emergency);
        publish(saved, EmergencyEvent.Type.CREATED);
        return emergencyMapper.toResponse(saved);
    }

    /** El despachador asigna ambulancia y hospital destino. */
    @Transactional
    public EmergencyResponse assign(Long id, EmergencyAssignRequest request) {
        Emergency emergency = get(id);
        if (emergency.getStatus() != EmergencyStatus.PENDING && emergency.getStatus() != EmergencyStatus.ASSIGNED) {
            throw new BadRequestException("Solo se puede asignar una emergencia pendiente o ya asignada");
        }
        Ambulance ambulance = ambulanceService.get(request.ambulanceId());
        boolean sameAmbulance = emergency.getAmbulance() != null
                && emergency.getAmbulance().getId().equals(ambulance.getId());
        if (!sameAmbulance && ambulance.getStatus() != AmbulanceStatus.AVAILABLE) {
            throw new ConflictException("La ambulancia %s no está disponible".formatted(ambulance.getPlate()));
        }
        if (ambulance.getDriver() == null) {
            throw new BadRequestException("La ambulancia no tiene conductor asignado");
        }
        if (!sameAmbulance) {
            releaseAmbulance(emergency);
            ambulance.setStatus(AmbulanceStatus.BUSY);
        }
        emergency.setAmbulance(ambulance);
        emergency.setHospital(hospitalService.get(request.hospitalId()));
        emergency.setStatus(EmergencyStatus.ASSIGNED);
        publish(emergency, EmergencyEvent.Type.STATUS);
        return emergencyMapper.toResponse(emergency);
    }

    @Transactional
    public EmergencyResponse changeStatus(Long id, EmergencyStatus next, Account account) {
        Emergency emergency = getAccessible(id, account);
        EmergencyStatus current = emergency.getStatus();

        if (next == EmergencyStatus.ASSIGNED) {
            throw new BadRequestException("Use PATCH /emergencies/{id}/assignment para asignar ambulancia");
        }
        if (!current.canTransitionTo(next)) {
            throw new BadRequestException("Transición no permitida: %s -> %s".formatted(current, next));
        }
        checkRoleCanSetStatus(account.getRole(), next);

        emergency.setStatus(next);
        if (next.isFinal()) {
            releaseAmbulance(emergency);
        }
        publish(emergency, EmergencyEvent.Type.STATUS);
        return emergencyMapper.toResponse(emergency);
    }

    @Transactional
    public EmergencyResponse updateAnamnesis(Long id, AnamnesisRequest request, Account account) {
        Emergency emergency = getAccessible(id, account);
        if (emergency.getStatus() == EmergencyStatus.PENDING || emergency.getStatus() == EmergencyStatus.CANCELED) {
            throw new BadRequestException("La emergencia aún no ha sido atendida");
        }
        emergency.setAnamnesis(request.anamnesis().trim());
        publish(emergency, EmergencyEvent.Type.UPDATED);
        return emergencyMapper.toResponse(emergency);
    }

    @Transactional
    public void delete(Long id) {
        Emergency emergency = get(id);
        releaseAmbulance(emergency);
        emergencyRepository.delete(emergency);
        publish(emergency, EmergencyEvent.Type.DELETED);
    }

    // ---------- Auxiliares ----------

    private Emergency get(Long id) {
        return emergencyRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Emergencia", id));
    }

    private void checkRoleCanSetStatus(Role role, EmergencyStatus next) {
        boolean allowed = switch (role) {
            case ADMIN -> true;
            case CLIENT -> next == EmergencyStatus.CANCELED;
            case DRIVER, EMT -> next == EmergencyStatus.IN_PROGRESS || next == EmergencyStatus.COMPLETED;
        };
        if (!allowed) {
            throw new ForbiddenException("Su rol no puede cambiar la emergencia a " + next);
        }
    }

    private void publish(Emergency emergency, EmergencyEvent.Type type) {
        events.publishEvent(new EmergencyEvent(emergency.getId(), type));
    }

    private void releaseAmbulance(Emergency emergency) {
        Ambulance ambulance = emergency.getAmbulance();
        if (ambulance != null && ambulance.getStatus() == AmbulanceStatus.BUSY) {
            ambulance.setStatus(AmbulanceStatus.AVAILABLE);
        }
    }
}
