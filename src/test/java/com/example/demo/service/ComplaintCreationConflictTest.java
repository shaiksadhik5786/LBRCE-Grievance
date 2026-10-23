package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.entity.Student;
import com.example.demo.entity.AcademicYear;
import com.example.demo.entity.Complaint;
import com.example.demo.entity.RegisteredComplaint;
import com.example.demo.repository.AcademicYearRepository;
import com.example.demo.repository.AdminRepository;
import com.example.demo.repository.ComplaintHistoryRepository;
import com.example.demo.repository.ComplaintRepository;
import com.example.demo.repository.RegisteredComplaintRepository;
import com.example.demo.repository.StaffRepository;
import com.example.demo.repository.StreamRepository;
import com.example.demo.repository.StudentRegistrationRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.securityConfig.CurrentUserService;

class ComplaintCreationConflictTest {
    @Test
    void reportsWhenTheStudentAcademicYearIdDoesNotResolve() {
        Authentication auth = mock(Authentication.class);
        CurrentUserService currentUser = mock(CurrentUserService.class);
        AcademicYearRepository years = mock(AcademicYearRepository.class);
        Student student = new Student();
        student.setAcademicYearId(99);
        when(currentUser.student(auth)).thenReturn(student);
        when(years.findById(99)).thenReturn(Optional.empty());
        ComplaintRepository complaints = mock(ComplaintRepository.class);
        RegisteredComplaintRepository workflows = mock(RegisteredComplaintRepository.class);
        ComplaintHistoryRepository history = mock(ComplaintHistoryRepository.class);
        var uploads = mock(UploadStorageService.class);
        ComplaintWorkflowService service = new ComplaintWorkflowService(complaints, workflows, history, years,
                mock(StreamRepository.class), mock(StaffRepository.class), mock(StudentRepository.class),
                mock(StudentRegistrationRepository.class), currentUser, uploads);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.create(auth, "Subject", "Description", null));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        assertEquals("Your academic year is not linked to your student account. Please contact the college office.", error.getReason());
        verifyNoInteractions(complaints, workflows, history, uploads);
    }

    @Test
    void reportsNullAcademicYearIdWithoutQueryingTheRepository() {
        Authentication auth = mock(Authentication.class);
        CurrentUserService currentUser = mock(CurrentUserService.class);
        AcademicYearRepository years = mock(AcademicYearRepository.class);
        when(currentUser.student(auth)).thenReturn(new Student());
        ComplaintWorkflowService service = service(currentUser, years, mock(ComplaintRepository.class),
                mock(RegisteredComplaintRepository.class), mock(ComplaintHistoryRepository.class), mock(UploadStorageService.class));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.create(auth, "Subject", "Description", null));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        assertEquals("Your academic year is not linked to your student account. Please contact the college office.", error.getReason());
        verify(years, never()).findById(any());
    }

    @Test
    void createsRegisteredComplaintForAStudentLinkedToAnExistingYear() {
        Authentication auth = mock(Authentication.class);
        CurrentUserService currentUser = mock(CurrentUserService.class);
        AcademicYearRepository years = mock(AcademicYearRepository.class);
        ComplaintRepository complaints = mock(ComplaintRepository.class);
        RegisteredComplaintRepository workflows = mock(RegisteredComplaintRepository.class);
        ComplaintHistoryRepository history = mock(ComplaintHistoryRepository.class);
        RegisteredComplaint[] savedWorkflow = new RegisteredComplaint[1];
        Student student = new Student();
        student.setStudentId(12); student.setRollId("STU012");
        student.setName("Student"); student.setDepartment("Computer Science and Engineering");
        student.setAcademicYearId(5);
        AcademicYear year = new AcademicYear();
        year.setAcademicYearId(5); year.setYearName("2026-27");
        when(currentUser.student(auth)).thenReturn(student);
        when(currentUser.id(auth)).thenReturn(12);
        when(currentUser.name(auth)).thenReturn("Student");
        when(currentUser.role(auth)).thenReturn("ROLE_STUDENT");
        when(years.findById(5)).thenReturn(Optional.of(year));
        when(complaints.save(any(Complaint.class))).thenAnswer(call -> {
            Complaint complaint = call.getArgument(0);
            complaint.setComplaintId(120);
            return complaint;
        });
        when(workflows.save(any(RegisteredComplaint.class))).thenAnswer(call -> {
            RegisteredComplaint registered = call.getArgument(0);
            savedWorkflow[0] = registered;
            registered.setRegisteredComplaintId(120);
            return registered;
        });
        ComplaintWorkflowService service = service(currentUser, years, complaints, workflows, history,
                mock(UploadStorageService.class));

        var created = service.create(auth, " Library access ", "Unable to access the digital library.", null);

        assertEquals("REGISTERED", created.status());
        assertEquals(5, created.academicYearId());
        assertEquals("Student concern", created.complaintType());
        assertEquals("NORMAL", created.priority());
        assertEquals("PENDING", created.verificationStatus());
        assertNull(created.streamId());
        assertNull(created.assignedStaffId());
        assertNull(savedWorkflow[0].getAdmin());
        assertNull(savedWorkflow[0].getStream());
        assertNull(savedWorkflow[0].getAssignedStaff());
        assertNull(savedWorkflow[0].getAssignedDate());
        assertNull(savedWorkflow[0].getResolution());
        assertNull(savedWorkflow[0].getResolvedDate());
        assertNull(savedWorkflow[0].getStaffRemarks());
        assertNull(savedWorkflow[0].getVerificationDate());
        assertNull(savedWorkflow[0].getRejectionReason());
        assertNull(savedWorkflow[0].getSolutionProofName());
        assertNull(savedWorkflow[0].getSolutionProofPath());
        assertEquals("NORMAL", savedWorkflow[0].getPriority());
        assertEquals("REGISTERED", savedWorkflow[0].getRegistrationStatus());
        assertEquals("PENDING", savedWorkflow[0].getVerificationStatus());
        verify(workflows).save(any(RegisteredComplaint.class));
    }

    private ComplaintWorkflowService service(CurrentUserService currentUser, AcademicYearRepository years,
            ComplaintRepository complaints, RegisteredComplaintRepository workflows,
            ComplaintHistoryRepository history, UploadStorageService uploads) {
        return new ComplaintWorkflowService(complaints, workflows, history, years, mock(StreamRepository.class),
                mock(StaffRepository.class), mock(StudentRepository.class), mock(StudentRegistrationRepository.class),
                currentUser, uploads);
    }
}
