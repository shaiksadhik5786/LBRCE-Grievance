package com.example.demo.serviceImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.request.StudentRegistrationRequestDto;
import com.example.demo.entity.AcademicYear;
import com.example.demo.entity.Admin;
import com.example.demo.entity.Staff;
import com.example.demo.entity.Student;
import com.example.demo.entity.StudentRegistrationRequest;
import com.example.demo.repository.AcademicYearRepository;
import com.example.demo.repository.AdminRepository;
import com.example.demo.repository.StaffRepository;
import com.example.demo.repository.StreamRepository;
import com.example.demo.repository.StudentRegistrationRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.service.OtpService;

class PasswordStorageFlowTest {
    private static final String INITIAL_PASSWORD = "InitialPassword9";
    private static final String UPDATED_PASSWORD = "ReplacementPass8";
    private PasswordEncoder encoder;

    @BeforeEach
    void setUp() {
        encoder = new BCryptPasswordEncoder();
    }

    @Test
    void adminStaffAndStudentCreateAndUpdateEncodePasswordsBeforeSaving() {
        AdminRepository admins = mock(AdminRepository.class);
        StudentRepository students = mock(StudentRepository.class);
        StaffRepository staff = mock(StaffRepository.class);
        AcademicYearRepository years = mock(AcademicYearRepository.class);
        StudentRegistrationRepository registrations = mock(StudentRegistrationRepository.class);
        StreamRepository streams = mock(StreamRepository.class);
        when(admins.save(any(Admin.class))).thenAnswer(call -> call.getArgument(0));
        when(staff.save(any(Staff.class))).thenAnswer(call -> call.getArgument(0));
        when(students.save(any(Student.class))).thenAnswer(call -> call.getArgument(0));
        when(years.existsById(3)).thenReturn(true);
        when(streams.existsById(5)).thenReturn(true);

        OtpService otpService = mock(OtpService.class);
        AdminServiceImpl adminService = new AdminServiceImpl(admins, students, staff, encoder, otpService);
        StaffServiceImpl staffService = new StaffServiceImpl(staff, streams, students, admins, encoder, otpService);
        StudentServiceImpl studentService = new StudentServiceImpl(students, registrations, staff, admins, years, encoder);

        Admin admin = admin("ADM009", "admin@college.local", INITIAL_PASSWORD);
        assertStoredHash(adminService.saveAdmin(admin, "token").getPassword(), INITIAL_PASSWORD);
        admin.setAdminId(9);
        when(admins.findById(9)).thenReturn(java.util.Optional.of(admin));
        assertStoredHash(adminService.updateAdmin(9, admin("ADM009", "admin@college.local", UPDATED_PASSWORD), null).getPassword(), UPDATED_PASSWORD);

        Staff staffAccount = staff("STF009", "staff@college.local", INITIAL_PASSWORD);
        assertStoredHash(staffService.saveStaff(staffAccount, "token").getPassword(), INITIAL_PASSWORD);
        staffAccount.setStaffId(9);
        when(staff.findById(9)).thenReturn(java.util.Optional.of(staffAccount));
        assertStoredHash(staffService.updateStaff(9, staff("STF009", "staff@college.local", UPDATED_PASSWORD), null).getPassword(), UPDATED_PASSWORD);

        Student student = student("STU009", "student@college.local", INITIAL_PASSWORD);
        assertStoredHash(studentService.saveStudent(student).getPassword(), INITIAL_PASSWORD);
        student.setStudentId(9);
        when(students.findById(9)).thenReturn(java.util.Optional.of(student));
        assertStoredHash(studentService.updateStudent(9, student("STU009", "student@college.local", UPDATED_PASSWORD)).getPassword(), UPDATED_PASSWORD);
    }

