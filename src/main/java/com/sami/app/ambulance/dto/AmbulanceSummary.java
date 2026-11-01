package com.sami.app.ambulance.dto;

import java.time.Instant;

import com.sami.app.ambulance.AmbulanceType;

public record AmbulanceSummary(
        Long id,
        String plate,
        AmbulanceType type,
        Double latitude,
        Double longitude,
        Instant lastLocationAt) {
}
