package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.request.OtpSendRequest;
import com.example.demo.dto.request.OtpVerifyRequest;
import com.example.demo.dto.response.OtpResponse;
import com.example.demo.service.OtpService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/otp")
public class OtpController {
    private final OtpService otpService;

    public OtpController(OtpService otpService) {
        this.otpService = otpService;
    }

    @PostMapping("/send")
    public ResponseEntity<OtpResponse> send(@Valid @RequestBody OtpSendRequest request) {
        return ResponseEntity.ok(OtpResponse.sent(otpService.send(request.email(), request.purpose())));
    }

    @PostMapping("/verify")
    public ResponseEntity<OtpResponse> verify(@Valid @RequestBody OtpVerifyRequest request) {
        return ResponseEntity.ok(OtpResponse.verified(otpService.verify(request.email(), request.otp(),
                request.purpose(), request.challengeId())));
    }
}
