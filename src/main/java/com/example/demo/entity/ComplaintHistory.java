package com.example.demo.entity;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.*;

@Entity
@Table(name = "complaint_history", indexes = {
        @Index(name = "idx_history_complaint_time", columnList = "registered_complaint_id,created_at")
})
public class ComplaintHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id") private Integer historyId;
    @ManyToOne(optional = false) @JoinColumn(name = "registered_complaint_id", nullable = false)
    private RegisteredComplaint registeredComplaint;
    @Column(name = "previous_status", length = 30) private String previousStatus;
    @Column(name = "new_status", nullable = false, length = 30) private String newStatus;
    @Column(name = "action", nullable = false, length = 80) private String action;
    @Column(name = "performed_by_id", nullable = false) private Integer performedById;
    @Column(name = "performed_by_name", nullable = false, length = 120) private String performedByName;
    @Column(name = "role", nullable = false, length = 20) private String role;
    @Column(name = "remarks", columnDefinition = "TEXT") private String remarks;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Integer getHistoryId() { return historyId; }
    public void setHistoryId(Integer historyId) { this.historyId = historyId; }
    public RegisteredComplaint getRegisteredComplaint() { return registeredComplaint; }
    public void setRegisteredComplaint(RegisteredComplaint registeredComplaint) { this.registeredComplaint = registeredComplaint; }
    public String getPreviousStatus() { return previousStatus; }
    public void setPreviousStatus(String previousStatus) { this.previousStatus = previousStatus; }
    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public Integer getPerformedById() { return performedById; }
    public void setPerformedById(Integer performedById) { this.performedById = performedById; }
    public String getPerformedByName() { return performedByName; }
    public void setPerformedByName(String performedByName) { this.performedByName = performedByName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
