package com.sami.app.user.dto;

import com.sami.app.user.RhType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @Schema(example = "Pepe") @NotBlank @Size(max = 50) String firstName,
        @Schema(example = "Pérez") @NotBlank @Size(max = 50) String lastName,
        @Schema(example = "3001234567") @NotBlank @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Teléfono inválido") String phone,
        @Schema(example = "O_POSITIVE") @NotNull RhType rhType) {
}
