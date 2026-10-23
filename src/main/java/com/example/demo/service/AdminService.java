package com.example.demo.service;

import java.util.List;

import com.example.demo.entity.Admin;

public interface AdminService {

    Admin saveAdmin(Admin admin, String emailVerificationToken);

    Admin getAdminById(Integer adminId);

    Admin getAdminByRollId(String rollId);

    Admin login(String rollId, String password);

    List<Admin> getAllAdmins();

    Admin updateAdmin(Integer adminId, Admin admin, String emailVerificationToken);

    void deleteAdmin(Integer adminId);
}
