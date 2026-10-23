package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "student_registration_request")
public class StudentRegistrationRequest {
    public enum Status { PENDING, APPROVED, REJECTED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "roll_id", nullable = false, unique = true, length = 20)
    private String rollId;
    @Column(name = "academic_year_id", nullable = false)
    private Integer academicYearId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id", insertable = false, updatable = false)
    private AcademicYear academicYear;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(nullable = false, length = 100)
    private String department;
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    @Column(length = 15)
    private String phone;
    @Column(name = "password_hash", nullable = false, length = 255)
    private String password;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.PENDING;
    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;
    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt = LocalDateTime.now();

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getRollId() { return rollId; }
    public void setRollId(String rollId) { this.rollId = rollId; }
    public Integer getAcademicYearId() { return academicYearId; }
    public void setAcademicYearId(Integer academicYearId) { this.academicYearId = academicYearId; }
    public AcademicYear getAcademicYear() { return academicYear; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
}
