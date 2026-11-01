package com.sami.app.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.sami.app.account.Account;
import com.sami.app.account.Role;
import com.sami.app.common.config.SamiProperties;

class JwtServiceTest {

    private static final String SECRET =
            "dGVzdC1vbmx5LXNlY3JldC1zYW1pLWZvci11bml0LXRlc3RzLTEyMzQ1Njc4OTAtYWJjZGVm";

    private JwtService service(Duration expiration) {
        return new JwtService(new SamiProperties(
                new SamiProperties.Security(new SamiProperties.Jwt(SECRET, expiration)),
                new SamiProperties.Cors(List.of()),
                new SamiProperties.GoogleMaps(null),
                new SamiProperties.BootstrapAdmin(null, null)));
    }

    private Account account() {
        return Account.builder().id(UUID.randomUUID()).email("a@sami.com").password("x").role(Role.CLIENT).build();
    }

    @Test
    void generatedTokenReturnsSubject() {
        JwtService jwt = service(Duration.ofMinutes(5));
        String token = jwt.generateToken(account());
        assertThat(jwt.extractValidSubject(token)).contains("a@sami.com");
    }

    @Test
    void expiredTokenIsRejected() {
        JwtService jwt = service(Duration.ofSeconds(-1));
        String token = jwt.generateToken(account());
        assertThat(jwt.extractValidSubject(token)).isEmpty();
    }

    @Test
    void tamperedTokenIsRejected() {
        JwtService jwt = service(Duration.ofMinutes(5));
        String token = jwt.generateToken(account()) + "x";
        assertThat(jwt.extractValidSubject(token)).isEmpty();
        assertThat(jwt.extractValidSubject("no-es-un-token")).isEmpty();
    }
}
