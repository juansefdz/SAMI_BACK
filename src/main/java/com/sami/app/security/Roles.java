package com.sami.app.security;

/** Expresiones SpEL reutilizables para {@code @PreAuthorize}. */
public final class Roles {

    public static final String ADMIN = "hasRole('ADMIN')";
    public static final String CLIENT = "hasRole('CLIENT')";
    public static final String CREW = "hasAnyRole('DRIVER','EMT')";
    public static final String ADMIN_OR_CREW = "hasAnyRole('ADMIN','DRIVER','EMT')";
    public static final String ADMIN_OR_EMT = "hasAnyRole('ADMIN','EMT')";

    private Roles() {
    }
}
