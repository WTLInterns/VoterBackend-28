package com.votersystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {
    
    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @Size(min = 10, max = 20, message = "Phone number must be between 10 and 20 characters")
    private String newUsername; // Optional - only if changing phone number (field name kept for compatibility)

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String newPassword; // Optional - only if changing password
    
    public UpdateProfileRequest() {}
    
    public UpdateProfileRequest(String currentPassword, String newUsername, String newPassword) {
        this.currentPassword = currentPassword;
        this.newUsername = newUsername;
        this.newPassword = newPassword;
    }
    
    public String getCurrentPassword() {
        return currentPassword;
    }
    
    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }
    
    public String getNewUsername() {
        return newUsername;
    }
    
    public void setNewUsername(String newUsername) {
        this.newUsername = newUsername;
    }
    
    public String getNewPassword() {
        return newPassword;
    }
    
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
