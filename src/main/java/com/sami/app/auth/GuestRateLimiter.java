package com.sami.app.auth;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.sami.app.common.exception.TooManyRequestsException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Límite simple en memoria para las solicitudes sin cuenta: máximo
 * {@value #MAX_REQUESTS} por IP cada 10 minutos. Para varias instancias de la
 * API habría que moverlo a Redis o al balanceador.
 */
@Component
public class GuestRateLimiter {

    static final int MAX_REQUESTS = 3;
    static final Duration WINDOW = Duration.ofMinutes(10);

    private final Map<String, Deque<Instant>> hits = new ConcurrentHashMap<>();
    private final Clock clock;

    public GuestRateLimiter() {
        this(Clock.systemUTC());
    }

    GuestRateLimiter(Clock clock) {
        this.clock = clock;
    }

    public void check(HttpServletRequest request) {
        check(clientIp(request));
    }

    void check(String key) {
        Instant now = clock.instant();
        Deque<Instant> window = hits.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (window) {
            while (!window.isEmpty() && window.peekFirst().isBefore(now.minus(WINDOW))) {
                window.pollFirst();
            }
            if (window.size() >= MAX_REQUESTS) {
                throw new TooManyRequestsException(
                        "Demasiadas solicitudes desde esta conexión. Si es una emergencia vital llama al 123.");
            }
            window.addLast(now);
        }
    }

    /** Primera IP de X-Forwarded-For (detrás de proxy) o la dirección remota. */
    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
