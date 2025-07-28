package com.votersystem.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.votersystem.dto.AgentLocationResponse;
import com.votersystem.service.AgentService;
import com.votersystem.service.LocationTrackingService;
import com.votersystem.util.ApiResponse;

/**
 * REST Controller for agent location tracking
 * Provides endpoints for admin dashboard to retrieve location data
 */
@RestController
@RequestMapping("/location")
@CrossOrigin(origins = "*")
public class LocationController {

    @Autowired
    private LocationTrackingService locationTrackingService;

    @Autowired
    private AgentService agentService;

    /**
     * Get all current agent locations (Master Admin only)
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<ApiResponse<List<AgentLocationResponse>>> getAllCurrentLocations() {
        try {
            List<AgentLocationResponse> locations = locationTrackingService.getAllCurrentLocations();
            return ResponseEntity.ok(ApiResponse.success(locations, "Current locations retrieved successfully"));
        } catch (Exception e) {
            System.err.println("Error getting all current locations: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                ApiResponse.error("Failed to retrieve locations: " + e.getMessage())
            );
        }
    }

    /**
     * Health check endpoint for debugging
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Location service is running", "Health check successful"));
    }

    /**
     * Test endpoint to add sample location data (for testing only)
     */
    @PostMapping("/test/add-sample-data")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<ApiResponse<String>> addSampleLocationData() {
        try {
            // Add sample location data for testing
            locationTrackingService.addSampleLocationData();
            return ResponseEntity.ok(ApiResponse.success("Sample location data added successfully", "Test data created"));
        } catch (Exception e) {
            System.err.println("Error adding sample location data: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                ApiResponse.error("Failed to add sample data: " + e.getMessage())
            );
        }
    }

