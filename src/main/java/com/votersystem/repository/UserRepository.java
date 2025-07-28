package com.votersystem.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.votersystem.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Find by age range
    List<User> findByAgeBetween(Integer minAge, Integer maxAge);

    // Find by gender
    List<User> findByGender(User.Gender gender);
    
    // Find by last name (case-insensitive)
    List<User> findByLastNameContainingIgnoreCase(String lastName);
    
    // Find by payment status
    List<User> findByPaid(Boolean paid);
    
    // Find by payment status with pagination
    Page<User> findByPaid(Boolean paid, Pageable pageable);
    
    // Find users paid by specific agent
    List<User> findByPaidBy(String agentUsername);

    // Find users created by any of the specified agents (for sub-admin filtering)
    Page<User> findByCreatedByIn(List<String> agentUsernames, Pageable pageable);

    // Find users created by specific admin (for agent mobile app)
    List<User> findByCreatedBy(String createdBy);
    
    // Find users paid between dates
    List<User> findByPaidDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Count total users
    @Query("SELECT COUNT(u) FROM User u")
    Long countTotalUsers();
    
    // Count paid users
    @Query("SELECT COUNT(u) FROM User u WHERE u.paid = true")
    Long countPaidUsers();
    
    // Count unpaid users
    @Query("SELECT COUNT(u) FROM User u WHERE u.paid = false")
    Long countUnpaidUsers();
    
    // Get total amount collected
    @Query("SELECT COALESCE(SUM(u.amount), 0) FROM User u WHERE u.paid = true")
    Double getTotalAmountCollected();
    
    // Get amount collected by specific agent
    @Query("SELECT COALESCE(SUM(u.amount), 0) FROM User u WHERE u.paid = true AND u.paidBy = :agentUsername")
    Double getAmountCollectedByAgent(@Param("agentUsername") String agentUsername);
    
    // Get users paid today
    @Query("SELECT u FROM User u WHERE u.paid = true AND DATE(u.paidDate) = CURRENT_DATE")
    List<User> getUsersPaidToday();
    
    // Count users paid today
    @Query("SELECT COUNT(u) FROM User u WHERE u.paid = true AND DATE(u.paidDate) = CURRENT_DATE")
    Long countUsersPaidToday();
    
    // Get users paid today by specific agent
    @Query("SELECT u FROM User u WHERE u.paid = true AND u.paidBy = :agentUsername AND DATE(u.paidDate) = CURRENT_DATE")
    List<User> getUsersPaidTodayByAgent(@Param("agentUsername") String agentUsername);
    
    // Count users paid today by specific agent
    @Query("SELECT COUNT(u) FROM User u WHERE u.paid = true AND u.paidBy = :agentUsername AND DATE(u.paidDate) = CURRENT_DATE")
    Long countUsersPaidTodayByAgent(@Param("agentUsername") String agentUsername);
    
    // Search users by multiple criteria
    @Query("SELECT u FROM User u WHERE " +
           "(:firstName IS NULL OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))) AND " +
           "(:lastName IS NULL OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))) AND " +
           "(:age IS NULL OR u.age = :age) AND " +
           "(:gender IS NULL OR u.gender = :gender) AND " +
           "(:vidhansabhaNo IS NULL OR u.vidhansabhaNo LIKE CONCAT('%', :vidhansabhaNo, '%')) AND " +
           "(:vibhaghKramank IS NULL OR u.vibhaghKramank LIKE CONCAT('%', :vibhaghKramank, '%')) AND " +
           "(:paid IS NULL OR u.paid = :paid)")
    Page<User> searchUsers(@Param("firstName") String firstName,
                          @Param("lastName") String lastName,
                          @Param("age") Integer age,
                          @Param("gender") User.Gender gender,
                          @Param("vidhansabhaNo") String vidhansabhaNo,
                          @Param("vibhaghKramank") String vibhaghKramank,
                          @Param("paid") Boolean paid,
                          Pageable pageable);
    
    // Search users by multiple criteria with created_by filter
    @Query("SELECT u FROM User u WHERE " +
           "(:firstName IS NULL OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))) AND " +
           "(:lastName IS NULL OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))) AND " +
           "(:age IS NULL OR u.age = :age) AND " +
           "(:gender IS NULL OR u.gender = :gender) AND " +
           "(:vidhansabhaNo IS NULL OR u.vidhansabhaNo LIKE CONCAT('%', :vidhansabhaNo, '%')) AND " +
           "(:vibhaghKramank IS NULL OR u.vibhaghKramank LIKE CONCAT('%', :vibhaghKramank, '%')) AND " +
           "(:paid IS NULL OR u.paid = :paid) AND " +
           "u.createdBy IN :agentUsernames")
    Page<User> searchUsersFilteredByCreatedBy(@Param("firstName") String firstName,
                                            @Param("lastName") String lastName,
                                            @Param("age") Integer age,
                                            @Param("gender") User.Gender gender,
                                            @Param("vidhansabhaNo") String vidhansabhaNo,
                                            @Param("vibhaghKramank") String vibhaghKramank,
                                            @Param("paid") Boolean paid,
                                            @Param("agentUsernames") List<String> agentUsernames,
                                            Pageable pageable);

    // Get recent users (last 30 days)
    @Query("SELECT u FROM User u WHERE u.createdAt >= :thirtyDaysAgo ORDER BY u.createdAt DESC")
    List<User> getRecentUsers(@Param("thirtyDaysAgo") LocalDateTime thirtyDaysAgo);
}
