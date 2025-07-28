package com.votersystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "administrators")
public class Administrator implements UserDetails {
    
    @Id
    @Column(name = "id", length = 20)
    private String id;
    
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
    @Column(name = "role", nullable = false)
    private AdminRole role;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AdminStatus status = AdminStatus.ACTIVE;
    
    @Column(name = "total_payments", nullable = false)
    private Integer totalPayments = 0;
    
    @Column(name = "created_by", length = 50)
    private String createdBy; // Master admin username who created this sub-admin
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    public Administrator() {}
    
    public Administrator(String id, String firstName, String lastName, String mobile,
                        String passwordHash, AdminRole role, String createdBy) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.mobile = mobile;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdBy = createdBy;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    
    @Override
    public String getPassword() {
        return passwordHash;
    }
    
    @Override
    public String getUsername() {
        return mobile; // Use mobile as username for login
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return status == AdminStatus.ACTIVE;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return status == AdminStatus.ACTIVE;
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
    
    public AdminRole getRole() {
        return role;
    }
    
    public void setRole(AdminRole role) {
        this.role = role;
    }
    
    public AdminStatus getStatus() {
        return status;
    }
    
    public void setStatus(AdminStatus status) {
        this.status = status;
    }
    
    public Integer getTotalPayments() {
        return totalPayments;
    }
    
    public void setTotalPayments(Integer totalPayments) {
        this.totalPayments = totalPayments;
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
    
    // Helper methods
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public boolean isMasterAdmin() {
        return role == AdminRole.MASTER;
    }
    
    public boolean isSubAdmin() {
        return role == AdminRole.ADMIN;
    }
    
    // Enums
    public enum AdminRole {
        MASTER, ADMIN, SUPERVISOR
    }
    
    public enum AdminStatus {
        ACTIVE, BLOCKED
    }
}
