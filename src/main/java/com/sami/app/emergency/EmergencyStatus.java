package com.sami.app.emergency;

import java.util.EnumSet;
import java.util.Set;

/**
 * Ciclo de vida de una emergencia.
 *
 * <pre>
 * PENDING ──► ASSIGNED ──► IN_PROGRESS ──► COMPLETED
 *    │            │
 *    └────────────┴──► CANCELED
 * </pre>
 */
public enum EmergencyStatus {
    PENDING,
    ASSIGNED,
    IN_PROGRESS,
    COMPLETED,
    CANCELED;

    public static final Set<EmergencyStatus> ACTIVE = EnumSet.of(ASSIGNED, IN_PROGRESS);

    public boolean canTransitionTo(EmergencyStatus next) {
        return switch (this) {
            case PENDING -> next == ASSIGNED || next == CANCELED;
            case ASSIGNED -> next == IN_PROGRESS || next == CANCELED;
            case IN_PROGRESS -> next == COMPLETED;
            case COMPLETED, CANCELED -> false;
        };
    }

    public boolean isFinal() {
        return this == COMPLETED || this == CANCELED;
    }
}
