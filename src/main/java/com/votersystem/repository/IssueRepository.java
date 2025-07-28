package com.votersystem.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.votersystem.entity.Issue;

/**
 * Repository interface for Issue entity
 */
@Repository
public interface IssueRepository extends JpaRepository<Issue, Long> {
    
    // Find by ticket number
    Optional<Issue> findByTicketNumber(String ticketNumber);
    
    // Find by reporter (agent)
    Page<Issue> findByReportedBy(String reportedBy, Pageable pageable);
    List<Issue> findByReportedBy(String reportedBy);
    List<Issue> findByReportedByOrderByCreatedAtDesc(String reportedBy);

    // Find updated issues for polling
    List<Issue> findByReportedByAndUpdatedAtAfterOrderByUpdatedAtDesc(String reportedBy, LocalDateTime updatedAt);
    
    // Find by status
    Page<Issue> findByStatus(Issue.IssueStatus status, Pageable pageable);
    List<Issue> findByStatus(Issue.IssueStatus status);
    
    // Find by category
    Page<Issue> findByCategory(Issue.IssueCategory category, Pageable pageable);
    
    // Find by priority
    Page<Issue> findByPriority(Issue.IssuePriority priority, Pageable pageable);
    
    // Find by village
    Page<Issue> findByVillage(String village, Pageable pageable);

    // Find by district
    Page<Issue> findByDistrict(String district, Pageable pageable);
    
    // Find by date range
    Page<Issue> findBySubmissionDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    // Search by title or description
    @Query("SELECT i FROM Issue i WHERE " +
           "LOWER(i.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(i.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(i.ticketNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Issue> searchByTitleOrDescriptionOrTicket(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    // Find issues by reporter and status
    Page<Issue> findByReportedByAndStatus(String reportedBy, Issue.IssueStatus status, Pageable pageable);
    
    // Find issues that can be reopened (resolved or closed)
    @Query("SELECT i FROM Issue i WHERE i.reportedBy = :reportedBy AND " +
           "(i.status = 'RESOLVED' OR i.status = 'CLOSED')")
    List<Issue> findReopenableIssuesByReporter(@Param("reportedBy") String reportedBy);
    
    // Count issues by status
    long countByStatus(Issue.IssueStatus status);
    
    // Count issues by category
    long countByCategory(Issue.IssueCategory category);
    
    // Count issues by priority
    long countByPriority(Issue.IssuePriority priority);
    
    // Count issues by reporter
    long countByReportedBy(String reportedBy);
    
    // Find overdue issues (estimated resolution date passed)
    @Query("SELECT i FROM Issue i WHERE i.estimatedResolutionDate < CURRENT_DATE AND " +
           "(i.status = 'OPEN' OR i.status = 'IN_PROGRESS' OR i.status = 'REOPENED')")
    List<Issue> findOverdueIssues();
    
    // Find issues created today
    @Query("SELECT i FROM Issue i WHERE DATE(i.submissionDate) = CURRENT_DATE")
    List<Issue> findIssuesToday();
    
    // Find issues created this week
    @Query("SELECT i FROM Issue i WHERE i.submissionDate >= :weekStart")
    List<Issue> findIssuesThisWeek(@Param("weekStart") LocalDateTime weekStart);
    
    // Find issues created this month
    @Query("SELECT i FROM Issue i WHERE YEAR(i.submissionDate) = YEAR(CURRENT_DATE) AND " +
           "MONTH(i.submissionDate) = MONTH(CURRENT_DATE)")
    List<Issue> findIssuesThisMonth();
    
    // Get next sequence number for ticket generation
    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(i.ticketNumber, 10) AS int)), 0) + 1 " +
           "FROM Issue i WHERE i.ticketNumber LIKE CONCAT('ISS-', :year, '-%')")
    Integer getNextSequenceNumber(@Param("year") String year);
    
    // Statistics queries
    @Query("SELECT i.status, COUNT(i) FROM Issue i GROUP BY i.status")
    List<Object[]> getIssueCountByStatus();
    
    @Query("SELECT i.category, COUNT(i) FROM Issue i GROUP BY i.category")
    List<Object[]> getIssueCountByCategory();
    
    @Query("SELECT i.priority, COUNT(i) FROM Issue i GROUP BY i.priority")
    List<Object[]> getIssueCountByPriority();
    
    // Find issues with address
    @Query("SELECT i FROM Issue i WHERE i.address IS NOT NULL AND i.address != ''")
    List<Issue> findIssuesWithAddress();
    
    // Advanced search with multiple filters
    @Query("SELECT i FROM Issue i WHERE " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(:category IS NULL OR i.category = :category) AND " +
           "(:priority IS NULL OR i.priority = :priority) AND " +
           "(:reportedBy IS NULL OR i.reportedBy = :reportedBy) AND " +
           "(:village IS NULL OR i.village = :village) AND " +
           "(:district IS NULL OR i.district = :district) AND " +
           "(:searchTerm IS NULL OR " +
           " LOWER(i.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(i.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(i.ticketNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Issue> findIssuesWithFilters(
            @Param("status") Issue.IssueStatus status,
            @Param("category") Issue.IssueCategory category,
            @Param("priority") Issue.IssuePriority priority,
            @Param("reportedBy") String reportedBy,
            @Param("village") String village,
            @Param("district") String district,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);

    // Advanced search with multiple filters including date range
    @Query("SELECT i FROM Issue i WHERE " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(:category IS NULL OR i.category = :category) AND " +
           "(:priority IS NULL OR i.priority = :priority) AND " +
           "(:reportedBy IS NULL OR i.reportedBy = :reportedBy) AND " +
           "(:village IS NULL OR i.village = :village) AND " +
           "(:district IS NULL OR i.district = :district) AND " +
           "(:fromDate IS NULL OR i.submissionDate >= :fromDate) AND " +
           "(:toDate IS NULL OR i.submissionDate <= :toDate) AND " +
           "(:searchTerm IS NULL OR " +
           " LOWER(i.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(i.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(i.ticketNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Issue> findIssuesWithFiltersAndDateRange(
            @Param("status") Issue.IssueStatus status,
            @Param("category") Issue.IssueCategory category,
            @Param("priority") Issue.IssuePriority priority,
            @Param("reportedBy") String reportedBy,
            @Param("village") String village,
            @Param("district") String district,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);
}
