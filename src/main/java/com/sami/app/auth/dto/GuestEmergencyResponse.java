package com.sami.app.auth.dto;

import com.sami.app.emergency.dto.EmergencyResponse;

/** Token de la cuenta de invitado (para seguir la ambulancia) y la emergencia creada. */
public record GuestEmergencyResponse(AuthResponse auth, EmergencyResponse emergency) {
}
