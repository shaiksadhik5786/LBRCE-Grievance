package com.example.demo.controller;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Map;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.ApiDtos.*;
import com.example.demo.service.ComplaintWorkflowService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class ComplaintWorkflowController {
    private final ComplaintWorkflowService service;
    public ComplaintWorkflowController(ComplaintWorkflowService service) { this.service = service; }

    @PostMapping(value = "/complaints", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ComplaintDto> create(Authentication auth,
            @RequestParam String subject, @RequestParam String description,
            @RequestPart(required = false) MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(auth, subject, description, file));
    }

    @GetMapping("/complaints")
    @PreAuthorize("hasAnyRole('STUDENT','STAFF','ADMIN')")
    public ComplaintPageResponse list(Authentication auth,
            @RequestParam(required = false) Integer academicYearId, @RequestParam(required = false) Integer streamId,
            @RequestParam(required = false) String department, @RequestParam(required = false) String complaintType,
            @RequestParam(required = false) String status, @RequestParam(required = false) String verificationStatus,
            @RequestParam(required = false) String priority, @RequestParam(required = false) Integer assignedStaffId,
            @RequestParam(required = false) String rollId, @RequestParam(required = false) String studentName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        if (from != null && to != null && from.isAfter(to)) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date must be on or before end date.");
        int safeSize = Math.max(1, Math.min(size, 100));
        var filter = new ComplaintWorkflowService.ComplaintFilter(academicYearId, streamId, department, complaintType,
                status, verificationStatus, priority, assignedStaffId, rollId, studentName, from, to);
        Page<ComplaintDto> result = service.list(auth, filter, PageRequest.of(Math.max(0, page), safeSize, Sort.by(Sort.Direction.DESC, "registeredDate")));
        return new ComplaintPageResponse(result.getContent(), result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    @GetMapping("/complaints/{id}")
    @PreAuthorize("hasAnyRole('STUDENT','STAFF','ADMIN')")
    public ComplaintDto detail(@PathVariable Integer id, Authentication auth) { return service.detail(id, auth); }

    @PutMapping(value = "/complaints/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('STUDENT')")
    public ComplaintDto edit(@PathVariable Integer id, Authentication auth,
            @RequestParam String subject, @RequestParam String description,
            @RequestPart(required = false) MultipartFile file, @RequestParam(defaultValue = "false") boolean removeAttachment) {
        return service.edit(id, auth, subject, description, file, removeAttachment);
    }

    @PutMapping("/complaints/{id}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ComplaintDto review(@PathVariable Integer id, Authentication auth, @Valid @RequestBody ReviewRequest request) {
        return service.review(id, auth, request);
    }

    @PutMapping("/complaints/{id}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ComplaintDto assign(@PathVariable Integer id, Authentication auth, @Valid @RequestBody AssignmentRequest request) {
        return service.assign(id, auth, request);
    }

    @PutMapping("/complaints/{id}/start")
    @PreAuthorize("hasRole('STAFF')")
    public ComplaintDto start(@PathVariable Integer id, Authentication auth, @RequestBody(required = false) Map<String, String> body) {
        return service.start(id, auth, body == null ? null : body.get("remarks"));
    }

    @PutMapping(value = "/complaints/{id}/solution", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('STAFF')")
    public ComplaintDto solution(@PathVariable Integer id, Authentication auth,
            @RequestParam String solution, @RequestParam(required = false) String remarks,
            @RequestPart(required = false) MultipartFile file) {
        return service.submitSolution(id, auth, new SolutionRequest(solution, remarks), file);
    }

    @PutMapping("/complaints/{id}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ComplaintDto verify(@PathVariable Integer id, Authentication auth, @Valid @RequestBody VerificationRequest request) {
        return service.verify(id, auth, request);
    }

    @GetMapping("/complaints/{id}/attachments/{kind}")
    @PreAuthorize("hasAnyRole('STUDENT','STAFF','ADMIN')")
    public ResponseEntity<Resource> attachment(@PathVariable Integer id, @PathVariable String kind, Authentication auth) {
        var download = service.attachment(id, kind, auth);
        Path path = download.path();
        String filename = download.displayName() == null ? "proof" : download.displayName();
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(download.mimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(filename).build().toString())
                .body(new FileSystemResource(path));
    }

    @GetMapping("/dashboard/stats")
    @PreAuthorize("hasAnyRole('STUDENT','STAFF','ADMIN')")
    public Map<String, Long> stats(Authentication auth) { return service.stats(auth); }
}
