package com.sami.app.ambulance;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.sami.app.account.Account;
import com.sami.app.ambulance.dto.AmbulanceRequest;
import com.sami.app.ambulance.dto.AmbulanceResponse;
import com.sami.app.ambulance.dto.LocationUpdateRequest;
import com.sami.app.common.web.PageResponse;
import com.sami.app.security.Roles;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Ambulancias")
@RestController
@RequestMapping("/ambulances")
@RequiredArgsConstructor
public class AmbulanceController {

    private final AmbulanceService ambulanceService;

    @Operation(summary = "Lista ambulancias (filtro opcional por estado)")
    @PreAuthorize(Roles.ADMIN)
    @GetMapping
    public PageResponse<AmbulanceResponse> findAll(@RequestParam(required = false) AmbulanceStatus status,
            Pageable pageable) {
        return ambulanceService.findAll(status, pageable);
    }

    @Operation(summary = "Ambulancia de la tripulación autenticada")
    @PreAuthorize(Roles.CREW)
    @GetMapping("/me")
    public AmbulanceResponse findMine(@AuthenticationPrincipal Account account) {
        return ambulanceService.findMine(account.getId());
    }

    @PreAuthorize(Roles.ADMIN)
    @GetMapping("/{id}")
    public AmbulanceResponse findById(@PathVariable Long id) {
        return ambulanceService.findById(id);
    }

    @PreAuthorize(Roles.ADMIN)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AmbulanceResponse create(@Valid @RequestBody AmbulanceRequest request) {
        return ambulanceService.create(request);
    }

    @PreAuthorize(Roles.ADMIN)
    @PutMapping("/{id}")
    public AmbulanceResponse update(@PathVariable Long id, @Valid @RequestBody AmbulanceRequest request) {
        return ambulanceService.update(id, request);
    }

    @Operation(summary = "Reporta la ubicación GPS (terminal móvil)")
    @PreAuthorize(Roles.CREW)
    @PatchMapping("/{id}/location")
    public AmbulanceResponse updateLocation(@PathVariable Long id,
            @Valid @RequestBody LocationUpdateRequest request,
            @AuthenticationPrincipal Account account) {
        return ambulanceService.updateLocation(id, request, account);
    }

    @PreAuthorize(Roles.ADMIN)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        ambulanceService.delete(id);
    }
}
