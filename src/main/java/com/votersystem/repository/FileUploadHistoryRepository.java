package com.votersystem.repository;

import com.votersystem.entity.FileUploadHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FileUploadHistoryRepository extends JpaRepository<FileUploadHistory, Long> {
    
    // Find by uploaded by user
    List<FileUploadHistory> findByUploadedBy(String uploadedBy);
    
    // Find by uploaded by user with pagination
    Page<FileUploadHistory> findByUploadedBy(String uploadedBy, Pageable pageable);
    
    // Find by status
    List<FileUploadHistory> findByStatus(FileUploadHistory.UploadStatus status);
    
    // Find by file type
    List<FileUploadHistory> findByFileType(String fileType);
    
    // Find uploads between dates
    List<FileUploadHistory> findByUploadTimeBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find recent uploads (last 30 days)
    @Query("SELECT f FROM FileUploadHistory f WHERE f.uploadTime >= :thirtyDaysAgo ORDER BY f.uploadTime DESC")
    List<FileUploadHistory> findRecentUploads(@Param("thirtyDaysAgo") LocalDateTime thirtyDaysAgo);
    
    // Find all uploads ordered by upload time descending
    List<FileUploadHistory> findAllByOrderByUploadTimeDesc();
    
    // Find all uploads with pagination ordered by upload time descending
    Page<FileUploadHistory> findAllByOrderByUploadTimeDesc(Pageable pageable);
    
    // Count total uploads
    @Query("SELECT COUNT(f) FROM FileUploadHistory f")
    Long countTotalUploads();
    
    // Count successful uploads
    @Query("SELECT COUNT(f) FROM FileUploadHistory f WHERE f.status = 'SUCCESS'")
    Long countSuccessfulUploads();
    
    // Count failed uploads
    @Query("SELECT COUNT(f) FROM FileUploadHistory f WHERE f.status = 'FAILED'")
    Long countFailedUploads();
    
    // Get total records processed
    @Query("SELECT COALESCE(SUM(f.totalRecords), 0) FROM FileUploadHistory f")
    Long getTotalRecordsProcessed();
    
    // Get successful records processed
    @Query("SELECT COALESCE(SUM(f.successfulRecords), 0) FROM FileUploadHistory f")
    Long getSuccessfulRecordsProcessed();
}
