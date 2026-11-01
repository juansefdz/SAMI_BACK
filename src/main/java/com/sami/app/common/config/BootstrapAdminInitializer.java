package com.sami.app.common.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.sami.app.account.AccountRepository;
import com.sami.app.account.Role;
import com.sami.app.user.UserService;
import com.sami.app.user.dto.StaffCreateRequest;
import com.sami.app.user.RhType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Crea el primer administrador si se definen SAMI_ADMIN_EMAIL y
 * SAMI_ADMIN_PASSWORD y todavía no existe ningún ADMIN.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BootstrapAdminInitializer implements ApplicationRunner {

    private final SamiProperties properties;
    private final AccountRepository accountRepository;
    private final UserService userService;

    @Override
    public void run(ApplicationArguments args) {
        SamiProperties.BootstrapAdmin admin = properties.bootstrapAdmin();
        if (admin == null || !admin.isConfigured() || accountRepository.existsByRole(Role.ADMIN)) {
            return;
        }
        userService.createStaff(new StaffCreateRequest(
                admin.email(), admin.password(), Role.ADMIN, "Admin", "SAMI", "0000000000", RhType.O_POSITIVE));
        log.info("Administrador inicial creado: {}", admin.email());
    }
}
