package com.example.demo.serviceImpl;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.example.demo.entity.AcademicYear;
import com.example.demo.repository.AcademicYearRepository;
import com.example.demo.repository.ComplaintRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.service.AcademicYearService;

@Service
public class AcademicYearServiceImpl implements AcademicYearService {
    private final AcademicYearRepository years;
    private final StudentRepository students;
    private final ComplaintRepository complaints;
    public AcademicYearServiceImpl(AcademicYearRepository years, StudentRepository students, ComplaintRepository complaints) {
        this.years = years; this.students = students; this.complaints = complaints;
    }
    @Override @Transactional public AcademicYear saveAcademicYear(AcademicYear year) {
        validate(year); normalize(year); if ("Y".equals(year.getIsActive())) deactivateOthers(null);
        return years.save(year);
    }
    @Override public List<AcademicYear> getAllAcademicYears() { return years.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "startDate")); }
    @Override public AcademicYear getAcademicYearById(Integer id) { return years.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Academic year not found.")); }
    @Override @Transactional public AcademicYear updateAcademicYear(Integer id, AcademicYear changes) {
        AcademicYear existing = getAcademicYearById(id); validate(changes); normalize(changes);
        if ("Y".equals(changes.getIsActive())) deactivateOthers(id);
        existing.setYearName(changes.getYearName()); existing.setStartDate(changes.getStartDate());
        existing.setEndDate(changes.getEndDate()); existing.setIsActive(changes.getIsActive()); return years.save(existing);
    }
    @Override @Transactional public void deleteAcademicYear(Integer id) {
        AcademicYear year = getAcademicYearById(id);
        if (students.existsByAcademicYearId(id) || complaints.existsByAcademicYear_AcademicYearId(id))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Academic years with student or complaint history cannot be deleted. Deactivate the year instead.");
        years.delete(year);
    }
    private void validate(AcademicYear year) {
        if (year.getYearName() == null || year.getYearName().isBlank() || year.getYearName().length() > 20)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter an academic year name of at most 20 characters.");
        if (year.getStartDate() == null || year.getEndDate() == null || !year.getStartDate().isBefore(year.getEndDate()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Academic year end date must be after its start date.");
    }
    private void normalize(AcademicYear year) {
        String active = year.getIsActive() == null ? "N" : year.getIsActive().trim().toUpperCase();
        if (!active.equals("Y") && !active.equals("N")) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Active must be Y or N.");
        year.setIsActive(active); year.setYearName(year.getYearName().trim());
    }
    private void deactivateOthers(Integer exceptId) {
        for (AcademicYear other : years.findByIsActiveIgnoreCase("Y")) if (!other.getAcademicYearId().equals(exceptId)) {
            other.setIsActive("N"); years.save(other);
        }
    }
}
