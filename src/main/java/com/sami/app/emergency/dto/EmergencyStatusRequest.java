package com.sami.app.emergency.dto;

import com.sami.app.emergency.EmergencyStatus;

import jakarta.validation.constraints.NotNull;

public record EmergencyStatusRequest(@NotNull EmergencyStatus status) {
}
