package com.example.demo.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class DepartmentPolicyTest {
    @Test
    void acceptsTheSevenPublishedDepartmentNames() {
        List<String> departments = List.of(
                "Computer Science and Engineering",
                "Electronics and Communication Engineering",
                "Electrical and Electronics Engineering",
                "Mechanical Engineering",
                "Civil Engineering",
                "Information Technology",
                "Artificial Intelligence and Data Science");

        for (String department : departments) assertEquals(department, DepartmentPolicy.validate(department));
    }

    @Test
    void rejectsValuesOutsideThePublishedList() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> DepartmentPolicy.validate("Computer Science"));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        assertEquals("Select a department from the approved department list.", error.getReason());
    }
}
