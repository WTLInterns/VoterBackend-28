package com.votersystem.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.votersystem.dto.IssueResponse;
import com.votersystem.dto.IssueStatisticsResponse;
import com.votersystem.entity.Agent;
import com.votersystem.entity.Issue;
import com.votersystem.repository.AgentRepository;
import com.votersystem.repository.IssueRepository;
import com.votersystem.service.IssueService;
import com.votersystem.util.ApiResponse;
import com.votersystem.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/admin/issues")
@PreAuthorize("hasRole('ADMIN')")
public class SubAdminIssueController {

    private static final Logger logger = LoggerFactory.getLogger(SubAdminIssueController.class);

    @Autowired
    private IssueService issueService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private IssueRepository issueRepository;

    /**
     * Test endpoint to verify authentication and user info
     */
    @GetMapping("/test-auth")
    public ResponseEntity<ApiResponse<Object>> testAuth(HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("No token found"));
            }

            String subAdminUsername = jwtUtil.extractUsername(token);
            if (subAdminUsername == null) {
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("Invalid token"));
            }

            // Get agent count
            List<Agent> agents = agentRepository.findByCreatedBy(subAdminUsername);

            Map<String, Object> info = new HashMap<>();
            info.put("username", subAdminUsername);
            info.put("agentCount", agents.size());
            info.put("tokenValid", true);

            return ResponseEntity.ok(ApiResponse.success(info, "Authentication test successful"));

        } catch (Exception e) {
            logger.error("Auth test error: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Auth test failed: " + e.getMessage()));
        }
    }

    /**
     * Get all issues for sub-admin (only issues from agents they created)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<IssueResponse>>> getAllIssues(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "submissionDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) String search,
            HttpServletRequest request) {

        try {
            // Get sub-admin username from JWT token
            String token = extractTokenFromRequest(request);
            if (token == null) {
                logger.error("No authorization token found in request");
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("Authentication required"));
            }

            String subAdminUsername = jwtUtil.extractUsername(token);
            if (subAdminUsername == null) {
                logger.error("Unable to extract username from token");
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("Invalid authentication token"));
            }

            logger.debug("SubAdmin {} requesting issues", subAdminUsername);

            // Check if sub-admin has any agents
            List<Agent> subAdminAgents = agentRepository.findByCreatedBy(subAdminUsername);
            if (subAdminAgents.isEmpty()) {
                logger.info("No agents found for sub-admin: {}", subAdminUsername);
                return ResponseEntity.ok(ApiResponse.success(
                    new PageImpl<>(new ArrayList<>(), PageRequest.of(page, size), 0),
                    "No agents found. Please create agents first to start receiving issues."));
            }

            Sort sort = sortDir.equalsIgnoreCase("desc") ?
                       Sort.by(sortBy).descending() :
                       Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);

            Page<IssueResponse> issues = issueService.getIssuesForSubAdmin(
                subAdminUsername, status, category, priority, dateFrom, dateTo, search, pageable);

            String message = issues.getTotalElements() > 0 ?
                "Issues retrieved successfully" :
                "No issues found matching the criteria";

            return ResponseEntity.ok(ApiResponse.success(issues, message));

        } catch (Exception e) {
            logger.error("Error retrieving issues for sub-admin: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to retrieve issues. Please try again."));
        }
    }
    
    /**
     * Get issue by ID (only if it belongs to sub-admin's agent)
     */
    @GetMapping("/{issueId}")
    public ResponseEntity<ApiResponse<IssueResponse>> getIssueById(
            @PathVariable String issueId,
            HttpServletRequest request) {
        
        // Get sub-admin username from JWT token
        String token = extractTokenFromRequest(request);
        String subAdminUsername = jwtUtil.extractUsername(token);
        
        IssueResponse issue = issueService.getIssueByIdForSubAdmin(issueId, subAdminUsername);
        return ResponseEntity.ok(ApiResponse.success(issue, "Issue retrieved successfully"));
    }
    
    /**
     * Update issue status
     */
    @PutMapping("/{issueId}/status")
    public ResponseEntity<ApiResponse<IssueResponse>> updateIssueStatus(
            @PathVariable String issueId,
            @Valid @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {

        try {
            // Get sub-admin username from JWT token
            String token = extractTokenFromRequest(httpRequest);
            String subAdminUsername = jwtUtil.extractUsername(token);

            String newStatus = request.get("status");
            logger.info("SubAdmin {} updating issue {} status to {}", subAdminUsername, issueId, newStatus);

            IssueResponse updatedIssue = issueService.updateIssueStatusForSubAdmin(issueId, newStatus, subAdminUsername);

            logger.info("Successfully updated issue {} status to {}", issueId, newStatus);
            return ResponseEntity.ok(ApiResponse.success(updatedIssue, "Issue status updated successfully"));

        } catch (Exception e) {
            logger.error("Failed to update issue {} status: {}", issueId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update status: " + e.getMessage()));
        }
    }
    
    /**
     * Set resolution date
     */
    @PutMapping("/{issueId}/resolution-date")
    public ResponseEntity<ApiResponse<IssueResponse>> setResolutionDate(
            @PathVariable String issueId,
            @Valid @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        
        // Get sub-admin username from JWT token
        String token = extractTokenFromRequest(httpRequest);
        String subAdminUsername = jwtUtil.extractUsername(token);

        String resolutionDate = request.get("resolutionDate");
        IssueResponse updatedIssue = issueService.setResolutionDateForSubAdmin(issueId, resolutionDate, subAdminUsername);

        return ResponseEntity.ok(ApiResponse.success(updatedIssue, "Resolution date set successfully"));
    }

    /**
     * Add comment to issue
     */
    @PostMapping("/{issueId}/comments")
    public ResponseEntity<ApiResponse<String>> addComment(
            @PathVariable String issueId,
            @Valid @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {

        try {
            // Get sub-admin username from JWT token
            String token = extractTokenFromRequest(httpRequest);
            String subAdminUsername = jwtUtil.extractUsername(token);

            String comment = request.get("comment");

            // Add comment but don't return full issue response to avoid large JSON
            issueService.addCommentForSubAdmin(issueId, comment, subAdminUsername);

            return ResponseEntity.ok(ApiResponse.success("Comment added successfully", "Comment added successfully"));

        } catch (Exception e) {
            logger.error("Failed to add comment: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to add comment: " + e.getMessage()));
        }
    }

    /**
     * Get issue statistics for sub-admin
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<IssueStatisticsResponse>> getStatistics(
            HttpServletRequest request) {

        try {
            // Get sub-admin username from JWT token
            String token = extractTokenFromRequest(request);
            if (token == null) {
                logger.error("No authorization token found in statistics request");
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("Authentication required"));
            }

            String subAdminUsername = jwtUtil.extractUsername(token);
            if (subAdminUsername == null) {
                logger.error("Unable to extract username from token in statistics request");
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("Invalid authentication token"));
            }

            logger.debug("SubAdmin {} requesting statistics", subAdminUsername);

            IssueStatisticsResponse statistics = issueService.getStatisticsForSubAdmin(subAdminUsername);
            return ResponseEntity.ok(ApiResponse.success(statistics, "Statistics retrieved successfully"));

        } catch (Exception e) {
            logger.error("Error retrieving statistics for sub-admin: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to retrieve statistics. Please try again."));
        }
    }

    /**
     * Get issues for map view (sub-admin's agents only)
     */
    @GetMapping("/map-view")
    public ResponseEntity<ApiResponse<List<IssueResponse>>> getIssuesForMapView(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String priority,
            HttpServletRequest request) {

        // Get sub-admin username from JWT token
        String token = extractTokenFromRequest(request);
        String subAdminUsername = jwtUtil.extractUsername(token);

        List<IssueResponse> issues = issueService.getIssuesForMapViewForSubAdmin(
            subAdminUsername, status, category, priority);

        return ResponseEntity.ok(ApiResponse.success(issues, "Map view issues retrieved successfully"));
    }

    /**
     * Export issues data for sub-admin
     */
    @GetMapping("/export")
    public ResponseEntity<ApiResponse<List<IssueResponse>>> exportIssues(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            HttpServletRequest request) {

        // Get sub-admin username from JWT token
        String token = extractTokenFromRequest(request);
        String subAdminUsername = jwtUtil.extractUsername(token);

        List<IssueResponse> issues = issueService.exportIssuesForSubAdmin(
            subAdminUsername, status, category, priority, dateFrom, dateTo);

        return ResponseEntity.ok(ApiResponse.success(issues, "Issues exported successfully"));
    }

    /**
     * DEBUG ENDPOINT: Test dual-lookup functionality for production debugging
     */
    @GetMapping("/debug/agent-lookup")
    public ResponseEntity<ApiResponse<Map<String, Object>>> debugAgentLookup(
            HttpServletRequest request) {
        try {
            // Get sub-admin username from JWT token
            String token = extractTokenFromRequest(request);
            String subAdminUsername = jwtUtil.extractUsername(token);

            // Get all agents created by this sub-admin
            List<Agent> subAdminAgents = agentRepository.findByCreatedBy(subAdminUsername);

            Map<String, Object> debugInfo = new HashMap<>();
            debugInfo.put("subAdminUsername", subAdminUsername);
            debugInfo.put("agentCount", subAdminAgents.size());

            List<Map<String, Object>> agentDetails = new ArrayList<>();
            for (Agent agent : subAdminAgents) {
                Map<String, Object> agentInfo = new HashMap<>();
                agentInfo.put("agentId", agent.getId());
                agentInfo.put("mobile", agent.getMobile());
                agentInfo.put("name", agent.getFirstName() + " " + agent.getLastName());

                // Count issues by agent ID (new format)
                List<Issue> issuesByAgentId = issueRepository.findByReportedBy(agent.getId());
                agentInfo.put("issuesByAgentId", issuesByAgentId.size());

                // Count issues by mobile (old format)
                List<Issue> issuesByMobile = issueRepository.findByReportedBy(agent.getMobile());
                agentInfo.put("issuesByMobile", issuesByMobile.size());

                agentDetails.add(agentInfo);
            }

            debugInfo.put("agents", agentDetails);

            return ResponseEntity.ok(ApiResponse.success(debugInfo, "Debug info retrieved successfully"));

        } catch (Exception e) {
            logger.error("Debug endpoint error: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Debug endpoint failed: " + e.getMessage()));
        }
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
