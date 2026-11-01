package com.sami.app.user.dto;

/** Vista reducida de un usuario para incrustar en otras respuestas. */
public record UserSummary(Long id, String firstName, String lastName, String phone) {
}
