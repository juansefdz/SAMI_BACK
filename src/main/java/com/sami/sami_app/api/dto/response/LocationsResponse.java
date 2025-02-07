package com.sami.sami_app.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LOCATION REQUEST DTO - GOOGLE MAP API
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO for Locations requests")
public class LocationsResponse {
    @Schema(description = "latitude of service", example = "10.1234")
    private Double serviceLatitude;

    @Schema(description = "longitude of service", example = "20.1234")
    private Double serviceLongitude;

    @Schema(description = "latitude of ambulance", example = "30.1234")
    private Double ambulanceLatitude;

    @Schema(description = "longitude of ambulance", example = "40.1234")
    private Double ambulanceLongitude;

    @Schema(description = "latitude of hospital", example = "50.1234")
    private Double hospitalLatitude;

    @Schema(description = "longitude of hospital", example = "60.1234")
    private Double hospitalLongitude;
}
