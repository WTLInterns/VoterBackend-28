package com.votersystem.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * DTO for updating agent interface status
 * Only allows values 1 (Money Distribution) or 2 (Issue Reporting)
 */
public class UpdateAgentInterfaceRequest {
    
    @NotNull(message = "Interface status is required")
    @Pattern(regexp = "^[12]$", message = "Interface status must be 1 (Money Distribution) or 2 (Issue Reporting)")
    private String interfaceStatus;
    
    // Constructors
    public UpdateAgentInterfaceRequest() {}
    
    public UpdateAgentInterfaceRequest(String interfaceStatus) {
        this.interfaceStatus = interfaceStatus;
    }
    
    // Getters and Setters
    public String getInterfaceStatus() {
        return interfaceStatus;
    }
    
    public void setInterfaceStatus(String interfaceStatus) {
        this.interfaceStatus = interfaceStatus;
    }
    
    // Helper method to get as Integer
    public Integer getInterfaceStatusAsInteger() {
        return interfaceStatus != null ? Integer.parseInt(interfaceStatus) : null;
    }
}
