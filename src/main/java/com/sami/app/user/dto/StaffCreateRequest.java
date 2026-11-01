package com.sami.app.user.dto;

import com.sami.app.account.Role;
import com.sami.app.user.RhType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Alta de personal (ADMIN, DRIVER, EMT) realizada por un administrador. */
public record StaffCreateRequest(
        @Schema(example = "conductor@sami.com") @NotBlank @Email @Size(max = 100) String email,
        @NotBlank @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres") String password,
        @Schema(example = "DRIVER") @NotNull Role role,
        @NotBlank @Size(max = 50) String firstName,
        @NotBlank @Size(max = 50) String lastName,
        @NotBlank @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Teléfono inválido") String phone,
        @NotNull RhType rhType) {
}
