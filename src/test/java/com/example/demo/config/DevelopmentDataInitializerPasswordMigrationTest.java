package com.example.demo.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentCaptor.forClass;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

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

class DevelopmentDataInitializerPasswordMigrationTest {
    @Test
    void migratesPlaintextCredentialsOnceAndPreservesExistingBcryptCredentials() {
        var encoder = new BCryptPasswordEncoder();
        String existingHash = encoder.encode("already-hashed-password");
        Admin legacyAdmin = new Admin(); legacyAdmin.setPassword("admin-credential");
        Admin existingAdmin = new Admin(); existingAdmin.setPassword(existingHash);
        Staff legacyStaff = new Staff(); legacyStaff.setPassword("staff-credential");
        Student legacyStudent = new Student(); legacyStudent.setPassword("student-credential");
        StudentRegistrationRequest legacyRegistration = new StudentRegistrationRequest();
        legacyRegistration.setPassword("registration-credential");

        StreamRepository streams = mock(StreamRepository.class);
        AcademicYearRepository years = mock(AcademicYearRepository.class);
        AdminRepository admins = mock(AdminRepository.class);
        StaffRepository staff = mock(StaffRepository.class);
        StudentRepository students = mock(StudentRepository.class);
        StudentRegistrationRepository registrations = mock(StudentRegistrationRepository.class);
        when(streams.count()).thenReturn(1L);
        when(years.count()).thenReturn(1L);
        when(admins.findAll()).thenReturn(List.of(legacyAdmin, existingAdmin));
        when(staff.findAll()).thenReturn(List.of(legacyStaff));
        when(students.findAll()).thenReturn(List.of(legacyStudent));
        when(registrations.findAll()).thenReturn(List.of(legacyRegistration));

        DevelopmentDataInitializer initializer = new DevelopmentDataInitializer(streams, years, admins,
                students, staff, registrations, encoder);
        initializer.run();

        assertMigrated(legacyAdmin.getPassword(), "admin-credential", encoder);
        assertMigrated(legacyStaff.getPassword(), "staff-credential", encoder);
        assertMigrated(legacyStudent.getPassword(), "student-credential", encoder);
        assertMigrated(legacyRegistration.getPassword(), "registration-credential", encoder);
        assertEquals(existingHash, existingAdmin.getPassword());
        assertTrue(encoder.matches("already-hashed-password", existingAdmin.getPassword()));
    }

    @Test
    void bootstrappedDemoAdminStoresBcryptWithoutChangingItsConfiguredCredential() {
        var encoder = new BCryptPasswordEncoder();
        StreamRepository streams = mock(StreamRepository.class);
        AcademicYearRepository years = mock(AcademicYearRepository.class);
        AdminRepository admins = mock(AdminRepository.class);
        StaffRepository staff = mock(StaffRepository.class);
        StudentRepository students = mock(StudentRepository.class);
        StudentRegistrationRepository registrations = mock(StudentRegistrationRepository.class);
        when(streams.count()).thenReturn(1L);
        when(years.count()).thenReturn(1L);
        when(admins.findAll()).thenReturn(List.of());
        AtomicReference<Admin> persisted = new AtomicReference<>();
        when(admins.save(any(Admin.class))).thenAnswer(invocation -> {
            Admin saved = invocation.getArgument(0);
            persisted.set(saved);
            return saved;
        });
        when(admins.findByRollId("ADM001")).thenAnswer(invocation -> Optional.ofNullable(persisted.get()));

        DevelopmentDataInitializer initializer = new DevelopmentDataInitializer(streams, years, admins,
                students, staff, registrations, encoder);
        ReflectionTestUtils.setField(initializer, "bootstrapAdmin", true);
        ReflectionTestUtils.setField(initializer, "adminRollId", "ADM001");
        ReflectionTestUtils.setField(initializer, "adminName", "Sai Charan");
        ReflectionTestUtils.setField(initializer, "adminEmail", "admin@example.edu");
        ReflectionTestUtils.setField(initializer, "adminPassword", "demo-credential");
        initializer.run();

        var savedAdmin = forClass(Admin.class);
        verify(admins).save(savedAdmin.capture());
        assertMigrated(savedAdmin.getValue().getPassword(), "demo-credential", encoder);
    }

    private void assertMigrated(String storedHash, String originalCredential, BCryptPasswordEncoder encoder) {
        assertNotEquals(originalCredential, storedHash);
        assertTrue(encoder.matches(originalCredential, storedHash));
    }
}
