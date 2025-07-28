package com.votersystem.repository;

import com.votersystem.entity.IssueAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for IssueAttachment entity
 */
@Repository
public interface IssueAttachmentRepository extends JpaRepository<IssueAttachment, Long> {
    
    // Find attachments by issue ID
    List<IssueAttachment> findByIssueId(Long issueId);
    
    // Find attachments by issue ID and file type
    List<IssueAttachment> findByIssueIdAndFileType(Long issueId, IssueAttachment.FileType fileType);
    
    // Find attachments by uploaded by (agent)
    List<IssueAttachment> findByUploadedBy(String uploadedBy);
    
    // Find attachments by Cloudinary public ID
    List<IssueAttachment> findByCloudinaryPublicId(String cloudinaryPublicId);
    
    // Count attachments by issue
    long countByIssueId(Long issueId);
    
    // Count attachments by file type
    long countByFileType(IssueAttachment.FileType fileType);
    
    // Get total file size by issue
    @Query("SELECT COALESCE(SUM(a.fileSize), 0) FROM IssueAttachment a WHERE a.issue.id = :issueId")
    Long getTotalFileSizeByIssue(@Param("issueId") Long issueId);
    
    // Find images by issue
    @Query("SELECT a FROM IssueAttachment a WHERE a.issue.id = :issueId AND a.fileType = 'IMAGE'")
    List<IssueAttachment> findImagesByIssue(@Param("issueId") Long issueId);
    
    // Find videos by issue
    @Query("SELECT a FROM IssueAttachment a WHERE a.issue.id = :issueId AND a.fileType = 'VIDEO'")
    List<IssueAttachment> findVideosByIssue(@Param("issueId") Long issueId);
}
