package com.example.demo.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import com.example.demo.dto.request.StudentRequest;
import com.example.demo.dto.response.StudentResponse;
import com.example.demo.entity.Student;
import com.example.demo.service.StudentService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    // CREATE
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentResponse> saveStudent(
            @Valid @RequestBody StudentRequest request) {

        Student student = convertToEntity(request);

        Student savedStudent = studentService.saveStudent(student);

        return ResponseEntity.ok(convertToResponse(savedStudent));
    }

    // GET BY ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentResponse> getStudentById(
            @PathVariable("id") Integer id) {

        Student student = studentService.getStudentById(id);

        return ResponseEntity.ok(convertToResponse(student));
    }

    // GET BY ROLL ID
    @GetMapping("/roll/{rollId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentResponse> getStudentByRollId(
            @PathVariable("rollId") String rollId) {

        Student student = studentService.getStudentByRollId(rollId);

        return ResponseEntity.ok(convertToResponse(student));
    }

    // LOGIN
    @PostMapping("/login")
    @PreAuthorize("denyAll()")
    public ResponseEntity<StudentResponse> login(
            @RequestParam("rollId") String rollId,
            @RequestParam("password") String password) {

        Student student = studentService.login(rollId, password);

        return ResponseEntity.ok(convertToResponse(student));
    }

    // GET ALL
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StudentResponse>> getAllStudents() {

        List<StudentResponse> students = studentService.getAllStudents()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(students);
    }

    // UPDATE
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentResponse> updateStudent(
            @PathVariable("id") Integer id,
            @Valid @RequestBody StudentRequest request) {

        Student student = convertToEntity(request);

        Student updatedStudent =
                studentService.updateStudent(id, student);

        return ResponseEntity.ok(convertToResponse(updatedStudent));
    }

    // DELETE
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteStudent(
            @PathVariable("id") Integer id) {

        studentService.deleteStudent(id);

        return ResponseEntity.ok("Student deleted successfully");
    }

    // REQUEST DTO -> ENTITY
    private Student convertToEntity(StudentRequest request) {

        Student student = new Student();

        student.setRollId(request.getRollId());
        student.setAcademicYearId(request.getAcademicYearId());
        student.setName(request.getName());
        student.setDepartment(request.getDepartment());
        student.setEmail(request.getEmail());
        student.setPhone(request.getPhone());
        student.setPassword(request.getPassword());

        return student;
    }

    // ENTITY -> RESPONSE DTO
    private StudentResponse convertToResponse(Student student) {

        StudentResponse response = new StudentResponse();

        response.setStudentId(student.getStudentId());
        response.setRollId(student.getRollId());
        response.setAcademicYearId(student.getAcademicYearId());
        response.setName(student.getName());
        response.setDepartment(student.getDepartment());
        response.setEmail(student.getEmail());
        response.setPhone(student.getPhone());

        return response;
    }
}
