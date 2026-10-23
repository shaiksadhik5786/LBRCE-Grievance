package com.example.demo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import com.example.demo.service.OtpPurpose;

public record OtpVerifyRequest(
        @NotBlank(message = "Email is required.")
        @Email(message = "Email address is invalid.")
        @Size(max = 100, message = "Email address is too long.")
        String email,
        @NotBlank(message = "OTP is required.")
        @Pattern(regexp = "^[0-9]{6}$", message = "OTP must contain exactly 6 digits.")
        String otp,
        @NotNull(message = "OTP purpose is required.") OtpPurpose purpose,
        @NotBlank(message = "OTP challenge is required.") @Size(max = 100) String challengeId) {
}
