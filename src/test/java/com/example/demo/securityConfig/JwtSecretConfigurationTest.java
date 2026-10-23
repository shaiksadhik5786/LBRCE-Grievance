package com.example.demo.securityConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.security.oauth2.jwt.JwtException;

class JwtSecretConfigurationTest {
    private static final String SECRET = "stable-local-test-secret-that-is-at-least-32-bytes";

    @Test
    void tokenRemainsValidAcrossApplicationInstancesUsingTheSameConfiguredSecret() {
        WebSecurityConfig firstInstance = configuredWith(SECRET);
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer("college-grievance-system").subject("STU001").issuedAt(now)
                .expiresAt(now.plus(1, ChronoUnit.HOURS)).claim("role", "ROLE_STUDENT").build();
        String token = firstInstance.jwtEncoder().encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();

        var decoded = configuredWith(SECRET).jwtDecoder().decode(token);

        assertEquals("STU001", decoded.getSubject());
        assertEquals("ROLE_STUDENT", decoded.getClaimAsString("role"));
    }

    @Test
    void emptySecretFailsInsteadOfGeneratingAnUnstableKey() {
        WebSecurityConfig config = configuredWith("");

        assertThrows(IllegalStateException.class, config::jwtEncoder);
    }

    @Test
    void decoderRejectsATokenSignedWithADifferentSecret() {
        WebSecurityConfig signer = configuredWith(SECRET);
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer("college-grievance-system").subject("STU001")
                .issuedAt(now).expiresAt(now.plus(1, ChronoUnit.HOURS)).claim("role", "ROLE_STUDENT").build();
        String token = signer.jwtEncoder().encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();

        assertNotNull(token);
        assertThrows(JwtException.class, () -> configuredWith(SECRET + "different").jwtDecoder().decode(token));
    }

    private WebSecurityConfig configuredWith(String secret) {
        WebSecurityConfig config = new WebSecurityConfig();
        ReflectionTestUtils.setField(config, "jwtSecret", secret);
        return config;
    }
}
