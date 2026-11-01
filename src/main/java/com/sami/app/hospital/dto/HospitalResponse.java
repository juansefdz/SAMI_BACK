package com.sami.app.hospital.dto;

import com.sami.app.hospital.ComplexityLevel;

public record HospitalResponse(
        Long id,
        String name,
        String address,
        Double latitude,
        Double longitude,
        ComplexityLevel complexityLevel,
        String specialty) {
}
