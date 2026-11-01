package com.sami.app.user.dto;

import com.sami.app.account.Role;
import com.sami.app.user.RhType;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String phone,
        RhType rhType,
        String email,
        Role role,
        /** true si la cuenta se creó en una solicitud sin registro y aún no tiene credenciales propias. */
        boolean guest) {
}
