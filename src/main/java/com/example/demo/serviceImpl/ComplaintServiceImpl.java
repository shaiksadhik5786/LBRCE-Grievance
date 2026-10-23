package com.example.demo.serviceImpl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.entity.Complaint;
import com.example.demo.repository.ComplaintRepository;
import com.example.demo.service.ComplaintService;

@Service
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintRepository complaintRepository;

    public ComplaintServiceImpl(ComplaintRepository complaintRepository) {
        this.complaintRepository = complaintRepository;
    }

    @Override
    public Complaint saveComplaint(Complaint complaint) {
        return complaintRepository.save(complaint);
    }

    @Override
    public List<Complaint> getAllComplaints() {
        return complaintRepository.findAll();
    }

    @Override
    public Complaint getComplaintById(Integer id) {
        return complaintRepository.findById(id)
                .orElse(null);
    }

    @Override
    public Complaint updateComplaint(Integer id, Complaint complaint) {

        Complaint existing = complaintRepository.findById(id)
                .orElse(null);

        if (existing != null) {

            existing.setAcademicYear(complaint.getAcademicYear());
            existing.setStudent(complaint.getStudent());
            existing.setComplaintType(complaint.getComplaintType());
            existing.setSubject(complaint.getSubject());
            existing.setDescription(complaint.getDescription());
            existing.setImageName(complaint.getImageName());
            existing.setImagePath(complaint.getImagePath());
            existing.setImageType(complaint.getImageType());

            return complaintRepository.save(existing);
        }

        return null;
    }

    @Override
    public void deleteComplaint(Integer id) {
        complaintRepository.deleteById(id);
    }
}