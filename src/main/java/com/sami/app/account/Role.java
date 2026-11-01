package com.sami.app.account;

public enum Role {
    ADMIN,
    CLIENT,
    DRIVER,
    EMT;

    /** Roles que forman la tripulación de una ambulancia. */
    public boolean isCrew() {
        return this == DRIVER || this == EMT;
    }
}
