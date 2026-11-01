package com.sami.app.hospital;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.sami.app.hospital.dto.HospitalRequest;
import com.sami.app.hospital.dto.HospitalResponse;
import com.sami.app.hospital.dto.HospitalSummary;

@Mapper
public interface HospitalMapper {

    HospitalResponse toResponse(Hospital hospital);

    HospitalSummary toSummary(Hospital hospital);

    @Mapping(target = "id", ignore = true)
    void update(@MappingTarget Hospital hospital, HospitalRequest request);
}
