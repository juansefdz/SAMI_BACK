package com.sami.sami_app.api.dto.request.create;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.sami.sami_app.util.enums.StatusService;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.DecimalMax;

/**
 * SERVICE REQUEST DTO
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for service entity requests")
public class ServiceEntityRequest {

    @DecimalMin(value = "-90.0", inclusive = true, message = "Service - Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", inclusive = true, message = "Service - Latitude must be between -90 and 90")
    @Schema(description = "latituted of the service's location", example = "50.1234")
    @NotNull(message = "The latitude of the service is required")
    private double latidudeService;

    @DecimalMin(value = "-180.0", inclusive = true, message = "Service - Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", inclusive = true, message = "Service - Longitude must be between -180 and 180")
    @Schema(description = "Longitude of the service's location", example = "50.1234")
    @NotNull(message = "The longitude of the service is required")
    private double longitudeService;

    @Schema(description = "Status of Services, this can be  INACTIVE, ACTIVE, CANCELED", example = "ACTIVE")
    @NotNull(message = "The status of the service is required")
    private StatusService statusService;

    @Size(max = 500, message = "The anamnesis must have a maximum of 500 characters")
    @Schema(description = "Description of the anamnesis ", example = "patient due to bicycle accident, fracture of left foot with multiple ematomas ")
    private String anamnesis;

    @Schema(description = "ID of the Hospital", example = "1")
    @Min(value = 1, message = "The id of the hospital must be greater than zero")
    @NotNull(message = "the ID of the Hospital is necessary,value cannot be less than 1")
    private Long idHospital;

    @Schema(description = "ID of the ambulance", example = "1")
    @Min(value = 1, message = "The id of the ambulance must be greater than zero")
    @NotNull(message = "the ID of the Ambulance is necessary,value cannot be less than 1")
    private Long idAmbulance;

    @Schema(description = "ID of the client", example = "1")
    @Min(value = 1, message = "The id of the client must be greater than zero")
    @NotNull(message = "the ID of the client is necessary,value cannot be less than 1")
    private Long idClient;
}
