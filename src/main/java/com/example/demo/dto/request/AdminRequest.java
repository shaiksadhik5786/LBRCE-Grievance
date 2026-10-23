package com.example.demo.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AdminRequest {

    private String rollId;
    private String name;
    @NotBlank(message = "Email is required.") @Email(message = "Email address is invalid.") @Size(max = 100)
    private String email;
    @Pattern(regexp = "^$|^[0-9]{10}$", message = "Phone number must contain exactly 10 digits.")
    private String phone;
    private String designation;
    @Pattern(regexp = "^$|^.{8,72}$", message = "Password must be between 8 and 72 characters.")
    private String password;
    @Size(max = 100) private String emailVerificationToken;

    public AdminRequest() {
    }

    public String getRollId() {
        return rollId;
    }

    public void setRollId(String rollId) {
        this.rollId = rollId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmailVerificationToken() { return emailVerificationToken; }
    public void setEmailVerificationToken(String emailVerificationToken) { this.emailVerificationToken = emailVerificationToken; }
}
