package com.example.demo.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.request.ComplaintRequest;
import com.example.demo.dto.response.ComplaintResponse;
import com.example.demo.entity.AcademicYear;
import com.example.demo.entity.Complaint;
import com.example.demo.entity.Student;
import com.example.demo.repository.AcademicYearRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.service.ComplaintService;

@RestController
@RequestMapping("/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;

    private final AcademicYearRepository academicYearRepository;

    private final StudentRepository studentRepository;

    public ComplaintController(
            ComplaintService complaintService,
            AcademicYearRepository academicYearRepository,
            StudentRepository studentRepository) {

        this.complaintService = complaintService;
        this.academicYearRepository = academicYearRepository;
        this.studentRepository = studentRepository;
    }

    // CREATE
    @PostMapping
    public ComplaintResponse createComplaint(
            @RequestBody ComplaintRequest request) {

        Complaint complaint = convertToEntity(request);

        Complaint saved =
                complaintService.saveComplaint(complaint);

        return convertToResponse(saved);
    }

    // GET ALL
    @GetMapping
    public List<ComplaintResponse> getAllComplaints() {

        return complaintService.getAllComplaints()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ComplaintResponse getComplaintById(
            @PathVariable("id") Integer id) {

        Complaint complaint =
                complaintService.getComplaintById(id);

        return convertToResponse(complaint);
    }

    // UPDATE
    @PutMapping("/{id}")
    public ComplaintResponse updateComplaint(
            @PathVariable("id") Integer id,
            @RequestBody ComplaintRequest request) {

        Complaint complaint = convertToEntity(request);

        Complaint updated =
                complaintService.updateComplaint(id, complaint);

        return convertToResponse(updated);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public String deleteComplaint(
            @PathVariable("id") Integer id) {

        complaintService.deleteComplaint(id);

        return "Complaint deleted successfully";
    }

    // REQUEST DTO -> ENTITY
    private Complaint convertToEntity(
            ComplaintRequest request) {

        Complaint complaint = new Complaint();

        AcademicYear academicYear =
                academicYearRepository
                        .findById(request.getAcademicYearId())
                        .orElse(null);

        Student student =
                studentRepository
                        .findById(request.getStudentId())
                        .orElse(null);

        complaint.setAcademicYear(academicYear);
        complaint.setStudent(student);

        complaint.setComplaintType(
                request.getComplaintType());

        complaint.setSubject(
                request.getSubject());

        complaint.setDescription(
                request.getDescription());

        complaint.setImageName(
                request.getImageName());

        complaint.setImagePath(
                request.getImagePath());

        complaint.setImageType(
                request.getImageType());

        return complaint;
    }

    // ENTITY -> RESPONSE DTO
    private ComplaintResponse convertToResponse(
            Complaint complaint) {

        if (complaint == null) {
            return null;
        }

        ComplaintResponse response =
                new ComplaintResponse();

        response.setComplaintId(
                complaint.getComplaintId());

        if (complaint.getAcademicYear() != null) {

            response.setAcademicYearId(
                    complaint.getAcademicYear()
                            .getAcademicYearId());
        }

        if (complaint.getStudent() != null) {

            response.setStudentId(
                    complaint.getStudent()
                            .getStudentId());
        }

        response.setComplaintType(
                complaint.getComplaintType());

        response.setSubject(
                complaint.getSubject());

        response.setDescription(
                complaint.getDescription());

        response.setImageName(
                complaint.getImageName());

        response.setImagePath(
                complaint.getImagePath());

        response.setImageType(
                complaint.getImageType());

        response.setCreatedDate(
                complaint.getCreatedDate());

        return response;
    }
}