package com.example.demo.controller;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.ApiDtos.LoginRequest;
import com.example.demo.dto.ApiDtos.LoginResponse;
import com.example.demo.dto.ApiDtos.LoginOtpRequest;
import com.example.demo.service.OtpService;
import com.example.demo.service.OtpPurpose;
import com.example.demo.dto.ApiDtos.ProfileResponse;
import com.example.demo.entity.*;
import com.example.demo.entity.StudentRegistrationRequest.Status;
import com.example.demo.repository.*;
import com.example.demo.securityConfig.CurrentUserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final StudentRepository students;
    private final StaffRepository staff;
    private final AdminRepository admins;
    private final StudentRegistrationRepository registrations;
    private final JwtEncoder jwtEncoder;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUser;
    private final OtpService otpService;

    public AuthController(StudentRepository students, StaffRepository staff, AdminRepository admins,
            StudentRegistrationRepository registrations, JwtEncoder jwtEncoder,
            PasswordEncoder passwordEncoder, CurrentUserService currentUser, OtpService otpService) {
        this.students = students; this.staff = staff; this.admins = admins; this.registrations = registrations;
        this.jwtEncoder = jwtEncoder; this.passwordEncoder = passwordEncoder; this.currentUser = currentUser;
        this.otpService = otpService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        String rollId = request.rollId().trim();
        Optional<LoginIdentity> identity = students.findByRollId(rollId).filter(s -> passwordEncoder.matches(request.password(), s.getPassword()))
                .map(s -> new LoginIdentity("ROLE_STUDENT", s.getStudentId(), s.getRollId(), s.getName(), s.getEmail()))
                .or(() -> staff.findByRollId(rollId).filter(s -> passwordEncoder.matches(request.password(), s.getPassword()))
                        .map(s -> new LoginIdentity("ROLE_STAFF", s.getStaffId(), s.getRollId(), s.getName(), s.getEmail())))
                .or(() -> admins.findByRollId(rollId).filter(a -> passwordEncoder.matches(request.password(), a.getPassword()))
                        .map(a -> new LoginIdentity("ROLE_ADMIN", a.getAdminId(), a.getRollId(), a.getName(), a.getEmail())));

        if (identity.isEmpty()) rejectUnapprovedOrInvalid(rollId, request.password());
        LoginIdentity user = identity.orElseThrow();
        if (!"ROLE_STUDENT".equals(user.role())) {
            OtpPurpose purpose = "ROLE_ADMIN".equals(user.role()) ? OtpPurpose.ADMIN_LOGIN : OtpPurpose.STAFF_LOGIN;
            String challengeId = otpService.send(user.email(), purpose);
            return new LoginResponse(null, user.role(), user.userId(), user.rollId(), user.name(), true,
                    challengeId, maskEmail(user.email()));
        }
        return issueToken(user);
    }

    @PostMapping("/login/verify-otp")
    public LoginResponse verifyLoginOtp(@Valid @RequestBody LoginOtpRequest request) {
        if (request.purpose() != OtpPurpose.ADMIN_LOGIN && request.purpose() != OtpPurpose.STAFF_LOGIN)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Login OTP purpose is invalid.");
        String email = otpService.verifyLogin(request.otp(), request.purpose(), request.challengeId());
        Optional<LoginIdentity> identity = request.purpose() == OtpPurpose.ADMIN_LOGIN
                ? admins.findByEmail(email).map(a -> new LoginIdentity("ROLE_ADMIN", a.getAdminId(), a.getRollId(), a.getName(), a.getEmail()))
                : staff.findByEmail(email).map(s -> new LoginIdentity("ROLE_STAFF", s.getStaffId(), s.getRollId(), s.getName(), s.getEmail()));
        return issueToken(identity.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "The login challenge is no longer valid.")));
    }

    private LoginResponse issueToken(LoginIdentity user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer("college-grievance-system").subject(user.rollId())
                .issuedAt(now).expiresAt(now.plus(8, ChronoUnit.HOURS)).claim("role", user.role())
                .claim("userId", user.userId()).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new LoginResponse(token, user.role(), user.userId(), user.rollId(), user.name(), false, null, null);
    }

    private String maskEmail(String email) {
        int at = email == null ? -1 : email.indexOf('@');
        if (at <= 0) return "your registered email";
        String local = email.substring(0, at);
        return local.substring(0, 1) + "***" + email.substring(at);
    }

    private void rejectUnapprovedOrInvalid(String rollId, String password) {
        StudentRegistrationRequest request = registrations.findFirstByRollIdAndStatusOrderBySubmittedAtDesc(rollId, Status.PENDING)
                .or(() -> registrations.findFirstByRollIdAndStatusOrderBySubmittedAtDesc(rollId, Status.REJECTED)).orElse(null);
        if (request != null && passwordEncoder.matches(password, request.getPassword())) {
            if (request.getStatus() == Status.PENDING)
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Your registration is awaiting admin approval.");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Your registration was rejected. Contact the college office for help.");
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid roll ID or password.");
    }

    @GetMapping("/me")
    public ProfileResponse me(Authentication authentication) {
        var p = currentUser.profile(authentication);
        return new ProfileResponse(p.role(), p.userId(), p.rollId(), p.name(), p.email(), p.phone(), p.department(),
                p.designation(), p.academicYearId(), p.academicYear(), p.streamId(), p.streamName());
    }

    private record LoginIdentity(String role, Integer userId, String rollId, String name, String email) {}
}
