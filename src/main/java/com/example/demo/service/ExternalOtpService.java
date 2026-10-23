package com.example.demo.service;

public interface ExternalOtpService {
    void send(String normalizedEmail);
    void verify(String normalizedEmail, String otp);
}
