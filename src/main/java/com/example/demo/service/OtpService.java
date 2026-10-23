package com.example.demo.service;

public interface OtpService {
    String send(String email, OtpPurpose purpose);
    String verify(String email, String otp, OtpPurpose purpose, String challengeId);
    String verifyLogin(String otp, OtpPurpose purpose, String challengeId);
    void requireAndConsumeVerification(String email, OtpPurpose purpose, String verificationToken);
}
