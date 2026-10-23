package com.example.demo.serviceImpl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.entity.RegisteredComplaint;
import com.example.demo.repository.RegisteredComplaintRepository;
import com.example.demo.service.RegisteredComplaintService;

@Service
public class RegisteredComplaintServiceImpl implements RegisteredComplaintService {

    private final RegisteredComplaintRepository registeredComplaintRepository;

    public RegisteredComplaintServiceImpl(
            RegisteredComplaintRepository registeredComplaintRepository) {
        this.registeredComplaintRepository = registeredComplaintRepository;
    }

    @Override
    public RegisteredComplaint saveRegisteredComplaint(
            RegisteredComplaint registeredComplaint) {

        return registeredComplaintRepository.save(registeredComplaint);
    }

    @Override
    public List<RegisteredComplaint> getAllRegisteredComplaints() {

        return registeredComplaintRepository.findAll();
    }

    @Override
    public RegisteredComplaint getRegisteredComplaintById(Integer id) {

        return registeredComplaintRepository.findById(id)
                .orElse(null);
    }

    @Override
    public RegisteredComplaint updateRegisteredComplaint(
            Integer id,
            RegisteredComplaint registeredComplaint) {

        RegisteredComplaint existing =
                registeredComplaintRepository.findById(id)
                .orElse(null);

        if (existing != null) {

            existing.setComplaint(registeredComplaint.getComplaint());
            existing.setStudent(registeredComplaint.getStudent());
            existing.setAdmin(registeredComplaint.getAdmin());
            existing.setStream(registeredComplaint.getStream());
            existing.setAssignedStaff(registeredComplaint.getAssignedStaff());

            existing.setAssignedDate(registeredComplaint.getAssignedDate());
            existing.setPriority(registeredComplaint.getPriority());
            existing.setRegistrationStatus(
                    registeredComplaint.getRegistrationStatus());
            existing.setStaffRemarks(registeredComplaint.getStaffRemarks());
            existing.setResolution(registeredComplaint.getResolution());
            existing.setResolvedDate(registeredComplaint.getResolvedDate());
            existing.setVerificationStatus(
                    registeredComplaint.getVerificationStatus());
            existing.setVerificationDate(
                    registeredComplaint.getVerificationDate());

            return registeredComplaintRepository.save(existing);
        }

        return null;
    }

    @Override
    public void deleteRegisteredComplaint(Integer id) {

        registeredComplaintRepository.deleteById(id);
    }
}