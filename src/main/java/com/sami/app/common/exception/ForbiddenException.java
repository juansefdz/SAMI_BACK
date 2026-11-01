package com.sami.app.common.exception;

/** Acceso denegado a un recurso concreto (p. ej. una emergencia de otro cliente). */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
