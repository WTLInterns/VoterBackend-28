package com.votersystem.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_upload_history")
public class FileUploadHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "filename", nullable = false)
    private String filename;
    
    @Column(name = "file_type", nullable = false)
    private String fileType;
    
    @Column(name = "file_size", nullable = false)
    private Long fileSize;
    
    @Column(name = "uploaded_by", nullable = false)
    private String uploadedBy;
    
    @Column(name = "total_records", nullable = false)
    private Integer totalRecords;
    
    @Column(name = "successful_records", nullable = false)
    private Integer successfulRecords;
    
    @Column(name = "failed_records", nullable = false)
    private Integer failedRecords;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UploadStatus status;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @CreationTimestamp
    @Column(name = "upload_time", nullable = false, updatable = false)
    private LocalDateTime uploadTime;
    
    // Constructors
    public FileUploadHistory() {}
    
    public FileUploadHistory(String filename, String fileType, Long fileSize, String uploadedBy,
                           Integer totalRecords, Integer successfulRecords, Integer failedRecords,
                           UploadStatus status, String errorMessage) {
        this.filename = filename;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.uploadedBy = uploadedBy;
        this.totalRecords = totalRecords;
        this.successfulRecords = successfulRecords;
        this.failedRecords = failedRecords;
        this.status = status;
        this.errorMessage = errorMessage;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getFilename() {
        return filename;
    }
    
    public void setFilename(String filename) {
        this.filename = filename;
    }
    
    public String getFileType() {
        return fileType;
    }
    
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    
    public Long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    
    public String getUploadedBy() {
        return uploadedBy;
    }
    
    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }
    
    public Integer getTotalRecords() {
        return totalRecords;
    }
    
    public void setTotalRecords(Integer totalRecords) {
        this.totalRecords = totalRecords;
    }
    
    public Integer getSuccessfulRecords() {
        return successfulRecords;
    }
    
    public void setSuccessfulRecords(Integer successfulRecords) {
        this.successfulRecords = successfulRecords;
    }
    
    public Integer getFailedRecords() {
        return failedRecords;
    }
    
    public void setFailedRecords(Integer failedRecords) {
        this.failedRecords = failedRecords;
    }
    
    public UploadStatus getStatus() {
        return status;
    }
    
    public void setStatus(UploadStatus status) {
        this.status = status;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public LocalDateTime getUploadTime() {
        return uploadTime;
    }
    
    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }
    
    // Enum for Upload Status
    public enum UploadStatus {
        SUCCESS, PARTIAL_SUCCESS, FAILED
    }
}
