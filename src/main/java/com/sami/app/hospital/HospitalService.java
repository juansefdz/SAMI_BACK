package com.sami.app.hospital;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sami.app.common.exception.ResourceNotFoundException;
import com.sami.app.common.web.PageResponse;
import com.sami.app.hospital.dto.HospitalRequest;
import com.sami.app.hospital.dto.HospitalResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HospitalService {

    private final HospitalRepository hospitalRepository;
    private final HospitalMapper hospitalMapper;

    public PageResponse<HospitalResponse> findAll(Pageable pageable) {
        return PageResponse.of(hospitalRepository.findAll(pageable), hospitalMapper::toResponse);
    }

    public HospitalResponse findById(Long id) {
        return hospitalMapper.toResponse(get(id));
    }

    @Transactional
    public HospitalResponse create(HospitalRequest request) {
        Hospital hospital = new Hospital();
        hospitalMapper.update(hospital, request);
        return hospitalMapper.toResponse(hospitalRepository.save(hospital));
    }

    @Transactional
    public HospitalResponse update(Long id, HospitalRequest request) {
        Hospital hospital = get(id);
        hospitalMapper.update(hospital, request);
        return hospitalMapper.toResponse(hospital);
    }

    @Transactional
    public void delete(Long id) {
        hospitalRepository.delete(get(id));
    }

    public Hospital get(Long id) {
        return hospitalRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Hospital", id));
    }
}
