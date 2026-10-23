package com.example.demo.securityConfig;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

class JwtRoleMappingTest {
    @Test
    void jwtRoleClaimMapsToTheExistingProtectedApiAuthority() {
        Jwt jwt = Jwt.withTokenValue("test-token").header("alg", "HS256")
                .claim("sub", "ADM001").claim("role", "ROLE_ADMIN").build();

        AbstractAuthenticationToken authentication = new WebSecurityConfig().jwtAuthenticationConverter().convert(jwt);

        assertTrue(authentication.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")));
    }
}
