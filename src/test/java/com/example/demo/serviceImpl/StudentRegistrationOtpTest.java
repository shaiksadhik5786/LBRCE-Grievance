package com.example.demo.serviceImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.request.StudentRegistrationRequestDto;
import com.example.demo.entity.AcademicYear;
import com.example.demo.entity.StudentRegistrationRequest;
import com.example.demo.entity.StudentRegistrationRequest.Status;
import com.example.demo.repository.AcademicYearRepository;
import com.example.demo.repository.AdminRepository;
import com.example.demo.repository.StaffRepository;
import com.example.demo.repository.StudentRegistrationRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.service.OtpService;
import com.example.demo.service.OtpPurpose;

class StudentRegistrationOtpTest {
    private StudentRegistrationRepository registrations;
    private OtpService otpService;
    private StudentRegistrationServiceImpl service;

    @BeforeEach
    void setUp() {
        registrations = mock(StudentRegistrationRepository.class);
        StudentRepository students = mock(StudentRepository.class);
        AcademicYearRepository years = mock(AcademicYearRepository.class);
        otpService = mock(OtpService.class);
        AcademicYear year = new AcademicYear();
        year.setIsActive("Y");
        when(years.findById(2)).thenReturn(Optional.of(year));
        when(registrations.save(any(StudentRegistrationRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        service = new StudentRegistrationServiceImpl(registrations, students, years, mock(StaffRepository.class),
                mock(AdminRepository.class), new BCryptPasswordEncoder(), otpService);
    }

    @Test
    void registrationWithoutTrustedVerificationIsRejected() {
        StudentRegistrationRequestDto request = validRequest();
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Please verify your email with OTP before completing registration."))
                .when(otpService).requireAndConsumeVerification(request.getEmail(), OtpPurpose.STUDENT_REGISTRATION, request.getEmailVerificationToken());
        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.submit(request));
        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
    }

    @Test
    void verifiedRegistrationIsCreatedPendingAndDoesNotAutoApprove() {
        StudentRegistrationRequestDto request = validRequest();
        var response = service.submit(request);
        verify(otpService).requireAndConsumeVerification("student@example.com", OtpPurpose.STUDENT_REGISTRATION, "opaque-token");
        assertEquals(Status.PENDING, response.getStatus());
    }

    @Test
    void changedEmailMustMatchTheServerSideVerification() {
        StudentRegistrationRequestDto request = validRequest();
        request.setEmail("changed@example.com");
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Please verify your email with OTP before completing registration."))
                .when(otpService).requireAndConsumeVerification("changed@example.com", OtpPurpose.STUDENT_REGISTRATION, "opaque-token");
        assertThrows(ResponseStatusException.class, () -> service.submit(request));
        verify(otpService).requireAndConsumeVerification("changed@example.com", OtpPurpose.STUDENT_REGISTRATION, "opaque-token");
    }

    private StudentRegistrationRequestDto validRequest() {
        StudentRegistrationRequestDto request = new StudentRegistrationRequestDto();
        request.setRollId("STU012");
        request.setAcademicYearId(2);
        request.setName("Student");
        request.setDepartment("Computer Science and Engineering");
        request.setEmail("student@example.com");
        request.setPhone("9876543210");
        request.setPassword("InitialPassword9");
        request.setConfirmPassword("InitialPassword9");
        request.setEmailVerificationToken("opaque-token");
        return request;
    }
}
