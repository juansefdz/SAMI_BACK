package com.sami.app.ambulance;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sami.app.account.Account;
import com.sami.app.account.Role;
import com.sami.app.ambulance.dto.AmbulanceRequest;
import com.sami.app.ambulance.dto.AmbulanceResponse;
import com.sami.app.ambulance.dto.LocationUpdateRequest;
import com.sami.app.common.exception.BadRequestException;
import com.sami.app.common.exception.ConflictException;
import com.sami.app.common.exception.ForbiddenException;
import com.sami.app.common.exception.ResourceNotFoundException;
import com.sami.app.common.web.PageResponse;
import com.sami.app.emergency.EmergencyRepository;
import com.sami.app.emergency.EmergencyStatus;
import com.sami.app.realtime.EmergencyEvent;
import com.sami.app.user.User;
import com.sami.app.user.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AmbulanceService {

    private final AmbulanceRepository ambulanceRepository;
    private final AmbulanceMapper ambulanceMapper;
    private final UserService userService;
    private final EmergencyRepository emergencyRepository;
    private final ApplicationEventPublisher events;

    public PageResponse<AmbulanceResponse> findAll(AmbulanceStatus status, Pageable pageable) {
        Page<Ambulance> page = status == null
                ? ambulanceRepository.findAllBy(pageable)
                : ambulanceRepository.findByStatus(status, pageable);
        return PageResponse.of(page, ambulanceMapper::toResponse);
    }

    public AmbulanceResponse findById(Long id) {
        return ambulanceMapper.toResponse(get(id));
    }

    /** Ambulancia asignada al conductor/paramédico autenticado. */
    public AmbulanceResponse findMine(UUID accountId) {
        return ambulanceMapper.toResponse(getByCrewAccount(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("No tiene una ambulancia asignada")));
    }

    @Transactional
    public AmbulanceResponse create(AmbulanceRequest request) {
        if (ambulanceRepository.existsByPlateIgnoreCase(request.plate().trim())) {
            throw new ConflictException("Ya existe una ambulancia con la placa " + request.plate());
        }
        Ambulance ambulance = new Ambulance();
        apply(ambulance, request);
        return ambulanceMapper.toResponse(ambulanceRepository.save(ambulance));
    }

    @Transactional
    public AmbulanceResponse update(Long id, AmbulanceRequest request) {
        if (ambulanceRepository.existsByPlateIgnoreCaseAndIdNot(request.plate().trim(), id)) {
            throw new ConflictException("Ya existe una ambulancia con la placa " + request.plate());
        }
        Ambulance ambulance = get(id);
        apply(ambulance, request);
        return ambulanceMapper.toResponse(ambulance);
    }

    /** Solo la tripulación de la ambulancia puede reportar su posición. */
    @Transactional
    public AmbulanceResponse updateLocation(Long id, LocationUpdateRequest request, Account actor) {
        Ambulance ambulance = get(id);
        User crewMember = userService.getByAccount(actor.getId());
        if (!ambulance.hasCrewMember(crewMember.getId())) {
            throw new ForbiddenException("No pertenece a la tripulación de esta ambulancia");
        }
        ambulance.setLatitude(request.latitude());
        ambulance.setLongitude(request.longitude());
        ambulance.setLastLocationAt(Instant.now());
        emergencyRepository.findFirstByAmbulance_IdAndStatusInOrderByCreatedAtDesc(id, EmergencyStatus.ACTIVE)
                .ifPresent(e -> events.publishEvent(new EmergencyEvent(e.getId(), EmergencyEvent.Type.LOCATION)));
        return ambulanceMapper.toResponse(ambulance);
    }

    @Transactional
    public void delete(Long id) {
        ambulanceRepository.delete(get(id));
    }

    public Ambulance get(Long id) {
        return ambulanceRepository.findWithCrewById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ambulancia", id));
    }

    public Optional<Ambulance> getByCrewAccount(UUID accountId) {
        User user = userService.getByAccount(accountId);
        return ambulanceRepository.findByCrewMember(user.getId());
    }

    private void apply(Ambulance ambulance, AmbulanceRequest request) {
        ambulanceMapper.update(ambulance, request);
        ambulance.setDriver(resolveCrew(request.driverId(), Role.DRIVER, ambulance.getId()));
        ambulance.setEmt(resolveCrew(request.emtId(), Role.EMT, ambulance.getId()));
    }

    /** Valida el rol del tripulante y que no esté asignado a otra ambulancia. */
    private User resolveCrew(Long userId, Role expectedRole, Long ambulanceId) {
        if (userId == null) {
            return null;
        }
        User user = userService.getWithAccount(userId);
        if (user.getAccount().getRole() != expectedRole) {
            throw new BadRequestException("El usuario %d no tiene el rol %s".formatted(userId, expectedRole));
        }
        ambulanceRepository.findByCrewMember(userId)
                .filter(other -> !other.getId().equals(ambulanceId))
                .ifPresent(other -> {
                    throw new ConflictException("El usuario %d ya está asignado a la ambulancia %s"
                            .formatted(userId, other.getPlate()));
                });
        return user;
    }
}
