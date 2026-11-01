package com.sami.app.auth;

import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sami.app.account.Account;
import com.sami.app.account.AccountRepository;
import com.sami.app.account.Role;
import com.sami.app.auth.dto.AuthResponse;
import com.sami.app.auth.dto.ClaimAccountRequest;
import com.sami.app.auth.dto.GuestEmergencyRequest;
import com.sami.app.auth.dto.GuestEmergencyResponse;
import com.sami.app.auth.dto.LoginRequest;
import com.sami.app.auth.dto.RegisterRequest;
import com.sami.app.common.exception.BadRequestException;
import com.sami.app.common.exception.ConflictException;
import com.sami.app.emergency.EmergencyService;
import com.sami.app.emergency.dto.EmergencyCreateRequest;
import com.sami.app.emergency.dto.EmergencyResponse;
import com.sami.app.security.JwtService;
import com.sami.app.user.User;
import com.sami.app.user.UserMapper;
import com.sami.app.user.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final UserMapper userMapper;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmergencyService emergencyService;

    /** Lanza BadCredentialsException (401) si las credenciales no son válidas. */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().trim().toLowerCase(), request.password()));
        Account account = (Account) authentication.getPrincipal();
        return buildResponse(account, userService.getByAccount(account.getId()));
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        User user = userService.createWithAccount(request.email(), request.password(), Role.CLIENT,
                User.builder()
                        .firstName(request.firstName().trim())
                        .lastName(request.lastName().trim())
                        .phone(request.phone())
                        .rhType(request.rhType())
                        .build());
        return buildResponse(user.getAccount(), user);
    }

    /**
     * Crea una cuenta de invitado (sin email real ni contraseña conocida) y la
     * emergencia en la misma transacción. El token devuelto permite seguir el servicio.
     */
    @Transactional
    public GuestEmergencyResponse requestAsGuest(GuestEmergencyRequest request) {
        String placeholderEmail = "guest-" + UUID.randomUUID() + "@guest.sami.local";
        User user = userService.createWithAccount(placeholderEmail, UUID.randomUUID().toString(), Role.CLIENT,
                User.builder()
                        .firstName(request.firstName().trim())
                        .lastName(request.lastName() == null ? "" : request.lastName().trim())
                        .phone(request.phone())
                        .build());
        Account account = user.getAccount();
        account.setGuest(true);

        EmergencyResponse emergency = emergencyService.create(new EmergencyCreateRequest(
                request.latitude(), request.longitude(), request.address(), request.description(),
                request.patientsCount(), request.severity()), account);

        return new GuestEmergencyResponse(buildResponse(account, user), emergency);
    }

    /** Asigna email y contraseña a una cuenta de invitado. Devuelve un token nuevo (cambia el email). */
    @Transactional
    public AuthResponse claimGuestAccount(UUID accountId, ClaimAccountRequest request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BadRequestException("Cuenta no encontrada"));
        if (!account.isGuest()) {
            throw new BadRequestException("La cuenta ya tiene credenciales");
        }
        String email = request.email().trim().toLowerCase();
        if (accountRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("El email ya está registrado");
        }
        account.setEmail(email);
        account.setPassword(passwordEncoder.encode(request.password()));
        account.setGuest(false);
        return buildResponse(account, userService.getByAccount(accountId));
    }

    private AuthResponse buildResponse(Account account, User user) {
        return AuthResponse.bearer(
                jwtService.generateToken(account),
                jwtService.getExpiration().toSeconds(),
                userMapper.toResponse(user));
    }
}
