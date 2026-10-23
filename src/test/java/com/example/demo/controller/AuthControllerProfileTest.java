package com.example.demo.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.example.demo.dto.ApiDtos.ProfileResponse;
import com.example.demo.repository.AdminRepository;
import com.example.demo.repository.StaffRepository;
import com.example.demo.repository.StudentRegistrationRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.securityConfig.CurrentUserService;
import com.example.demo.securityConfig.CurrentUserService.ApiProfile;

class AuthControllerProfileTest {
    @Test
    void profileEndpointProjectsOnlyTheAuthenticatedUsersNonSensitiveFields() {
        CurrentUserService currentUser = mock(CurrentUserService.class);
        Authentication authentication = new UsernamePasswordAuthenticationToken("STU021", null,
                java.util.List.of(new SimpleGrantedAuthority("ROLE_STUDENT")));
        when(currentUser.profile(authentication)).thenReturn(new ApiProfile("ROLE_STUDENT", 21, "STU021",
                "Student Example", "student@example.edu", "9000000021", "Computer Science and Engineering",
                null, 3, "2025-26", null, null));
        AuthController controller = new AuthController(mock(StudentRepository.class), mock(StaffRepository.class),
                mock(AdminRepository.class), mock(StudentRegistrationRepository.class), mock(JwtEncoder.class),
                new BCryptPasswordEncoder(), currentUser, mock(com.example.demo.service.OtpService.class));

        ProfileResponse response = controller.me(authentication);

        assertEquals("STU021", response.rollId());
        assertEquals("Student Example", response.name());
        assertEquals("2025-26", response.academicYear());
        verify(currentUser).profile(authentication);
        var responseFieldNames = Arrays.stream(ProfileResponse.class.getRecordComponents())
                .map(component -> component.getName().toLowerCase(Locale.ROOT)).toList();
        assertFalse(responseFieldNames.stream().anyMatch(name -> name.contains("password") || name.contains("hash")
                || name.contains("token") || name.contains("secret")));
    }
}
