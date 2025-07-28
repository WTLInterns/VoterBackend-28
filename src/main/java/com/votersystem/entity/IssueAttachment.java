package com.votersystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Issue Attachment Entity
 * Represents media files (images/videos) attached to issues
 */
@Entity
@Table(name = "issue_attachments")
public class IssueAttachment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Issue is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id", nullable = false)
    private Issue issue;
    
    @NotBlank(message = "File name is required")
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;
    
    @NotBlank(message = "File URL is required")
    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;
    
    @Column(name = "cloudinary_public_id", length = 255)
    private String cloudinaryPublicId;
    
    @NotNull(message = "File type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false, length = 20)
    private FileType fileType;
    
    @Column(name = "file_size")
    private Long fileSize; // in bytes
    
    @Column(name = "mime_type", length = 100)
    private String mimeType;
    
    @Column(name = "uploaded_by", length = 20)
    private String uploadedBy; // Agent ID who uploaded
    
    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;
    
    // File type enum
    public enum FileType {
        IMAGE("Image"),
        VIDEO("Video"),
        DOCUMENT("Document");
        
        private final String displayName;
        
        FileType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Constructors
    public IssueAttachment() {}
    
    public IssueAttachment(Issue issue, String fileName, String fileUrl, FileType fileType, String uploadedBy) {
        this.issue = issue;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.fileType = fileType;
        this.uploadedBy = uploadedBy;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Issue getIssue() { return issue; }
    public void setIssue(Issue issue) { this.issue = issue; }
    
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    
    public String getCloudinaryPublicId() { return cloudinaryPublicId; }
    public void setCloudinaryPublicId(String cloudinaryPublicId) { this.cloudinaryPublicId = cloudinaryPublicId; }
    
    public FileType getFileType() { return fileType; }
    public void setFileType(FileType fileType) { this.fileType = fileType; }
    
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    
    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }
    
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
