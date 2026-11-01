package com.sami.app.emergency;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.sami.app.account.Account;
import com.sami.app.common.web.PageResponse;
import com.sami.app.emergency.dto.AnamnesisRequest;
import com.sami.app.emergency.dto.EmergencyAssignRequest;
import com.sami.app.emergency.dto.EmergencyCreateRequest;
import com.sami.app.emergency.dto.EmergencyResponse;
import com.sami.app.emergency.dto.EmergencyStatusRequest;
import com.sami.app.security.Roles;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Emergencias")
@RestController
@RequestMapping("/emergencies")
@RequiredArgsConstructor
public class EmergencyController {

    private final EmergencyService emergencyService;

    @Operation(summary = "Solicita una ambulancia (cliente)")
    @PreAuthorize(Roles.CLIENT)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmergencyResponse create(@Valid @RequestBody EmergencyCreateRequest request,
            @AuthenticationPrincipal Account account) {
        return emergencyService.create(request, account);
    }

    @Operation(summary = "Lista todas las emergencias (despacho)")
    @PreAuthorize(Roles.ADMIN)
    @GetMapping
    public PageResponse<EmergencyResponse> findAll(
            @RequestParam(required = false) EmergencyStatus status,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return emergencyService.findAll(status, pageable);
    }

    @Operation(summary = "Historial de emergencias del cliente autenticado")
    @PreAuthorize(Roles.CLIENT)
    @GetMapping("/me")
    public PageResponse<EmergencyResponse> findMine(@AuthenticationPrincipal Account account,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return emergencyService.findMine(account, pageable);
    }

    @Operation(summary = "Emergencia activa de la ambulancia del tripulante (204 si no hay)")
    @PreAuthorize(Roles.CREW)
    @GetMapping("/current")
    public ResponseEntity<EmergencyResponse> findCurrent(@AuthenticationPrincipal Account account) {
        return emergencyService.findCurrentForCrew(account)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/{id}")
    public EmergencyResponse findById(@PathVariable Long id, @AuthenticationPrincipal Account account) {
        return emergencyService.findById(id, account);
    }

    @Operation(summary = "Asigna ambulancia y hospital (despacho)")
    @PreAuthorize(Roles.ADMIN)
    @PatchMapping("/{id}/assignment")
    public EmergencyResponse assign(@PathVariable Long id, @Valid @RequestBody EmergencyAssignRequest request) {
        return emergencyService.assign(id, request);
    }

    @Operation(summary = "Cambia el estado según el rol (cliente cancela, tripulación inicia/completa)")
    @PatchMapping("/{id}/status")
    public EmergencyResponse changeStatus(@PathVariable Long id,
            @Valid @RequestBody EmergencyStatusRequest request,
            @AuthenticationPrincipal Account account) {
        return emergencyService.changeStatus(id, request.status(), account);
    }

    @Operation(summary = "Registra la anamnesis (paramédico)")
    @PreAuthorize(Roles.ADMIN_OR_EMT)
    @PatchMapping("/{id}/anamnesis")
    public EmergencyResponse updateAnamnesis(@PathVariable Long id,
            @Valid @RequestBody AnamnesisRequest request,
            @AuthenticationPrincipal Account account) {
        return emergencyService.updateAnamnesis(id, request, account);
    }

    @PreAuthorize(Roles.ADMIN)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        emergencyService.delete(id);
    }
}
