package com.example.demo.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.example.demo.dto.request.RejectRegistrationRequest;
import com.example.demo.dto.request.StudentRegistrationRequestDto;
import com.example.demo.dto.response.StudentRegistrationResponse;
import com.example.demo.entity.StudentRegistrationRequest.Status;
import com.example.demo.service.StudentRegistrationService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/student-registrations")
@Validated
public class StudentRegistrationController {
    private final StudentRegistrationService service;
    public StudentRegistrationController(StudentRegistrationService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<StudentRegistrationResponse> submit(@Valid @RequestBody StudentRegistrationRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.submit(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<StudentRegistrationResponse> list(@RequestParam(required = false) Status status,
            @RequestParam(required = false) Integer academicYearId,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String rollId,
            @RequestParam(required = false) String name) {
        return service.list(status, academicYearId, department, rollId, name);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public StudentRegistrationResponse get(@PathVariable Integer id) { return service.get(id); }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public StudentRegistrationResponse approve(@PathVariable Integer id) { return service.approve(id); }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public StudentRegistrationResponse reject(@PathVariable Integer id, @Valid @RequestBody RejectRegistrationRequest request) {
        return service.reject(id, request.getReason());
    }
}
