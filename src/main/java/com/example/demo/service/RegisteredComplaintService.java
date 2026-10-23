package com.example.demo.service;

import java.util.List;

import com.example.demo.entity.RegisteredComplaint;

public interface RegisteredComplaintService {

    RegisteredComplaint saveRegisteredComplaint(RegisteredComplaint registeredComplaint);

    List<RegisteredComplaint> getAllRegisteredComplaints();

    RegisteredComplaint getRegisteredComplaintById(Integer id);

    RegisteredComplaint updateRegisteredComplaint(
            Integer id,
            RegisteredComplaint registeredComplaint);

    void deleteRegisteredComplaint(Integer id);
}