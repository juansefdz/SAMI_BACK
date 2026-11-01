package com.sami.app.auth;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import com.sami.app.common.exception.TooManyRequestsException;

class GuestRateLimiterTest {

    @Test
    void blocksAfterLimitAndRecoversAfterWindow() {
        AtomicReference<Instant> now = new AtomicReference<>(Instant.parse("2026-11-01T12:00:00Z"));
        Clock clock = new Clock() {
            public ZoneOffset getZone() { return ZoneOffset.UTC; }
            public Clock withZone(java.time.ZoneId zone) { return this; }
            public Instant instant() { return now.get(); }
        };
        GuestRateLimiter limiter = new GuestRateLimiter(clock);

        for (int i = 0; i < GuestRateLimiter.MAX_REQUESTS; i++) {
            limiter.check("1.2.3.4");
        }
        assertThatThrownBy(() -> limiter.check("1.2.3.4")).isInstanceOf(TooManyRequestsException.class);
        assertThatCode(() -> limiter.check("5.6.7.8")).doesNotThrowAnyException();

        now.set(now.get().plus(GuestRateLimiter.WINDOW).plusSeconds(1));
        assertThatCode(() -> limiter.check("1.2.3.4")).doesNotThrowAnyException();
    }
}
