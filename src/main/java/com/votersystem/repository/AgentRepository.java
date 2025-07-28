package com.votersystem.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.votersystem.entity.Agent;

@Repository
public interface AgentRepository extends JpaRepository<Agent, String> {
    
    // Find by mobile (used for login)
    Optional<Agent> findByMobile(String mobile);

    // Check if mobile exists
    boolean existsByMobile(String mobile);

    // Compatibility methods - delegate to mobile methods
    default Optional<Agent> findByUsername(String username) {
        return findByMobile(username);
    }

    default boolean existsByUsername(String username) {
        return existsByMobile(username);
    }

    default boolean existsByEmail(String email) {
        return false; // Email field removed
    }
    
    // Find by status
    List<Agent> findByStatus(Agent.AgentStatus status);
    
    // Find active agents
    @Query("SELECT a FROM Agent a WHERE a.status = 'ACTIVE'")
    List<Agent> findActiveAgents();
    
    // Find blocked agents
    @Query("SELECT a FROM Agent a WHERE a.status = 'BLOCKED'")
    List<Agent> findBlockedAgents();
    
    // Find agents created by specific admin
    List<Agent> findByCreatedBy(String createdBy);

    // Find agents created by specific admin with specific status
    List<Agent> findByCreatedByAndStatus(String createdBy, Agent.AgentStatus status);
    
    // Count total agents
    @Query("SELECT COUNT(a) FROM Agent a")
    Long countTotalAgents();
    
    // Count active agents
    @Query("SELECT COUNT(a) FROM Agent a WHERE a.status = 'ACTIVE'")
    Long countActiveAgents();
    
    // Count blocked agents
    @Query("SELECT COUNT(a) FROM Agent a WHERE a.status = 'BLOCKED'")
    Long countBlockedAgents();
    
    // Get agents with payments today > 0
    @Query("SELECT a FROM Agent a WHERE a.paymentsToday > 0")
    List<Agent> getAgentsWithPaymentsToday();
    
    // Get top performing agents by total payments
    @Query("SELECT a FROM Agent a ORDER BY a.totalPayments DESC")
    List<Agent> getTopPerformingAgents();

    // Get agents with location - new method to avoid compilation issues
    @Query("SELECT a FROM Agent a")
    List<Agent> getAllAgentsForLocation();

    // Temporary method to replace getAgentsWithLocation until schema is updated
    default List<Agent> getAgentsWithLocation() {
        return getAllAgentsForLocation();
    }

    // Find next agent ID for auto-generation
    @Query("SELECT MAX(CAST(SUBSTRING(a.id, 6) AS int)) FROM Agent a WHERE a.id LIKE 'AGENT%'")
    Integer findMaxAgentNumber();
    
    // Search agents by name
    @Query("SELECT a FROM Agent a WHERE " +
           "LOWER(CONCAT(a.firstName, ' ', a.lastName)) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Agent> searchByName(@Param("name") String name);
}
