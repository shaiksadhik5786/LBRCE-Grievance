package com.example.demo.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.server.ResponseStatusException;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void databaseConflictReturnsSafeFieldSpecificMessage() {
        var response = handler.handleConflict(new DataIntegrityViolationException("Duplicate entry STU001 for key student.roll_id"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("An account with this roll ID already exists.", response.getBody().getMessage());
        assertFalse(response.getBody().getMessage().contains("STU001"));
    }

    @Test
    void internalFailureDoesNotExposeExceptionDetails() {
        var response = handler.handleException(new RuntimeException("SQL: select * from student; C:\\internal\\file.java"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Something went wrong on the server.", response.getBody().getMessage());
        assertFalse(response.getBody().getMessage().contains("SQL"));
    }

    @Test
    void businessFailureKeepsItsMeaningfulReason() {
        var response = handler.handleStatusException(new ResponseStatusException(HttpStatus.CONFLICT,
                "Student with roll ID STU001 already exists."));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Student with roll ID STU001 already exists.", response.getBody().getMessage());
    }

    @Test
    void malformedAcademicYearFormatReturnsAFieldSpecificValidationMessage() {
        var response = handler.handleUnreadable(new HttpMessageNotReadableException(
                "Invalid academicYearId value", mock(HttpInputMessage.class)));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Academic year must be a valid number.", response.getBody().getMessage());
    }
}
