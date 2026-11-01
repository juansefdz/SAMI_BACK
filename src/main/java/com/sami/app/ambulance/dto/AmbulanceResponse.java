package com.sami.app.ambulance.dto;

import java.time.Instant;

import com.sami.app.ambulance.AmbulanceStatus;
import com.sami.app.ambulance.AmbulanceType;
import com.sami.app.user.dto.UserSummary;

public record AmbulanceResponse(
        Long id,
        String plate,
        AmbulanceType type,
        AmbulanceStatus status,
        Double latitude,
        Double longitude,
        Instant lastLocationAt,
        UserSummary driver,
        UserSummary emt) {
}
