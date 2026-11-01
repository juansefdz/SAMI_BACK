package com.sami.app.emergency;

import static com.sami.app.emergency.EmergencyStatus.ASSIGNED;
import static com.sami.app.emergency.EmergencyStatus.CANCELED;
import static com.sami.app.emergency.EmergencyStatus.COMPLETED;
import static com.sami.app.emergency.EmergencyStatus.IN_PROGRESS;
import static com.sami.app.emergency.EmergencyStatus.PENDING;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EmergencyStatusTest {

    @Test
    void allowsHappyPath() {
        assertThat(PENDING.canTransitionTo(ASSIGNED)).isTrue();
        assertThat(ASSIGNED.canTransitionTo(IN_PROGRESS)).isTrue();
        assertThat(IN_PROGRESS.canTransitionTo(COMPLETED)).isTrue();
    }

    @Test
    void allowsCancelOnlyBeforeStarting() {
        assertThat(PENDING.canTransitionTo(CANCELED)).isTrue();
        assertThat(ASSIGNED.canTransitionTo(CANCELED)).isTrue();
        assertThat(IN_PROGRESS.canTransitionTo(CANCELED)).isFalse();
    }

    @Test
    void finalStatesAreTerminal() {
        for (EmergencyStatus next : EmergencyStatus.values()) {
            assertThat(COMPLETED.canTransitionTo(next)).isFalse();
            assertThat(CANCELED.canTransitionTo(next)).isFalse();
        }
    }

    @Test
    void cannotSkipSteps() {
        assertThat(PENDING.canTransitionTo(IN_PROGRESS)).isFalse();
        assertThat(PENDING.canTransitionTo(COMPLETED)).isFalse();
        assertThat(ASSIGNED.canTransitionTo(COMPLETED)).isFalse();
    }
}
