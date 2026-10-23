package com.example.demo.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import com.example.demo.dto.request.AcademicYearRequest;
import com.example.demo.dto.response.AcademicYearResponse;
import com.example.demo.entity.AcademicYear;
import com.example.demo.service.AcademicYearService;

@RestController
@RequestMapping("/api/academic-years")
public class AcademicYearController {

    private final AcademicYearService academicYearService;

    public AcademicYearController(AcademicYearService academicYearService) {
        this.academicYearService = academicYearService;
    }

    // CREATE
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public AcademicYearResponse createAcademicYear(
            @RequestBody AcademicYearRequest request) {

        AcademicYear academicYear = convertToEntity(request);

        AcademicYear saved =
                academicYearService.saveAcademicYear(academicYear);

        return convertToResponse(saved);
    }

    // GET ALL
    @GetMapping
    public List<AcademicYearResponse> getAllAcademicYears() {

        return academicYearService.getAllAcademicYears()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // GET BY ID
    @GetMapping("/{id}")
    public AcademicYearResponse getAcademicYearById(
            @PathVariable("id") Integer id) {

        AcademicYear academicYear =
                academicYearService.getAcademicYearById(id);

        return convertToResponse(academicYear);
    }

    // UPDATE
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AcademicYearResponse updateAcademicYear(
            @PathVariable("id") Integer id,
            @RequestBody AcademicYearRequest request) {

        AcademicYear academicYear = convertToEntity(request);

        AcademicYear updated =
                academicYearService.updateAcademicYear(id, academicYear);

        return convertToResponse(updated);
    }

    // DELETE
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteAcademicYear(
            @PathVariable("id") Integer id) {

        academicYearService.deleteAcademicYear(id);

        return "Academic Year deleted successfully";
    }

    // REQUEST DTO -> ENTITY
    private AcademicYear convertToEntity(
            AcademicYearRequest request) {

        AcademicYear academicYear = new AcademicYear();

        academicYear.setYearName(request.getYearName());
        academicYear.setStartDate(request.getStartDate());
        academicYear.setEndDate(request.getEndDate());
        academicYear.setIsActive(request.getIsActive());

        return academicYear;
    }

    // ENTITY -> RESPONSE DTO
    private AcademicYearResponse convertToResponse(
            AcademicYear academicYear) {

        if (academicYear == null) {
            return null;
        }

        AcademicYearResponse response = new AcademicYearResponse();

        response.setAcademicYearId(
                academicYear.getAcademicYearId());

        response.setYearName(
                academicYear.getYearName());

        response.setStartDate(
                academicYear.getStartDate());

        response.setEndDate(
                academicYear.getEndDate());

        response.setIsActive(
                academicYear.getIsActive());

        return response;
    }
}
