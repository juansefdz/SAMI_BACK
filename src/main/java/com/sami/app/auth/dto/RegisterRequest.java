package com.sami.app.auth.dto;

import com.sami.app.user.RhType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Registro público: siempre crea un CLIENT con su perfil. */
public record RegisterRequest(
        @Schema(example = "cliente@sami.com") @NotBlank @Email @Size(max = 100) String email,
        @NotBlank @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres") String password,
        @Schema(example = "Pepe") @NotBlank @Size(max = 50) String firstName,
        @Schema(example = "Pérez") @NotBlank @Size(max = 50) String lastName,
        @Schema(example = "3001234567") @NotBlank @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Teléfono inválido") String phone,
        @Schema(example = "O_POSITIVE") @NotNull RhType rhType) {
}
