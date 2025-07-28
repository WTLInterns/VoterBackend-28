package com.votersystem.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.votersystem.dto.CreateIssueRequest;
import com.votersystem.dto.IssueResponse;
import com.votersystem.dto.IssueStatisticsResponse;
import com.votersystem.entity.Administrator;
import com.votersystem.entity.Agent;
import com.votersystem.entity.Issue;
import com.votersystem.entity.IssueAttachment;
import com.votersystem.entity.IssueComment;
import com.votersystem.repository.AdministratorRepository;
import com.votersystem.repository.AgentRepository;
import com.votersystem.repository.IssueCommentRepository;
import com.votersystem.repository.IssueRepository;

/**
 * Service for managing issues in the issue reporting system
 */
@Service
public class IssueService {
    
    private static final Logger logger = LoggerFactory.getLogger(IssueService.class);
    
    @Autowired
    private IssueRepository issueRepository;
    
    @Autowired
    private IssueCommentRepository issueCommentRepository;
    
    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private AdministratorRepository administratorRepository;

    @Autowired
    private TicketGenerationService ticketGenerationService;

    @Autowired
    private MediaService mediaService;

    @Autowired
    private IssueAttachmentService attachmentService;
    
    /**
     * Create a new issue
     */
    @Transactional
    public Issue createIssue(Issue issue) {
        try {
            // Generate unique ticket number
            String ticketNumber = ticketGenerationService.generateTicketNumber();
            issue.setTicketNumber(ticketNumber);
            
            // Set submission date
            issue.setSubmissionDate(LocalDateTime.now());
            
            // Get reporter name from agent
            Optional<Agent> agent = agentRepository.findById(issue.getReportedBy());
            if (agent.isPresent()) {
                issue.setReporterName(agent.get().getFirstName() + " " + agent.get().getLastName());
            }
            
            // Save issue
            Issue savedIssue = issueRepository.save(issue);

            logger.info("Created new issue with ticket: {}", ticketNumber);
            return savedIssue;
            
        } catch (Exception e) {
            logger.error("Failed to create issue: {}", e.getMessage());
            throw new RuntimeException("Failed to create issue", e);
        }
    }

    /**
     * Create a new issue with media files
     */
    @Transactional
    public Issue createIssueWithMedia(CreateIssueRequest request, List<MultipartFile> files, String agentId) {
        try {
            // Create issue entity
            Issue issue = new Issue();
            issue.setTitle(request.getTitle());
            issue.setDescription(request.getDescription());
            issue.setCategory(request.getCategory());
            issue.setPriority(request.getPriority());
            issue.setReportedBy(agentId);
            issue.setAddress(request.getAddress());
            issue.setArea(request.getArea());
            issue.setVillage(request.getVillage());
            issue.setDistrict(request.getDistrict());

            // Generate ticket number and set defaults
            String ticketNumber = ticketGenerationService.generateTicketNumber();
            issue.setTicketNumber(ticketNumber);
            issue.setSubmissionDate(LocalDateTime.now());
            issue.setStatus(Issue.IssueStatus.OPEN);

            // Get reporter name from agent
            Optional<Agent> agent = agentRepository.findById(agentId);
            if (agent.isPresent()) {
                issue.setReporterName(agent.get().getFirstName() + " " + agent.get().getLastName());
            }

            // Save issue first
            Issue savedIssue = issueRepository.save(issue);

            // Upload and save media files
            if (files != null && !files.isEmpty()) {
                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        try {
                            // Determine file type
                            IssueAttachment.FileType fileType = determineFileType(file);

                            // Upload to Cloudinary
                            String folder = "issues/" + savedIssue.getTicketNumber();
                            MediaService.MediaUploadResult uploadResult;

                            if (fileType == IssueAttachment.FileType.IMAGE) {
                                uploadResult = mediaService.uploadImage(file, folder).join();
                            } else {
                                uploadResult = mediaService.uploadVideo(file, folder).join();
                            }

                            if (uploadResult.isSuccess()) {
                                // Save attachment record
                                attachmentService.createAttachment(
                                    savedIssue, file.getOriginalFilename(), uploadResult.getUrl(),
                                    fileType, uploadResult.getPublicId(), file.getSize(),
                                    file.getContentType(), agentId);

                                logger.info("Uploaded {} for issue {}", fileType.name().toLowerCase(), ticketNumber);
                            } else {
                                logger.warn("Failed to upload file {} for issue {}: {}",
                                           file.getOriginalFilename(), ticketNumber, uploadResult.getMessage());
                            }
                        } catch (Exception e) {
                            logger.error("Error uploading file {} for issue {}: {}",
                                        file.getOriginalFilename(), ticketNumber, e.getMessage());
                        }
                    }
                }
            }

