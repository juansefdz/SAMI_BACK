package com.sami.app.emergency;

import org.mapstruct.Mapper;

import com.sami.app.ambulance.AmbulanceMapper;
import com.sami.app.emergency.dto.EmergencyResponse;
import com.sami.app.hospital.HospitalMapper;
import com.sami.app.user.UserMapper;

@Mapper(uses = { UserMapper.class, AmbulanceMapper.class, HospitalMapper.class })
public interface EmergencyMapper {

    EmergencyResponse toResponse(Emergency emergency);
}
