package com.votersystem.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.votersystem.entity.Administrator;
import com.votersystem.entity.Issue;
import com.votersystem.entity.IssueComment;
import com.votersystem.repository.AdministratorRepository;
import com.votersystem.repository.IssueCommentRepository;
import com.votersystem.repository.IssueRepository;

/**
 * Service for managing issue comments
 */
@Service
public class IssueCommentService {
    
    private static final Logger logger = LoggerFactory.getLogger(IssueCommentService.class);
    
    @Autowired
    private IssueCommentRepository commentRepository;
    
    @Autowired
    private IssueRepository issueRepository;
    
    @Autowired
    private AdministratorRepository administratorRepository;
    
    /**
     * Add comment to an issue
     */
    @Transactional
    public IssueComment addComment(Long issueId, String commentText, String commentedBy, 
                                  IssueComment.CommentType commentType, Boolean isInternal) {
        try {
            // Validate issue exists
            Optional<Issue> issueOpt = issueRepository.findById(issueId);
            if (issueOpt.isEmpty()) {
                throw new RuntimeException("Issue not found");
            }
            
            Issue issue = issueOpt.get();
            
            // Get commenter name
            String commenterName = getCommenterName(commentedBy);
            
            // Create comment
            IssueComment comment = new IssueComment();
            comment.setIssue(issue);
            comment.setComment(commentText);
            comment.setCommentedBy(commentedBy);
            comment.setCommenterName(commenterName);
            comment.setCommentType(commentType);
            comment.setIsInternal(isInternal != null ? isInternal : false);
            
            IssueComment savedComment = commentRepository.save(comment);
            
            logger.info("Added comment to issue {} by {}", issue.getTicketNumber(), commentedBy);
            return savedComment;
            
        } catch (Exception e) {
            logger.error("Failed to add comment: {}", e.getMessage());
            throw new RuntimeException("Failed to add comment", e);
        }
    }
    
    /**
     * Get comments for an issue
     */
    public List<IssueComment> getCommentsByIssue(Long issueId) {
        return commentRepository.findByIssueIdOrderByCreatedAtAsc(issueId);
    }
    
    /**
     * Get public comments for an issue (excluding internal comments)
     */
    public List<IssueComment> getPublicCommentsByIssue(Long issueId) {
        return commentRepository.findPublicCommentsByIssue(issueId);
    }
    
    /**
     * Get internal comments for an issue
     */
    public List<IssueComment> getInternalCommentsByIssue(Long issueId) {
        return commentRepository.findInternalCommentsByIssue(issueId);
    }
    
    /**
     * Get comments by commenter
     */
    public List<IssueComment> getCommentsByCommenter(String commentedBy) {
        return commentRepository.findByCommentedByOrderByCreatedAtDesc(commentedBy);
    }
    
    /**
     * Get latest comment for an issue
     */
    public IssueComment getLatestComment(Long issueId) {
        return commentRepository.findLatestCommentByIssue(issueId);
    }
    
    /**
     * Count comments for an issue
     */
    public long countCommentsByIssue(Long issueId) {
        return commentRepository.countByIssueId(issueId);
    }
    
    /**
     * Count public comments for an issue
     */
    public long countPublicCommentsByIssue(Long issueId) {
        return commentRepository.countPublicCommentsByIssue(issueId);
    }
    
    /**
     * Add system comment for status changes
     */
    @Transactional
    public IssueComment addSystemComment(Long issueId, String commentText, String updatedBy) {
        try {
            Optional<Issue> issueOpt = issueRepository.findById(issueId);
            if (issueOpt.isEmpty()) {
                throw new RuntimeException("Issue not found");
            }
            
            Issue issue = issueOpt.get();
            
            IssueComment comment = new IssueComment();
            comment.setIssue(issue);
            comment.setComment(commentText);
            comment.setCommentedBy(updatedBy);
            comment.setCommenterName(getCommenterName(updatedBy));
            comment.setCommentType(IssueComment.CommentType.UPDATE);
            comment.setIsInternal(false);
            
            IssueComment savedComment = commentRepository.save(comment);
            
            logger.info("Added system comment to issue {}: {}", issue.getTicketNumber(), commentText);
            return savedComment;
            
        } catch (Exception e) {
            logger.error("Failed to add system comment: {}", e.getMessage());
            throw new RuntimeException("Failed to add system comment", e);
        }
    }
    
    /**
     * Add resolution comment
     */
    @Transactional
    public IssueComment addResolutionComment(Long issueId, String resolutionNotes, String resolvedBy) {
        try {
            Optional<Issue> issueOpt = issueRepository.findById(issueId);
            if (issueOpt.isEmpty()) {
                throw new RuntimeException("Issue not found");
            }
            
            Issue issue = issueOpt.get();
            
            IssueComment comment = new IssueComment();
            comment.setIssue(issue);
            comment.setComment("Issue resolved. " + (resolutionNotes != null ? "Notes: " + resolutionNotes : ""));
            comment.setCommentedBy(resolvedBy);
            comment.setCommenterName(getCommenterName(resolvedBy));
            comment.setCommentType(IssueComment.CommentType.RESOLUTION);
            comment.setIsInternal(false);
            
            IssueComment savedComment = commentRepository.save(comment);
            
            logger.info("Added resolution comment to issue {}", issue.getTicketNumber());
            return savedComment;
            
        } catch (Exception e) {
            logger.error("Failed to add resolution comment: {}", e.getMessage());
            throw new RuntimeException("Failed to add resolution comment", e);
        }
    }
    
    /**
     * Get commenter name from username
     */
    private String getCommenterName(String username) {
        try {
            // Try to get from Administrator table
            Optional<Administrator> adminOpt = administratorRepository.findByUsername(username);
            if (adminOpt.isPresent()) {
                Administrator admin = adminOpt.get();
                return admin.getFirstName() + " " + admin.getLastName();
            }
            
            // If not found, return username
            return username;
            
        } catch (Exception e) {
            logger.warn("Failed to get commenter name for {}: {}", username, e.getMessage());
            return username;
        }
    }
}
