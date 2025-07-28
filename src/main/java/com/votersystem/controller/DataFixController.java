package com.votersystem.controller;

import com.votersystem.entity.Agent;
import com.votersystem.service.AgentService;
import com.votersystem.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/data-fix")
@CrossOrigin(origins = "*")
public class DataFixController {

    @Autowired
    private AgentService agentService;

    /**
     * Fix existing agents that don't have proper createdBy values
     * This should be called once to fix legacy data
     */
    @PostMapping("/fix-agent-creators")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<ApiResponse<String>> fixAgentCreators() {
        try {
            List<Agent> allAgents = agentService.getAllAgents();
            int fixedCount = 0;
            
            for (Agent agent : allAgents) {
                if (agent.getCreatedBy() == null || agent.getCreatedBy().trim().isEmpty()) {
                    // Set a default creator for existing agents without createdBy
                    // You can modify this logic based on your needs
                    agent.setCreatedBy("master"); // or assign to a specific sub-admin
                    agentService.saveAgent(agent);
                    fixedCount++;
                }
            }
            
            String message = String.format("Fixed %d agents with missing createdBy values", fixedCount);
            return ResponseEntity.ok(ApiResponse.success(null, message));
            
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Failed to fix agent creators: " + e.getMessage()));
        }
    }

    /**
     * Assign specific agents to a sub-admin
     */
    @PostMapping("/assign-agents-to-admin")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<ApiResponse<String>> assignAgentsToAdmin(
            @RequestBody AssignAgentsRequest request) {
        try {
            int assignedCount = 0;
            
            for (String agentId : request.getAgentIds()) {
                Agent agent = agentService.getAgentById(agentId);
                if (agent != null) {
                    agent.setCreatedBy(request.getAdminUsername());
                    agentService.saveAgent(agent);
                    assignedCount++;
                }
            }
            
            String message = String.format("Assigned %d agents to admin %s", 
                assignedCount, request.getAdminUsername());
            return ResponseEntity.ok(ApiResponse.success(null, message));
            
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Failed to assign agents: " + e.getMessage()));
        }
    }

    /**
     * Get all agents without proper createdBy values
     */
    @GetMapping("/orphaned-agents")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<ApiResponse<List<Agent>>> getOrphanedAgents() {
        try {
            List<Agent> orphanedAgents = agentService.getAllAgents().stream()
                .filter(agent -> agent.getCreatedBy() == null || agent.getCreatedBy().trim().isEmpty())
                .toList();
            
            return ResponseEntity.ok(ApiResponse.success(orphanedAgents, 
                "Found " + orphanedAgents.size() + " agents without proper creators"));
                
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Failed to get orphaned agents: " + e.getMessage()));
        }
    }

    // Request class for assigning agents
    public static class AssignAgentsRequest {
        private List<String> agentIds;
        private String adminUsername;

        public List<String> getAgentIds() { return agentIds; }
        public void setAgentIds(List<String> agentIds) { this.agentIds = agentIds; }
        public String getAdminUsername() { return adminUsername; }
        public void setAdminUsername(String adminUsername) { this.adminUsername = adminUsername; }
    }
}
