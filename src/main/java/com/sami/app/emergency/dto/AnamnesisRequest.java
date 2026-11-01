package com.sami.app.emergency.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnamnesisRequest(@NotBlank @Size(max = 4000) String anamnesis) {
}
