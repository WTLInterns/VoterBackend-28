package com.votersystem.service;

import com.votersystem.entity.Issue;
import com.votersystem.entity.IssueAttachment;
import com.votersystem.repository.IssueAttachmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing issue attachments
 */
@Service
public class IssueAttachmentService {
    
    private static final Logger logger = LoggerFactory.getLogger(IssueAttachmentService.class);
    
    @Autowired
    private IssueAttachmentRepository attachmentRepository;
    
    /**
     * Create a new attachment
     */
    @Transactional
    public IssueAttachment createAttachment(Issue issue, String fileName, String fileUrl, 
                                           IssueAttachment.FileType fileType, String cloudinaryPublicId,
                                           Long fileSize, String mimeType, String uploadedBy) {
        try {
            IssueAttachment attachment = new IssueAttachment();
            attachment.setIssue(issue);
            attachment.setFileName(fileName);
            attachment.setFileUrl(fileUrl);
            attachment.setFileType(fileType);
            attachment.setCloudinaryPublicId(cloudinaryPublicId);
            attachment.setFileSize(fileSize);
            attachment.setMimeType(mimeType);
            attachment.setUploadedBy(uploadedBy);
            
            IssueAttachment savedAttachment = attachmentRepository.save(attachment);
            
            logger.info("Created attachment {} for issue {}", savedAttachment.getId(), issue.getTicketNumber());
            return savedAttachment;
            
        } catch (Exception e) {
            logger.error("Failed to create attachment: {}", e.getMessage());
            throw new RuntimeException("Failed to create attachment", e);
        }
    }
    
    /**
     * Get attachment by ID
     */
    public Optional<IssueAttachment> getAttachmentById(Long attachmentId) {
        return attachmentRepository.findById(attachmentId);
    }
    
    /**
     * Get attachments by issue
     */
    public List<IssueAttachment> getAttachmentsByIssue(Long issueId) {
        return attachmentRepository.findByIssueId(issueId);
    }
    
    /**
     * Get attachments by issue and file type
     */
    public List<IssueAttachment> getAttachmentsByIssueAndType(Long issueId, IssueAttachment.FileType fileType) {
        return attachmentRepository.findByIssueIdAndFileType(issueId, fileType);
    }
    
    /**
     * Get images by issue
     */
    public List<IssueAttachment> getImagesByIssue(Long issueId) {
        return attachmentRepository.findImagesByIssue(issueId);
    }
    
    /**
     * Get videos by issue
     */
    public List<IssueAttachment> getVideosByIssue(Long issueId) {
        return attachmentRepository.findVideosByIssue(issueId);
    }
    
    /**
     * Get attachments by uploader
     */
    public List<IssueAttachment> getAttachmentsByUploader(String uploadedBy) {
        return attachmentRepository.findByUploadedBy(uploadedBy);
    }
    
    /**
     * Count attachments by issue
     */
    public long countAttachmentsByIssue(Long issueId) {
        return attachmentRepository.countByIssueId(issueId);
    }
    
    /**
     * Get total file size by issue
     */
    public Long getTotalFileSizeByIssue(Long issueId) {
        return attachmentRepository.getTotalFileSizeByIssue(issueId);
    }
    
    /**
     * Delete attachment
     */
    @Transactional
    public void deleteAttachment(Long attachmentId) {
        try {
            Optional<IssueAttachment> attachmentOpt = attachmentRepository.findById(attachmentId);
            if (attachmentOpt.isPresent()) {
                attachmentRepository.deleteById(attachmentId);
                logger.info("Deleted attachment {}", attachmentId);
            } else {
                throw new RuntimeException("Attachment not found");
            }
            
        } catch (Exception e) {
            logger.error("Failed to delete attachment: {}", e.getMessage());
            throw new RuntimeException("Failed to delete attachment", e);
        }
    }
    
    /**
     * Delete all attachments for an issue
     */
    @Transactional
    public void deleteAttachmentsByIssue(Long issueId) {
        try {
            List<IssueAttachment> attachments = attachmentRepository.findByIssueId(issueId);
            for (IssueAttachment attachment : attachments) {
                attachmentRepository.delete(attachment);
            }
            
            logger.info("Deleted all attachments for issue {}", issueId);
            
        } catch (Exception e) {
            logger.error("Failed to delete attachments for issue: {}", e.getMessage());
            throw new RuntimeException("Failed to delete attachments", e);
        }
    }
    
    /**
     * Get attachment statistics
     */
    public AttachmentStatistics getAttachmentStatistics() {
        AttachmentStatistics stats = new AttachmentStatistics();
        
        stats.setTotalAttachments(attachmentRepository.count());
        stats.setImageAttachments(attachmentRepository.countByFileType(IssueAttachment.FileType.IMAGE));
        stats.setVideoAttachments(attachmentRepository.countByFileType(IssueAttachment.FileType.VIDEO));
        stats.setDocumentAttachments(attachmentRepository.countByFileType(IssueAttachment.FileType.DOCUMENT));
        
        return stats;
    }
    
    // Statistics class
    public static class AttachmentStatistics {
        private long totalAttachments;
        private long imageAttachments;
        private long videoAttachments;
        private long documentAttachments;
        
        // Getters and setters
        public long getTotalAttachments() { return totalAttachments; }
        public void setTotalAttachments(long totalAttachments) { this.totalAttachments = totalAttachments; }
        
        public long getImageAttachments() { return imageAttachments; }
        public void setImageAttachments(long imageAttachments) { this.imageAttachments = imageAttachments; }
        
        public long getVideoAttachments() { return videoAttachments; }
        public void setVideoAttachments(long videoAttachments) { this.videoAttachments = videoAttachments; }
        
        public long getDocumentAttachments() { return documentAttachments; }
        public void setDocumentAttachments(long documentAttachments) { this.documentAttachments = documentAttachments; }
    }
}
