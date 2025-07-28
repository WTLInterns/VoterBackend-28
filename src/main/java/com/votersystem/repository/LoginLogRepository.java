package com.votersystem.repository;

import com.votersystem.entity.LoginLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoginLogRepository extends JpaRepository<LoginLog, Long> {
    
    // Find by username
    List<LoginLog> findByUsername(String username);
    
    // Find by user type
    List<LoginLog> findByUserType(LoginLog.UserType userType);
    
    // Find by status
    List<LoginLog> findByStatus(LoginLog.LoginStatus status);
    
    // Find failed login attempts
    @Query("SELECT l FROM LoginLog l WHERE l.status = 'FAILED'")
    List<LoginLog> findFailedLoginAttempts();
    
    // Find successful login attempts
    @Query("SELECT l FROM LoginLog l WHERE l.status = 'SUCCESS'")
    List<LoginLog> findSuccessfulLoginAttempts();
    
    // Find login attempts by IP address
    List<LoginLog> findByIpAddress(String ipAddress);
    
    // Find login attempts between dates
    List<LoginLog> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find recent login attempts with pagination
    Page<LoginLog> findByOrderByTimestampDesc(Pageable pageable);
    
    // Find failed attempts for specific user in time range
    @Query("SELECT l FROM LoginLog l WHERE l.username = :username AND l.status = 'FAILED' AND l.timestamp >= :since")
    List<LoginLog> findFailedAttemptsForUserSince(@Param("username") String username, @Param("since") LocalDateTime since);
    
    // Count failed attempts for IP in time range
    @Query("SELECT COUNT(l) FROM LoginLog l WHERE l.ipAddress = :ipAddress AND l.status = 'FAILED' AND l.timestamp >= :since")
    Long countFailedAttemptsForIpSince(@Param("ipAddress") String ipAddress, @Param("since") LocalDateTime since);
    
    // Get login statistics
    @Query("SELECT l.status, COUNT(l) FROM LoginLog l GROUP BY l.status")
    List<Object[]> getLoginStatistics();
    
    // Get daily login statistics
    @Query("SELECT DATE(l.timestamp), l.status, COUNT(l) FROM LoginLog l " +
           "WHERE l.timestamp >= :startDate " +
           "GROUP BY DATE(l.timestamp), l.status " +
           "ORDER BY DATE(l.timestamp) DESC")
    List<Object[]> getDailyLoginStatistics(@Param("startDate") LocalDateTime startDate);
}
