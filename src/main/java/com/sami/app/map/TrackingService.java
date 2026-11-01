package com.sami.app.map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sami.app.account.Account;
import com.sami.app.ambulance.Ambulance;
import com.sami.app.emergency.Emergency;
import com.sami.app.emergency.EmergencyService;
import com.sami.app.emergency.EmergencyStatus;
import com.sami.app.hospital.Hospital;
import com.sami.app.map.dto.Coordinates;
import com.sami.app.map.dto.RouteEstimate;
import com.sami.app.map.dto.TrackingResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TrackingService {

    private final EmergencyService emergencyService;
    private final RoutingService routingService;

    @Transactional(readOnly = true)
    public TrackingResponse track(Long emergencyId, Account account) {
        Emergency emergency = emergencyService.getAccessible(emergencyId, account);
        Ambulance ambulance = emergency.getAmbulance();
        Hospital hospital = emergency.getHospital();

        Coordinates patient = new Coordinates(emergency.getLatitude(), emergency.getLongitude());
        Coordinates ambulancePos = ambulance == null ? null
                : new Coordinates(ambulance.getLatitude(), ambulance.getLongitude());
        Coordinates hospitalPos = hospital == null ? null
                : new Coordinates(hospital.getLatitude(), hospital.getLongitude());

        EmergencyStatus status = emergency.getStatus();
        // ASSIGNED: la ambulancia va hacia el paciente; IN_PROGRESS: va con el paciente al hospital.
        RouteEstimate etaToPatient = status == EmergencyStatus.ASSIGNED
                ? routingService.estimate(ambulancePos, patient).orElse(null)
                : null;
        RouteEstimate etaToHospital = switch (status) {
            case ASSIGNED -> routingService.estimate(patient, hospitalPos).orElse(null);
            case IN_PROGRESS -> routingService.estimate(ambulancePos, hospitalPos).orElse(null);
            default -> null;
        };

        return new TrackingResponse(
                emergency.getId(),
                emergency.getStatus(),
                patient,
                ambulancePos,
                ambulance == null ? null : ambulance.getLastLocationAt(),
                hospitalPos,
                etaToPatient,
                etaToHospital);
    }
}
