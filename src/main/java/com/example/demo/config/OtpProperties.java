package com.example.demo.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.otp")
public record OtpProperties(
        String baseUrl,
        String apiKey,
        Duration verificationTtl,
        Duration connectTimeout,
        Duration readTimeout) {

    public OtpProperties {
        if (verificationTtl == null) verificationTtl = Duration.ofMinutes(10);
        if (connectTimeout == null) connectTimeout = Duration.ofSeconds(5);
        if (readTimeout == null) readTimeout = Duration.ofSeconds(10);
    }
}
