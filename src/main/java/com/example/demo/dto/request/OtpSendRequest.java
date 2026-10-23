package com.example.demo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import com.example.demo.service.OtpPurpose;

public record OtpSendRequest(
        @NotBlank(message = "Email is required.")
        @Email(message = "Email address is invalid.")
        @Size(max = 100, message = "Email address is too long.")
        String email,
        @NotNull(message = "OTP purpose is required.") OtpPurpose purpose) {
}
