package com.sami.app.emergency.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record EmergencyAssignRequest(
        @NotNull @Positive Long ambulanceId,
        @NotNull @Positive Long hospitalId) {
}
