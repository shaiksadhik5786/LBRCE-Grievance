package com.example.demo.securityConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.entity.AcademicYear;
import com.example.demo.entity.Admin;
import com.example.demo.entity.Staff;
import com.example.demo.entity.Stream;
import com.example.demo.entity.Student;
import com.example.demo.repository.AcademicYearRepository;
import com.example.demo.repository.AdminRepository;
import com.example.demo.repository.StaffRepository;
import com.example.demo.repository.StreamRepository;
import com.example.demo.repository.StudentRepository;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceProfileTest {
    @Mock private StudentRepository students;
    @Mock private StaffRepository staff;
    @Mock private AdminRepository admins;
    @Mock private AcademicYearRepository years;
    @Mock private StreamRepository streams;

    private CurrentUserService currentUser;

    @BeforeEach
    void setUp() {
        currentUser = new CurrentUserService(students, staff, admins, years, streams);
    }

    @Test
    void studentProfileUsesAuthenticatedRollIdAndReturnsAcademicDetails() {
        Student student = new Student();
        student.setStudentId(21);
        student.setRollId("STU021");
        student.setName("Student Example");
        student.setEmail("student@example.edu");
        student.setPhone("9000000021");
        student.setDepartment("Computer Science and Engineering");
        student.setAcademicYearId(3);
        AcademicYear year = new AcademicYear();
        year.setYearName("2025-26");
        when(students.findByRollId("STU021")).thenReturn(Optional.of(student));
        when(years.findById(3)).thenReturn(Optional.of(year));

        var profile = currentUser.profile(authentication("STU021", "ROLE_STUDENT"));

        assertEquals("ROLE_STUDENT", profile.role());
        assertEquals(21, profile.userId());
        assertEquals("STU021", profile.rollId());
        assertEquals("2025-26", profile.academicYear());
        assertEquals("STU021", profile.rollId());
        verify(students).findByRollId("STU021");
        verifyNoInteractions(staff, admins);
    }

    @Test
    void staffProfileUsesAuthenticatedRollIdAndReturnsAssignedStream() {
        Staff member = new Staff();
        member.setStaffId(8);
        member.setRollId("STF008");
        member.setName("Staff Example");
        member.setEmail("staff@example.edu");
        member.setPhone("9000000008");
        member.setDesignation("Lecturer");
        member.setDepartment("Electronics and Communication Engineering");
        member.setStreamId(4);
        Stream stream = new Stream();
        stream.setStreamName("Academic");
        when(staff.findByRollId("STF008")).thenReturn(Optional.of(member));
        when(streams.findById(4)).thenReturn(Optional.of(stream));

        var profile = currentUser.profile(authentication("STF008", "ROLE_STAFF"));

        assertEquals("ROLE_STAFF", profile.role());
        assertEquals(8, profile.userId());
        assertEquals("STF008", profile.rollId());
        assertEquals("Lecturer", profile.designation());
        assertEquals("Academic", profile.streamName());
        verify(staff).findByRollId("STF008");
        verifyNoInteractions(students, admins);
    }

    @Test
    void adminProfileUsesAuthenticatedRollIdAndReturnsOnlyAdminDetails() {
        Admin admin = new Admin();
        admin.setAdminId(2);
        admin.setRollId("ADM002");
        admin.setName("Admin Example");
        admin.setEmail("admin@example.edu");
        admin.setPhone("9000000002");
        admin.setDesignation("Administrator");
        when(admins.findByRollId("ADM002")).thenReturn(Optional.of(admin));

        var profile = currentUser.profile(authentication("ADM002", "ROLE_ADMIN"));

        assertEquals("ROLE_ADMIN", profile.role());
        assertEquals(2, profile.userId());
        assertEquals("ADM002", profile.rollId());
        assertEquals("Administrator", profile.designation());
        verify(admins).findByRollId("ADM002");
        verifyNoInteractions(students, staff);
    }

    @Test
    void studentCannotResolveAnotherRoleAccountByChangingTheAuthenticatedIdentity() {
        when(students.findByRollId("STF008")).thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> currentUser.profile(authentication("STF008", "ROLE_STUDENT")));

        assertEquals(401, error.getStatusCode().value());
        verify(staff, never()).findByRollId("STF008");
        verify(admins, never()).findByRollId("STF008");
    }

    @Test
    void unsupportedRoleCannotResolveAProfile() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> currentUser.profile(authentication("UNKNOWN", "ROLE_UNKNOWN")));

        assertEquals(403, error.getStatusCode().value());
        verifyNoInteractions(students, staff, admins, years, streams);
    }

    private Authentication authentication(String rollId, String role) {
        return new UsernamePasswordAuthenticationToken(rollId, null,
                List.of(new SimpleGrantedAuthority(role)));
    }
}
