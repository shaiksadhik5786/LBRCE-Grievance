package com.example.demo.service;

import java.util.List;

import com.example.demo.entity.Student;

public interface StudentService {

    Student saveStudent(Student student);

    Student getStudentById(Integer studentId);

    Student getStudentByRollId(String rollId);

    Student login(String rollId, String password);

    List<Student> getAllStudents();

    Student updateStudent(Integer studentId, Student student);

    void deleteStudent(Integer studentId);
}