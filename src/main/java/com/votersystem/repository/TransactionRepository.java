package com.votersystem.repository;

import com.votersystem.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
    
    // Find by user ID
    List<Transaction> findByUserId(Long userId);
    
    // Find by agent ID
    List<Transaction> findByAgentId(String agentId);
    
    // Find by status
    List<Transaction> findByStatus(Transaction.TransactionStatus status);
    
    // Find transactions between dates
    List<Transaction> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find transactions by agent and date range
    List<Transaction> findByAgentIdAndCreatedAtBetween(String agentId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Find today's transactions
    @Query("SELECT t FROM Transaction t WHERE DATE(t.createdAt) = CURRENT_DATE")
    List<Transaction> findTodaysTransactions();
    
    // Find today's transactions by agent
    @Query("SELECT t FROM Transaction t WHERE t.agentId = :agentId AND DATE(t.createdAt) = CURRENT_DATE")
    List<Transaction> findTodaysTransactionsByAgent(@Param("agentId") String agentId);
    
    // Count total transactions
    @Query("SELECT COUNT(t) FROM Transaction t")
    Long countTotalTransactions();
    
    // Count transactions by status
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.status = :status")
    Long countByStatus(@Param("status") Transaction.TransactionStatus status);
    
    // Count today's transactions
    @Query("SELECT COUNT(t) FROM Transaction t WHERE DATE(t.createdAt) = CURRENT_DATE")
    Long countTodaysTransactions();
    
    // Count today's transactions by agent
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.agentId = :agentId AND DATE(t.createdAt) = CURRENT_DATE")
    Long countTodaysTransactionsByAgent(@Param("agentId") String agentId);
    
    // Get total amount
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.status = 'COMPLETED'")
    BigDecimal getTotalAmount();
    
    // Get total amount by agent
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.agentId = :agentId AND t.status = 'COMPLETED'")
    BigDecimal getTotalAmountByAgent(@Param("agentId") String agentId);
    
    // Get today's total amount
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.status = 'COMPLETED' AND DATE(t.createdAt) = CURRENT_DATE")
    BigDecimal getTodaysTotalAmount();
    
    // Get today's total amount by agent
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.agentId = :agentId AND t.status = 'COMPLETED' AND DATE(t.createdAt) = CURRENT_DATE")
    BigDecimal getTodaysTotalAmountByAgent(@Param("agentId") String agentId);
    
    // Get recent transactions with pagination
    Page<Transaction> findByOrderByCreatedAtDesc(Pageable pageable);

    // Get recent transactions with user and agent data
    @Query("SELECT t FROM Transaction t LEFT JOIN FETCH t.user LEFT JOIN FETCH t.agent ORDER BY t.createdAt DESC")
    Page<Transaction> findAllWithUserAndAgentOrderByCreatedAtDesc(Pageable pageable);
    
    // Get agent's recent transactions
    Page<Transaction> findByAgentIdOrderByCreatedAtDesc(String agentId, Pageable pageable);
    
    // Monthly transaction statistics
    @Query("SELECT YEAR(t.createdAt), MONTH(t.createdAt), COUNT(t), SUM(t.amount) " +
           "FROM Transaction t WHERE t.status = 'COMPLETED' " +
           "GROUP BY YEAR(t.createdAt), MONTH(t.createdAt) " +
           "ORDER BY YEAR(t.createdAt) DESC, MONTH(t.createdAt) DESC")
    List<Object[]> getMonthlyStatistics();
    
    // Daily transaction statistics for current month
    @Query("SELECT DAY(t.createdAt), COUNT(t), SUM(t.amount) " +
           "FROM Transaction t WHERE t.status = 'COMPLETED' " +
           "AND YEAR(t.createdAt) = YEAR(CURRENT_DATE) " +
           "AND MONTH(t.createdAt) = MONTH(CURRENT_DATE) " +
           "GROUP BY DAY(t.createdAt) " +
           "ORDER BY DAY(t.createdAt)")
    List<Object[]> getDailyStatisticsForCurrentMonth();
}
