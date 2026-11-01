package com.sami.app.ambulance.dto;

import com.sami.app.ambulance.AmbulanceStatus;
import com.sami.app.ambulance.AmbulanceType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AmbulanceRequest(
        @Schema(example = "ABC123") @NotBlank @Size(max = 10) String plate,
        @Schema(example = "BASIC") @NotNull AmbulanceType type,
        @Schema(example = "AVAILABLE") @NotNull AmbulanceStatus status,
        @Schema(example = "6.2193") @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
        @Schema(example = "-75.5833") @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude,
        @Schema(description = "Usuario con rol DRIVER", example = "2") @Positive Long driverId,
        @Schema(description = "Usuario con rol EMT", example = "3") @Positive Long emtId) {
}
