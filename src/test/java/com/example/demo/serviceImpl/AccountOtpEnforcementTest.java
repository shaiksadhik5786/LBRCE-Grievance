package com.example.demo.serviceImpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.entity.Admin;
import com.example.demo.entity.Staff;
import com.example.demo.repository.*;
import com.example.demo.service.OtpPurpose;
import com.example.demo.service.OtpService;

class AccountOtpEnforcementTest {
    @Test
    void staffCreationRequiresAndConsumesStaffVerification() {
        StaffRepository repository = mock(StaffRepository.class);
        StreamRepository streams = mock(StreamRepository.class);
        OtpService otp = mock(OtpService.class);
        when(streams.existsById(1)).thenReturn(true);
        when(repository.save(any(Staff.class))).thenAnswer(call -> call.getArgument(0));
        StaffServiceImpl service = new StaffServiceImpl(repository, streams, mock(StudentRepository.class),
                mock(AdminRepository.class), new BCryptPasswordEncoder(), otp);
        Staff staff = staff();
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Verification required"))
                .when(otp).requireAndConsumeVerification("staff@example.com", OtpPurpose.STAFF_CREATION, null);
        assertEquals(HttpStatus.FORBIDDEN,
                assertThrows(ResponseStatusException.class, () -> service.saveStaff(staff, null)).getStatusCode());

        Staff saved = service.saveStaff(staff(), "staff-token");
        verify(otp).requireAndConsumeVerification("staff@example.com", OtpPurpose.STAFF_CREATION, "staff-token");
        assertTrue(saved.getPassword().startsWith("$2"));
    }

    @Test
    void adminCreationRequiresAndConsumesAdminVerification() {
        AdminRepository repository = mock(AdminRepository.class);
        OtpService otp = mock(OtpService.class);
        AdminServiceImpl service = new AdminServiceImpl(repository, mock(StudentRepository.class),
                mock(StaffRepository.class), new BCryptPasswordEncoder(), otp);
        when(repository.save(any(Admin.class))).thenAnswer(call -> call.getArgument(0));
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Verification required"))
                .when(otp).requireAndConsumeVerification("admin@example.com", OtpPurpose.ADMIN_CREATION, null);
        assertEquals(HttpStatus.FORBIDDEN,
                assertThrows(ResponseStatusException.class, () -> service.saveAdmin(admin(), null)).getStatusCode());

        Admin saved = service.saveAdmin(admin(), "admin-token");
        verify(otp).requireAndConsumeVerification("admin@example.com", OtpPurpose.ADMIN_CREATION, "admin-token");
        assertTrue(saved.getPassword().startsWith("$2"));
    }

    private Staff staff() {
        Staff staff = new Staff(); staff.setRollId("STF100"); staff.setName("Staff");
        staff.setEmail("staff@example.com"); staff.setPhone("9876543210"); staff.setDesignation("Advisor");
        staff.setDepartment("Computer Science and Engineering"); staff.setStreamId(1); staff.setPassword("Password9");
        return staff;
    }
    private Admin admin() {
        Admin admin = new Admin(); admin.setRollId("ADM100"); admin.setName("Admin");
        admin.setEmail("admin@example.com"); admin.setPhone("9876543210"); admin.setDesignation("Administrator");
        admin.setPassword("Password9"); return admin;
    }
}
