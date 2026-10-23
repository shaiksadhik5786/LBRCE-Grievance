package com.example.demo.securityConfig;

import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class PasswordPolicy {
    private PasswordPolicy() {}

    public static void validate(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required.");
        if (rawPassword.length() < 8)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters.");
        if (rawPassword.getBytes(StandardCharsets.UTF_8).length > 72)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must not exceed 72 UTF-8 bytes.");
    }
}
