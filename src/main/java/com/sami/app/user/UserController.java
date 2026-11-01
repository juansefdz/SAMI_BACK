package com.sami.app.user;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.sami.app.account.Account;
import com.sami.app.account.Role;
import com.sami.app.auth.AuthService;
import com.sami.app.auth.dto.AuthResponse;
import com.sami.app.auth.dto.ClaimAccountRequest;
import com.sami.app.common.web.PageResponse;
import com.sami.app.security.Roles;
import com.sami.app.user.dto.StaffCreateRequest;
import com.sami.app.user.dto.UserResponse;
import com.sami.app.user.dto.UserUpdateRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Usuarios")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    @Operation(summary = "Perfil del usuario autenticado")
    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal Account account) {
        return userService.findByAccount(account.getId());
    }

    @Operation(summary = "Actualiza el perfil del usuario autenticado")
    @PutMapping("/me")
    public UserResponse updateMe(@AuthenticationPrincipal Account account,
            @Valid @RequestBody UserUpdateRequest request) {
        return userService.updateByAccount(account.getId(), request);
    }

    @Operation(summary = "Completa una cuenta de invitado con email y contraseña")
    @PostMapping("/me/claim")
    public AuthResponse claim(@AuthenticationPrincipal Account account, @Valid @RequestBody ClaimAccountRequest request) {
        return authService.claimGuestAccount(account.getId(), request);
    }

    @Operation(summary = "Lista usuarios (filtro opcional por rol)")
    @PreAuthorize(Roles.ADMIN)
    @GetMapping
    public PageResponse<UserResponse> findAll(@RequestParam(required = false) Role role, Pageable pageable) {
        return userService.findAll(role, pageable);
    }

    @PreAuthorize(Roles.ADMIN)
    @GetMapping("/{id}")
    public UserResponse findById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @Operation(summary = "Crea personal: administrador, conductor o paramédico")
    @PreAuthorize(Roles.ADMIN)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createStaff(@Valid @RequestBody StaffCreateRequest request) {
        return userService.createStaff(request);
    }

    @PreAuthorize(Roles.ADMIN)
    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        return userService.update(id, request);
    }

    @PreAuthorize(Roles.ADMIN)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }
}
