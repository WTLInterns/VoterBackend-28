package com.votersystem.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.votersystem.entity.Issue;
import com.votersystem.entity.IssueAttachment;
import com.votersystem.entity.IssueComment;

/**
 * DTO for issue response
 */
public class IssueResponse {
    
    private Long id;
    private String ticketNumber;
    private String title;
    private String description;
    private Issue.IssueCategory category;
    private Issue.IssuePriority priority;
    private Issue.IssueStatus status;
    private LocalDateTime submissionDate;
    
    // Location information
    private String address;
    private String area;
    private String village;
    private String district;
    
    // Reporter information
    private String reportedBy;
    private String reporterName;
    private String agentFirstName;
    private String agentLastName;
    private String agentPhone;
    
    // Resolution information
    private LocalDate estimatedResolutionDate;
    private LocalDate actualResolutionDate;
    private String resolvedBy;
    private String resolutionNotes;
    
    // Reopen information
    private Integer reopenedCount;
    private String reopenedBy;
    private LocalDateTime reopenedDate;
    private String reopenReason;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Related data
    private List<AttachmentResponse> attachments;
    private List<CommentResponse> comments;

    // Reopen capability
    private boolean canReopen;
    
    // Constructors
    public IssueResponse() {}
    
    public IssueResponse(Issue issue) {
        this.id = issue.getId();
        this.ticketNumber = issue.getTicketNumber();
        this.title = issue.getTitle();
        this.description = issue.getDescription();
        this.category = issue.getCategory();
        this.priority = issue.getPriority();
        this.status = issue.getStatus();
        this.submissionDate = issue.getSubmissionDate();

        this.address = issue.getAddress();
        this.area = issue.getArea();
        this.village = issue.getVillage();
        this.district = issue.getDistrict();

        this.reportedBy = issue.getReportedBy();
        this.reporterName = issue.getReporterName();
        // Agent details will be null in this constructor - use the other constructor for complete info

        this.estimatedResolutionDate = issue.getEstimatedResolutionDate();
        this.actualResolutionDate = issue.getActualResolutionDate();
        this.resolvedBy = issue.getResolvedBy();
        this.resolutionNotes = issue.getResolutionNotes();

        this.reopenedCount = issue.getReopenedCount();
        this.reopenedBy = issue.getReopenedBy();
        this.reopenedDate = issue.getReopenedDate();
        this.reopenReason = issue.getReopenReason();

        this.createdAt = issue.getCreatedAt();
        this.updatedAt = issue.getUpdatedAt();

        // Set canReopen based on status and reporter
        this.canReopen = canIssueBeReopened(issue);

        // Convert attachments and comments
        if (issue.getAttachments() != null) {
            this.attachments = issue.getAttachments().stream()
                    .map(AttachmentResponse::new)
                    .collect(Collectors.toList());
        }

        if (issue.getComments() != null) {
            this.comments = issue.getComments().stream()
                    .map(CommentResponse::new)
                    .collect(Collectors.toList());
        }
    }

