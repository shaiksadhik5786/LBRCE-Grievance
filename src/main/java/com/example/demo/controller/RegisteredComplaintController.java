package com.example.demo.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.request.RegisteredComplaintRequest;
import com.example.demo.dto.response.RegisteredComplaintResponse;
import com.example.demo.entity.Admin;
import com.example.demo.entity.Complaint;
import com.example.demo.entity.RegisteredComplaint;
import com.example.demo.entity.Staff;
import com.example.demo.entity.Stream;
import com.example.demo.entity.Student;
import com.example.demo.repository.AdminRepository;
import com.example.demo.repository.ComplaintRepository;
import com.example.demo.repository.StaffRepository;
import com.example.demo.repository.StreamRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.service.RegisteredComplaintService;

@RestController
@RequestMapping("/registered-complaints")
public class RegisteredComplaintController {

    private final RegisteredComplaintService registeredComplaintService;

    private final ComplaintRepository complaintRepository;
    private final StudentRepository studentRepository;
    private final AdminRepository adminRepository;
    private final StreamRepository streamRepository;
    private final StaffRepository staffRepository;

    public RegisteredComplaintController(
            RegisteredComplaintService registeredComplaintService,
            ComplaintRepository complaintRepository,
            StudentRepository studentRepository,
            AdminRepository adminRepository,
            StreamRepository streamRepository,
            StaffRepository staffRepository) {

        this.registeredComplaintService = registeredComplaintService;
        this.complaintRepository = complaintRepository;
        this.studentRepository = studentRepository;
        this.adminRepository = adminRepository;
        this.streamRepository = streamRepository;
        this.staffRepository = staffRepository;
    }

    // CREATE
    @PostMapping
    public RegisteredComplaintResponse createRegisteredComplaint(
            @RequestBody RegisteredComplaintRequest request) {

        RegisteredComplaint registeredComplaint =
                convertToEntity(request);

        RegisteredComplaint saved =
                registeredComplaintService
                        .saveRegisteredComplaint(registeredComplaint);

        return convertToResponse(saved);
    }

    // GET ALL
    @GetMapping
    public List<RegisteredComplaintResponse>
            getAllRegisteredComplaints() {

        return registeredComplaintService
                .getAllRegisteredComplaints()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // GET BY ID
    @GetMapping("/{id}")
    public RegisteredComplaintResponse
            getRegisteredComplaintById(
                    @PathVariable("id") Integer id) {

        RegisteredComplaint registeredComplaint =
                registeredComplaintService
                        .getRegisteredComplaintById(id);

        return convertToResponse(registeredComplaint);
    }

    // UPDATE
    @PutMapping("/{id}")
    public RegisteredComplaintResponse
            updateRegisteredComplaint(
                    @PathVariable("id") Integer id,
                    @RequestBody RegisteredComplaintRequest request) {

        RegisteredComplaint registeredComplaint =
                convertToEntity(request);

        RegisteredComplaint updated =
                registeredComplaintService
                        .updateRegisteredComplaint(
                                id,
                                registeredComplaint
                        );

        return convertToResponse(updated);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public String deleteRegisteredComplaint(
            @PathVariable("id") Integer id) {

        registeredComplaintService
                .deleteRegisteredComplaint(id);

        return "Registered Complaint deleted successfully";
    }

    // REQUEST DTO -> ENTITY
    private RegisteredComplaint convertToEntity(
            RegisteredComplaintRequest request) {

        RegisteredComplaint registeredComplaint =
                new RegisteredComplaint();

        // Complaint
        if (request.getComplaintId() != null) {

            Complaint complaint =
                    complaintRepository
                            .findById(request.getComplaintId())
                            .orElse(null);

            registeredComplaint.setComplaint(complaint);
        }

        // Student
        if (request.getStudentId() != null) {

            Student student =
                    studentRepository
                            .findById(request.getStudentId())
                            .orElse(null);

            registeredComplaint.setStudent(student);
        }

        // Admin
        if (request.getAdminId() != null) {

            Admin admin =
                    adminRepository
                            .findById(request.getAdminId())
                            .orElse(null);

            registeredComplaint.setAdmin(admin);
        }

        // Stream
        if (request.getStreamId() != null) {

            Stream stream =
                    streamRepository
                            .findById(request.getStreamId())
                            .orElse(null);

            registeredComplaint.setStream(stream);
        }

        // Assigned Staff
        if (request.getAssignedStaffId() != null) {

            Staff assignedStaff =
                    staffRepository
                            .findById(request.getAssignedStaffId())
                            .orElse(null);

            registeredComplaint.setAssignedStaff(assignedStaff);
        }

        registeredComplaint.setAssignedDate(
                request.getAssignedDate());

        registeredComplaint.setPriority(
                request.getPriority());

        registeredComplaint.setRegistrationStatus(
                request.getRegistrationStatus());

        registeredComplaint.setStaffRemarks(
                request.getStaffRemarks());

        registeredComplaint.setResolution(
                request.getResolution());

        registeredComplaint.setResolvedDate(
                request.getResolvedDate());

        registeredComplaint.setVerificationStatus(
                request.getVerificationStatus());

        registeredComplaint.setVerificationDate(
                request.getVerificationDate());

        return registeredComplaint;
    }

    // ENTITY -> RESPONSE DTO
    private RegisteredComplaintResponse convertToResponse(
            RegisteredComplaint registeredComplaint) {

        if (registeredComplaint == null) {
            return null;
        }

        RegisteredComplaintResponse response =
                new RegisteredComplaintResponse();

        response.setRegisteredComplaintId(
                registeredComplaint.getRegisteredComplaintId());

        if (registeredComplaint.getComplaint() != null) {

            response.setComplaintId(
                    registeredComplaint.getComplaint()
                            .getComplaintId());
        }

        if (registeredComplaint.getStudent() != null) {

            response.setStudentId(
                    registeredComplaint.getStudent()
                            .getStudentId());
        }

        if (registeredComplaint.getAdmin() != null) {

            response.setAdminId(
                    registeredComplaint.getAdmin()
                            .getAdminId());
        }

        if (registeredComplaint.getStream() != null) {

            response.setStreamId(
                    registeredComplaint.getStream()
                            .getStreamId());
        }

        if (registeredComplaint.getAssignedStaff() != null) {

            response.setAssignedStaffId(
                    registeredComplaint.getAssignedStaff()
                            .getStaffId());
        }

        response.setRegisteredDate(
                registeredComplaint.getRegisteredDate());

        response.setAssignedDate(
                registeredComplaint.getAssignedDate());

        response.setPriority(
                registeredComplaint.getPriority());

        response.setRegistrationStatus(
                registeredComplaint.getRegistrationStatus());

        response.setStaffRemarks(
                registeredComplaint.getStaffRemarks());

        response.setResolution(
                registeredComplaint.getResolution());

        response.setResolvedDate(
                registeredComplaint.getResolvedDate());

        response.setVerificationStatus(
                registeredComplaint.getVerificationStatus());

        response.setVerificationDate(
                registeredComplaint.getVerificationDate());

        return response;
    }
}