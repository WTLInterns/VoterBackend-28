package com.votersystem.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "agents")
public class Agent {
    
    @Id
    @Column(name = "id", length = 20)
    private String id; // AGENT001, AGENT002, etc.
    
    @NotBlank(message = "First name is required")
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;
    
    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Mobile number must be exactly 10 digits starting with 6, 7, 8, or 9")
    @Column(name = "mobile", nullable = false, unique = true, length = 20)
    private String mobile;
    
    @NotBlank(message = "Password is required")
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AgentStatus status = AgentStatus.ACTIVE;

    @Column(name = "interface_status", nullable = false)
    private Integer interfaceStatus = 2; // 1 = Money Distribution, 2 = Issue Reporting (Default)

    @Column(name = "payments_today", nullable = false)
    private Integer paymentsToday = 0;

    @Column(name = "total_payments", nullable = false)
    private Integer totalPayments = 0;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "last_location", length = 500)
    private String lastLocation;

    @Column(name = "created_by", length = 50)
    private String createdBy; // Sub-admin username who created this agent
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;
    
    // Constructors
    public Agent() {}
    
    public Agent(String id, String firstName, String lastName, String mobile,
                 String passwordHash, String createdBy) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.mobile = mobile;
        this.passwordHash = passwordHash;
        this.createdBy = createdBy;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getMobile() {
        return mobile;
    }
    
    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    // Compatibility method - returns mobile as username
    public String getUsername() {
        return this.mobile;
    }

    // Compatibility method - returns empty string for email
    public String getEmail() {
        return "";
    }

    // Compatibility method - no-op for email setting
    public void setEmail(String email) {
        // No-op - email field removed
    }
    

    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public AgentStatus getStatus() {
        return status;
    }

    public void setStatus(AgentStatus status) {
        this.status = status;
    }

    public Integer getInterfaceStatus() {
        return interfaceStatus;
    }

    public void setInterfaceStatus(Integer interfaceStatus) {
        this.interfaceStatus = interfaceStatus;
    }


    
    public Integer getPaymentsToday() {
        return paymentsToday;
    }
    
    public void setPaymentsToday(Integer paymentsToday) {
        this.paymentsToday = paymentsToday;
    }
    
    public Integer getTotalPayments() {
        return totalPayments;
    }
    
    public void setTotalPayments(Integer totalPayments) {
        this.totalPayments = totalPayments;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(String lastLocation) {
        this.lastLocation = lastLocation;
    }

    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    // Helper methods
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public boolean isActive() {
        return status == AgentStatus.ACTIVE;
    }
    
    public void incrementPaymentCount() {
        this.paymentsToday++;
        this.totalPayments++;
    }

    // New methods to handle payment amounts instead of counts
    public void addPaymentAmount(Integer amount) {
        if (amount != null && amount > 0) {
            this.paymentsToday += amount;
            this.totalPayments += amount;
        }
    }

    public void resetTodaysPayments() {
        this.paymentsToday = 0;
    }
    

    
    // Enum for Agent Status
    public enum AgentStatus {
        ACTIVE, BLOCKED
    }
}
