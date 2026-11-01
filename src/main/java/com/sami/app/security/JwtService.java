package com.sami.app.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.sami.app.account.Account;
import com.sami.app.common.config.SamiProperties;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/** Emite y valida tokens JWT firmados con HMAC-SHA. */
@Component
public class JwtService {

    private final SecretKey key;
    private final Duration expiration;

    public JwtService(SamiProperties properties) {
        SamiProperties.Jwt jwt = properties.security().jwt();
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwt.secret()));
        this.expiration = jwt.expiration();
    }

    public String generateToken(Account account) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(account.getEmail())
                .claim("accountId", account.getId().toString())
                .claim("role", account.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expiration)))
                .signWith(key)
                .compact();
    }

    /**
     * Devuelve el email (subject) si el token tiene firma válida y no expiró.
     */
    public Optional<String> extractValidSubject(String token) {
        try {
            return Optional.ofNullable(Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public Duration getExpiration() {
        return expiration;
    }
}
