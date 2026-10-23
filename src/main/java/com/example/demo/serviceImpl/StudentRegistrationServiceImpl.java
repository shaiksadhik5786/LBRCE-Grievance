package com.example.demo.serviceImpl;

import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.example.demo.dto.request.StudentRegistrationRequestDto;
import com.example.demo.dto.response.StudentRegistrationResponse;
import com.example.demo.entity.AcademicYear;
import com.example.demo.entity.Student;
import com.example.demo.entity.StudentRegistrationRequest;
import com.example.demo.entity.StudentRegistrationRequest.Status;
import com.example.demo.repository.AcademicYearRepository;
import com.example.demo.repository.StudentRegistrationRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.StaffRepository;
import com.example.demo.repository.AdminRepository;
import com.example.demo.service.StudentRegistrationService;
import com.example.demo.service.OtpService;
import com.example.demo.service.OtpPurpose;
import com.example.demo.securityConfig.PasswordPolicy;
import com.example.demo.validation.DepartmentPolicy;

@Service
public class StudentRegistrationServiceImpl implements StudentRegistrationService {
    private final StudentRegistrationRepository requests;
    private final StudentRepository students;
    private final AcademicYearRepository years;
    private final StaffRepository staff;
    private final AdminRepository admins;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;

    public StudentRegistrationServiceImpl(StudentRegistrationRepository requests, StudentRepository students,
            AcademicYearRepository years, StaffRepository staff, AdminRepository admins,
            PasswordEncoder passwordEncoder, OtpService otpService) {
        this.requests = requests;
        this.students = students;
        this.years = years;
        this.staff = staff;
        this.admins = admins;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
    }

    @Override @Transactional
    public StudentRegistrationResponse submit(StudentRegistrationRequestDto dto) {
        String rollId = dto.getRollId().trim();
        String email = dto.getEmail().trim().toLowerCase(Locale.ROOT);
        PasswordPolicy.validate(dto.getPassword());
        String department = DepartmentPolicy.validate(dto.getDepartment());
        if (!dto.getPassword().equals(dto.getConfirmPassword()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password and confirm password must match");
        AcademicYear year = years.findById(dto.getAcademicYearId()).orElse(null);
        if (year == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Academic year was not found");
        if (!"Y".equalsIgnoreCase(year.getIsActive())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose the active academic year.");
        if (students.existsByRollId(rollId) || requests.existsByRollId(rollId))
            throw duplicate("Student with roll ID " + rollId + " already exists.");
        if (staff.existsByRollId(rollId) || admins.existsByRollId(rollId))
            throw duplicate("Roll ID is already assigned to another account type.");
        if (students.existsByEmail(email) || staff.existsByEmail(email) || admins.existsByEmail(email) || requests.existsByEmail(email))
            throw duplicate("Email already exists.");

        otpService.requireAndConsumeVerification(email, OtpPurpose.STUDENT_REGISTRATION,
                dto.getEmailVerificationToken());

        StudentRegistrationRequest entity = new StudentRegistrationRequest();
        entity.setRollId(rollId);
        entity.setAcademicYearId(dto.getAcademicYearId());
        entity.setName(dto.getName().trim());
        entity.setDepartment(department);
        entity.setEmail(email);
        entity.setPhone(dto.getPhone() == null || dto.getPhone().isBlank() ? null : dto.getPhone().trim());
        entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        return response(requests.save(entity));
    }

    private ResponseStatusException duplicate(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }

    @Override @Transactional(readOnly = true)
    public List<StudentRegistrationResponse> list(Status status, Integer academicYearId, String department,
            String rollId, String name) {
        return (status == null ? requests.findAll() : requests.findByStatus(status)).stream()
                .filter(r -> academicYearId == null || academicYearId.equals(r.getAcademicYearId()))
                .filter(r -> containsIgnoreCase(r.getDepartment(), department))
                .filter(r -> containsIgnoreCase(r.getRollId(), rollId))
                .filter(r -> containsIgnoreCase(r.getName(), name))
                .map(this::response).toList();
    }

    private boolean containsIgnoreCase(String value, String query) {
        return query == null || query.isBlank() || value.toLowerCase(Locale.ROOT).contains(query.trim().toLowerCase(Locale.ROOT));
    }

    @Override @Transactional(readOnly = true)
    public StudentRegistrationResponse get(Integer id) { return response(find(id)); }

    @Override @Transactional
    public StudentRegistrationResponse approve(Integer id) {
        StudentRegistrationRequest request = find(id);
        ensurePending(request);
        if (students.existsByRollId(request.getRollId()) || students.existsByEmail(request.getEmail()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A student account already exists for these details.");
        Student student = new Student();
        student.setRollId(request.getRollId());
        student.setAcademicYearId(request.getAcademicYearId());
        student.setName(request.getName());
        student.setDepartment(DepartmentPolicy.validate(request.getDepartment()));
        student.setEmail(request.getEmail());
        student.setPhone(request.getPhone());
        student.setPassword(request.getPassword());
        students.save(student);
        request.setStatus(Status.APPROVED);
        request.setRejectionReason(null);
        return response(requests.save(request));
    }

    @Override @Transactional
    public StudentRegistrationResponse reject(Integer id, String reason) {
        StudentRegistrationRequest request = find(id);
        ensurePending(request);
        request.setStatus(Status.REJECTED);
        request.setRejectionReason(reason.trim());
        return response(requests.save(request));
    }

    private void ensurePending(StudentRegistrationRequest request) {
        if (request.getStatus() != Status.PENDING)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only pending registrations can be reviewed.");
    }

    private StudentRegistrationRequest find(Integer id) {
        return requests.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registration request not found"));
    }

    private StudentRegistrationResponse response(StudentRegistrationRequest r) {
        StudentRegistrationResponse out = new StudentRegistrationResponse();
        out.setId(r.getId()); out.setRollId(r.getRollId());
        out.setName(r.getName()); out.setDepartment(r.getDepartment()); out.setAcademicYearId(r.getAcademicYearId());
        out.setEmail(r.getEmail()); out.setPhone(r.getPhone()); out.setSubmittedAt(r.getSubmittedAt());
        out.setStatus(r.getStatus()); out.setRejectionReason(r.getRejectionReason());
        return out;
    }
}
