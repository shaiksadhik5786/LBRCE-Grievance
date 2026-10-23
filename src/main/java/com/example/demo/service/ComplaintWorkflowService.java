package com.example.demo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.ApiDtos.*;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.securityConfig.CurrentUserService;

@Service
public class ComplaintWorkflowService {
    private static final List<String> STATUSES = List.of("REGISTERED", "ACCEPTED", "REJECTED", "ASSIGNED", "IN_PROGRESS", "SOLUTION_SUBMITTED", "REOPENED", "SOLVED");
    private final ComplaintRepository complaints;
    private final RegisteredComplaintRepository workflows;
    private final ComplaintHistoryRepository history;
    private final AcademicYearRepository years;
    private final StreamRepository streams;
    private final StaffRepository staff;
    private final StudentRepository students;
    private final StudentRegistrationRepository registrations;
    private final CurrentUserService currentUser;
    private final UploadStorageService uploads;

    public ComplaintWorkflowService(ComplaintRepository complaints, RegisteredComplaintRepository workflows,
            ComplaintHistoryRepository history, AcademicYearRepository years, StreamRepository streams,
            StaffRepository staff, StudentRepository students, StudentRegistrationRepository registrations,
            CurrentUserService currentUser, UploadStorageService uploads) {
        this.complaints = complaints; this.workflows = workflows; this.history = history; this.years = years;
        this.streams = streams; this.staff = staff; this.students = students; this.registrations = registrations;
        this.currentUser = currentUser; this.uploads = uploads;
    }

    @Transactional
    public ComplaintDto create(Authentication auth, String subject, String description, MultipartFile file) {
        Student student = currentUser.student(auth);
        validateComplaint(subject, description);
        AcademicYear year = academicYearFor(student);
        Complaint complaint = new Complaint();
        complaint.setStudent(student); complaint.setAcademicYear(year);
        complaint.setComplaintType("Student concern"); complaint.setSubject(subject.trim()); complaint.setDescription(description.trim());
        UploadStorageService.StoredFile attachment = uploads.store(file);
        if (attachment != null) {
            complaint.setImageName(attachment.displayName()); complaint.setImagePath(attachment.storedName()); complaint.setImageType(attachment.mimeType());
        }
        Complaint saved = complaints.save(complaint);
        RegisteredComplaint workflow = new RegisteredComplaint();
        workflow.setComplaint(saved); workflow.setStudent(student); workflow.setRegistrationStatus("REGISTERED");
        workflow.setPriority("NORMAL"); workflow.setVerificationStatus("PENDING");
        workflow = workflows.save(workflow);
        addHistory(workflow, null, "REGISTERED", "Complaint submitted", auth, null);
        return map(workflow, List.of());
    }

