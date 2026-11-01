package com.sami.app.emergency.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EmergencyCreateRequest(
        @Schema(example = "6.2193") @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
        @Schema(example = "-75.5833") @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude,
        @Schema(example = "Cl. 10 #2-16, Medellín") @Size(max = 200) String address,
        @Schema(example = "Accidente en bicicleta, posible fractura de pie izquierdo") @NotBlank @Size(max = 500) String description,
        @Schema(example = "1") @NotNull @Min(1) @Max(20) Integer patientsCount,
        @Schema(description = "1 = leve, 5 = crítica", example = "3") @NotNull @Min(1) @Max(5) Integer severity) {
}
