package com.sami.app.map.dto;

/** Distancia y duración estimadas de una ruta en carro. */
public record RouteEstimate(long distanceMeters, long durationSeconds, String durationText) {
}
