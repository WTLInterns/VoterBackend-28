package com.votersystem.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for reopening an issue
 */
public class ReopenIssueRequest {
    
    @NotBlank(message = "Reopen reason is required")
    private String reopenReason;
    
    // Constructors
    public ReopenIssueRequest() {}
    
    public ReopenIssueRequest(String reopenReason) {
        this.reopenReason = reopenReason;
    }
    
    // Getters and Setters
    public String getReopenReason() { return reopenReason; }
    public void setReopenReason(String reopenReason) { this.reopenReason = reopenReason; }
}
