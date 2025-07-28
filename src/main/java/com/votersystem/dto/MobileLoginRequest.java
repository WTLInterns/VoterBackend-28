package com.votersystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class MobileLoginRequest {
    
    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Mobile number must be exactly 10 digits starting with 6, 7, 8, or 9")
    private String mobile;
    
    @NotBlank(message = "Password is required")
    private String password;
    
    // Constructors
    public MobileLoginRequest() {}
    
    public MobileLoginRequest(String mobile, String password) {
        this.mobile = mobile;
        this.password = password;
    }
    
    // Getters and Setters
    public String getMobile() {
        return mobile;
    }
    
    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}