    @Transactional(readOnly = true)
    public Page<ComplaintDto> list(Authentication auth, ComplaintFilter f, Pageable pageable) {
        String role = currentUser.role(auth);
        Specification<RegisteredComplaint> spec = (root, query, cb) -> {
            query.distinct(true);
            List<jakarta.persistence.criteria.Predicate> ps = new ArrayList<>();
            Join<RegisteredComplaint, Student> registeredStudent = root.join("student", JoinType.INNER);
            Join<RegisteredComplaint, Complaint> complaint = root.join("complaint", JoinType.INNER);
            complaint.join("student", JoinType.INNER);
            if (role.equals("ROLE_STUDENT")) ps.add(cb.equal(registeredStudent.get("studentId"), currentUser.student(auth).getStudentId()));
            else if (role.equals("ROLE_STAFF")) ps.add(cb.equal(root.get("assignedStaff").get("staffId"), currentUser.staff(auth).getStaffId()));
            if (f.academicYearId() != null) ps.add(cb.equal(complaint.join("academicYear").get("academicYearId"), f.academicYearId()));
            if (f.streamId() != null) ps.add(cb.equal(root.join("stream", JoinType.LEFT).get("streamId"), f.streamId()));
            if (f.department() != null && !f.department().isBlank()) ps.add(contains(cb, registeredStudent.get("department"), f.department()));
            if (f.complaintType() != null && !f.complaintType().isBlank()) ps.add(contains(cb, complaint.get("complaintType"), f.complaintType()));
            if (f.status() != null && !f.status().isBlank()) ps.add(cb.equal(root.get("registrationStatus"), f.status().toUpperCase(Locale.ROOT)));
            if (f.verificationStatus() != null && !f.verificationStatus().isBlank()) ps.add(cb.equal(root.get("verificationStatus"), f.verificationStatus().toUpperCase(Locale.ROOT)));
            if (f.priority() != null && !f.priority().isBlank()) ps.add(cb.equal(root.get("priority"), f.priority().toUpperCase(Locale.ROOT)));
            if (f.assignedStaffId() != null) ps.add(cb.equal(root.join("assignedStaff", JoinType.LEFT).get("staffId"), f.assignedStaffId()));
            if (f.rollId() != null && !f.rollId().isBlank()) ps.add(contains(cb, registeredStudent.get("rollId"), f.rollId()));
            if (f.studentName() != null && !f.studentName().isBlank()) ps.add(contains(cb, registeredStudent.get("name"), f.studentName()));
            if (f.from() != null) ps.add(cb.greaterThanOrEqualTo(complaint.get("createdDate"), f.from().atStartOfDay()));
            if (f.to() != null) ps.add(cb.lessThan(complaint.get("createdDate"), f.to().plusDays(1).atStartOfDay()));
            return cb.and(ps.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        List<Integer> activeIds = workflows.findActiveWorkflowIds();
        if (activeIds.isEmpty()) return new PageImpl<>(List.of(), pageable, 0);
        spec = spec.and((root, query, cb) -> root.get("registeredComplaintId").in(activeIds));
        return workflows.findAll(spec, pageable).map(item -> map(item, List.of()));
    }

    @Transactional(readOnly = true)
    public ComplaintDto detail(Integer complaintId, Authentication auth) {
        RegisteredComplaint item = visible(complaintId, auth);
        List<HistoryDto> events = history.findByRegisteredComplaint_RegisteredComplaintIdOrderByCreatedAtAsc(item.getRegisteredComplaintId())
                .stream().map(h -> new HistoryDto(h.getHistoryId(), h.getPreviousStatus(), h.getNewStatus(), h.getAction(),
                        h.getPerformedById(), h.getPerformedByName(), h.getRole(), h.getRemarks(), h.getCreatedAt())).toList();
        return map(item, events);
    }

    @Transactional
    public ComplaintDto edit(Integer complaintId, Authentication auth, String subject, String description,
            MultipartFile file, boolean removeAttachment) {
        RegisteredComplaint item = visible(complaintId, auth);
        if (!"REGISTERED".equals(item.getRegistrationStatus())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "A complaint can only be edited before admin acceptance.");
        validateComplaint(subject, description);
        Complaint complaint = item.getComplaint();
        complaint.setSubject(subject.trim()); complaint.setDescription(description.trim());
        UploadStorageService.StoredFile attachment = uploads.store(file);
        if (attachment != null) {
            complaint.setImageName(attachment.displayName()); complaint.setImagePath(attachment.storedName()); complaint.setImageType(attachment.mimeType());
        } else if (removeAttachment) {
            complaint.setImageName(null); complaint.setImagePath(null); complaint.setImageType(null);
        }
        complaints.save(complaint);
        addHistory(item, item.getRegistrationStatus(), item.getRegistrationStatus(), "Student edited complaint", auth, null);
        return detail(complaintId, auth);
    }

    @Transactional
    public ComplaintDto review(Integer complaintId, Authentication auth, ReviewRequest request) {
        RegisteredComplaint item = visible(complaintId, auth);
        requireStatus(item, "REGISTERED");
        String action = request.action().trim().toUpperCase(Locale.ROOT);
        String previous = item.getRegistrationStatus();
        item.setAdmin(currentUser.admin(auth));
        if (action.equals("ACCEPT")) {
            item.setRegistrationStatus("ACCEPTED"); item.setRejectionReason(null);
        } else if (action.equals("REJECT")) {
            if (request.reason() == null || request.reason().isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provide a reason when rejecting a complaint.");
            item.setRegistrationStatus("REJECTED"); item.setRejectionReason(request.reason().trim());
        } else throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Action must be ACCEPT or REJECT.");
        item = workflows.save(item);
        addHistory(item, previous, item.getRegistrationStatus(), "Admin " + action.toLowerCase(Locale.ROOT) + "ed complaint", auth,
                action.equals("REJECT") ? request.reason().trim() : request.reason());
        return detail(complaintId, auth);
    }

    @Transactional
    public ComplaintDto assign(Integer complaintId, Authentication auth, AssignmentRequest request) {
        RegisteredComplaint item = visible(complaintId, auth);
        if (!Set.of("ACCEPTED", "ASSIGNED").contains(item.getRegistrationStatus()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only accepted complaints can be classified and assigned.");
        String priority = request.priority().trim().toUpperCase(Locale.ROOT);
        if (!Set.of("NORMAL", "LOW", "MEDIUM", "HIGH", "URGENT").contains(priority)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a valid priority.");
        Stream stream = streams.findById(request.streamId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stream not found."));
        Staff assignee = staff.findById(request.staffId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Staff member not found."));
        if (!Objects.equals(assignee.getStreamId(), stream.getStreamId()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a staff member assigned to the selected stream.");
        String previous = item.getRegistrationStatus();
        item.setAdmin(currentUser.admin(auth)); item.setStream(stream); item.setAssignedStaff(assignee); item.setPriority(priority);
        item.setAssignedDate(LocalDateTime.now()); item.setRegistrationStatus("ASSIGNED");
        item = workflows.save(item);
        addHistory(item, previous, "ASSIGNED", "Classified as " + stream.getStreamName() + " and assigned to " + assignee.getName(), auth,
                "Priority: " + priority);
        return detail(complaintId, auth);
    }

    @Transactional
    public ComplaintDto start(Integer complaintId, Authentication auth, String remarks) {
        RegisteredComplaint item = visible(complaintId, auth);
        if (!Set.of("ASSIGNED", "REOPENED").contains(item.getRegistrationStatus()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only assigned or reopened complaints can be started.");
        String previous = item.getRegistrationStatus();
        item.setRegistrationStatus("IN_PROGRESS");
        if (remarks != null && !remarks.isBlank()) item.setStaffRemarks(remarks.trim());
        item = workflows.save(item);
        addHistory(item, previous, "IN_PROGRESS", "Staff started work", auth, remarks);
        return detail(complaintId, auth);
    }

    @Transactional
    public ComplaintDto submitSolution(Integer complaintId, Authentication auth, SolutionRequest request, MultipartFile file) {
        RegisteredComplaint item = visible(complaintId, auth);
        requireStatus(item, "IN_PROGRESS");
        item.setRegistrationStatus("SOLUTION_SUBMITTED"); item.setResolution(request.solution().trim());
        item.setStaffRemarks(request.remarks() == null ? null : request.remarks().trim()); item.setResolvedDate(LocalDateTime.now());
        UploadStorageService.StoredFile proof = uploads.store(file);
        if (proof != null) { item.setSolutionProofName(proof.displayName()); item.setSolutionProofPath(proof.storedName()); }
        item = workflows.save(item);
        addHistory(item, "IN_PROGRESS", "SOLUTION_SUBMITTED", "Staff submitted a solution", auth, request.remarks());
        return detail(complaintId, auth);
    }

    @Transactional
    public ComplaintDto verify(Integer complaintId, Authentication auth, VerificationRequest request) {
        RegisteredComplaint item = visible(complaintId, auth);
        requireStatus(item, "SOLUTION_SUBMITTED");
        String action = request.action().trim().toUpperCase(Locale.ROOT);
        if (!Set.of("SOLVED", "REOPENED").contains(action)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Action must be SOLVED or REOPENED.");
        item.setRegistrationStatus(action); item.setVerificationStatus(action.equals("SOLVED") ? "VERIFIED" : "REOPENED");
        item.setVerificationDate(LocalDateTime.now());
        item = workflows.save(item);
        addHistory(item, "SOLUTION_SUBMITTED", action, action.equals("SOLVED") ? "Admin verified the solution" : "Admin reopened the complaint", auth, request.remarks());
        return detail(complaintId, auth);
    }

    @Transactional(readOnly = true)
    public PathForDownload attachment(Integer complaintId, String kind, Authentication auth) {
        RegisteredComplaint item = visible(complaintId, auth);
        String path, name, mime;
        if (kind.equals("student")) {
            path = item.getComplaint().getImagePath(); name = item.getComplaint().getImageName(); mime = item.getComplaint().getImageType();
        } else if (kind.equals("solution")) {
            path = item.getSolutionProofPath(); name = item.getSolutionProofName(); mime = null;
        } else throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Attachment not found.");
        if (path == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Attachment not found.");
        return new PathForDownload(uploads.resolve(path), name, mime == null ? "application/octet-stream" : mime);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> stats(Authentication auth) {
        String role = currentUser.role(auth);
        Integer userId = role.equals("ROLE_STUDENT") ? currentUser.student(auth).getStudentId()
                : role.equals("ROLE_STAFF") ? currentUser.staff(auth).getStaffId() : null;
        Map<String, Long> result = new LinkedHashMap<>();
        long total = 0;
        for (String status : STATUSES) {
            long count = role.equals("ROLE_ADMIN") ? workflows.countActiveByRegistrationStatus(status)
                    : role.equals("ROLE_STUDENT") ? workflows.countActiveByStudentAndStatus(userId, status)
                    : workflows.countActiveByAssignedStaffAndStatus(userId, status);
            result.put(status, count); total += count;
        }
        result.put("TOTAL", total);
        if (role.equals("ROLE_ADMIN")) result.put("PENDING_REGISTRATIONS", registrations.countByStatus(StudentRegistrationRequest.Status.PENDING));
        return result;
    }

    public RegisteredComplaint visible(Integer complaintId, Authentication auth) {
        RegisteredComplaint item = workflows.findByComplaint_ComplaintId(complaintId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Complaint not found."));
        if (!hasStudentRecords(item))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This legacy complaint is unavailable because its student record no longer exists.");
        String role = currentUser.role(auth);
        if (role.equals("ROLE_STUDENT") && !Objects.equals(item.getStudent().getStudentId(), currentUser.student(auth).getStudentId()))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Complaint not found.");
        if (role.equals("ROLE_STAFF") && (item.getAssignedStaff() == null || !Objects.equals(item.getAssignedStaff().getStaffId(), currentUser.staff(auth).getStaffId())))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Complaint not found.");
        if (!Set.of("ROLE_STUDENT", "ROLE_STAFF", "ROLE_ADMIN").contains(role)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return item;
    }

    private boolean hasStudentRecords(RegisteredComplaint item) {
        if (item.getStudent() == null || item.getComplaint() == null || item.getComplaint().getStudent() == null)
            return false;
        Integer workflowStudentId = item.getStudent().getStudentId();
        Integer complaintStudentId = item.getComplaint().getStudent().getStudentId();
        return workflowStudentId != null && complaintStudentId != null
                && students.existsById(workflowStudentId) && students.existsById(complaintStudentId);
    }

    private void addHistory(RegisteredComplaint item, String previous, String current, String action, Authentication auth, String remarks) {
        ComplaintHistory event = new ComplaintHistory(); event.setRegisteredComplaint(item); event.setPreviousStatus(previous);
        event.setNewStatus(current); event.setAction(action); event.setPerformedById(currentUser.id(auth));
        event.setPerformedByName(currentUser.name(auth)); event.setRole(currentUser.role(auth).substring(5));
        event.setRemarks(remarks); history.save(event);
    }
    private AcademicYear academicYearFor(Student student) {
        Integer academicYearId = student.getAcademicYearId();
        String message = "Your academic year is not linked to your student account. Please contact the college office.";
        if (academicYearId == null) throw new ResponseStatusException(HttpStatus.CONFLICT, message);
        return years.findById(academicYearId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, message));
    }
    private void requireStatus(RegisteredComplaint item, String status) {
        if (!status.equals(item.getRegistrationStatus())) throw new ResponseStatusException(HttpStatus.CONFLICT,
                "This action is not available while the complaint is " + item.getRegistrationStatus().replace('_', ' ').toLowerCase(Locale.ROOT) + ".");
    }
    private void validateComplaint(String subject, String description) {
        if (subject == null || subject.isBlank() || subject.length() > 200) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subject is required and must be at most 200 characters.");
        if (description == null || description.isBlank() || description.length() > 10000) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Description is required and must be at most 10,000 characters.");
    }
    private jakarta.persistence.criteria.Predicate contains(jakarta.persistence.criteria.CriteriaBuilder cb,
            jakarta.persistence.criteria.Expression<String> field, String value) {
        return cb.like(cb.lower(field), "%" + value.trim().toLowerCase(Locale.ROOT).replace("%", "\\%").replace("_", "\\_") + "%", '\\');
    }

    private ComplaintDto map(RegisteredComplaint item, List<HistoryDto> events) {
        Complaint c = item.getComplaint(); Student s = item.getStudent();
        return new ComplaintDto(item.getRegisteredComplaintId(), c.getComplaintId(), c.getComplaintType(), c.getSubject(), c.getDescription(),
                c.getImageName(), s.getRollId(), s.getName(), s.getDepartment(), c.getAcademicYear().getAcademicYearId(),
                c.getAcademicYear().getYearName(), item.getStream() == null ? null : item.getStream().getStreamId(),
                item.getStream() == null ? null : item.getStream().getStreamName(), item.getAssignedStaff() == null ? null : item.getAssignedStaff().getStaffId(),
                item.getAssignedStaff() == null ? null : item.getAssignedStaff().getName(), item.getPriority(), item.getRegistrationStatus(),
                item.getVerificationStatus(), item.getRejectionReason(), item.getStaffRemarks(), item.getResolution(), item.getSolutionProofName(),
                c.getCreatedDate(), item.getRegisteredDate(), item.getAssignedDate(), item.getResolvedDate(), events);
    }

    public record ComplaintFilter(Integer academicYearId, Integer streamId, String department, String complaintType,
            String status, String verificationStatus, String priority, Integer assignedStaffId,
            String rollId, String studentName, LocalDate from, LocalDate to) {}
    public record PathForDownload(java.nio.file.Path path, String displayName, String mimeType) {}
}
