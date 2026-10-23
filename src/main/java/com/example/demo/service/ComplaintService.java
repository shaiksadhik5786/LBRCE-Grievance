package com.example.demo.service;

import java.util.List;

import com.example.demo.entity.Complaint;

public interface ComplaintService {

    Complaint saveComplaint(Complaint complaint);

    List<Complaint> getAllComplaints();

    Complaint getComplaintById(Integer id);

    Complaint updateComplaint(Integer id, Complaint complaint);

    void deleteComplaint(Integer id);
}