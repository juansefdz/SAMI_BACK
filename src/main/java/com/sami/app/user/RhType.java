package com.sami.app.user;

/** Grupo sanguíneo y factor RH. */
public enum RhType {
    A_POSITIVE("A+"),
    A_NEGATIVE("A-"),
    B_POSITIVE("B+"),
    B_NEGATIVE("B-"),
    AB_POSITIVE("AB+"),
    AB_NEGATIVE("AB-"),
    O_POSITIVE("O+"),
    O_NEGATIVE("O-");

    private final String label;

    RhType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
