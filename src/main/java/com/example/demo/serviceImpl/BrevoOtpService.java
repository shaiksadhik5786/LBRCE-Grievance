package com.example.demo.serviceImpl;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.config.OtpProperties;
import com.example.demo.service.ExternalOtpService;

@Service
public class BrevoOtpService implements ExternalOtpService {
    private final RestClient restClient;
    private final OtpProperties properties;

    public BrevoOtpService(RestClient otpRestClient, OtpProperties properties) {
        this.restClient = otpRestClient;
        this.properties = properties;
    }

    @Override
    public void send(String normalizedEmail) {
        call("/api/otp/send", Map.of("email", normalizedEmail), false);
    }

    @Override
    public void verify(String normalizedEmail, String otp) {
        call("/api/otp/verify", Map.of("email", normalizedEmail, "otp", otp), true);
    }

    private void call(String path, Map<String, String> body, boolean verifying) {
        ensureConfigured();
        try {
            Map<?, ?> response = restClient.post()
                    .uri(cleanBaseUrl() + path)
                    .header("X-API-Key", properties.apiKey())
                    .body(body)
                    .retrieve()
                    .body(Map.class);
            if (verifying && response != null && explicitlyFalse(response)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The OTP is invalid or has expired.");
            }
        } catch (RestClientResponseException ex) {
            throw safeFailure(ex.getStatusCode().value(), verifying);
        } catch (ResourceAccessException ex) {
            throw unavailable();
        }
    }

    private boolean explicitlyFalse(Map<?, ?> response) {
        return Boolean.FALSE.equals(response.get("success")) || Boolean.FALSE.equals(response.get("verified"))
                || Boolean.FALSE.equals(response.get("valid"));
    }

    private ResponseStatusException safeFailure(int status, boolean verifying) {
        if (status == 400) return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                verifying ? "The OTP is invalid or has expired." : "The OTP request was rejected. Check the email address and try again.");
        if (status == 429) return new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                "Too many OTP requests. Please wait before trying again.");
        if (status == 401 || status == 403) return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "Email verification is temporarily unavailable due to a server configuration problem.");
        return unavailable();
    }

    private ResponseStatusException unavailable() {
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "Email verification service is temporarily unavailable. Please try again later.");
    }

    private void ensureConfigured() {
        if (!StringUtils.hasText(properties.baseUrl()) || !StringUtils.hasText(properties.apiKey())) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Email verification is temporarily unavailable due to a server configuration problem.");
        }
    }

    private String cleanBaseUrl() {
        return properties.baseUrl().replaceAll("/+$", "");
    }
}
