package com.votersystem.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.votersystem.entity.AgentLocation;

@Repository
public interface AgentLocationRepository extends JpaRepository<AgentLocation, Long> {
    
    // Find current location for an agent
    @Query("SELECT al FROM AgentLocation al WHERE al.agentId = :agentId AND al.isCurrent = true")
    Optional<AgentLocation> findCurrentLocationByAgentId(@Param("agentId") String agentId);

    // Find all current locations for multiple agents
    @Query("SELECT al FROM AgentLocation al WHERE al.agentId IN :agentIds AND al.isCurrent = true")
    List<AgentLocation> findCurrentLocationsByAgentIds(@Param("agentIds") List<String> agentIds);

    // Find all current locations (for master admin)
    @Query("SELECT al FROM AgentLocation al WHERE al.isCurrent = true")
    List<AgentLocation> findAllCurrentLocations();
    
    // Find current online agents
    @Query("SELECT al FROM AgentLocation al WHERE al.isCurrent = true AND al.connectionStatus = 'ONLINE'")
    List<AgentLocation> findAllOnlineAgents();
    
    // Find online agents by agent IDs (for sub-admin)
    @Query("SELECT al FROM AgentLocation al WHERE al.agentId IN :agentIds AND al.isCurrent = true AND al.connectionStatus = 'ONLINE'")
    List<AgentLocation> findOnlineAgentsByIds(@Param("agentIds") List<String> agentIds);
    
    // Find location history for an agent
    @Query("SELECT al FROM AgentLocation al WHERE al.agentId = :agentId ORDER BY al.timestamp DESC")
    Page<AgentLocation> findLocationHistoryByAgentId(@Param("agentId") String agentId, Pageable pageable);
    
    // Find location history within date range
    @Query("SELECT al FROM AgentLocation al WHERE al.agentId = :agentId AND al.timestamp BETWEEN :startDate AND :endDate ORDER BY al.timestamp DESC")
    List<AgentLocation> findLocationHistoryByAgentIdAndDateRange(
            @Param("agentId") String agentId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    
    // Find agents within a radius (using Haversine formula approximation)
    @Query(value = "SELECT * FROM agent_locations al WHERE al.is_current = true " +
           "AND (6371 * acos(cos(radians(:latitude)) * cos(radians(al.latitude)) * " +
           "cos(radians(al.longitude) - radians(:longitude)) + sin(radians(:latitude)) * " +
           "sin(radians(al.latitude)))) <= :radiusKm", nativeQuery = true)
    List<AgentLocation> findAgentsWithinRadius(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("radiusKm") Double radiusKm
    );
    
    // Update connection status for an agent
    @Modifying
    @Transactional
    @Query("UPDATE AgentLocation al SET al.connectionStatus = :status, al.timestamp = :timestamp WHERE al.agentId = :agentId AND al.isCurrent = true")
    int updateConnectionStatus(@Param("agentId") String agentId, @Param("status") AgentLocation.ConnectionStatus status, @Param("timestamp") LocalDateTime timestamp);
    
    // Mark all previous locations as not current for an agent
    @Modifying
    @Transactional
    @Query("UPDATE AgentLocation al SET al.isCurrent = false WHERE al.agentId = :agentId AND al.isCurrent = true")
    int markPreviousLocationsAsNotCurrent(@Param("agentId") String agentId);
    
    // Delete old location history (older than specified days)
    @Modifying
    @Transactional
    @Query("DELETE FROM AgentLocation al WHERE al.timestamp < :cutoffDate AND al.isCurrent = false")
    int deleteOldLocationHistory(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    // Count online agents
    @Query("SELECT COUNT(al) FROM AgentLocation al WHERE al.isCurrent = true AND al.connectionStatus = 'ONLINE'")
    Long countOnlineAgents();
    
    // Count online agents by agent IDs (for sub-admin)
    @Query("SELECT COUNT(al) FROM AgentLocation al WHERE al.agentId IN :agentIds AND al.isCurrent = true AND al.connectionStatus = 'ONLINE'")
    Long countOnlineAgentsByIds(@Param("agentIds") List<String> agentIds);
    
    // Find agents by connection status
    @Query("SELECT al FROM AgentLocation al WHERE al.isCurrent = true AND al.connectionStatus = :status")
    List<AgentLocation> findAgentsByConnectionStatus(@Param("status") AgentLocation.ConnectionStatus status);
    
    // Find recent locations (last N hours)
    @Query("SELECT al FROM AgentLocation al WHERE al.timestamp >= :since ORDER BY al.timestamp DESC")
    List<AgentLocation> findRecentLocations(@Param("since") LocalDateTime since);
    
    // Find agents that haven't updated location recently (potentially offline)
    @Query("SELECT al FROM AgentLocation al WHERE al.isCurrent = true AND al.timestamp < :cutoffTime")
    List<AgentLocation> findStaleLocations(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    // Get location statistics
    @Query("SELECT " +
           "COUNT(CASE WHEN al.connectionStatus = 'ONLINE' THEN 1 END) as online, " +
           "COUNT(CASE WHEN al.connectionStatus = 'OFFLINE' THEN 1 END) as offline, " +
           "COUNT(CASE WHEN al.connectionStatus = 'DISCONNECTED' THEN 1 END) as disconnected " +
           "FROM AgentLocation al WHERE al.isCurrent = true")
    Object[] getLocationStatistics();
    
    // Find locations by agent IDs with pagination
    @Query("SELECT al FROM AgentLocation al WHERE al.agentId IN :agentIds AND al.isCurrent = true ORDER BY al.timestamp DESC")
    Page<AgentLocation> findCurrentLocationsByAgentIds(@Param("agentIds") List<String> agentIds, Pageable pageable);
}
