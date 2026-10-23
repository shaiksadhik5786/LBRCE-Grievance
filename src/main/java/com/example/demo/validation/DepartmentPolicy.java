package com.example.demo.validation;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class DepartmentPolicy {
    private static final List<String> ALLOWED = List.of(
            "Computer Science and Engineering",
            "Electronics and Communication Engineering",
            "Electrical and Electronics Engineering",
            "Mechanical Engineering",
            "Civil Engineering",
            "Information Technology",
            "Artificial Intelligence and Data Science");

    private DepartmentPolicy() {}

    public static String validate(String department) {
        String normalized = department == null ? "" : department.trim();
        if (!ALLOWED.contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Select a department from the approved department list.");
        }
        return normalized;
    }
}
