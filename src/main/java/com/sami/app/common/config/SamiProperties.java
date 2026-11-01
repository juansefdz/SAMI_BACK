package com.sami.app.common.config;

import java.time.Duration;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Propiedades propias de la aplicación (prefijo {@code sami.*}).
 * Se validan al arrancar: si falta el secreto JWT la app no inicia.
 */
@Validated
@ConfigurationProperties(prefix = "sami")
public record SamiProperties(
        @Valid @NotNull Security security,
        @Valid @NotNull Cors cors,
        @Valid @NotNull GoogleMaps googleMaps,
        @Valid @DefaultValue BootstrapAdmin bootstrapAdmin) {

    public record Security(@Valid @NotNull Jwt jwt) {
    }

    public record Jwt(
            @NotBlank(message = "Defina JWT_SECRET (Base64, mínimo 256 bits)") String secret,
            @NotNull Duration expiration) {
    }

    public record Cors(@NotNull List<String> allowedOrigins) {
    }

    public record GoogleMaps(String apiKey) {
    }

    /** Administrador inicial opcional, se crea solo si no existe ningún ADMIN. */
    public record BootstrapAdmin(String email, String password) {
        public boolean isConfigured() {
            return email != null && !email.isBlank() && password != null && !password.isBlank();
        }
    }
}
