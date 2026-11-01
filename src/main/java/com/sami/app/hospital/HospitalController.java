package com.sami.app.hospital;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.sami.app.common.web.PageResponse;
import com.sami.app.hospital.dto.HospitalRequest;
import com.sami.app.hospital.dto.HospitalResponse;
import com.sami.app.security.Roles;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Hospitales")
@RestController
@RequestMapping("/hospitals")
@RequiredArgsConstructor
public class HospitalController {

    private final HospitalService hospitalService;

    @GetMapping
    public PageResponse<HospitalResponse> findAll(Pageable pageable) {
        return hospitalService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public HospitalResponse findById(@PathVariable Long id) {
        return hospitalService.findById(id);
    }

    @PreAuthorize(Roles.ADMIN)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HospitalResponse create(@Valid @RequestBody HospitalRequest request) {
        return hospitalService.create(request);
    }

    @PreAuthorize(Roles.ADMIN)
    @PutMapping("/{id}")
    public HospitalResponse update(@PathVariable Long id, @Valid @RequestBody HospitalRequest request) {
        return hospitalService.update(id, request);
    }

    @PreAuthorize(Roles.ADMIN)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        hospitalService.delete(id);
    }
}
