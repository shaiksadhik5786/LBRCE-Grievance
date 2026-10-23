package com.example.demo.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.ApiDtos.LoginRequest;
import com.example.demo.dto.ApiDtos.LoginResponse;
import com.example.demo.entity.Admin;
import com.example.demo.entity.Staff;
import com.example.demo.entity.Student;
import com.example.demo.repository.AdminRepository;
import com.example.demo.repository.StaffRepository;
import com.example.demo.repository.StudentRegistrationRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.securityConfig.CurrentUserService;
import com.example.demo.service.OtpService;
import com.example.demo.service.OtpPurpose;
import com.example.demo.dto.ApiDtos.LoginOtpRequest;

class AuthControllerPasswordTest {
    private static final String RAW_PASSWORD = "CorrectHorse9";
    private PasswordEncoder passwordEncoder;
    private StudentRepository students;
    private StaffRepository staff;
    private AdminRepository admins;
    private StudentRegistrationRepository registrations;
    private JwtEncoder jwtEncoder;
    private AuthController controller;
    private OtpService otpService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        students = mock(StudentRepository.class);
        staff = mock(StaffRepository.class);
        admins = mock(AdminRepository.class);
        registrations = mock(StudentRegistrationRepository.class);
        jwtEncoder = mock(JwtEncoder.class);
        otpService = mock(OtpService.class);
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(
                Jwt.withTokenValue("signed-test-token").header("alg", "HS256").claim("sub", "test-user").build());
        controller = new AuthController(students, staff, admins, registrations, jwtEncoder,
                passwordEncoder, mock(CurrentUserService.class), otpService);
    }

    @Test
    void adminCorrectPasswordRequiresOtpBeforeJwt() {
        Admin account = new Admin();
        account.setAdminId(4);
        account.setRollId("ADM004");
        account.setName("Admin");
        account.setEmail("admin@example.com");
        account.setPassword(passwordEncoder.encode(RAW_PASSWORD));
        when(admins.findByRollId("ADM004")).thenReturn(Optional.of(account));
        when(otpService.send("admin@example.com", OtpPurpose.ADMIN_LOGIN)).thenReturn("admin-challenge");

        LoginResponse response = controller.login(new LoginRequest("ADM004", RAW_PASSWORD));

        assertEquals("ROLE_ADMIN", response.role());
        assertEquals(null, response.token());
        assertEquals(true, response.otpRequired());
        assertEquals("admin-challenge", response.otpChallengeId());
    }

    @Test
    void adminWrongPasswordIsUnauthorized() {
        Admin account = new Admin();
        account.setRollId("ADM004");
        account.setPassword(passwordEncoder.encode(RAW_PASSWORD));
        when(admins.findByRollId("ADM004")).thenReturn(Optional.of(account));

        assertUnauthorized(() -> controller.login(new LoginRequest("ADM004", "wrong-password")));
    }

    @Test
    void staffCorrectPasswordRequiresOtpBeforeJwt() {
        Staff account = new Staff();
        account.setStaffId(8);
        account.setRollId("STF008");
        account.setName("Staff");
        account.setEmail("staff@example.com");
        account.setPassword(passwordEncoder.encode(RAW_PASSWORD));
        when(staff.findByRollId("STF008")).thenReturn(Optional.of(account));
        when(otpService.send("staff@example.com", OtpPurpose.STAFF_LOGIN)).thenReturn("staff-challenge");

        LoginResponse response = controller.login(new LoginRequest("STF008", RAW_PASSWORD));

        assertEquals("ROLE_STAFF", response.role());
        assertEquals(null, response.token());
        assertEquals(true, response.otpRequired());
        assertEquals("staff-challenge", response.otpChallengeId());
    }

    @Test
    void staffWrongPasswordIsUnauthorized() {
        Staff account = new Staff();
        account.setRollId("STF008");
        account.setPassword(passwordEncoder.encode(RAW_PASSWORD));
        when(staff.findByRollId("STF008")).thenReturn(Optional.of(account));

        assertUnauthorized(() -> controller.login(new LoginRequest("STF008", "wrong-password")));
    }

    @Test
    void studentCorrectPasswordAuthenticatesAndReturnsJwt() {
        Student account = new Student();
        account.setStudentId(21);
        account.setRollId("STU021");
        account.setName("Student");
        account.setPassword(passwordEncoder.encode(RAW_PASSWORD));
        when(students.findByRollId("STU021")).thenReturn(Optional.of(account));

        LoginResponse response = controller.login(new LoginRequest("STU021", RAW_PASSWORD));

        assertEquals("ROLE_STUDENT", response.role());
        assertEquals("signed-test-token", response.token());
        verify(jwtEncoder).encode(any(JwtEncoderParameters.class));
    }

    @Test
    void studentWrongPasswordIsUnauthorized() {
        Student account = new Student();
        account.setRollId("STU021");
        account.setPassword(passwordEncoder.encode(RAW_PASSWORD));
        when(students.findByRollId("STU021")).thenReturn(Optional.of(account));

        assertUnauthorized(() -> controller.login(new LoginRequest("STU021", "wrong-password")));
    }

    @Test
    void validAdminLoginOtpIssuesJwt() {
        Admin account = new Admin(); account.setAdminId(4); account.setRollId("ADM004");
        account.setName("Admin"); account.setEmail("admin@example.com");
        when(otpService.verifyLogin("123456", OtpPurpose.ADMIN_LOGIN, "challenge")).thenReturn("admin@example.com");
        when(admins.findByEmail("admin@example.com")).thenReturn(Optional.of(account));

        LoginResponse response = controller.verifyLoginOtp(new LoginOtpRequest("challenge", "123456", OtpPurpose.ADMIN_LOGIN));

        assertEquals("signed-test-token", response.token());
        assertEquals(false, response.otpRequired());
        verify(jwtEncoder).encode(any(JwtEncoderParameters.class));
    }

    private void assertUnauthorized(Runnable login) {
        ResponseStatusException error = assertThrows(ResponseStatusException.class, login::run);
        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatusCode());
    }
}
