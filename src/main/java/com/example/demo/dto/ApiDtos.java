package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.List;
import jakarta.validation.constraints.*;

public final class ApiDtos {
    private ApiDtos() {}

    public record LoginRequest(@NotBlank String rollId, @NotBlank String password) {}
    public record LoginResponse(String token, String role, Integer userId, String rollId, String name,
            boolean otpRequired, String otpChallengeId, String maskedEmail) {}
    public record LoginOtpRequest(@NotBlank String challengeId,
            @NotBlank @Pattern(regexp = "^[0-9]{6}$", message = "OTP must contain exactly 6 digits.") String otp,
            @NotNull com.example.demo.service.OtpPurpose purpose) {}
    public record ProfileResponse(String role, Integer userId, String rollId, String name, String email,
            String phone, String department, String designation, Integer academicYearId,
            String academicYear, Integer streamId, String streamName) {}
    public record ReviewRequest(@NotBlank String action, String reason) {}
    public record AssignmentRequest(@NotNull Integer streamId, @NotNull Integer staffId,
            @NotBlank String priority) {}
    public record SolutionRequest(@NotBlank @Size(max = 10000) String solution,
            @Size(max = 5000) String remarks) {}
    public record VerificationRequest(@NotBlank String action, @Size(max = 5000) String remarks) {}
    public record HistoryDto(Integer id, String previousStatus, String newStatus, String action,
            Integer performedById, String performedByName, String role, String remarks, LocalDateTime createdAt) {}
    public record ComplaintDto(Integer registeredComplaintId, Integer complaintId, String complaintType,
            String subject, String description, String attachmentName, String studentRollId,
            String studentName, String department, Integer academicYearId,
            String academicYear, Integer streamId, String streamName, Integer assignedStaffId,
            String assignedStaffName, String priority, String status, String verificationStatus,
            String rejectionReason, String staffRemarks, String resolution, String solutionProofName,
            LocalDateTime createdDate, LocalDateTime registeredDate, LocalDateTime assignedDate,
            LocalDateTime resolvedDate, List<HistoryDto> history) {}
    public record ComplaintPageResponse(List<ComplaintDto> content, int number, int size, long totalElements, int totalPages) {}
    public record RegistrationDecision(String reason) {}
}
