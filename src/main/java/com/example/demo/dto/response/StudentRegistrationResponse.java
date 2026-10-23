package com.example.demo.dto.response;

import java.time.LocalDateTime;
import com.example.demo.entity.StudentRegistrationRequest.Status;

public class StudentRegistrationResponse {
    private Integer id;
    private String rollId, name, department, email, phone, rejectionReason;
    private Integer academicYearId;
    private LocalDateTime submittedAt;
    private Status status;
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getRollId() { return rollId; }
    public void setRollId(String rollId) { this.rollId = rollId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Integer getAcademicYearId() { return academicYearId; }
    public void setAcademicYearId(Integer academicYearId) { this.academicYearId = academicYearId; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}
