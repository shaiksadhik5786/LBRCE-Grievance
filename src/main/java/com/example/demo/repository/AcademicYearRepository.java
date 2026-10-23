package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.AcademicYear;

public interface AcademicYearRepository extends JpaRepository<AcademicYear, Integer> {
    java.util.List<AcademicYear> findByIsActiveIgnoreCase(String isActive);
}
