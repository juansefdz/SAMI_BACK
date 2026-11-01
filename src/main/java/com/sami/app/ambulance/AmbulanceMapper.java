package com.sami.app.ambulance;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.sami.app.ambulance.dto.AmbulanceRequest;
import com.sami.app.ambulance.dto.AmbulanceResponse;
import com.sami.app.ambulance.dto.AmbulanceSummary;
import com.sami.app.user.UserMapper;

@Mapper(uses = UserMapper.class)
public interface AmbulanceMapper {

    AmbulanceResponse toResponse(Ambulance ambulance);

    AmbulanceSummary toSummary(Ambulance ambulance);

    /** La tripulación se asigna en el servicio porque requiere validar roles. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "driver", ignore = true)
    @Mapping(target = "emt", ignore = true)
    @Mapping(target = "lastLocationAt", ignore = true)
    @Mapping(target = "plate", expression = "java(request.plate().trim().toUpperCase())")
    void update(@MappingTarget Ambulance ambulance, AmbulanceRequest request);
}
