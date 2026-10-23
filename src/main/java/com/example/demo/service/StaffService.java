package com.example.demo.service;

import java.util.List;

import com.example.demo.entity.Staff;

public interface StaffService {

    Staff saveStaff(Staff staff, String emailVerificationToken);

    Staff getStaffById(Integer staffId);

    Staff getStaffByRollId(String rollId);

    Staff login(String rollId, String password);

    List<Staff> getAllStaff();

    List<Staff> getStaffByStream(Integer streamId);

    Staff updateStaff(Integer staffId, Staff staff, String emailVerificationToken);

    void deleteStaff(Integer staffId);
}
