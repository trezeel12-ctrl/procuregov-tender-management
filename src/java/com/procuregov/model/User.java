package com.procuregov.model;

import java.time.LocalDateTime;

public class User {
    private int userId;
    private String fullName;
    private String email;
    private String passwordHash;
    private String role;
    private String registrationNumber;
    private String physicalAddress;
    private String contactNumber;
    private int failedLoginAttempts;
    private LocalDateTime lastFailedAttempt;
    private LocalDateTime createdAt;

    // Constructors
    public User() {}

    public User(String fullName, String email, String passwordHash, String role) {
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }

    public String getPhysicalAddress() { return physicalAddress; }
    public void setPhysicalAddress(String physicalAddress) { this.physicalAddress = physicalAddress; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(int failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }

    public LocalDateTime getLastFailedAttempt() { return lastFailedAttempt; }
    public void setLastFailedAttempt(LocalDateTime lastFailedAttempt) { this.lastFailedAttempt = lastFailedAttempt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "User{userId=" + userId + ", fullName='" + fullName + "', role='" + role + "', email='" + email + "'}";
    }
}