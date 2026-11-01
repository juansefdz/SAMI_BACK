package com.sami.app.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Solicitud de ambulancia sin registro previo: solo lo mínimo para atender. */
public record GuestEmergencyRequest(
        @Schema(example = "Laura") @NotBlank @Size(max = 50) String firstName,
        @Schema(example = "Gómez") @Size(max = 50) String lastName,
        @Schema(example = "3004567890") @NotBlank @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Teléfono inválido") String phone,
        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude,
        @Size(max = 200) String address,
        @NotBlank @Size(max = 500) String description,
        @NotNull @Min(1) @Max(20) Integer patientsCount,
        @NotNull @Min(1) @Max(5) Integer severity) {
}
