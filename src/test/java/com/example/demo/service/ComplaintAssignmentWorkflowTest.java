package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import com.example.demo.dto.ApiDtos.AssignmentRequest;
import com.example.demo.entity.AcademicYear;
import com.example.demo.entity.Admin;
import com.example.demo.entity.Complaint;
import com.example.demo.entity.RegisteredComplaint;
import com.example.demo.entity.Staff;
import com.example.demo.entity.Stream;
import com.example.demo.entity.Student;
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

class ComplaintAssignmentWorkflowTest {
    @Test
    void adminCanClassifyAndAssignAnAcceptedComplaintToMatchingStaff() {
        Authentication auth = mock(Authentication.class);
        CurrentUserService currentUser = mock(CurrentUserService.class);
        RegisteredComplaintRepository workflows = mock(RegisteredComplaintRepository.class);
        ComplaintHistoryRepository history = mock(ComplaintHistoryRepository.class);
        StreamRepository streams = mock(StreamRepository.class);
        StaffRepository staff = mock(StaffRepository.class);
        StudentRepository students = mock(StudentRepository.class);

        AcademicYear year = new AcademicYear();
        year.setAcademicYearId(5);
        year.setYearName("2026-27");
        Student student = new Student();
        student.setStudentId(12);
        student.setRollId("STU012");
        student.setName("Student");
        student.setDepartment("Computer Science and Engineering");
        Complaint complaint = new Complaint();
        complaint.setComplaintId(120);
        complaint.setAcademicYear(year);
        complaint.setStudent(student);
        complaint.setComplaintType("Student concern");
        complaint.setSubject("Library access");
        complaint.setDescription("Unable to access the digital library.");
        RegisteredComplaint workflow = new RegisteredComplaint();
        workflow.setRegisteredComplaintId(120);
        workflow.setComplaint(complaint);
        workflow.setStudent(student);
        workflow.setRegistrationStatus("ACCEPTED");

        Stream stream = new Stream();
        stream.setStreamId(3);
        stream.setStreamName("Library");
        Staff assignee = new Staff();
        assignee.setStaffId(7);
        assignee.setName("Library Staff");
        assignee.setStreamId(3);
        Admin admin = new Admin();
        admin.setAdminId(2);
        admin.setName("College Admin");

        when(workflows.findByComplaint_ComplaintId(120)).thenReturn(Optional.of(workflow));
        when(workflows.save(workflow)).thenReturn(workflow);
        when(history.findByRegisteredComplaint_RegisteredComplaintIdOrderByCreatedAtAsc(120)).thenReturn(List.of());
        when(streams.findById(3)).thenReturn(Optional.of(stream));
        when(staff.findById(7)).thenReturn(Optional.of(assignee));
        when(students.existsById(12)).thenReturn(true);
        when(currentUser.role(auth)).thenReturn("ROLE_ADMIN");
        when(currentUser.admin(auth)).thenReturn(admin);
        when(currentUser.id(auth)).thenReturn(2);
        when(currentUser.name(auth)).thenReturn("College Admin");

        ComplaintWorkflowService service = new ComplaintWorkflowService(
                mock(ComplaintRepository.class), workflows, history, mock(AcademicYearRepository.class),
                streams, staff, students, mock(StudentRegistrationRepository.class),
                currentUser, mock(UploadStorageService.class));

        var result = service.assign(120, auth, new AssignmentRequest(3, 7, "NORMAL"));

        assertEquals("ASSIGNED", result.status());
        assertEquals("NORMAL", result.priority());
        assertEquals(3, result.streamId());
        assertEquals(7, result.assignedStaffId());
        assertEquals(admin, workflow.getAdmin());
        assertEquals(stream, workflow.getStream());
        assertEquals(assignee, workflow.getAssignedStaff());
        assertNotNull(workflow.getAssignedDate());
        verify(workflows).save(workflow);
    }

    @Test
    void missingStudentLinksAreExcludedFromWorkflowActions() {
        Authentication auth = mock(Authentication.class);
        CurrentUserService currentUser = mock(CurrentUserService.class);
        RegisteredComplaintRepository workflows = mock(RegisteredComplaintRepository.class);
        StudentRepository students = mock(StudentRepository.class);

        Student missingStudent = new Student();
        missingStudent.setStudentId(404);
        Complaint complaint = new Complaint();
        complaint.setComplaintId(404);
        complaint.setStudent(missingStudent);
        RegisteredComplaint workflow = new RegisteredComplaint();
        workflow.setComplaint(complaint);
        workflow.setStudent(missingStudent);
        when(workflows.findByComplaint_ComplaintId(404)).thenReturn(Optional.of(workflow));
        when(students.existsById(404)).thenReturn(false);

        ComplaintWorkflowService service = new ComplaintWorkflowService(
                mock(ComplaintRepository.class), workflows, mock(ComplaintHistoryRepository.class),
                mock(AcademicYearRepository.class), mock(StreamRepository.class), mock(StaffRepository.class),
                students, mock(StudentRegistrationRepository.class), currentUser, mock(UploadStorageService.class));

        org.springframework.web.server.ResponseStatusException error = org.junit.jupiter.api.Assertions
                .assertThrows(org.springframework.web.server.ResponseStatusException.class,
                        () -> service.visible(404, auth));
        assertEquals(404, error.getStatusCode().value());
    }
}
