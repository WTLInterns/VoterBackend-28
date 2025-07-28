package com.votersystem.dto;

import com.votersystem.entity.Transaction;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {
    private String id;
    private Long userId;
    private String agentId;
    private BigDecimal amount;
    private String location;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String status;
    private LocalDateTime createdAt;
    
    // User information
    private String userName;
    private String userFirstName;
    private String userLastName;
    
    // Agent information
    private String agent;
    private String agentFirstName;
    private String agentLastName;
    
    // Additional fields for frontend compatibility
    private String date;
    private String user;
    private String timestamp;
    
    public TransactionResponse() {}
    
    public TransactionResponse(Transaction transaction) {
        this.id = transaction.getId();
        this.userId = transaction.getUserId();
        this.agentId = transaction.getAgentId();
        this.amount = transaction.getAmount();
        this.location = transaction.getLocation();
        this.latitude = transaction.getLatitude();
        this.longitude = transaction.getLongitude();
        this.status = transaction.getStatus().toString();
        this.createdAt = transaction.getCreatedAt();
        this.date = transaction.getCreatedAt().toString();
        this.timestamp = transaction.getCreatedAt().toString();
        
        // Set user information if available
        if (transaction.getUser() != null) {
            this.userFirstName = transaction.getUser().getFirstName();
            this.userLastName = transaction.getUser().getLastName();
            this.userName = this.userFirstName + " " + this.userLastName;
            this.user = this.userName;
        }
        
        // Set agent information if available
        if (transaction.getAgent() != null) {
            this.agentFirstName = transaction.getAgent().getFirstName();
            this.agentLastName = transaction.getAgent().getLastName();
            this.agent = this.agentFirstName + " " + this.agentLastName;
        }
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getAgentId() {
        return agentId;
    }
    
    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public BigDecimal getLatitude() {
        return latitude;
    }
    
    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }
    
    public BigDecimal getLongitude() {
        return longitude;
    }
    
    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getUserFirstName() {
        return userFirstName;
    }
    
    public void setUserFirstName(String userFirstName) {
        this.userFirstName = userFirstName;
    }
    
    public String getUserLastName() {
        return userLastName;
    }
    
    public void setUserLastName(String userLastName) {
        this.userLastName = userLastName;
    }
    
    public String getAgent() {
        return agent;
    }
    
    public void setAgent(String agent) {
        this.agent = agent;
    }
    
    public String getAgentFirstName() {
        return agentFirstName;
    }
    
    public void setAgentFirstName(String agentFirstName) {
        this.agentFirstName = agentFirstName;
    }
    
    public String getAgentLastName() {
        return agentLastName;
    }
    
    public void setAgentLastName(String agentLastName) {
        this.agentLastName = agentLastName;
    }
    
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
    
    public String getUser() {
        return user;
    }
    
    public void setUser(String user) {
        this.user = user;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
