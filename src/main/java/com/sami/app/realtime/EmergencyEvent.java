package com.sami.app.realtime;

/**
 * Aviso de cambio en una emergencia. Los clientes, al recibirlo, vuelven a
 * pedir los datos por REST (así no se duplica la lógica de permisos ni de ETA).
 */
public record EmergencyEvent(Long emergencyId, Type type) {

    public enum Type {
        CREATED,
        STATUS,
        LOCATION,
        UPDATED,
        DELETED
    }
}
