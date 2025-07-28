package com.votersystem.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
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

import com.votersystem.dto.IssueResponse;
import com.votersystem.entity.Agent;
import com.votersystem.entity.Issue;
import com.votersystem.entity.IssueComment;
import com.votersystem.repository.AgentRepository;
import com.votersystem.service.IssueCommentService;
import com.votersystem.service.IssueService;
import com.votersystem.util.ApiResponse;

import jakarta.validation.Valid;

/**
 * Controller for Master Admin Issue Management (Web Dashboard)
 */
@RestController
@RequestMapping("/master/issues")
@CrossOrigin(origins = "*")
public class MasterAdminIssueController {
    
    private static final Logger logger = LoggerFactory.getLogger(MasterAdminIssueController.class);
    
    @Autowired
    private IssueService issueService;

    @Autowired
    private IssueCommentService issueCommentService;

    @Autowired
    private AgentRepository agentRepository;
    
    /**
     * Get all issues with filtering and pagination
     */
    @GetMapping
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<ApiResponse<Page<IssueResponse>>> getAllIssues(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "submissionDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Issue.IssueStatus status,
            @RequestParam(required = false) Issue.IssueCategory category,
            @RequestParam(required = false) Issue.IssuePriority priority,
            @RequestParam(required = false) String reportedBy,
            @RequestParam(required = false) String village,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) String searchTerm) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                       Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<Issue> issues = issueService.searchIssues(status, category, priority,
                                                          reportedBy, village, district, dateFrom, dateTo, searchTerm, pageable);

            // Get all agents for agent name lookup
            List<Agent> allAgents = agentRepository.findAll();

            Page<IssueResponse> response = issues.map(issue -> {
                // Find the agent for this issue
                Agent agent = allAgents.stream()
                    .filter(a -> a.getId().equals(issue.getReportedBy()) ||
                               a.getMobile().equals(issue.getReportedBy()))
                    .findFirst()
                    .orElse(null);

                return new IssueResponse(issue, agent);
            });
            
            logger.info("Master admin retrieved {} issues with filters", issues.getTotalElements());
            return ResponseEntity.ok(ApiResponse.success(response, "Issues retrieved successfully"));
            
        } catch (Exception e) {
            logger.error("Failed to get issues: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get issues: " + e.getMessage()));
        }
    }
    
    /**
     * Get issue by ID with full details
     */
    @GetMapping("/{issueId}")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<ApiResponse<IssueResponse>> getIssueById(@PathVariable Long issueId) {
        try {
            Optional<Issue> issueOpt = issueService.getIssueById(issueId);
            if (issueOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Issue issue = issueOpt.get();

            // Find the agent for this issue
            Agent agent = agentRepository.findById(issue.getReportedBy())
                .orElse(agentRepository.findByMobile(issue.getReportedBy()).orElse(null));

            IssueResponse response = new IssueResponse(issue, agent);
            return ResponseEntity.ok(ApiResponse.success(response, "Issue retrieved successfully"));
            
        } catch (Exception e) {
            logger.error("Failed to get issue by ID: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get issue: " + e.getMessage()));
        }
    }
    
    /**
     * Get issue by ticket number
     */
    @GetMapping("/ticket/{ticketNumber}")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<ApiResponse<IssueResponse>> getIssueByTicket(@PathVariable String ticketNumber) {
        try {
            Optional<Issue> issueOpt = issueService.getIssueByTicketNumber(ticketNumber);
            if (issueOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            IssueResponse response = new IssueResponse(issueOpt.get());
            return ResponseEntity.ok(ApiResponse.success(response, "Issue retrieved successfully"));
            
        } catch (Exception e) {
            logger.error("Failed to get issue by ticket: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get issue: " + e.getMessage()));
        }
    }
    
    /**
     * Update issue status
     */
    @PutMapping("/{issueId}/status")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<ApiResponse<IssueResponse>> updateIssueStatus(
            @PathVariable Long issueId,
            @RequestBody UpdateStatusRequest request,
            Authentication authentication) {
        try {
            String masterAdminUsername = authentication.getName();
            
            Issue updatedIssue = issueService.updateIssueStatus(issueId, request.getStatus(), masterAdminUsername);
            IssueResponse response = new IssueResponse(updatedIssue);
            
            logger.info("Master admin {} updated issue {} status to {}", 
                       masterAdminUsername, updatedIssue.getTicketNumber(), request.getStatus());
            return ResponseEntity.ok(ApiResponse.success(response, "Issue status updated successfully"));
            
        } catch (Exception e) {
            logger.error("Failed to update issue status: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update status: " + e.getMessage()));
        }
    }
    
    /**
     * Set estimated resolution date
     */
    @PutMapping("/{issueId}/resolution-date")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<ApiResponse<IssueResponse>> setEstimatedResolutionDate(
            @PathVariable Long issueId,
            @RequestBody SetResolutionDateRequest request,
            Authentication authentication) {
        try {
            String masterAdminUsername = authentication.getName();
            
            Issue updatedIssue = issueService.setEstimatedResolutionDate(issueId, request.getEstimatedDate(), masterAdminUsername);
            IssueResponse response = new IssueResponse(updatedIssue);
            
            logger.info("Master admin {} set resolution date for issue {} to {}", 
                       masterAdminUsername, updatedIssue.getTicketNumber(), request.getEstimatedDate());
            return ResponseEntity.ok(ApiResponse.success(response, "Resolution date set successfully"));
            
        } catch (Exception e) {
            logger.error("Failed to set resolution date: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to set resolution date: " + e.getMessage()));
        }
    }
    
    /**
     * Add comment to issue
     */
    @PostMapping("/{issueId}/comments")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<ApiResponse<String>> addComment(
            @PathVariable Long issueId,
            @Valid @RequestBody AddCommentRequest request,
            Authentication authentication) {
        try {
            String masterAdminUsername = authentication.getName();

            // Add comment but return simple response to avoid large JSON
            issueCommentService.addComment(issueId, request.getComment(),
                                         masterAdminUsername, request.getCommentType(),
                                         request.getIsInternal());

            logger.info("Master admin {} added comment to issue ID {}", masterAdminUsername, issueId);
            return ResponseEntity.ok(ApiResponse.success("Comment added successfully", "Comment added successfully"));

        } catch (Exception e) {
            logger.error("Failed to add comment: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to add comment: " + e.getMessage()));
        }
    }
    
    /**
     * Get issue statistics for dashboard
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<ApiResponse<IssueService.IssueStatistics>> getIssueStatistics() {
        try {
            IssueService.IssueStatistics statistics = issueService.getIssueStatistics();
            return ResponseEntity.ok(ApiResponse.success(statistics, "Statistics retrieved successfully"));
            
        } catch (Exception e) {
            logger.error("Failed to get statistics: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get statistics: " + e.getMessage()));
        }
    }
    
    /**
     * Get issues with location for map view
     */
    @GetMapping("/map-view")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<ApiResponse<List<IssueLocationResponse>>> getIssuesForMap() {
        try {
            List<Issue> issuesWithLocation = issueService.getIssuesWithAddress();
            List<IssueLocationResponse> response = issuesWithLocation.stream()
                    .map(IssueLocationResponse::new)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(response, "Map issues retrieved successfully"));
            
        } catch (Exception e) {
            logger.error("Failed to get map issues: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get map issues: " + e.getMessage()));
        }
    }
    
    /**
     * Get overdue issues
     */
    @GetMapping("/overdue")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<ApiResponse<List<IssueResponse>>> getOverdueIssues() {
        try {
            List<Issue> overdueIssues = issueService.getOverdueIssues();
            List<IssueResponse> response = overdueIssues.stream()
                    .map(IssueResponse::new)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(response, "Overdue issues retrieved successfully"));
            
        } catch (Exception e) {
            logger.error("Failed to get overdue issues: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get overdue issues: " + e.getMessage()));
        }
    }
    
    // Request DTOs
    public static class UpdateStatusRequest {
        private Issue.IssueStatus status;
        private String resolutionNotes;
        
        public Issue.IssueStatus getStatus() { return status; }
        public void setStatus(Issue.IssueStatus status) { this.status = status; }
        
        public String getResolutionNotes() { return resolutionNotes; }
        public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }
    }
    
    public static class SetResolutionDateRequest {
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate estimatedDate;
        
        public LocalDate getEstimatedDate() { return estimatedDate; }
        public void setEstimatedDate(LocalDate estimatedDate) { this.estimatedDate = estimatedDate; }
    }
    
    public static class AddCommentRequest {
        private String comment;
        private IssueComment.CommentType commentType = IssueComment.CommentType.UPDATE;
        private Boolean isInternal = false;
        
        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
        
        public IssueComment.CommentType getCommentType() { return commentType; }
        public void setCommentType(IssueComment.CommentType commentType) { this.commentType = commentType; }
        
        public Boolean getIsInternal() { return isInternal; }
        public void setIsInternal(Boolean isInternal) { this.isInternal = isInternal; }
    }
    
    // Response DTO for location view (without GPS coordinates)
    public static class IssueLocationResponse {
        private Long id;
        private String ticketNumber;
        private String title;
        private Issue.IssueCategory category;
        private Issue.IssuePriority priority;
        private Issue.IssueStatus status;
        private String address;
        private String area;
        private String village;
        private String district;
        private String reporterName;

        public IssueLocationResponse(Issue issue) {
            this.id = issue.getId();
            this.ticketNumber = issue.getTicketNumber();
            this.title = issue.getTitle();
            this.category = issue.getCategory();
            this.priority = issue.getPriority();
            this.status = issue.getStatus();
            this.address = issue.getAddress();
            this.area = issue.getArea();
            this.village = issue.getVillage();
            this.district = issue.getDistrict();
            this.reporterName = issue.getReporterName();
        }
        
        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getTicketNumber() { return ticketNumber; }
        public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public Issue.IssueCategory getCategory() { return category; }
        public void setCategory(Issue.IssueCategory category) { this.category = category; }
        
        public Issue.IssuePriority getPriority() { return priority; }
        public void setPriority(Issue.IssuePriority priority) { this.priority = priority; }
        
        public Issue.IssueStatus getStatus() { return status; }
        public void setStatus(Issue.IssueStatus status) { this.status = status; }
        
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public String getArea() { return area; }
        public void setArea(String area) { this.area = area; }

        public String getVillage() { return village; }
        public void setVillage(String village) { this.village = village; }

        public String getDistrict() { return district; }
        public void setDistrict(String district) { this.district = district; }

        public String getReporterName() { return reporterName; }
        public void setReporterName(String reporterName) { this.reporterName = reporterName; }
    }
}
