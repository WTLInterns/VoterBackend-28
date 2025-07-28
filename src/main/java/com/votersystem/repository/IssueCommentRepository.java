package com.votersystem.repository;

import com.votersystem.entity.IssueComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for IssueComment entity
 */
@Repository
public interface IssueCommentRepository extends JpaRepository<IssueComment, Long> {
    
    // Find comments by issue ID
    List<IssueComment> findByIssueIdOrderByCreatedAtAsc(Long issueId);
    
    // Find comments by issue ID excluding internal comments
    @Query("SELECT c FROM IssueComment c WHERE c.issue.id = :issueId AND c.isInternal = false ORDER BY c.createdAt ASC")
    List<IssueComment> findPublicCommentsByIssue(@Param("issueId") Long issueId);
    
    // Find internal comments by issue ID
    @Query("SELECT c FROM IssueComment c WHERE c.issue.id = :issueId AND c.isInternal = true ORDER BY c.createdAt ASC")
    List<IssueComment> findInternalCommentsByIssue(@Param("issueId") Long issueId);
    
    // Find comments by commenter
    List<IssueComment> findByCommentedByOrderByCreatedAtDesc(String commentedBy);
    
    // Find comments by comment type
    List<IssueComment> findByCommentType(IssueComment.CommentType commentType);
    
    // Count comments by issue
    long countByIssueId(Long issueId);
    
    // Count public comments by issue
    @Query("SELECT COUNT(c) FROM IssueComment c WHERE c.issue.id = :issueId AND c.isInternal = false")
    long countPublicCommentsByIssue(@Param("issueId") Long issueId);
    
    // Find latest comment by issue
    @Query("SELECT c FROM IssueComment c WHERE c.issue.id = :issueId ORDER BY c.createdAt DESC LIMIT 1")
    IssueComment findLatestCommentByIssue(@Param("issueId") Long issueId);
}
