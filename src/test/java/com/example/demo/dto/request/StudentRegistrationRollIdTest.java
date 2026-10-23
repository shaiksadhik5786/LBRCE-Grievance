package com.example.demo.dto.request;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class StudentRegistrationRollIdTest {
    @Test
    void registrationAcceptsStudentRollId() {
        var acceptedFields = Arrays.stream(StudentRegistrationRequestDto.class.getDeclaredFields())
                .map(java.lang.reflect.Field::getName).toList();
        assertTrue(acceptedFields.contains("rollId"));
    }
}
