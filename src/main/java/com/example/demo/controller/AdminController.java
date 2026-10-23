package com.example.demo.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import com.example.demo.dto.request.AdminRequest;
import com.example.demo.dto.response.AdminResponse;
import com.example.demo.entity.Admin;
import com.example.demo.service.AdminService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admins")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<AdminResponse> getAdminById(
            @PathVariable("id") Integer id) {

        Admin admin = adminService.getAdminById(id);

        return ResponseEntity.ok(convertToResponse(admin));
    }

    // GET BY ROLL ID
    @GetMapping("/roll/{rollId}")
    public ResponseEntity<AdminResponse> getAdminByRollId(
            @PathVariable("rollId") String rollId) {

        Admin admin = adminService.getAdminByRollId(rollId);

        return ResponseEntity.ok(convertToResponse(admin));
    }

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<AdminResponse> login(
            @RequestParam("rollId") String rollId,
            @RequestParam("password") String password) {

        Admin admin = adminService.login(rollId, password);

        return ResponseEntity.ok(convertToResponse(admin));
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<List<AdminResponse>> getAllAdmins() {

        List<AdminResponse> admins = adminService.getAllAdmins()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(admins);
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<AdminResponse> updateAdmin(
            @PathVariable("id") Integer id,
            @Valid @RequestBody AdminRequest request) {

        Admin admin = convertToEntity(request);

        Admin updatedAdmin = adminService.updateAdmin(id, admin, request.getEmailVerificationToken());

        return ResponseEntity.ok(convertToResponse(updatedAdmin));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAdmin(
            @PathVariable("id") Integer id) {

        adminService.deleteAdmin(id);

        return ResponseEntity.ok("Admin deleted successfully");
    }

    // REQUEST DTO -> ENTITY
    private Admin convertToEntity(AdminRequest request) {

        Admin admin = new Admin();

        admin.setRollId(request.getRollId());
        admin.setName(request.getName());
        admin.setEmail(request.getEmail());
        admin.setPhone(request.getPhone());
        admin.setDesignation(request.getDesignation());
        admin.setPassword(request.getPassword());

        return admin;
    }

    // ENTITY -> RESPONSE DTO
    private AdminResponse convertToResponse(Admin admin) {

        AdminResponse response = new AdminResponse();

        response.setAdminId(admin.getAdminId());
        response.setRollId(admin.getRollId());
        response.setName(admin.getName());
        response.setEmail(admin.getEmail());
        response.setPhone(admin.getPhone());
        response.setDesignation(admin.getDesignation());

        return response;
    }
}
