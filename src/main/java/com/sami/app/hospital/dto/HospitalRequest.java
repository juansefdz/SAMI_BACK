package com.sami.app.hospital.dto;

import com.sami.app.hospital.ComplexityLevel;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record HospitalRequest(
        @Schema(example = "Hospital General de Medellín") @NotBlank @Size(max = 100) String name,
        @Schema(example = "Cra. 48 #32-102, Medellín") @NotBlank @Size(max = 200) String address,
        @Schema(example = "6.2350") @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
        @Schema(example = "-75.5690") @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude,
        @Schema(example = "LEVEL_III") @NotNull ComplexityLevel complexityLevel,
        @Schema(example = "Cardiología") @NotBlank @Size(max = 150) String specialty) {
}
