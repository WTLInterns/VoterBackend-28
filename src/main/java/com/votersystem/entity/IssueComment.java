package com.votersystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Issue Comment Entity
 * Represents comments/updates added by Master Admin on issues
 */
@Entity
@Table(name = "issue_comments")
public class IssueComment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Issue is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id", nullable = false)
    private Issue issue;
    
    @NotBlank(message = "Comment is required")
    @Column(name = "comment", nullable = false, columnDefinition = "TEXT")
    private String comment;
    
    @NotBlank(message = "Commented by is required")
    @Column(name = "commented_by", nullable = false, length = 50)
    private String commentedBy; // Master Admin username
    
    @Column(name = "commenter_name", length = 200)
    private String commenterName; // Master Admin full name
    
    @NotNull(message = "Comment type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "comment_type", nullable = false, length = 20)
    private CommentType commentType;
    
    @Column(name = "is_internal", nullable = false)
    private Boolean isInternal = false; // Internal comments not visible to agents
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Comment type enum
    public enum CommentType {
        UPDATE("Update"),
        PROGRESS("Progress"),
        RESOLUTION("Resolution"),
        REOPEN("Reopen"),
        INTERNAL("Internal");
        
        private final String displayName;
        
        CommentType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Constructors
    public IssueComment() {}
    
    public IssueComment(Issue issue, String comment, String commentedBy, CommentType commentType) {
        this.issue = issue;
        this.comment = comment;
        this.commentedBy = commentedBy;
        this.commentType = commentType;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Issue getIssue() { return issue; }
    public void setIssue(Issue issue) { this.issue = issue; }
    
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    
    public String getCommentedBy() { return commentedBy; }
    public void setCommentedBy(String commentedBy) { this.commentedBy = commentedBy; }
    
    public String getCommenterName() { return commenterName; }
    public void setCommenterName(String commenterName) { this.commenterName = commenterName; }
    
    public CommentType getCommentType() { return commentType; }
    public void setCommentType(CommentType commentType) { this.commentType = commentType; }
    
    public Boolean getIsInternal() { return isInternal; }
    public void setIsInternal(Boolean isInternal) { this.isInternal = isInternal; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
