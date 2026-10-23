package com.example.demo.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.service.OtpService;
import com.example.demo.service.OtpPurpose;

import static org.mockito.Mockito.mock;

class OtpControllerTest {
    private OtpService otpService;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        otpService = mock(OtpService.class);
        mvc = MockMvcBuilders.standaloneSetup(new OtpController(otpService))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void sendsOtpForValidEmailWithoutReturningOtp() throws Exception {
        mvc.perform(post("/api/otp/send").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"Student@Example.com\",\"purpose\":\"STUDENT_REGISTRATION\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OTP sent to your email."))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("123456"))));
        verify(otpService).send("Student@Example.com", OtpPurpose.STUDENT_REGISTRATION);
    }

    @Test
    void rejectsInvalidEmail() throws Exception {
        mvc.perform(post("/api/otp/send").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"not-an-email\",\"purpose\":\"STUDENT_REGISTRATION\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email address is invalid."));
    }

    @Test
    void verifiesSixDigitOtpAndReturnsOnlyOpaqueToken() throws Exception {
        when(otpService.verify("student@example.com", "123456", OtpPurpose.STUDENT_REGISTRATION, "challenge"))
                .thenReturn("opaque-token");
        mvc.perform(post("/api/otp/verify").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"student@example.com\",\"otp\":\"123456\",\"purpose\":\"STUDENT_REGISTRATION\",\"challengeId\":\"challenge\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationToken").value("opaque-token"))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("123456"))));
    }

    @Test
    void rejectsOtpThatIsNotExactlySixDigits() throws Exception {
        mvc.perform(post("/api/otp/verify").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"student@example.com\",\"otp\":\"12345x\",\"purpose\":\"STUDENT_REGISTRATION\",\"challengeId\":\"challenge\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("OTP must contain exactly 6 digits."));
    }
}
