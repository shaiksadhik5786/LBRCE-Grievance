package com.example.demo.dto;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import com.example.demo.dto.ApiDtos.LoginResponse;
import com.example.demo.dto.ApiDtos.ProfileResponse;
import com.example.demo.dto.response.AdminResponse;
import com.example.demo.dto.response.StaffResponse;
import com.example.demo.dto.response.StudentRegistrationResponse;
import com.example.demo.dto.response.StudentResponse;

class ApiResponseSecurityTest {
    @Test
    void accountAndAuthenticationResponsesDoNotDefinePasswordHashOrSecretFields() {
        List<Class<?>> responses = List.of(LoginResponse.class, ProfileResponse.class, AdminResponse.class,
                StaffResponse.class, StudentResponse.class, StudentRegistrationResponse.class);
        for (Class<?> response : responses) {
            List<String> fieldNames = response.isRecord()
                    ? Arrays.stream(response.getRecordComponents()).map(component -> component.getName()).toList()
                    : Arrays.stream(response.getDeclaredFields()).map(field -> field.getName()).toList();
            assertFalse(fieldNames.stream().map(name -> name.toLowerCase(Locale.ROOT))
                    .anyMatch(name -> name.contains("password") || name.contains("hash") || name.contains("secret")),
                    response.getSimpleName());
        }
    }
}
