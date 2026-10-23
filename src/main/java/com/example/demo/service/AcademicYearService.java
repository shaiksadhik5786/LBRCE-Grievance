package com.example.demo.service;

import java.util.List;

import com.example.demo.entity.AcademicYear;

public interface AcademicYearService {

    AcademicYear saveAcademicYear(AcademicYear academicYear);

    List<AcademicYear> getAllAcademicYears();

    AcademicYear getAcademicYearById(Integer id);

    AcademicYear updateAcademicYear(Integer id, AcademicYear academicYear);

    void deleteAcademicYear(Integer id);
}