    /**
     * Get current locations for agents created by the authenticated admin (Sub-Admin)
     */
    @GetMapping("/my-agents")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AgentLocationResponse>>> getMyAgentsLocations(Principal principal) {
        try {
            String adminUsername = principal.getName();
            
            // Get agent IDs created by this admin
            List<String> agentIds = agentService.getAgentIdsByCreatedBy(adminUsername);
            
            if (agentIds.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success(List.of(), "No agents found for this admin"));
            }
            
            List<AgentLocationResponse> locations = locationTrackingService.getCurrentLocationsByAgentIds(agentIds);
            return ResponseEntity.ok(ApiResponse.success(locations, "Agent locations retrieved successfully"));
        } catch (Exception e) {
            System.err.println("Error getting agent locations for admin: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                ApiResponse.error("Failed to retrieve agent locations: " + e.getMessage())
            );
        }
    }

    /**
     * Get online agents only (Master Admin)
     */
    @GetMapping("/online")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<ApiResponse<List<AgentLocationResponse>>> getOnlineAgents() {
        try {
            List<AgentLocationResponse> locations = locationTrackingService.getOnlineAgents();
            return ResponseEntity.ok(ApiResponse.success(locations, "Online agents retrieved successfully"));
        } catch (Exception e) {
            System.err.println("Error getting online agents: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                ApiResponse.error("Failed to retrieve online agents: " + e.getMessage())
            );
        }
    }

    /**
     * Get online agents for specific admin (Sub-Admin)
     */
    @GetMapping("/online/my-agents")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AgentLocationResponse>>> getMyOnlineAgents(Principal principal) {
        try {
            String adminUsername = principal.getName();
            
            // Get agent IDs created by this admin
            List<String> agentIds = agentService.getAgentIdsByCreatedBy(adminUsername);
            
            if (agentIds.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success(List.of(), "No agents found for this admin"));
            }
            
            List<AgentLocationResponse> locations = locationTrackingService.getOnlineAgentsByIds(agentIds);
            return ResponseEntity.ok(ApiResponse.success(locations, "Online agent locations retrieved successfully"));
        } catch (Exception e) {
            System.err.println("Error getting online agent locations for admin: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                ApiResponse.error("Failed to retrieve online agent locations: " + e.getMessage())
            );
        }
    }

    /**
     * Get specific agent location by ID
     */
    @GetMapping("/agent/{agentId}")
    @PreAuthorize("hasRole('MASTER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AgentLocationResponse>> getAgentLocation(
            @PathVariable String agentId, Principal principal) {
        try {
            // Check authorization for sub-admin
            if (principal != null && !hasAccessToAgent(agentId, principal.getName())) {
                return ResponseEntity.status(403).body(
                    ApiResponse.error("Access denied: You can only view locations of agents you created")
                );
            }
            
            AgentLocationResponse location = locationTrackingService.getAgentCurrentLocation(agentId);
            if (location == null) {
                return ResponseEntity.status(404).body(
                    ApiResponse.error("Agent location not found")
                );
            }
            
            return ResponseEntity.ok(ApiResponse.success(location, "Agent location retrieved successfully"));
        } catch (Exception e) {
            System.err.println("Error getting agent location: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                ApiResponse.error("Failed to retrieve agent location: " + e.getMessage())
            );
        }
    }

    /**
     * Get location statistics
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<ApiResponse<LocationStats>> getLocationStats() {
        try {
            long onlineCount = locationTrackingService.countOnlineAgents();
            long totalAgents = agentService.getTotalAgentCount();
            
            LocationStats stats = new LocationStats(onlineCount, totalAgents - onlineCount, totalAgents);
            return ResponseEntity.ok(ApiResponse.success(stats, "Location statistics retrieved successfully"));
        } catch (Exception e) {
            System.err.println("Error getting location statistics: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                ApiResponse.error("Failed to retrieve location statistics: " + e.getMessage())
            );
        }
    }

    /**
     * Get location statistics for sub-admin
     */
    @GetMapping("/stats/my-agents")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LocationStats>> getMyAgentsLocationStats(Principal principal) {
        try {
            String adminUsername = principal.getName();
            
            // Get agent IDs created by this admin
            List<String> agentIds = agentService.getAgentIdsByCreatedBy(adminUsername);
            
            long onlineCount = locationTrackingService.countOnlineAgentsByIds(agentIds);
            long totalAgents = agentIds.size();
            
            LocationStats stats = new LocationStats(onlineCount, totalAgents - onlineCount, totalAgents);
            return ResponseEntity.ok(ApiResponse.success(stats, "Agent location statistics retrieved successfully"));
        } catch (Exception e) {
            System.err.println("Error getting agent location statistics for admin: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                ApiResponse.error("Failed to retrieve agent location statistics: " + e.getMessage())
            );
        }
    }

    /**
     * Check if admin has access to specific agent
     */
    private boolean hasAccessToAgent(String agentId, String adminUsername) {
        try {
            List<String> agentIds = agentService.getAgentIdsByCreatedBy(adminUsername);
            return agentIds.contains(agentId);
        } catch (Exception e) {
            System.err.println("Error checking agent access: " + e.getMessage());
            return false;
        }
    }

    /**
     * Location statistics DTO
     */
    public static class LocationStats {
        private long onlineAgents;
        private long offlineAgents;
        private long totalAgents;

        public LocationStats(long onlineAgents, long offlineAgents, long totalAgents) {
            this.onlineAgents = onlineAgents;
            this.offlineAgents = offlineAgents;
            this.totalAgents = totalAgents;
        }

        // Getters and setters
        public long getOnlineAgents() { return onlineAgents; }
        public void setOnlineAgents(long onlineAgents) { this.onlineAgents = onlineAgents; }
        
        public long getOfflineAgents() { return offlineAgents; }
        public void setOfflineAgents(long offlineAgents) { this.offlineAgents = offlineAgents; }
        
        public long getTotalAgents() { return totalAgents; }
        public void setTotalAgents(long totalAgents) { this.totalAgents = totalAgents; }
    }
}
