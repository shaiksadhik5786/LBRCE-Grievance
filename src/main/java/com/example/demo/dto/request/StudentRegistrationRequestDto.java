package com.example.demo.dto.request;

import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = false)
public class StudentRegistrationRequestDto {
    @NotBlank(message = "Roll ID is required.") @Size(max = 20) private String rollId;
    @NotNull(message = "Academic year is required.") @Positive(message = "Academic year is invalid.") private Integer academicYearId;
    @NotBlank(message = "Name is required.") @Size(max = 100) private String name;
    @NotBlank(message = "Department is required.") @Size(max = 100) private String department;
    @NotBlank(message = "Email is required.") @Email(message = "Email address is invalid.") @Size(max = 100) private String email;
    @NotBlank(message = "Phone number must contain exactly 10 digits.")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must contain exactly 10 digits.") private String phone;
    @NotBlank @Size(min = 8, max = 72) private String password;
    @NotBlank private String confirmPassword;
    @NotBlank(message = "Please verify your email with OTP before completing registration.")
    @Size(max = 100) private String emailVerificationToken;

    public String getRollId() { return rollId; }
    public void setRollId(String rollId) { this.rollId = rollId; }
    public Integer getAcademicYearId() { return academicYearId; }
    public void setAcademicYearId(Integer academicYearId) { this.academicYearId = academicYearId; }
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
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    public String getEmailVerificationToken() { return emailVerificationToken; }
    public void setEmailVerificationToken(String emailVerificationToken) { this.emailVerificationToken = emailVerificationToken; }
}
