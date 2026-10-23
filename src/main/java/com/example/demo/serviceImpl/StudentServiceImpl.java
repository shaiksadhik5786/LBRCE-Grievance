package com.example.demo.serviceImpl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.entity.Student;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.StudentRegistrationRepository;
import com.example.demo.repository.StaffRepository;
import com.example.demo.repository.AdminRepository;
import com.example.demo.repository.AcademicYearRepository;
import com.example.demo.entity.StudentRegistrationRequest.Status;
import com.example.demo.service.StudentService;
import com.example.demo.securityConfig.PasswordPolicy;
import com.example.demo.validation.DepartmentPolicy;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final StudentRegistrationRepository registrations;
    private final StaffRepository staffRepository;
    private final AdminRepository adminRepository;
    private final AcademicYearRepository academicYears;
    private final PasswordEncoder passwordEncoder;

    public StudentServiceImpl(StudentRepository studentRepository, StudentRegistrationRepository registrations,
            StaffRepository staffRepository, AdminRepository adminRepository,
            AcademicYearRepository academicYears, PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.registrations = registrations;
        this.staffRepository = staffRepository;
        this.adminRepository = adminRepository;
        this.academicYears = academicYears;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Student saveStudent(Student student) {
        student.setDepartment(DepartmentPolicy.validate(student.getDepartment()));
        if (studentRepository.existsByRollId(student.getRollId()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Student with roll ID " + student.getRollId() + " already exists.");
        if (staffRepository.existsByRollId(student.getRollId()) || adminRepository.existsByRollId(student.getRollId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Roll ID is already assigned to another account type.");
        }
        if (studentRepository.existsByEmail(student.getEmail()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists.");
        if (staffRepository.existsByEmail(student.getEmail()) || adminRepository.existsByEmail(student.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already assigned to another account type.");
        }
        if (!academicYears.existsById(student.getAcademicYearId()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Academic year was not found.");
        PasswordPolicy.validate(student.getPassword());
        student.setPassword(passwordEncoder.encode(student.getPassword()));
        return studentRepository.save(student);
    }

    @Override
    public Student getStudentById(Integer studentId) {

        return studentRepository.findById(studentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found."));
    }

    @Override
    public Student getStudentByRollId(String rollId) {

        return studentRepository.findByRollId(rollId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found."));
    }

    @Override
    public Student login(String rollId, String password) {
        Student student = studentRepository.findByRollId(rollId).orElse(null);
        if (student != null && passwordEncoder.matches(password, student.getPassword())) return student;
        var pending = registrations.findFirstByRollIdAndStatusOrderBySubmittedAtDesc(rollId, Status.PENDING).orElse(null);
        if (pending != null && passwordEncoder.matches(password, pending.getPassword()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Your student registration is awaiting admin approval.");
        var rejected = registrations.findFirstByRollIdAndStatusOrderBySubmittedAtDesc(rollId, Status.REJECTED).orElse(null);
        if (rejected != null && passwordEncoder.matches(password, rejected.getPassword()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Your student registration was rejected.");
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid roll ID or password");
    }

    @Override
    public List<Student> getAllStudents() {

        return studentRepository.findAll();
    }

    @Override
    public Student updateStudent(Integer studentId, Student student) {

        Student existingStudent = getStudentById(studentId);
        student.setDepartment(DepartmentPolicy.validate(student.getDepartment()));

        existingStudent.setRollId(student.getRollId());
        existingStudent.setAcademicYearId(student.getAcademicYearId());
        existingStudent.setName(student.getName());
        existingStudent.setDepartment(student.getDepartment());
        existingStudent.setEmail(student.getEmail());
        existingStudent.setPhone(student.getPhone());
        if (student.getPassword() != null && !student.getPassword().isBlank()) {
            PasswordPolicy.validate(student.getPassword());
            existingStudent.setPassword(passwordEncoder.encode(student.getPassword()));
        }

        return studentRepository.save(existingStudent);
    }

    @Override
    public void deleteStudent(Integer studentId) {

        if (!studentRepository.existsById(studentId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found.");
        }

        studentRepository.deleteById(studentId);
    }
}
