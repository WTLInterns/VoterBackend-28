package com.votersystem.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Issue Entity for Issue Reporting System
 * Represents issues reported by agents/leaders
 */
@Entity
@Table(name = "issues")
public class Issue {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Ticket number is required")
    @Column(name = "ticket_number", nullable = false, unique = true, length = 20)
    private String ticketNumber; // Format: ISS-2025-001234
    
    @NotBlank(message = "Title is required")
    @Column(name = "title", nullable = false, length = 200)
    private String title;
    
    @NotBlank(message = "Description is required")
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;
    
    @NotNull(message = "Category is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private IssueCategory category;
    
    @NotNull(message = "Priority is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private IssuePriority priority;
    
    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private IssueStatus status = IssueStatus.OPEN;
    
    @NotNull(message = "Submission date is required")
    @Column(name = "submission_date", nullable = false)
    private LocalDateTime submissionDate;
    
    // Location information
    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "area", length = 200)
    private String area;

    @Column(name = "village", length = 100)
    private String village;

    @Column(name = "district", length = 100)
    private String district;
    
    // Reporter information (Agent who reported)
    @NotBlank(message = "Reporter ID is required")
    @Column(name = "reported_by", nullable = false, length = 20)
    private String reportedBy; // Agent ID
    
    @Column(name = "reporter_name", length = 200)
    private String reporterName; // Agent full name
    
    // Resolution information
    @Column(name = "estimated_resolution_date")
    private LocalDate estimatedResolutionDate; // Optional
    
    @Column(name = "actual_resolution_date")
    private LocalDate actualResolutionDate;
    
    @Column(name = "resolved_by", length = 50)
    private String resolvedBy; // Master Admin username who resolved
    
    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;
    
    // Reopen information
    @Column(name = "reopened_count", nullable = false)
    private Integer reopenedCount = 0;
    
    @Column(name = "reopened_by", length = 20)
    private String reopenedBy; // Agent ID who reopened
    
    @Column(name = "reopened_date")
    private LocalDateTime reopenedDate;
    
    @Column(name = "reopen_reason", columnDefinition = "TEXT")
    private String reopenReason;
    
    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToMany(mappedBy = "issue", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IssueAttachment> attachments = new ArrayList<>();
    
    @OneToMany(mappedBy = "issue", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IssueComment> comments = new ArrayList<>();
    
    // Enums
    public enum IssueCategory {
        POLITICAL("Political"),
        SOCIAL("Social"),
        INFRASTRUCTURE("Infrastructure"),
        HEALTH("Health"),
        EDUCATION("Education"),
        ENVIRONMENT("Environment"),
        TRANSPORT("Transport"),
        WATER("Water"),
        ELECTRICITY("Electricity"),
        OTHER("Other");
        
        private final String displayName;
        
        IssueCategory(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum IssuePriority {
        LOW("Low"),
        MEDIUM("Medium"),
        HIGH("High"),
        URGENT("Urgent");
        
        private final String displayName;
        
        IssuePriority(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum IssueStatus {
        OPEN("Open"),
        IN_PROGRESS("In Progress"),
        RESOLVED("Resolved"),
        CLOSED("Closed"),
        REOPENED("Reopened");
        
        private final String displayName;
        
        IssueStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum LocationType {
        GPS("GPS"),
        MANUAL("Manual"),
        PIN("Pin");
        
        private final String displayName;
        
        LocationType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Constructors
    public Issue() {}
    
    public Issue(String title, String description, IssueCategory category, IssuePriority priority, String reportedBy) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.reportedBy = reportedBy;
        this.submissionDate = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public IssueCategory getCategory() { return category; }
    public void setCategory(IssueCategory category) { this.category = category; }
    
    public IssuePriority getPriority() { return priority; }
    public void setPriority(IssuePriority priority) { this.priority = priority; }
    
    public IssueStatus getStatus() { return status; }
    public void setStatus(IssueStatus status) { this.status = status; }
    
    public LocalDateTime getSubmissionDate() { return submissionDate; }
    public void setSubmissionDate(LocalDateTime submissionDate) { this.submissionDate = submissionDate; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getVillage() { return village; }
    public void setVillage(String village) { this.village = village; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    
    public String getReportedBy() { return reportedBy; }
    public void setReportedBy(String reportedBy) { this.reportedBy = reportedBy; }
    
    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }
    
    public LocalDate getEstimatedResolutionDate() { return estimatedResolutionDate; }
    public void setEstimatedResolutionDate(LocalDate estimatedResolutionDate) { this.estimatedResolutionDate = estimatedResolutionDate; }
    
    public LocalDate getActualResolutionDate() { return actualResolutionDate; }
    public void setActualResolutionDate(LocalDate actualResolutionDate) { this.actualResolutionDate = actualResolutionDate; }
    
    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }
    
    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }
    
    public Integer getReopenedCount() { return reopenedCount; }
    public void setReopenedCount(Integer reopenedCount) { this.reopenedCount = reopenedCount; }
    
    public String getReopenedBy() { return reopenedBy; }
    public void setReopenedBy(String reopenedBy) { this.reopenedBy = reopenedBy; }
    
    public LocalDateTime getReopenedDate() { return reopenedDate; }
    public void setReopenedDate(LocalDateTime reopenedDate) { this.reopenedDate = reopenedDate; }
    
    public String getReopenReason() { return reopenReason; }
    public void setReopenReason(String reopenReason) { this.reopenReason = reopenReason; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public List<IssueAttachment> getAttachments() { return attachments; }
    public void setAttachments(List<IssueAttachment> attachments) { this.attachments = attachments; }
    
    public List<IssueComment> getComments() { return comments; }
    public void setComments(List<IssueComment> comments) { this.comments = comments; }
}
