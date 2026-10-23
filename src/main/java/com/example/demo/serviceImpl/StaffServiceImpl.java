package com.example.demo.serviceImpl;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import com.example.demo.entity.Staff;
import com.example.demo.repository.StaffRepository;
import com.example.demo.repository.StreamRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.AdminRepository;
import com.example.demo.service.StaffService;
import com.example.demo.securityConfig.PasswordPolicy;
import com.example.demo.service.OtpService;
import com.example.demo.service.OtpPurpose;
import com.example.demo.validation.DepartmentPolicy;
import java.util.Locale;

@Service
public class StaffServiceImpl implements StaffService {
    private final StaffRepository staffRepository;
    private final StreamRepository streams;
    private final StudentRepository students;
    private final AdminRepository admins;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;

    public StaffServiceImpl(StaffRepository staffRepository, StreamRepository streams, StudentRepository students,
            AdminRepository admins, PasswordEncoder passwordEncoder, OtpService otpService) {
        this.staffRepository = staffRepository; this.streams = streams; this.students = students; this.admins = admins;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
    }
    @Override public Staff saveStaff(Staff staff, String emailVerificationToken) {
        staff.setEmail(staff.getEmail().trim().toLowerCase(Locale.ROOT));
        staff.setDepartment(DepartmentPolicy.validate(staff.getDepartment()));
        if (staffRepository.existsByRollId(staff.getRollId())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Roll ID already exists.");
        if (students.existsByRollId(staff.getRollId()) || admins.existsByRollId(staff.getRollId())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Roll ID is already assigned to another account type.");
        if (staffRepository.existsByEmail(staff.getEmail())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists.");
        if (students.existsByEmail(staff.getEmail()) || admins.existsByEmail(staff.getEmail())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already assigned to another account type.");
        if (!streams.existsById(staff.getStreamId())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected stream was not found.");
        PasswordPolicy.validate(staff.getPassword());
        otpService.requireAndConsumeVerification(staff.getEmail(), OtpPurpose.STAFF_CREATION,
                emailVerificationToken);
        staff.setPassword(passwordEncoder.encode(staff.getPassword()));
        return staffRepository.save(staff);
    }
    @Override public Staff getStaffById(Integer id) {
        return staffRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Staff member not found."));
    }
    @Override public Staff getStaffByRollId(String rollId) {
        return staffRepository.findByRollId(rollId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Staff member not found."));
    }
    @Override public Staff login(String rollId, String password) {
        Staff user = staffRepository.findByRollId(rollId).orElse(null);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) return user;
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid roll ID or password.");
    }
    @Override public List<Staff> getAllStaff() { return staffRepository.findAll(); }
    @Override public List<Staff> getStaffByStream(Integer streamId) { return staffRepository.findByStreamId(streamId); }
    @Override public Staff updateStaff(Integer id, Staff changes, String emailVerificationToken) {
        Staff staff = getStaffById(id);
        changes.setEmail(changes.getEmail().trim().toLowerCase(Locale.ROOT));
        changes.setDepartment(DepartmentPolicy.validate(changes.getDepartment()));
        boolean emailChanged = !staff.getEmail().equalsIgnoreCase(changes.getEmail());
        if (!staff.getRollId().equals(changes.getRollId()) && staffRepository.existsByRollId(changes.getRollId()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Roll ID already exists.");
        if (!staff.getRollId().equals(changes.getRollId()) && (students.existsByRollId(changes.getRollId()) || admins.existsByRollId(changes.getRollId())))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Roll ID is already assigned to another account type.");
        if (!staff.getEmail().equals(changes.getEmail()) && staffRepository.existsByEmail(changes.getEmail()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists.");
        if (!staff.getEmail().equals(changes.getEmail()) && (students.existsByEmail(changes.getEmail()) || admins.existsByEmail(changes.getEmail())))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already assigned to another account type.");
        if (!streams.existsById(changes.getStreamId())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected stream was not found.");
        if (emailChanged) otpService.requireAndConsumeVerification(changes.getEmail(), OtpPurpose.STAFF_CREATION,
                emailVerificationToken);
        staff.setRollId(changes.getRollId()); staff.setName(changes.getName()); staff.setEmail(changes.getEmail());
        staff.setPhone(changes.getPhone()); staff.setDesignation(changes.getDesignation()); staff.setDepartment(changes.getDepartment());
        staff.setStreamId(changes.getStreamId());
        if (changes.getPassword() != null && !changes.getPassword().isBlank()) {
            PasswordPolicy.validate(changes.getPassword());
            staff.setPassword(passwordEncoder.encode(changes.getPassword()));
        }
        return staffRepository.save(staff);
    }
    @Override public void deleteStaff(Integer id) {
        if (!staffRepository.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Staff member not found.");
        try { staffRepository.deleteById(id); }
        catch (org.springframework.dao.DataIntegrityViolationException ex) { throw new ResponseStatusException(HttpStatus.CONFLICT, "Staff with complaint history cannot be deleted."); }
    }
}
