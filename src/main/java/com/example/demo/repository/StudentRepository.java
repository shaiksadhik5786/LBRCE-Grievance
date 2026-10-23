package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Student;

public interface StudentRepository extends JpaRepository<Student, Integer> {

    Optional<Student> findByRollId(String rollId);

    boolean existsByRollId(String rollId);

    boolean existsByEmail(String email);


    boolean existsByAcademicYearId(Integer academicYearId);

    @org.springframework.data.jpa.repository.Query("select distinct s.department from Student s where s.department is not null order by s.department")
    java.util.List<String> findDistinctDepartments();
}
