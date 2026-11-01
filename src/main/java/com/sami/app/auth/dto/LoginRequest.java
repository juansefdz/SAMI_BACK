package com.sami.app.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @Schema(example = "cliente@sami.com") @NotBlank @Email @Size(max = 100) String email,
        @NotBlank @Size(max = 72) String password) {
}
