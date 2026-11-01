package com.sami.app.auth;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.sami.app.auth.dto.AuthResponse;
import com.sami.app.auth.dto.GuestEmergencyRequest;
import com.sami.app.auth.dto.GuestEmergencyResponse;
import com.sami.app.auth.dto.LoginRequest;
import com.sami.app.auth.dto.RegisterRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Autenticación")
@SecurityRequirements // endpoints públicos
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final GuestRateLimiter guestRateLimiter;

    @Operation(summary = "Inicia sesión y devuelve un JWT")
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @Operation(summary = "Solicita una ambulancia sin cuenta (crea una cuenta de invitado)")
    @PostMapping("/guest-emergency")
    @ResponseStatus(HttpStatus.CREATED)
    public GuestEmergencyResponse guestEmergency(@Valid @RequestBody GuestEmergencyRequest request,
            HttpServletRequest httpRequest) {
        guestRateLimiter.check(httpRequest);
        return authService.requestAsGuest(request);
    }

    @Operation(summary = "Registra un cliente nuevo")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }
}