            logger.info("Created issue {} with {} media files", ticketNumber,
                       files != null ? files.size() : 0);
            return savedIssue;

        } catch (Exception e) {
            logger.error("Failed to create issue with media: {}", e.getMessage());
            throw new RuntimeException("Failed to create issue with media", e);
        }
    }

    /**
     * Determine file type based on content type
     */
    private IssueAttachment.FileType determineFileType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null) {
            if (contentType.startsWith("image/")) {
                return IssueAttachment.FileType.IMAGE;
            } else if (contentType.startsWith("video/")) {
                return IssueAttachment.FileType.VIDEO;
            }
        }

        // Fallback to filename extension
        String filename = file.getOriginalFilename();
        if (filename != null) {
            String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
            switch (extension) {
                case "jpg":
                case "jpeg":
                case "png":
                case "gif":
                case "bmp":
                case "webp":
                    return IssueAttachment.FileType.IMAGE;
                case "mp4":
                case "avi":
                case "mov":
                case "wmv":
                case "flv":
                case "webm":
                    return IssueAttachment.FileType.VIDEO;
            }
        }

        // Default to image
        return IssueAttachment.FileType.IMAGE;
    }

    /**
     * Get issue by ID
     */
    public Optional<Issue> getIssueById(Long id) {
        return issueRepository.findById(id);
    }
    
    /**
     * Get issue by ticket number
     */
    public Optional<Issue> getIssueByTicketNumber(String ticketNumber) {
        return issueRepository.findByTicketNumber(ticketNumber);
    }
    
    /**
     * Get issues by reporter (agent)
     */
    public Page<Issue> getIssuesByReporter(String reportedBy, Pageable pageable) {
        return issueRepository.findByReportedBy(reportedBy, pageable);
    }
    
    /**
     * Get all issues with pagination
     */
    public Page<Issue> getAllIssues(Pageable pageable) {
        return issueRepository.findAll(pageable);
    }
    
    /**
     * Search issues with filters including date range
     */
    public Page<Issue> searchIssues(Issue.IssueStatus status, Issue.IssueCategory category,
                                   Issue.IssuePriority priority, String reportedBy,
                                   String village, String district, String dateFrom, String dateTo,
                                   String searchTerm, Pageable pageable) {

        // Parse date filters
        LocalDateTime fromDate = null;
        LocalDateTime toDate = null;

        try {
            if (dateFrom != null && !dateFrom.isEmpty()) {
                fromDate = LocalDateTime.parse(dateFrom + "T00:00:00");
            }
            if (dateTo != null && !dateTo.isEmpty()) {
                toDate = LocalDateTime.parse(dateTo + "T23:59:59");
            }
            logger.info("Master Admin date filtering: fromDate={}, toDate={}", fromDate, toDate);
        } catch (Exception e) {
            logger.warn("Invalid date format in search filters: dateFrom={}, dateTo={}", dateFrom, dateTo);
        }

        return issueRepository.findIssuesWithFiltersAndDateRange(status, category, priority,
                                                               reportedBy, village, district,
                                                               fromDate, toDate, searchTerm, pageable);
    }
    
    /**
     * Update issue status
     */
    @Transactional
    public Issue updateIssueStatus(Long issueId, Issue.IssueStatus newStatus, String updatedBy) {
        try {
            Optional<Issue> issueOpt = issueRepository.findById(issueId);
            if (issueOpt.isEmpty()) {
                throw new RuntimeException("Issue not found");
            }
            
            Issue issue = issueOpt.get();
            Issue.IssueStatus oldStatus = issue.getStatus();
            issue.setStatus(newStatus);
            
            // Set resolution date if resolved
            if (newStatus == Issue.IssueStatus.RESOLVED || newStatus == Issue.IssueStatus.CLOSED) {
                issue.setActualResolutionDate(LocalDate.now());
                issue.setResolvedBy(updatedBy);
            }
            
            Issue savedIssue = issueRepository.save(issue);
            
            // Add status change comment
            addStatusChangeComment(issue, oldStatus, newStatus, updatedBy);
            
            logger.info("Updated issue {} status from {} to {}", issue.getTicketNumber(), oldStatus, newStatus);
            return savedIssue;
            
        } catch (Exception e) {
            logger.error("Failed to update issue status: {}", e.getMessage());
            throw new RuntimeException("Failed to update issue status", e);
        }
    }
    
    /**
     * Reopen issue (only by agent who reported it)
     */
    @Transactional
    public Issue reopenIssue(Long issueId, String reopenedBy, String reopenReason) {
        try {
            Optional<Issue> issueOpt = issueRepository.findById(issueId);
            if (issueOpt.isEmpty()) {
                throw new RuntimeException("Issue not found");
            }
            
            Issue issue = issueOpt.get();
            
            // Validate that only the reporter can reopen
            if (!issue.getReportedBy().equals(reopenedBy)) {
                throw new RuntimeException("Only the reporter can reopen this issue");
            }
            
            // Validate that issue can be reopened
            if (issue.getStatus() != Issue.IssueStatus.RESOLVED && 
                issue.getStatus() != Issue.IssueStatus.CLOSED) {
                throw new RuntimeException("Issue cannot be reopened in current status");
            }
            
            // Update issue
            issue.setStatus(Issue.IssueStatus.REOPENED);
            issue.setReopenedBy(reopenedBy);
            issue.setReopenedDate(LocalDateTime.now());
            issue.setReopenReason(reopenReason);
            issue.setReopenedCount(issue.getReopenedCount() + 1);
            
            // Clear resolution data
            issue.setActualResolutionDate(null);
            issue.setResolvedBy(null);
            
            Issue savedIssue = issueRepository.save(issue);
            
            // Add reopen comment
            addReopenComment(issue, reopenReason, reopenedBy);
            
            logger.info("Reopened issue {} by {}", issue.getTicketNumber(), reopenedBy);
            return savedIssue;
            
        } catch (Exception e) {
            logger.error("Failed to reopen issue: {}", e.getMessage());
            throw new RuntimeException("Failed to reopen issue", e);
        }
    }
    
    /**
     * Set estimated resolution date
     */
    @Transactional
    public Issue setEstimatedResolutionDate(Long issueId, LocalDate estimatedDate, String updatedBy) {
        try {
            Optional<Issue> issueOpt = issueRepository.findById(issueId);
            if (issueOpt.isEmpty()) {
                throw new RuntimeException("Issue not found");
            }
            
            Issue issue = issueOpt.get();
            issue.setEstimatedResolutionDate(estimatedDate);
            
            Issue savedIssue = issueRepository.save(issue);
            
            // Add comment about estimated date
            addEstimatedDateComment(issue, estimatedDate, updatedBy);
            
            logger.info("Set estimated resolution date for issue {} to {}", issue.getTicketNumber(), estimatedDate);
            return savedIssue;
            
        } catch (Exception e) {
            logger.error("Failed to set estimated resolution date: {}", e.getMessage());
            throw new RuntimeException("Failed to set estimated resolution date", e);
        }
    }
    
    /**
     * Get issues that can be reopened by agent
     */
    public List<Issue> getReopenableIssues(String agentId) {
        return issueRepository.findReopenableIssuesByReporter(agentId);
    }
    
    /**
     * Get issue statistics
     */
    public IssueStatistics getIssueStatistics() {
        IssueStatistics stats = new IssueStatistics();

        stats.setTotalIssues(issueRepository.count());
        stats.setOpenIssues(issueRepository.countByStatus(Issue.IssueStatus.OPEN));
        stats.setInProgressIssues(issueRepository.countByStatus(Issue.IssueStatus.IN_PROGRESS));
        stats.setResolvedIssues(issueRepository.countByStatus(Issue.IssueStatus.RESOLVED));
        stats.setClosedIssues(issueRepository.countByStatus(Issue.IssueStatus.CLOSED));
        stats.setReopenedIssues(issueRepository.countByStatus(Issue.IssueStatus.REOPENED));

        stats.setIssuesToday(issueRepository.findIssuesToday().size());
        stats.setIssuesThisWeek(issueRepository.findIssuesThisWeek(LocalDateTime.now().minusDays(7)).size());
        stats.setIssuesThisMonth(issueRepository.findIssuesThisMonth().size());

        stats.setOverdueIssues(issueRepository.findOverdueIssues().size());

        return stats;
    }

    /**
     * Get issues with address for location view
     */
    public List<Issue> getIssuesWithAddress() {
        return issueRepository.findIssuesWithAddress();
    }

    /**
     * Get overdue issues
     */
    public List<Issue> getOverdueIssues() {
        return issueRepository.findOverdueIssues();
    }
    
    // Helper methods
    private void addStatusChangeComment(Issue issue, Issue.IssueStatus oldStatus, 
                                       Issue.IssueStatus newStatus, String updatedBy) {
        IssueComment comment = new IssueComment();
        comment.setIssue(issue);
        comment.setComment(String.format("Status changed from %s to %s", 
                                        oldStatus.getDisplayName(), newStatus.getDisplayName()));
        comment.setCommentedBy(updatedBy);
        comment.setCommentType(IssueComment.CommentType.UPDATE);
        comment.setIsInternal(false);
        
        issueCommentRepository.save(comment);
    }
    
    private void addReopenComment(Issue issue, String reopenReason, String reopenedBy) {
        IssueComment comment = new IssueComment();
        comment.setIssue(issue);
        comment.setComment("Issue reopened. Reason: " + reopenReason);
        comment.setCommentedBy(reopenedBy);
        comment.setCommentType(IssueComment.CommentType.REOPEN);
        comment.setIsInternal(false);
        
        issueCommentRepository.save(comment);
    }
    
    private void addEstimatedDateComment(Issue issue, LocalDate estimatedDate, String updatedBy) {
        IssueComment comment = new IssueComment();
        comment.setIssue(issue);
        comment.setComment("Estimated resolution date set to: " + estimatedDate);
        comment.setCommentedBy(updatedBy);
        comment.setCommentType(IssueComment.CommentType.UPDATE);
        comment.setIsInternal(false);
        
        issueCommentRepository.save(comment);
    }
    
    // Statistics class
    public static class IssueStatistics {
        private long totalIssues;
        private long openIssues;
        private long inProgressIssues;
        private long resolvedIssues;
        private long closedIssues;
        private long reopenedIssues;
        private long issuesToday;
        private long issuesThisWeek;
        private long issuesThisMonth;
        private long overdueIssues;
        
        // Getters and setters
        public long getTotalIssues() { return totalIssues; }
        public void setTotalIssues(long totalIssues) { this.totalIssues = totalIssues; }
        
        public long getOpenIssues() { return openIssues; }
        public void setOpenIssues(long openIssues) { this.openIssues = openIssues; }
        
        public long getInProgressIssues() { return inProgressIssues; }
        public void setInProgressIssues(long inProgressIssues) { this.inProgressIssues = inProgressIssues; }
        
        public long getResolvedIssues() { return resolvedIssues; }
        public void setResolvedIssues(long resolvedIssues) { this.resolvedIssues = resolvedIssues; }
        
        public long getClosedIssues() { return closedIssues; }
        public void setClosedIssues(long closedIssues) { this.closedIssues = closedIssues; }
        
        public long getReopenedIssues() { return reopenedIssues; }
        public void setReopenedIssues(long reopenedIssues) { this.reopenedIssues = reopenedIssues; }
        
        public long getIssuesToday() { return issuesToday; }
        public void setIssuesToday(long issuesToday) { this.issuesToday = issuesToday; }
        
        public long getIssuesThisWeek() { return issuesThisWeek; }
        public void setIssuesThisWeek(long issuesThisWeek) { this.issuesThisWeek = issuesThisWeek; }
        
        public long getIssuesThisMonth() { return issuesThisMonth; }
        public void setIssuesThisMonth(long issuesThisMonth) { this.issuesThisMonth = issuesThisMonth; }
        
        public long getOverdueIssues() { return overdueIssues; }
        public void setOverdueIssues(long overdueIssues) { this.overdueIssues = overdueIssues; }
    }

    // Sub-Admin specific methods

    /**
     * Get issues for sub-admin (only issues from agents they created)
     * PRODUCTION-READY: Handles both old issues (with mobile numbers) and new issues (with agent IDs)
     */
    public Page<IssueResponse> getIssuesForSubAdmin(String subAdminUsername, String status, String category,
                                                   String priority, String dateFrom, String dateTo,
                                                   String search, Pageable pageable) {
        try {
            // Get all agents created by this sub-admin
            List<Agent> subAdminAgents = agentRepository.findByCreatedBy(subAdminUsername);

            if (subAdminAgents.isEmpty()) {
                logger.debug("No agents found for sub-admin: {}", subAdminUsername);
                return new PageImpl<>(new ArrayList<>(), pageable, 0);
            }

            // Extract both agent IDs and mobile numbers for dual lookup
            List<String> agentIds = subAdminAgents.stream()
                    .map(Agent::getId)
                    .collect(Collectors.toList());

            List<String> agentMobiles = subAdminAgents.stream()
                    .map(Agent::getMobile)
                    .collect(Collectors.toList());

            // Parse date filters
            final LocalDateTime fromDate = (dateFrom != null && !dateFrom.isEmpty())
                ? LocalDateTime.parse(dateFrom + "T00:00:00") : null;
            final LocalDateTime toDate = (dateTo != null && !dateTo.isEmpty())
                ? LocalDateTime.parse(dateTo + "T23:59:59") : null;

            logger.info("SubAdmin {} searching for issues from {} agents (IDs: {}, Mobiles: {})",
                       subAdminUsername, subAdminAgents.size(), agentIds, agentMobiles);

            // DUAL LOOKUP: Search by both agent IDs (new issues) and mobile numbers (old issues)
            List<Issue> allIssues = new ArrayList<>();

            // Search by agent IDs (new format)
            for (String agentId : agentIds) {
                List<Issue> agentIssues = issueRepository.findByReportedBy(agentId);
                allIssues.addAll(agentIssues);
                logger.debug("Found {} issues for agent ID: {}", agentIssues.size(), agentId);
            }

            // Search by mobile numbers (old format) - avoid duplicates
            Set<Long> existingIssueIds = allIssues.stream()
                    .map(Issue::getId)
                    .collect(Collectors.toSet());

            for (String mobile : agentMobiles) {
                List<Issue> mobileIssues = issueRepository.findByReportedBy(mobile);
                for (Issue issue : mobileIssues) {
                    if (!existingIssueIds.contains(issue.getId())) {
                        allIssues.add(issue);
                        existingIssueIds.add(issue.getId());
                    }
                }
                logger.debug("Found {} issues for mobile: {}", mobileIssues.size(), mobile);
            }

            logger.info("Total issues found for sub-admin {}: {}", subAdminUsername, allIssues.size());

            // Apply filters manually (this could be optimized with a custom query later)
            List<Issue> filteredIssues = allIssues.stream()
                .filter(issue -> status == null || status.isEmpty() || issue.getStatus().name().equals(status))
                .filter(issue -> category == null || category.isEmpty() || issue.getCategory().name().equals(category))
                .filter(issue -> priority == null || priority.isEmpty() || issue.getPriority().name().equals(priority))
                .filter(issue -> fromDate == null || !issue.getSubmissionDate().isBefore(fromDate))
                .filter(issue -> toDate == null || !issue.getSubmissionDate().isAfter(toDate))
                .filter(issue -> search == null || search.isEmpty() ||
                    issue.getTitle().toLowerCase().contains(search.toLowerCase()) ||
                    issue.getDescription().toLowerCase().contains(search.toLowerCase()) ||
                    issue.getTicketNumber().toLowerCase().contains(search.toLowerCase()))
                .collect(Collectors.toList());

            // Convert to Page (simple implementation)
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), filteredIssues.size());
            List<Issue> pageContent = filteredIssues.subList(start, end);

            // Convert to IssueResponse with agent information
            List<IssueResponse> responseList = pageContent.stream()
                .map(issue -> {
                    // Find the agent for this issue
                    Agent agent = subAdminAgents.stream()
                        .filter(a -> a.getId().equals(issue.getReportedBy()) ||
                                   a.getMobile().equals(issue.getReportedBy()))
                        .findFirst()
                        .orElse(null);

                    return new IssueResponse(issue, agent);
                })
                .collect(Collectors.toList());

            return new PageImpl<>(responseList, pageable, filteredIssues.size());

        } catch (Exception e) {
            logger.error("Error getting issues for sub-admin: {}", subAdminUsername, e);
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    /**
     * Get issue by ID for sub-admin (only if it belongs to their agent)
     */
    public IssueResponse getIssueByIdForSubAdmin(String issueId, String subAdminUsername) {
        Optional<Issue> issueOpt = issueRepository.findById(Long.parseLong(issueId));
        if (issueOpt.isEmpty()) {
            throw new RuntimeException("Issue not found");
        }
        Issue issue = issueOpt.get();

        // Check if the issue belongs to an agent created by this sub-admin
        // PRODUCTION-READY: Handle both agent ID and mobile number formats
        Agent agent = null;

        // Try to find agent by ID first (new format)
        Optional<Agent> agentOpt = agentRepository.findById(issue.getReportedBy());
        if (agentOpt.isPresent()) {
            agent = agentOpt.get();
        } else {
            // Try to find agent by mobile number (old format)
            agentOpt = agentRepository.findByMobile(issue.getReportedBy());
            if (agentOpt.isPresent()) {
                agent = agentOpt.get();
            }
        }

        if (agent == null) {
            throw new RuntimeException("Agent not found for reportedBy: " + issue.getReportedBy());
        }

        if (!subAdminUsername.equals(agent.getCreatedBy())) {
            throw new RuntimeException("Access denied: Issue does not belong to your agents");
        }

        return new IssueResponse(issue);
    }

    /**
     * Update issue status for sub-admin
     */
    public IssueResponse updateIssueStatusForSubAdmin(String issueId, String newStatus, String subAdminUsername) {
        Optional<Issue> issueOpt = issueRepository.findById(Long.parseLong(issueId));
        if (issueOpt.isEmpty()) {
            throw new RuntimeException("Issue not found");
        }
        Issue issue = issueOpt.get();

        // Check if the issue belongs to an agent created by this sub-admin
        // PRODUCTION-READY: Handle both agent ID and mobile number formats
        Agent agent = null;

        // Try to find agent by ID first (new format)
        Optional<Agent> agentOpt = agentRepository.findById(issue.getReportedBy());
        if (agentOpt.isPresent()) {
            agent = agentOpt.get();
        } else {
            // Try to find agent by mobile number (old format)
            agentOpt = agentRepository.findByMobile(issue.getReportedBy());
            if (agentOpt.isPresent()) {
                agent = agentOpt.get();
            }
        }

        if (agent == null) {
            throw new RuntimeException("Agent not found for reportedBy: " + issue.getReportedBy());
        }

        if (!subAdminUsername.equals(agent.getCreatedBy())) {
            throw new RuntimeException("Access denied: Issue does not belong to your agents");
        }

        String oldStatus = issue.getStatus().toString();
        issue.setStatus(Issue.IssueStatus.valueOf(newStatus));
        issue = issueRepository.save(issue);

        return new IssueResponse(issue);
    }

    /**
     * Add comment for agent
     */
    public IssueResponse addCommentForAgent(String issueId, String comment, String agentId) {
        Optional<Issue> issueOpt = issueRepository.findById(Long.parseLong(issueId));
        if (issueOpt.isEmpty()) {
            throw new RuntimeException("Issue not found");
        }
        Issue issue = issueOpt.get();

        // Check if the issue belongs to this agent
        if (!agentId.equals(issue.getReportedBy())) {
            throw new RuntimeException("Access denied: You can only comment on your own issues");
        }

        // Get agent name for the comment
        String commenterName = getAgentCommenterName(agentId);

        IssueComment issueComment = new IssueComment();
        issueComment.setIssue(issue);
        issueComment.setComment(comment);
        issueComment.setCommentedBy(agentId);
        issueComment.setCommenterName(commenterName);
        issueComment.setCommentType(IssueComment.CommentType.UPDATE);
        issueComment.setIsInternal(false);

        issueCommentRepository.save(issueComment);

        // Reload issue with comments
        issueOpt = issueRepository.findById(Long.parseLong(issueId));
        issue = issueOpt.get();
        return new IssueResponse(issue);
    }

    /**
     * Add comment for sub-admin
     */
    public IssueResponse addCommentForSubAdmin(String issueId, String comment, String subAdminUsername) {
        Optional<Issue> issueOpt = issueRepository.findById(Long.parseLong(issueId));
        if (issueOpt.isEmpty()) {
            throw new RuntimeException("Issue not found");
        }
        Issue issue = issueOpt.get();

        // Check if the issue belongs to an agent created by this sub-admin
        // PRODUCTION-READY: Handle both agent ID and mobile number formats
        Agent agent = null;

        // Try to find agent by ID first (new format)
        Optional<Agent> agentOpt = agentRepository.findById(issue.getReportedBy());
        if (agentOpt.isPresent()) {
            agent = agentOpt.get();
        } else {
            // Try to find agent by mobile number (old format)
            agentOpt = agentRepository.findByMobile(issue.getReportedBy());
            if (agentOpt.isPresent()) {
                agent = agentOpt.get();
            }
        }

        if (agent == null) {
            throw new RuntimeException("Agent not found for reportedBy: " + issue.getReportedBy());
        }

        if (!subAdminUsername.equals(agent.getCreatedBy())) {
            throw new RuntimeException("Access denied: Issue does not belong to your agents");
        }

        // Get sub-admin name for the comment
        String commenterName = getCommenterName(subAdminUsername);

        IssueComment issueComment = new IssueComment();
        issueComment.setIssue(issue);
        issueComment.setComment(comment);
        issueComment.setCommentedBy(subAdminUsername);
        issueComment.setCommenterName(commenterName);
        issueComment.setCommentType(IssueComment.CommentType.UPDATE);
        issueComment.setIsInternal(false);

        issueCommentRepository.save(issueComment);

        // Reload issue with comments
        issueOpt = issueRepository.findById(Long.parseLong(issueId));
        issue = issueOpt.get();
        return new IssueResponse(issue);
    }

    /**
     * Set resolution date for sub-admin
     */
    public IssueResponse setResolutionDateForSubAdmin(String issueId, String resolutionDate, String subAdminUsername) {
        Optional<Issue> issueOpt = issueRepository.findById(Long.parseLong(issueId));
        if (issueOpt.isEmpty()) {
            throw new RuntimeException("Issue not found");
        }
        Issue issue = issueOpt.get();

        // Check if the issue belongs to an agent created by this sub-admin
        // PRODUCTION-READY: Handle both agent ID and mobile number formats
        Agent agent = null;

        // Try to find agent by ID first (new format)
        Optional<Agent> agentOpt = agentRepository.findById(issue.getReportedBy());
        if (agentOpt.isPresent()) {
            agent = agentOpt.get();
        } else {
            // Try to find agent by mobile number (old format)
            agentOpt = agentRepository.findByMobile(issue.getReportedBy());
            if (agentOpt.isPresent()) {
                agent = agentOpt.get();
            }
        }

        if (agent == null) {
            throw new RuntimeException("Agent not found for reportedBy: " + issue.getReportedBy());
        }

        if (!subAdminUsername.equals(agent.getCreatedBy())) {
            throw new RuntimeException("Access denied: Issue does not belong to your agents");
        }

        if (resolutionDate != null && !resolutionDate.isEmpty()) {
            issue.setActualResolutionDate(java.time.LocalDate.parse(resolutionDate));
        } else {
            issue.setActualResolutionDate(null);
        }

        issue = issueRepository.save(issue);
        return new IssueResponse(issue);
    }

    /**
     * Get statistics for sub-admin
     */
    public IssueStatisticsResponse getStatisticsForSubAdmin(String subAdminUsername) {
        try {
            logger.debug("Getting statistics for sub-admin: {}", subAdminUsername);

            // Get all agents created by this sub-admin
            List<Agent> subAdminAgents = agentRepository.findByCreatedBy(subAdminUsername);

            if (subAdminAgents.isEmpty()) {
                return new IssueStatisticsResponse(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
            }

            // Extract both agent IDs and mobile numbers for dual lookup
            List<String> agentIds = subAdminAgents.stream()
                    .map(Agent::getId)
                    .collect(Collectors.toList());

            List<String> agentMobiles = subAdminAgents.stream()
                    .map(Agent::getMobile)
                    .collect(Collectors.toList());

            // DUAL LOOKUP: Get all issues from these agents (both old and new format)
            List<Issue> allIssues = new ArrayList<>();

            // Search by agent IDs (new format)
            for (String agentId : agentIds) {
                List<Issue> agentIssues = issueRepository.findByReportedBy(agentId);
                allIssues.addAll(agentIssues);
            }

            // Search by mobile numbers (old format) - avoid duplicates
            Set<Long> existingIssueIds = allIssues.stream()
                    .map(Issue::getId)
                    .collect(Collectors.toSet());

            for (String mobile : agentMobiles) {
                List<Issue> mobileIssues = issueRepository.findByReportedBy(mobile);
                for (Issue issue : mobileIssues) {
                    if (!existingIssueIds.contains(issue.getId())) {
                        allIssues.add(issue);
                        existingIssueIds.add(issue.getId());
                    }
                }
            }

            // Calculate statistics
            int totalIssues = allIssues.size();
            int openIssues = (int) allIssues.stream().filter(i -> i.getStatus() == Issue.IssueStatus.OPEN).count();
            int inProgressIssues = (int) allIssues.stream().filter(i -> i.getStatus() == Issue.IssueStatus.IN_PROGRESS).count();
            int resolvedIssues = (int) allIssues.stream().filter(i -> i.getStatus() == Issue.IssueStatus.RESOLVED).count();
            int closedIssues = (int) allIssues.stream().filter(i -> i.getStatus() == Issue.IssueStatus.CLOSED).count();

            // Category statistics
            int infrastructureIssues = (int) allIssues.stream().filter(i -> i.getCategory() == Issue.IssueCategory.INFRASTRUCTURE).count();
            int waterIssues = (int) allIssues.stream().filter(i -> i.getCategory() == Issue.IssueCategory.WATER).count();
            int electricityIssues = (int) allIssues.stream().filter(i -> i.getCategory() == Issue.IssueCategory.ELECTRICITY).count();
            int environmentIssues = (int) allIssues.stream().filter(i -> i.getCategory() == Issue.IssueCategory.ENVIRONMENT).count();
            int otherIssues = (int) allIssues.stream().filter(i -> i.getCategory() == Issue.IssueCategory.OTHER).count();

            return new IssueStatisticsResponse(
                totalIssues, openIssues, inProgressIssues, resolvedIssues, closedIssues,
                infrastructureIssues, waterIssues, electricityIssues, environmentIssues, otherIssues
            );

        } catch (Exception e) {
            logger.error("Error getting statistics for sub-admin: {}", subAdminUsername, e);
            return new IssueStatisticsResponse(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        }
    }

    /**
     * Get issues for map view for sub-admin
     */
    public List<IssueResponse> getIssuesForMapViewForSubAdmin(String subAdminUsername, String status,
                                                             String category, String priority) {
        // For now, return empty list - this would need proper repository implementation
        return new java.util.ArrayList<>();
    }

    /**
     * Export issues for sub-admin
     */
    public List<IssueResponse> exportIssuesForSubAdmin(String subAdminUsername, String status, String category,
                                                      String priority, String dateFrom, String dateTo) {
        // For now, return empty list - this would need proper repository implementation
        return new java.util.ArrayList<>();
    }

    /**
     * Get commenter name from username
     */
    private String getCommenterName(String username) {
        try {
            // Try to get from Administrator table
            Optional<Administrator> adminOpt = administratorRepository.findByUsername(username);
            if (adminOpt.isPresent()) {
                Administrator admin = adminOpt.get();
                return admin.getFirstName() + " " + admin.getLastName();
            }

            // If not found, return username
            return username;

        } catch (Exception e) {
            logger.warn("Failed to get commenter name for {}: {}", username, e.getMessage());
            return username;
        }
    }

    /**
     * Get agent commenter name from agent ID
     */
    private String getAgentCommenterName(String agentId) {
        try {
            // Try to get from Agent table
            Optional<Agent> agentOpt = agentRepository.findById(agentId);
            if (agentOpt.isPresent()) {
                Agent agent = agentOpt.get();
                return agent.getFirstName() + " " + agent.getLastName();
            }

            // If not found, return agent ID
            return agentId;

        } catch (Exception e) {
            logger.warn("Failed to get agent commenter name for {}: {}", agentId, e.getMessage());
            return agentId;
        }
    }

    /**
     * Get updated issues for agent since timestamp (for polling)
     */
    public List<IssueResponse> getUpdatedIssuesForAgent(String agentId, LocalDateTime sinceTimestamp) {
        try {
            List<Issue> issues;

            if (sinceTimestamp != null) {
                // Get issues updated since the given timestamp
                issues = issueRepository.findByReportedByAndUpdatedAtAfterOrderByUpdatedAtDesc(agentId, sinceTimestamp);
            } else {
                // If no timestamp provided, return all issues (fallback)
                issues = issueRepository.findByReportedByOrderByCreatedAtDesc(agentId);
            }

            // Get agent information for the response
            Optional<Agent> agentOpt = agentRepository.findById(agentId);
            Agent agent = agentOpt.orElse(null);

            return issues.stream()
                    .map(issue -> new IssueResponse(issue, agent))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Failed to get updated issues for agent {}: {}", agentId, e.getMessage());
            return new ArrayList<>();
        }
    }
}
