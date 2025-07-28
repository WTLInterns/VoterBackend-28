package com.votersystem.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.votersystem.dto.CreateIssueRequest;
import com.votersystem.dto.IssueResponse;
import com.votersystem.dto.ReopenIssueRequest;
import com.votersystem.entity.Issue;
import com.votersystem.service.IssueService;
import com.votersystem.util.ApiResponse;
import com.votersystem.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * Controller for Agent Issue operations (Mobile App Backend)
 */
@RestController
@RequestMapping("/agent/issues")
@CrossOrigin(origins = "*")
public class AgentIssueController {

    private static final Logger logger = LoggerFactory.getLogger(AgentIssueController.class);

    @Autowired
    private IssueService issueService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Extract agent ID from JWT token
     */
    private String getAgentIdFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtUtil.extractUserId(token);
        }
        return null;
    }

    /**
     * Create a new issue with optional media files
     */
    @PostMapping(consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<ApiResponse<IssueResponse>> createIssueWithOptionalMedia(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("category") String category,
            @RequestParam("priority") String priority,
            @RequestParam("address") String address,
            @RequestParam("area") String area,
            @RequestParam("village") String village,
            @RequestParam("district") String district,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            Authentication authentication,
            HttpServletRequest request) {
        try {
            // Extract agent ID from JWT token instead of using mobile number
            String agentId = getAgentIdFromToken(request);
            if (agentId == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Unable to extract agent ID from token"));
            }

            // Create issue request object
            CreateIssueRequest issueRequest = new CreateIssueRequest();
            issueRequest.setTitle(title);
            issueRequest.setDescription(description);
            issueRequest.setCategory(Issue.IssueCategory.valueOf(category.toUpperCase()));
            issueRequest.setPriority(Issue.IssuePriority.valueOf(priority.toUpperCase()));
            issueRequest.setAddress(address);
            issueRequest.setArea(area);
            issueRequest.setVillage(village);
            issueRequest.setDistrict(district);

            // Create issue with optional media files
            Issue savedIssue;
            if (files != null && !files.isEmpty()) {
                // Create issue with media
                savedIssue = issueService.createIssueWithMedia(issueRequest, files, agentId);
                logger.info("Agent {} created issue {} with {} media files",
                           agentId, savedIssue.getTicketNumber(), files.size());
            } else {
                // Create issue without media
                Issue issue = new Issue();
                issue.setTitle(issueRequest.getTitle());
                issue.setDescription(issueRequest.getDescription());
                issue.setCategory(issueRequest.getCategory());
                issue.setPriority(issueRequest.getPriority());
                issue.setReportedBy(agentId);
                issue.setAddress(issueRequest.getAddress());
                issue.setArea(issueRequest.getArea());
                issue.setVillage(issueRequest.getVillage());
                issue.setDistrict(issueRequest.getDistrict());

                savedIssue = issueService.createIssue(issue);
                logger.info("Agent {} created issue {} without media", agentId, savedIssue.getTicketNumber());
            }

            IssueResponse response = new IssueResponse(savedIssue);
            return ResponseEntity.ok(ApiResponse.success(response, "Issue created successfully"));

        } catch (Exception e) {
            logger.error("Failed to create issue: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create issue: " + e.getMessage()));
        }
    }

    /**
     * Get agent's own issues
     */
    @GetMapping("/my-issues")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<ApiResponse<Page<IssueResponse>>> getMyIssues(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "submissionDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Authentication authentication,
            HttpServletRequest request) {
        try {
            // Extract agent ID from JWT token instead of using mobile number
            String agentId = getAgentIdFromToken(request);
            if (agentId == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Unable to extract agent ID from token"));
            }

            Sort sort = sortDir.equalsIgnoreCase("desc") ?
                       Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<Issue> issues = issueService.getIssuesByReporter(agentId, pageable);
            Page<IssueResponse> response = issues.map(IssueResponse::new);

            return ResponseEntity.ok(ApiResponse.success(response, "Issues retrieved successfully"));

        } catch (Exception e) {
            logger.error("Failed to get agent issues: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get issues: " + e.getMessage()));
        }
    }

    /**
     * Get issue by ticket number
     */
    @GetMapping("/ticket/{ticketNumber}")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<ApiResponse<IssueResponse>> getIssueByTicket(
            @PathVariable String ticketNumber,
            Authentication authentication,
            HttpServletRequest request) {
        try {
            // Extract agent ID from JWT token instead of using mobile number
            String agentId = getAgentIdFromToken(request);
            if (agentId == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Unable to extract agent ID from token"));
            }

            Optional<Issue> issueOpt = issueService.getIssueByTicketNumber(ticketNumber);
            if (issueOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Issue issue = issueOpt.get();

            // Verify agent can access this issue
            if (!issue.getReportedBy().equals(agentId)) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error("Access denied to this issue"));
            }

            IssueResponse response = new IssueResponse(issue);
            return ResponseEntity.ok(ApiResponse.success(response, "Issue retrieved successfully"));

        } catch (Exception e) {
            logger.error("Failed to get issue by ticket: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get issue: " + e.getMessage()));
        }
    }

    /**
     * Get issue by ID
     */
    @GetMapping("/{issueId}")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<ApiResponse<IssueResponse>> getIssueById(
            @PathVariable Long issueId,
            Authentication authentication,
            HttpServletRequest request) {
        try {
            // Extract agent ID from JWT token instead of using mobile number
            String agentId = getAgentIdFromToken(request);
            if (agentId == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Unable to extract agent ID from token"));
            }

            Optional<Issue> issueOpt = issueService.getIssueById(issueId);
            if (issueOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Issue issue = issueOpt.get();

            // Verify agent can access this issue
            if (!issue.getReportedBy().equals(agentId)) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error("Access denied to this issue"));
            }

            IssueResponse response = new IssueResponse(issue);
            return ResponseEntity.ok(ApiResponse.success(response, "Issue retrieved successfully"));

        } catch (Exception e) {
            logger.error("Failed to get issue by ID: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get issue: " + e.getMessage()));
        }
    }

    /**
     * Add comment to issue
     */
    @PostMapping("/{issueId}/comments")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<ApiResponse<IssueResponse>> addComment(
            @PathVariable Long issueId,
            @Valid @RequestBody Map<String, String> requestBody,
            Authentication authentication,
            HttpServletRequest request) {
        try {
            // Extract agent ID from JWT token instead of using mobile number
            String agentId = getAgentIdFromToken(request);
            if (agentId == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Unable to extract agent ID from token"));
            }
            String comment = requestBody.get("comment");

            // Verify agent can access this issue
            Optional<Issue> issueOpt = issueService.getIssueById(issueId);
            if (issueOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Issue issue = issueOpt.get();
            if (!issue.getReportedBy().equals(agentId)) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error("Access denied to this issue"));
            }

            IssueResponse updatedIssue = issueService.addCommentForAgent(issueId.toString(), comment, agentId);

            logger.info("Agent {} added comment to issue {}", agentId, issue.getTicketNumber());
            return ResponseEntity.ok(ApiResponse.success(updatedIssue, "Comment added successfully"));

        } catch (Exception e) {
            logger.error("Failed to add comment: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to add comment: " + e.getMessage()));
        }
    }

    /**
     * Reopen an issue
     */
    @PutMapping("/{issueId}/reopen")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<ApiResponse<IssueResponse>> reopenIssue(
            @PathVariable Long issueId,
            @Valid @RequestBody ReopenIssueRequest requestBody,
            Authentication authentication,
            HttpServletRequest request) {
        try {
            // Extract agent ID from JWT token instead of using mobile number
            String agentId = getAgentIdFromToken(request);
            if (agentId == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Unable to extract agent ID from token"));
            }

            Issue reopenedIssue = issueService.reopenIssue(issueId, agentId, requestBody.getReopenReason());
            IssueResponse response = new IssueResponse(reopenedIssue);

            logger.info("Agent {} reopened issue {}", agentId, reopenedIssue.getTicketNumber());
            return ResponseEntity.ok(ApiResponse.success(response, "Issue reopened successfully"));

        } catch (Exception e) {
            logger.error("Failed to reopen issue: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to reopen issue: " + e.getMessage()));
        }
    }

    /**
     * Get issues that can be reopened by agent
     */
    @GetMapping("/reopenable")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<ApiResponse<List<IssueResponse>>> getReopenableIssues(
            Authentication authentication,
            HttpServletRequest request) {
        try {
            // Extract agent ID from JWT token instead of using mobile number
            String agentId = getAgentIdFromToken(request);
            if (agentId == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Unable to extract agent ID from token"));
            }

            List<Issue> issues = issueService.getReopenableIssues(agentId);
            List<IssueResponse> response = issues.stream()
                    .map(IssueResponse::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(response, "Reopenable issues retrieved successfully"));

        } catch (Exception e) {
            logger.error("Failed to get reopenable issues: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get reopenable issues: " + e.getMessage()));
        }
    }

    /**
     * Get issue categories
     */
    @GetMapping("/categories")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<ApiResponse<Issue.IssueCategory[]>> getIssueCategories() {
        try {
            Issue.IssueCategory[] categories = Issue.IssueCategory.values();
            return ResponseEntity.ok(ApiResponse.success(categories, "Categories retrieved successfully"));

        } catch (Exception e) {
            logger.error("Failed to get categories: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get categories: " + e.getMessage()));
        }
    }

    /**
     * Get issue priorities
     */
    @GetMapping("/priorities")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<ApiResponse<Issue.IssuePriority[]>> getIssuePriorities() {
        try {
            Issue.IssuePriority[] priorities = Issue.IssuePriority.values();
            return ResponseEntity.ok(ApiResponse.success(priorities, "Priorities retrieved successfully"));

        } catch (Exception e) {
            logger.error("Failed to get priorities: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get priorities: " + e.getMessage()));
        }
    }

    /**
     * Get issue statuses
     */
    @GetMapping("/statuses")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<ApiResponse<Issue.IssueStatus[]>> getIssueStatuses() {
        try {
            Issue.IssueStatus[] statuses = Issue.IssueStatus.values();
            return ResponseEntity.ok(ApiResponse.success(statuses, "Statuses retrieved successfully"));

        } catch (Exception e) {
            logger.error("Failed to get statuses: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get statuses: " + e.getMessage()));
        }
    }

    /**
     * Get issue updates since timestamp (for polling)
     */
    @GetMapping("/updates")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<ApiResponse<List<IssueResponse>>> getIssueUpdates(
            @RequestParam(required = false) String since,
            Authentication authentication,
            HttpServletRequest request) {
        try {
            // Extract agent ID from JWT token instead of using mobile number
            String agentId = getAgentIdFromToken(request);
            if (agentId == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Unable to extract agent ID from token"));
            }

            // Parse timestamp (ISO format: 2025-01-17T10:30:00)
            LocalDateTime sinceTimestamp = null;
            if (since != null && !since.trim().isEmpty()) {
                try {
                    sinceTimestamp = LocalDateTime.parse(since);
                } catch (Exception e) {
                    logger.warn("Invalid timestamp format: {}", since);
                    // If invalid, return all issues (fallback)
                }
            }

            List<IssueResponse> updatedIssues = issueService.getUpdatedIssuesForAgent(agentId, sinceTimestamp);

            return ResponseEntity.ok(ApiResponse.success(updatedIssues,
                "Updated issues retrieved successfully"));

        } catch (Exception e) {
            logger.error("Failed to get issue updates: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get issue updates: " + e.getMessage()));
        }
    }
}
