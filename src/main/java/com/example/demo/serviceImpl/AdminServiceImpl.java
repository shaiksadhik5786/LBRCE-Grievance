package com.example.demo.serviceImpl;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import com.example.demo.entity.Admin;
import com.example.demo.repository.AdminRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.StaffRepository;
import com.example.demo.service.AdminService;
import com.example.demo.securityConfig.PasswordPolicy;
import com.example.demo.service.OtpService;
import com.example.demo.service.OtpPurpose;
import java.util.Locale;

@Service
public class AdminServiceImpl implements AdminService {
    private final AdminRepository admins;
    private final StudentRepository students;
    private final StaffRepository staff;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    public AdminServiceImpl(AdminRepository admins, StudentRepository students, StaffRepository staff,
            PasswordEncoder passwordEncoder, OtpService otpService) { this.admins = admins; this.students = students; this.staff = staff; this.passwordEncoder = passwordEncoder; this.otpService = otpService; }
    @Override public Admin saveAdmin(Admin admin, String emailVerificationToken) {
        admin.setEmail(admin.getEmail().trim().toLowerCase(Locale.ROOT));
        if (admins.existsByRollId(admin.getRollId())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Roll ID already exists.");
        if (students.existsByRollId(admin.getRollId()) || staff.existsByRollId(admin.getRollId())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Roll ID is already assigned to another account type.");
        if (admins.existsByEmail(admin.getEmail())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists.");
        if (students.existsByEmail(admin.getEmail()) || staff.existsByEmail(admin.getEmail())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already assigned to another account type.");
        PasswordPolicy.validate(admin.getPassword());
        otpService.requireAndConsumeVerification(admin.getEmail(), OtpPurpose.ADMIN_CREATION,
                emailVerificationToken);
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        return admins.save(admin);
    }
    @Override public Admin getAdminById(Integer id) { return admins.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found.")); }
    @Override public Admin getAdminByRollId(String id) { return admins.findByRollId(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found.")); }
    @Override public Admin login(String rollId, String password) {
        Admin admin = admins.findByRollId(rollId).orElse(null);
        if (admin != null && passwordEncoder.matches(password, admin.getPassword())) return admin;
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid roll ID or password.");
    }
    @Override public List<Admin> getAllAdmins() { return admins.findAll(); }
    @Override public Admin updateAdmin(Integer id, Admin changes, String emailVerificationToken) {
        Admin admin = getAdminById(id);
        changes.setEmail(changes.getEmail().trim().toLowerCase(Locale.ROOT));
        boolean emailChanged = !admin.getEmail().equalsIgnoreCase(changes.getEmail());
        if (!admin.getRollId().equals(changes.getRollId()) && admins.existsByRollId(changes.getRollId())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Roll ID already exists.");
        if (!admin.getRollId().equals(changes.getRollId()) && (students.existsByRollId(changes.getRollId()) || staff.existsByRollId(changes.getRollId()))) throw new ResponseStatusException(HttpStatus.CONFLICT, "Roll ID is already assigned to another account type.");
        if (!admin.getEmail().equals(changes.getEmail()) && admins.existsByEmail(changes.getEmail())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists.");
        if (!admin.getEmail().equals(changes.getEmail()) && (students.existsByEmail(changes.getEmail()) || staff.existsByEmail(changes.getEmail()))) throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already assigned to another account type.");
        if (emailChanged) otpService.requireAndConsumeVerification(changes.getEmail(), OtpPurpose.ADMIN_CREATION,
                emailVerificationToken);
        admin.setRollId(changes.getRollId()); admin.setName(changes.getName()); admin.setEmail(changes.getEmail());
        admin.setPhone(changes.getPhone()); admin.setDesignation(changes.getDesignation());
        if (changes.getPassword() != null && !changes.getPassword().isBlank()) {
            PasswordPolicy.validate(changes.getPassword());
            admin.setPassword(passwordEncoder.encode(changes.getPassword()));
        }
        return admins.save(admin);
    }
    @Override public void deleteAdmin(Integer id) {
        if (admins.count() < 2) throw new ResponseStatusException(HttpStatus.CONFLICT, "The last administrator cannot be deleted.");
        if (!admins.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found.");
        try { admins.deleteById(id); }
        catch (org.springframework.dao.DataIntegrityViolationException ex) { throw new ResponseStatusException(HttpStatus.CONFLICT, "Administrator with complaint history cannot be deleted."); }
    }
}
