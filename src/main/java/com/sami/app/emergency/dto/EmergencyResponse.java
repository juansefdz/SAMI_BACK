package com.sami.app.emergency.dto;

import java.time.Instant;

import com.sami.app.ambulance.dto.AmbulanceSummary;
import com.sami.app.emergency.EmergencyStatus;
import com.sami.app.hospital.dto.HospitalSummary;
import com.sami.app.user.dto.UserSummary;

public record EmergencyResponse(
        Long id,
        Double latitude,
        Double longitude,
        String address,
        String description,
        Integer patientsCount,
        Integer severity,
        String anamnesis,
        EmergencyStatus status,
        UserSummary client,
        AmbulanceSummary ambulance,
        HospitalSummary hospital,
        Instant createdAt,
        Instant updatedAt) {
}
