package com.example.demo.serviceImpl;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.config.OtpProperties;
import com.example.demo.service.ExternalOtpService;
import com.example.demo.service.OtpService;
import com.example.demo.service.OtpPurpose;

@Service
public class OtpServiceImpl implements OtpService {
    private record Challenge(String email, OtpPurpose purpose, Instant expiresAt) {}
    private record Verification(String email, OtpPurpose purpose, Instant expiresAt) {}

    private final ExternalOtpService externalOtpService;
    private final OtpProperties properties;
    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();
    private final ConcurrentHashMap<String, Challenge> challenges = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Verification> verifications = new ConcurrentHashMap<>();

    public OtpServiceImpl(ExternalOtpService externalOtpService, OtpProperties properties, Clock clock) {
        this.externalOtpService = externalOtpService;
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    public String send(String email, OtpPurpose purpose) {
        String normalizedEmail = normalize(email);
        invalidate(normalizedEmail, purpose);
        externalOtpService.send(normalizedEmail);
        String challengeId = newToken();
        challenges.put(challengeId, new Challenge(normalizedEmail, purpose,
                clock.instant().plus(properties.verificationTtl())));
        return challengeId;
    }

    @Override
    public String verify(String email, String otp, OtpPurpose purpose, String challengeId) {
        if (purpose == OtpPurpose.STAFF_LOGIN || purpose == OtpPurpose.ADMIN_LOGIN) throw invalidChallenge();
        String normalizedEmail = normalize(email);
        Challenge challenge = consumeChallenge(challengeId, purpose);
        if (!challenge.email().equals(normalizedEmail)) throw invalidChallenge();
        externalOtpService.verify(normalizedEmail, otp);
        invalidate(normalizedEmail, purpose);
        String token = newToken();
        verifications.put(token, new Verification(normalizedEmail, purpose,
                clock.instant().plus(properties.verificationTtl())));
        return token;
    }

    @Override
    public String verifyLogin(String otp, OtpPurpose purpose, String challengeId) {
        if (purpose != OtpPurpose.STAFF_LOGIN && purpose != OtpPurpose.ADMIN_LOGIN) throw invalidChallenge();
        Challenge challenge = consumeChallenge(challengeId, purpose);
        externalOtpService.verify(challenge.email(), otp);
        return challenge.email();
    }

    private Challenge consumeChallenge(String challengeId, OtpPurpose purpose) {
        Challenge challenge = StringUtils.hasText(challengeId) ? challenges.remove(challengeId) : null;
        if (challenge == null || challenge.purpose() != purpose || !challenge.expiresAt().isAfter(clock.instant())) {
            throw invalidChallenge();
        }
        return challenge;
    }

    private ResponseStatusException invalidChallenge() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "The OTP challenge is invalid or has expired. Request a new OTP.");
    }

    @Override
    public void requireAndConsumeVerification(String email, OtpPurpose purpose, String verificationToken) {
        String normalizedEmail = normalize(email);
        if (!StringUtils.hasText(verificationToken)) throw verificationRequired();
        Verification verification = verifications.remove(verificationToken);
        if (verification == null || !verification.email().equals(normalizedEmail) || verification.purpose() != purpose
                || !verification.expiresAt().isAfter(clock.instant())) {
            throw verificationRequired();
        }
    }

    private void invalidate(String email, OtpPurpose purpose) {
        challenges.entrySet().removeIf(entry -> entry.getValue().email().equals(email)
                && entry.getValue().purpose() == purpose);
        verifications.entrySet().removeIf(entry -> entry.getValue().email().equals(email)
                && entry.getValue().purpose() == purpose);
    }

    private String newToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private ResponseStatusException verificationRequired() {
        return new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Please verify your email with OTP before completing registration.");
    }
}
