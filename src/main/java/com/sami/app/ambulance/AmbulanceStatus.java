package com.sami.app.ambulance;

public enum AmbulanceStatus {
    /** Disponible para asignar. */
    AVAILABLE,
    /** Atendiendo una emergencia. */
    BUSY,
    /** Fuera de servicio (mantenimiento, sin tripulación, etc.). */
    OUT_OF_SERVICE
}