    // Constructor with agent information
    public IssueResponse(Issue issue, com.votersystem.entity.Agent agent) {
        // Call the basic constructor first
        this(issue);

        // Populate agent details if agent is provided
        if (agent != null) {
            this.agentFirstName = agent.getFirstName();
            this.agentLastName = agent.getLastName();
            this.agentPhone = agent.getMobile();
            // Override reporterName with agent's full name
            this.reporterName = agent.getFirstName() + " " + agent.getLastName();
        }
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
    
    public Issue.IssueCategory getCategory() { return category; }
    public void setCategory(Issue.IssueCategory category) { this.category = category; }
    
    public Issue.IssuePriority getPriority() { return priority; }
    public void setPriority(Issue.IssuePriority priority) { this.priority = priority; }
    
    public Issue.IssueStatus getStatus() { return status; }
    public void setStatus(Issue.IssueStatus status) { this.status = status; }
    
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

    public String getAgentFirstName() { return agentFirstName; }
    public void setAgentFirstName(String agentFirstName) { this.agentFirstName = agentFirstName; }

    public String getAgentLastName() { return agentLastName; }
    public void setAgentLastName(String agentLastName) { this.agentLastName = agentLastName; }

    public String getAgentPhone() { return agentPhone; }
    public void setAgentPhone(String agentPhone) { this.agentPhone = agentPhone; }

    // Computed field for agent name
    public String getAgentName() {
        if (agentFirstName != null && agentLastName != null) {
            return agentFirstName + " " + agentLastName;
        } else if (reporterName != null && !reporterName.trim().isEmpty()) {
            return reporterName;
        } else {
            return reportedBy; // Fallback to agent ID
        }
    }

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
    
    public List<AttachmentResponse> getAttachments() { return attachments; }
    public void setAttachments(List<AttachmentResponse> attachments) { this.attachments = attachments; }
    
    public List<CommentResponse> getComments() { return comments; }
    public void setComments(List<CommentResponse> comments) { this.comments = comments; }

    public boolean isCanReopen() { return canReopen; }
    public void setCanReopen(boolean canReopen) { this.canReopen = canReopen; }

    /**
     * Helper method to determine if an issue can be reopened
     */
    private boolean canIssueBeReopened(Issue issue) {
        // Issue can be reopened if it's RESOLVED or CLOSED
        return issue.getStatus() == Issue.IssueStatus.RESOLVED ||
               issue.getStatus() == Issue.IssueStatus.CLOSED;
    }

    // Nested classes for related data
    public static class AttachmentResponse {
        private Long id;
        private String fileName;
        private String fileUrl;
        private IssueAttachment.FileType fileType;
        private Long fileSize;
        private String uploadedBy;
        private LocalDateTime uploadedAt;
        
        public AttachmentResponse() {}
        
        public AttachmentResponse(IssueAttachment attachment) {
            this.id = attachment.getId();
            this.fileName = attachment.getFileName();
            this.fileUrl = attachment.getFileUrl();
            this.fileType = attachment.getFileType();
            this.fileSize = attachment.getFileSize();
            this.uploadedBy = attachment.getUploadedBy();
            this.uploadedAt = attachment.getUploadedAt();
        }
        
        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        
        public String getFileUrl() { return fileUrl; }
        public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
        
        public IssueAttachment.FileType getFileType() { return fileType; }
        public void setFileType(IssueAttachment.FileType fileType) { this.fileType = fileType; }
        
        public Long getFileSize() { return fileSize; }
        public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
        
        public String getUploadedBy() { return uploadedBy; }
        public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }
        
        public LocalDateTime getUploadedAt() { return uploadedAt; }
        public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    }
    
    public static class CommentResponse {
        private Long id;
        private String comment;
        private String commentedBy;
        private String commenterName;
        private IssueComment.CommentType commentType;
        private Boolean isInternal;
        private LocalDateTime createdAt;
        
        public CommentResponse() {}
        
        public CommentResponse(IssueComment comment) {
            this.id = comment.getId();
            this.comment = comment.getComment();
            this.commentedBy = comment.getCommentedBy();
            this.commenterName = comment.getCommenterName();
            this.commentType = comment.getCommentType();
            this.isInternal = comment.getIsInternal();
            this.createdAt = comment.getCreatedAt();
        }
        
        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
        
        public String getCommentedBy() { return commentedBy; }
        public void setCommentedBy(String commentedBy) { this.commentedBy = commentedBy; }
        
        public String getCommenterName() { return commenterName; }
        public void setCommenterName(String commenterName) { this.commenterName = commenterName; }
        
        public IssueComment.CommentType getCommentType() { return commentType; }
        public void setCommentType(IssueComment.CommentType commentType) { this.commentType = commentType; }
        
        public Boolean getIsInternal() { return isInternal; }
        public void setIsInternal(Boolean isInternal) { this.isInternal = isInternal; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
}
