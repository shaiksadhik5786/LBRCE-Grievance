package com.example.demo.dto.response;

public record OtpResponse(String message, String challengeId, String verificationToken) {
    public static OtpResponse sent(String challengeId) {
        return new OtpResponse("OTP sent to your email.", challengeId, null);
    }

    public static OtpResponse verified(String token) {
        return new OtpResponse("Email verified successfully.", null, token);
    }
}
