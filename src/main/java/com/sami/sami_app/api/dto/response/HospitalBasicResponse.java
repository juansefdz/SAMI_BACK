package com.sami.sami_app.api.dto.response;

import com.sami.sami_app.util.enums.ComplexityGrade;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HospitalBasicResponse {
    @Schema(description = "ID of the Hospital", example = "1")
    private Long idHospital;
    @Schema(description = "Name of the Hospital", example = "Hospital General de Medellín")
    private String name;
    @Schema(description = "Latitude of the Hospital", example = "40.1234")
    private Double latitudeHospital;
    @Schema(description = "Longitude of the Hospital", example = "40.1234")
    private Double longitudeHospital;
    @Schema(description = "Adrress of the Hospital", example = "Happy avenue 123")
    private String address;
    @Schema(description = "Grade of Complexity", example = "3")
    private ComplexityGrade complexityGrade;
    @Schema(description = "specialty",example = "this hospital is specialized in cardiac emergencies")
    private String specialty;
}
