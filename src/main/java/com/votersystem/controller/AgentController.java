package com.votersystem.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.votersystem.dto.LocationUpdateRequest;
import com.votersystem.entity.Agent;
import com.votersystem.entity.User;
import com.votersystem.service.AgentService;
import com.votersystem.service.UserService;
import com.votersystem.util.ApiResponse;
import com.votersystem.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/agent")
@CrossOrigin(origins = "*")
public class AgentController {
    
    @Autowired
    private AgentService agentService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    // Get agent profile
    @GetMapping("/profile")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<ApiResponse<Agent>> getAgentProfile(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        String username = jwtUtil.extractUsername(token);
        
        Agent agent = agentService.getAgentByUsername(username);
        return ResponseEntity.ok(ApiResponse.success(agent, "Agent profile retrieved"));
    }
    

    
    // Get users assigned to agent (for mobile app)
    @GetMapping("/users")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<ApiResponse<List<User>>> getAssignedUsers(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        String username = jwtUtil.extractUsername(token);

        // Return all users created by the same sub-admin who created this agent
        // This ensures agents see users from their assigned territory/area
        List<User> users = userService.getUsersForAgent(username);
        return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
    }
    
    // Search users by last name (for mobile app)
    @GetMapping("/users/search")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<ApiResponse<List<User>>> searchUsers(@RequestParam String lastName) {
        List<User> users = userService.searchUsersByLastName(lastName);
        return ResponseEntity.ok(ApiResponse.success(users, "Users found"));
    }

    // Advanced search users for agents (mobile app)
    @GetMapping("/users/search/advanced")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<ApiResponse<List<User>>> advancedSearchUsers(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) Integer age,
            @RequestParam(required = false) User.Gender gender,
            @RequestParam(required = false) String vidhansabhaNo,
            @RequestParam(required = false) String vibhaghKramank,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(defaultValue = "50") int limit,
            HttpServletRequest request) {

        String token = extractTokenFromRequest(request);
        String username = jwtUtil.extractUsername(token);

        // Get users for this agent with advanced search filters
        List<User> users = userService.searchUsersForAgent(username, firstName, lastName, age, gender,
                                                          vidhansabhaNo, vibhaghKramank, paid, limit);
        return ResponseEntity.ok(ApiResponse.success(users, "Users found"));
    }
    
    // Mark user as paid (for mobile app)
    @PostMapping("/users/{userId}/mark-paid")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<ApiResponse<User>> markUserAsPaid(
            @PathVariable Long userId,
            @RequestBody MarkAsPaidRequest request,
            HttpServletRequest httpRequest) {
        
        String token = extractTokenFromRequest(httpRequest);
        String username = jwtUtil.extractUsername(token);
        
        User user = userService.markUserAsPaid(userId, username, request.getAmount());
        return ResponseEntity.ok(ApiResponse.success(user, "User marked as paid successfully"));
    }
    
    // Get agent statistics
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<ApiResponse<AgentStatistics>> getAgentStatistics(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        String username = jwtUtil.extractUsername(token);

        AgentStatistics stats = agentService.getAgentStatistics(username);
        return ResponseEntity.ok(ApiResponse.success(stats, "Statistics retrieved successfully"));
    }

    // Update agent location (PUT approach - update existing record)
    @PutMapping("/location")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<ApiResponse<String>> updateLocation(
            @Valid @RequestBody LocationUpdateRequest request,
            HttpServletRequest httpRequest) {
        try {
            String token = extractTokenFromRequest(httpRequest);
            String username = jwtUtil.extractUsername(token);

            Agent agent = agentService.getAgentByUsername(username);
            agent.setLatitude(request.getLatitude());
            agent.setLongitude(request.getLongitude());
            agent.setLastLocation(request.getLocation());

            agentService.saveAgent(agent);

            return ResponseEntity.ok(ApiResponse.success("Location updated successfully", "Location updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update location: " + e.getMessage()));
        }
    }
    
    // Get users paid today by agent
    @GetMapping("/users/paid-today")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<ApiResponse<List<User>>> getUsersPaidToday(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        String username = jwtUtil.extractUsername(token);

        List<User> users = userService.getUsersPaidTodayByAgent(username);
        return ResponseEntity.ok(ApiResponse.success(users, "Today's paid users retrieved"));
    }


    
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    // DTOs
    public static class MarkAsPaidRequest {
        private BigDecimal amount;

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
    }
    
    public static class AgentStatistics {
        private Integer paymentsToday;
        private Integer totalPayments;
        private Double amountCollectedToday;
        private Double totalAmountCollected;

        public AgentStatistics(Integer paymentsToday, Integer totalPayments,
                             Double amountCollectedToday, Double totalAmountCollected) {
            this.paymentsToday = paymentsToday;
            this.totalPayments = totalPayments;
            this.amountCollectedToday = amountCollectedToday;
            this.totalAmountCollected = totalAmountCollected;
        }

        public Integer getPaymentsToday() { return paymentsToday; }
        public void setPaymentsToday(Integer paymentsToday) { this.paymentsToday = paymentsToday; }
        public Integer getTotalPayments() { return totalPayments; }
        public void setTotalPayments(Integer totalPayments) { this.totalPayments = totalPayments; }
        public Double getAmountCollectedToday() { return amountCollectedToday; }
        public void setAmountCollectedToday(Double amountCollectedToday) { this.amountCollectedToday = amountCollectedToday; }
        public Double getTotalAmountCollected() { return totalAmountCollected; }
        public void setTotalAmountCollected(Double totalAmountCollected) { this.totalAmountCollected = totalAmountCollected; }
    }


}
