package com.votersystem.dto;

public class LoginResponse {
    
    private String token;
    private String tokenType = "Bearer";
    private String username;
    private String userId;
    private String role;
    private String userType;
    private Long expiresIn;
    private Integer interfaceStatus; // For agents: 1 = Money Distribution, 2 = Issue Reporting
    
    // Constructors
    public LoginResponse() {}
    
    public LoginResponse(String token, String username, String userId, String role, String userType, Long expiresIn) {
        this.token = token;
        this.username = username;
        this.userId = userId;
        this.role = role;
        this.userType = userType;
        this.expiresIn = expiresIn;
    }

    // Constructor with interfaceStatus for agents
    public LoginResponse(String token, String username, String userId, String role, String userType, Long expiresIn, Integer interfaceStatus) {
        this.token = token;
        this.username = username;
        this.userId = userId;
        this.role = role;
        this.userType = userType;
        this.expiresIn = expiresIn;
        this.interfaceStatus = interfaceStatus;
    }
    
    // Getters and Setters
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getTokenType() {
        return tokenType;
    }
    
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getUserType() {
        return userType;
    }
    
    public void setUserType(String userType) {
        this.userType = userType;
    }
    
    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public Integer getInterfaceStatus() {
        return interfaceStatus;
    }

    public void setInterfaceStatus(Integer interfaceStatus) {
        this.interfaceStatus = interfaceStatus;
    }
}
