package com.sami.app.map.dto;

import java.time.Instant;

import com.sami.app.emergency.EmergencyStatus;

/**
 * Estado de seguimiento en tiempo real de una emergencia. Los campos de
 * ambulancia/hospital/ETA son nulos mientras no estén asignados o calculados.
 */
public record TrackingResponse(
        Long emergencyId,
        EmergencyStatus status,
        Coordinates patient,
        Coordinates ambulance,
        Instant ambulanceUpdatedAt,
        Coordinates hospital,
        RouteEstimate etaToPatient,
        RouteEstimate etaToHospital) {
}
