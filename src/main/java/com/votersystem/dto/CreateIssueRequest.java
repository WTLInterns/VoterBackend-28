package com.votersystem.dto;

import com.votersystem.entity.Issue;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for creating a new issue
 */
public class CreateIssueRequest {
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    @NotNull(message = "Category is required")
    private Issue.IssueCategory category;
    
    @NotNull(message = "Priority is required")
    private Issue.IssuePriority priority;
    
    // Location information
    private String address;
    private String area;
    private String village;
    private String district;
    
    // Constructors
    public CreateIssueRequest() {}
    
    public CreateIssueRequest(String title, String description, Issue.IssueCategory category, Issue.IssuePriority priority) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
    }
    
    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Issue.IssueCategory getCategory() { return category; }
    public void setCategory(Issue.IssueCategory category) { this.category = category; }
    
    public Issue.IssuePriority getPriority() { return priority; }
    public void setPriority(Issue.IssuePriority priority) { this.priority = priority; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getVillage() { return village; }
    public void setVillage(String village) { this.village = village; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
}
