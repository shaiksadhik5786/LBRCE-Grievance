package com.example.demo.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;

@Entity
@Table(name = "registered_complaint", indexes = {
        @Index(name = "idx_rc_status", columnList = "registration_status"),
        @Index(name = "idx_rc_stream", columnList = "stream_id"),
        @Index(name = "idx_rc_staff", columnList = "assigned_staff_id"),
        @Index(name = "idx_rc_registered_date", columnList = "registered_date"),
        @Index(name = "idx_rc_verification", columnList = "verification_status"),
        @Index(name = "idx_rc_priority", columnList = "priority")
})
public class RegisteredComplaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "registered_complaint_id")
    private Integer registeredComplaintId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    private Complaint complaint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = true)
    private Admin admin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stream_id")
    private Stream stream;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_staff_id")
    private Staff assignedStaff;

    @CreationTimestamp
    @Column(name = "registered_date", nullable = false, updatable = false)
    private LocalDateTime registeredDate;

    @Column(name = "assigned_date")
    private LocalDateTime assignedDate;

    @Column(name = "priority", nullable = false, length = 20)
    private String priority = "NORMAL";

    @Column(name = "registration_status", nullable = false, length = 30)
    private String registrationStatus = "REGISTERED";

    @Column(name = "staff_remarks", columnDefinition = "TEXT")
    private String staffRemarks;

    @Column(name = "resolution", columnDefinition = "TEXT")
    private String resolution;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    @Column(name = "solution_proof_name", length = 255)
    private String solutionProofName;

    @Column(name = "solution_proof_path", length = 500)
    private String solutionProofPath;

    @Column(name = "resolved_date")
    private LocalDateTime resolvedDate;

    @Column(name = "verification_status", nullable = false, length = 30)
    private String verificationStatus = "PENDING";

    @Column(name = "verification_date")
    private LocalDateTime verificationDate;

    public RegisteredComplaint() {
    }

    public Integer getRegisteredComplaintId() {
        return registeredComplaintId;
    }

    public void setRegisteredComplaintId(Integer registeredComplaintId) {
        this.registeredComplaintId = registeredComplaintId;
    }

    public Complaint getComplaint() {
        return complaint;
    }

    public void setComplaint(Complaint complaint) {
        this.complaint = complaint;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public Stream getStream() {
        return stream;
    }

    public void setStream(Stream stream) {
        this.stream = stream;
    }

    public Staff getAssignedStaff() {
        return assignedStaff;
    }

    public void setAssignedStaff(Staff assignedStaff) {
        this.assignedStaff = assignedStaff;
    }

    public LocalDateTime getRegisteredDate() {
        return registeredDate;
    }

    public void setRegisteredDate(LocalDateTime registeredDate) {
        this.registeredDate = registeredDate;
    }

    public LocalDateTime getAssignedDate() {
        return assignedDate;
    }

    public void setAssignedDate(LocalDateTime assignedDate) {
        this.assignedDate = assignedDate;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getRegistrationStatus() {
        return registrationStatus;
    }

    public void setRegistrationStatus(String registrationStatus) {
        this.registrationStatus = registrationStatus;
    }

    public String getStaffRemarks() {
        return staffRemarks;
    }

    public void setStaffRemarks(String staffRemarks) {
        this.staffRemarks = staffRemarks;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public String getSolutionProofName() { return solutionProofName; }
    public void setSolutionProofName(String solutionProofName) { this.solutionProofName = solutionProofName; }
    public String getSolutionProofPath() { return solutionProofPath; }
    public void setSolutionProofPath(String solutionProofPath) { this.solutionProofPath = solutionProofPath; }

    public LocalDateTime getResolvedDate() {
        return resolvedDate;
    }

    public void setResolvedDate(LocalDateTime resolvedDate) {
        this.resolvedDate = resolvedDate;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public LocalDateTime getVerificationDate() {
        return verificationDate;
    }

    public void setVerificationDate(LocalDateTime verificationDate) {
        this.verificationDate = verificationDate;
    }
}
