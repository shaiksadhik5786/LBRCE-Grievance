package com.example.demo.exception;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.MethodArgumentNotValidException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleStatusException(ResponseStatusException ex) {
        HttpStatusCode status = ex.getStatusCode();
        ErrorResponse error = new ErrorResponse(status.value(), ex.getReason(), LocalDateTime.now());
        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage()).filter(value -> value != null && !value.isBlank())
                .distinct().collect(Collectors.joining(" "));
        if (message.isBlank()) message = "Request validation failed.";
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message, LocalDateTime.now());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {

        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Something went wrong on the server.",
                LocalDateTime.now()
        );

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleConflict(DataIntegrityViolationException ex) {
        String details = causeMessages(ex).toLowerCase(Locale.ROOT);
        var knownConstraints = new ArrayList<String>();
        if (details.contains("roll_id") || details.contains("rollid")) knownConstraints.add("roll ID");
        if (details.contains("email")) knownConstraints.add("email");
        if (details.contains("academic_year")) knownConstraints.add("academic year");
        if (details.contains("stream_id")) knownConstraints.add("stream");
        String message = knownConstraints.size() != 1
                ? "A record with one or more of the same values already exists, or a linked record is unavailable."
                : switch (knownConstraints.get(0)) {
                    case "roll ID" -> "An account with this roll ID already exists.";
                    case "email" -> "An account with this email already exists.";
                    case "academic year" -> "The selected academic year is unavailable.";
                    case "stream" -> "The selected stream is unavailable.";
                    default -> "A record with one or more of the same values already exists, or a linked record is unavailable.";
                };
        ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), message, LocalDateTime.now());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    private String causeMessages(Throwable exception) {
        StringBuilder messages = new StringBuilder();
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause.getMessage() != null) messages.append(' ').append(cause.getMessage());
        }
        return messages.toString();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex) {
        String details = causeMessages(ex).toLowerCase(Locale.ROOT);
        String message = details.contains("academicyearid")
                ? "Academic year must be a valid number."
                : details.contains("streamid")
                        ? "Stream must be a valid number."
                        : details.contains("startdate") || details.contains("enddate")
                                ? "Date value is invalid. Use a valid calendar date."
                                : "The request body is missing, malformed, or contains a value in an invalid format.";
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message, LocalDateTime.now());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(AccessDeniedException ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.FORBIDDEN.value(), "You do not have permission to perform this action.", LocalDateTime.now());
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }
}
