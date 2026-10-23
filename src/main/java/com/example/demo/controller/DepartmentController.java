package com.example.demo.controller;

import java.util.List;
import java.util.TreeSet;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.demo.repository.StaffRepository;
import com.example.demo.repository.StudentRepository;

@RestController
@RequestMapping("/api/departments")
@PreAuthorize("hasRole('ADMIN')")
public class DepartmentController {
    private final StudentRepository students;
    private final StaffRepository staff;
    public DepartmentController(StudentRepository students, StaffRepository staff) { this.students = students; this.staff = staff; }
    @GetMapping public List<String> list() {
        TreeSet<String> departments = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        departments.addAll(students.findDistinctDepartments());
        staff.findAll().stream().map(s -> s.getDepartment()).filter(d -> d != null && !d.isBlank()).forEach(departments::add);
        return List.copyOf(departments);
    }
}
