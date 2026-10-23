package com.example.demo.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import com.example.demo.dto.request.StaffRequest;
import com.example.demo.dto.response.StaffResponse;
import com.example.demo.entity.Staff;
import com.example.demo.service.StaffService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/staff")
public class StaffController {

    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    // CREATE
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StaffResponse> saveStaff(
            @Valid @RequestBody StaffRequest request) {

        Staff staff = convertToEntity(request);

        Staff savedStaff = staffService.saveStaff(staff, request.getEmailVerificationToken());

        return ResponseEntity.ok(convertToResponse(savedStaff));
    }

    // GET BY ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StaffResponse> getStaffById(
            @PathVariable("id") Integer id) {

        Staff staff = staffService.getStaffById(id);

        return ResponseEntity.ok(convertToResponse(staff));
    }

    // GET BY ROLL ID
    @GetMapping("/roll/{rollId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StaffResponse> getStaffByRollId(
            @PathVariable("rollId") String rollId) {

        Staff staff = staffService.getStaffByRollId(rollId);

        return ResponseEntity.ok(convertToResponse(staff));
    }

    // LOGIN
    @PostMapping("/login")
    @PreAuthorize("denyAll()")
    public ResponseEntity<StaffResponse> login(
            @RequestParam("rollId") String rollId,
            @RequestParam("password") String password) {

        Staff staff = staffService.login(rollId, password);

        return ResponseEntity.ok(convertToResponse(staff));
    }

    // GET ALL
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StaffResponse>> getAllStaff() {

        List<StaffResponse> staffList = staffService.getAllStaff()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(staffList);
    }

    @GetMapping("/by-stream/{streamId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StaffResponse>> getStaffByStream(@PathVariable Integer streamId) {
        return ResponseEntity.ok(staffService.getStaffByStream(streamId).stream().map(this::convertToResponse).toList());
    }

    // UPDATE
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StaffResponse> updateStaff(
            @PathVariable("id") Integer id,
            @Valid @RequestBody StaffRequest request) {

        Staff staff = convertToEntity(request);

        Staff updatedStaff = staffService.updateStaff(id, staff, request.getEmailVerificationToken());

        return ResponseEntity.ok(convertToResponse(updatedStaff));
    }

    // DELETE
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteStaff(
            @PathVariable("id") Integer id) {

        staffService.deleteStaff(id);

        return ResponseEntity.ok("Staff deleted successfully");
    }

    // REQUEST DTO -> ENTITY
    private Staff convertToEntity(StaffRequest request) {

        Staff staff = new Staff();

        staff.setRollId(request.getRollId());
        staff.setName(request.getName());
        staff.setEmail(request.getEmail());
        staff.setPhone(request.getPhone());
        staff.setDesignation(request.getDesignation());
        staff.setDepartment(request.getDepartment());
        staff.setPassword(request.getPassword());
        staff.setStreamId(request.getStreamId());

        return staff;
    }

    // ENTITY -> RESPONSE DTO
    private StaffResponse convertToResponse(Staff staff) {

        StaffResponse response = new StaffResponse();

        response.setStaffId(staff.getStaffId());
        response.setRollId(staff.getRollId());
        response.setName(staff.getName());
        response.setEmail(staff.getEmail());
        response.setPhone(staff.getPhone());
        response.setDesignation(staff.getDesignation());
        response.setDepartment(staff.getDepartment());
        response.setStreamId(staff.getStreamId());

        return response;
    }
}