    @Test
    void studentRegistrationStoresOneHashAndApprovalCopiesTheHash() {
        StudentRegistrationRepository registrations = mock(StudentRegistrationRepository.class);
        StudentRepository students = mock(StudentRepository.class);
        AcademicYearRepository years = mock(AcademicYearRepository.class);
        StaffRepository staff = mock(StaffRepository.class);
        AdminRepository admins = mock(AdminRepository.class);
        AcademicYear year = new AcademicYear();
        year.setIsActive("Y");
        when(years.findById(2)).thenReturn(java.util.Optional.of(year));
        final Student[] approvedStudent = new Student[1];
        when(students.save(any(Student.class))).thenAnswer(call -> {
            approvedStudent[0] = call.getArgument(0);
            return approvedStudent[0];
        });

        StudentRegistrationRequestDto request = new StudentRegistrationRequestDto();
        request.setRollId("STU012");
        request.setAcademicYearId(2);
        request.setName("Student");
        request.setDepartment("Computer Science and Engineering");
        request.setEmail("student12@college.local");
        request.setPhone("9876543210");
        request.setPassword(INITIAL_PASSWORD);
        request.setConfirmPassword(INITIAL_PASSWORD);
        request.setEmailVerificationToken("test-verification-token");

        // Keep the persisted request object available to the approval lookup.
        final StudentRegistrationRequest[] savedRequest = new StudentRegistrationRequest[1];
        when(registrations.save(any(StudentRegistrationRequest.class))).thenAnswer(call -> {
            StudentRegistrationRequest entity = call.getArgument(0);
            if (entity.getId() == null) entity.setId(12);
            savedRequest[0] = entity;
            return entity;
        });
        when(registrations.findById(12)).thenAnswer(call -> java.util.Optional.ofNullable(savedRequest[0]));

        OtpService otpService = mock(OtpService.class);
        StudentRegistrationServiceImpl service = new StudentRegistrationServiceImpl(registrations, students,
                years, staff, admins, encoder, otpService);
        service.submit(request);
        String registrationHash = savedRequest[0].getPassword();
        assertStoredHash(registrationHash, INITIAL_PASSWORD);

        service.approve(12);

        assertEquals(registrationHash, savedRequest[0].getPassword());
        assertEquals(registrationHash, approvedStudent[0].getPassword());
        assertTrue(encoder.matches(INITIAL_PASSWORD, savedRequest[0].getPassword()));
    }

    @Test
    void duplicateStudentRollIdReturnsSpecificSafeConflictMessage() {
        StudentRepository students = mock(StudentRepository.class);
        when(students.existsByRollId("STU001")).thenReturn(true);
        StudentServiceImpl service = new StudentServiceImpl(students, mock(StudentRegistrationRepository.class),
                mock(StaffRepository.class), mock(AdminRepository.class), mock(AcademicYearRepository.class), encoder);
        Student request = student("STU001", "student@college.local", INITIAL_PASSWORD);

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.saveStudent(request));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        assertEquals("Student with roll ID STU001 already exists.", error.getReason());
    }

    private void assertStoredHash(String stored, String raw) {
        assertNotEquals(raw, stored);
        assertTrue(stored.matches("^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$"));
        assertTrue(encoder.matches(raw, stored));
        assertFalse(encoder.matches("wrong-password", stored));
    }

    private Admin admin(String rollId, String email, String password) {
        Admin account = new Admin();
        account.setRollId(rollId); account.setName("Admin"); account.setEmail(email);
        account.setDesignation("Administrator"); account.setPassword(password);
        return account;
    }

    private Staff staff(String rollId, String email, String password) {
        Staff account = new Staff();
        account.setRollId(rollId); account.setName("Staff"); account.setEmail(email);
        account.setDesignation("Advisor"); account.setDepartment("Computer Science and Engineering"); account.setStreamId(5);
        account.setPassword(password);
        return account;
    }

    private Student student(String rollId, String email, String password) {
        Student account = new Student();
        account.setRollId(rollId); account.setName("Student"); account.setEmail(email);
        account.setDepartment("Computer Science and Engineering"); account.setAcademicYearId(3);
        account.setPassword(password);
        return account;
    }
}
