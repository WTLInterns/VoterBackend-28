package com.votersystem.controller;

import com.votersystem.entity.Issue;
import com.votersystem.entity.IssueAttachment;
import com.votersystem.service.IssueService;
import com.votersystem.service.IssueAttachmentService;
import com.votersystem.service.MediaService;
import com.votersystem.util.ApiResponse;
import com.votersystem.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Controller for handling issue media uploads (images/videos)
 */
@RestController
@RequestMapping("/issues/{issueId}/attachments")
@CrossOrigin(origins = "*")
public class IssueMediaController {
    
    private static final Logger logger = LoggerFactory.getLogger(IssueMediaController.class);
    
    @Autowired
    private IssueService issueService;
    
    @Autowired
    private IssueAttachmentService attachmentService;
    
    @Autowired
    private MediaService mediaService;

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
     * Upload image attachment to issue
     */
    @PostMapping("/image")
    @PreAuthorize("hasRole('AGENT')")
    public CompletableFuture<ResponseEntity<ApiResponse<IssueAttachment>>> uploadImage(
            @PathVariable Long issueId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication,
            HttpServletRequest request) {

        // Extract agent ID from JWT token before async call
        String agentId = getAgentIdFromToken(request);
        if (agentId == null) {
            return CompletableFuture.completedFuture(
                ResponseEntity.badRequest()
                    .body(ApiResponse.error("Unable to extract agent ID from token"))
            );
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                
                // Validate issue exists and agent has access
                Optional<Issue> issueOpt = issueService.getIssueById(issueId);
                if (issueOpt.isEmpty()) {
                    return ResponseEntity.notFound().build();
                }
                
                Issue issue = issueOpt.get();
                if (!issue.getReportedBy().equals(agentId)) {
                    return ResponseEntity.status(403)
                            .body(ApiResponse.error("Access denied to this issue"));
                }
                
                // Upload to Cloudinary
                String folder = "issues/" + issue.getTicketNumber();
                MediaService.MediaUploadResult uploadResult = mediaService.uploadImage(file, folder).join();
                
                if (!uploadResult.isSuccess()) {
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.error("Failed to upload image: " + uploadResult.getMessage()));
                }
                
                // Save attachment record
                IssueAttachment attachment = attachmentService.createAttachment(
                        issue, file.getOriginalFilename(), uploadResult.getUrl(), 
                        IssueAttachment.FileType.IMAGE, uploadResult.getPublicId(), 
                        file.getSize(), file.getContentType(), agentId);
                
                logger.info("Agent {} uploaded image to issue {}", agentId, issue.getTicketNumber());
                return ResponseEntity.ok(ApiResponse.success(attachment, "Image uploaded successfully"));
                
            } catch (Exception e) {
                logger.error("Failed to upload image: {}", e.getMessage());
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Failed to upload image: " + e.getMessage()));
            }
        });
    }
    
    /**
     * Upload video attachment to issue
     */
    @PostMapping("/video")
    @PreAuthorize("hasRole('AGENT')")
    public CompletableFuture<ResponseEntity<ApiResponse<IssueAttachment>>> uploadVideo(
            @PathVariable Long issueId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication,
            HttpServletRequest request) {

        // Extract agent ID from JWT token before async call
        String agentId = getAgentIdFromToken(request);
        if (agentId == null) {
            return CompletableFuture.completedFuture(
                ResponseEntity.badRequest()
                    .body(ApiResponse.error("Unable to extract agent ID from token"))
            );
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                
                // Validate issue exists and agent has access
                Optional<Issue> issueOpt = issueService.getIssueById(issueId);
                if (issueOpt.isEmpty()) {
                    return ResponseEntity.notFound().build();
                }
                
                Issue issue = issueOpt.get();
                if (!issue.getReportedBy().equals(agentId)) {
                    return ResponseEntity.status(403)
                            .body(ApiResponse.error("Access denied to this issue"));
                }
                
                // Upload to Cloudinary
                String folder = "issues/" + issue.getTicketNumber();
                MediaService.MediaUploadResult uploadResult = mediaService.uploadVideo(file, folder).join();
                
                if (!uploadResult.isSuccess()) {
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.error("Failed to upload video: " + uploadResult.getMessage()));
                }
                
                // Save attachment record
                IssueAttachment attachment = attachmentService.createAttachment(
                        issue, file.getOriginalFilename(), uploadResult.getUrl(), 
                        IssueAttachment.FileType.VIDEO, uploadResult.getPublicId(), 
                        file.getSize(), file.getContentType(), agentId);
                
                logger.info("Agent {} uploaded video to issue {}", agentId, issue.getTicketNumber());
                return ResponseEntity.ok(ApiResponse.success(attachment, "Video uploaded successfully"));
                
            } catch (Exception e) {
                logger.error("Failed to upload video: {}", e.getMessage());
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Failed to upload video: " + e.getMessage()));
            }
        });
    }
    
    /**
     * Get all attachments for an issue
     */
    @GetMapping
    @PreAuthorize("hasRole('AGENT') or hasRole('MASTER')")
    public ResponseEntity<ApiResponse<List<IssueAttachment>>> getAttachments(
            @PathVariable Long issueId,
            Authentication authentication,
            HttpServletRequest request) {
        try {
            // For agents, extract agent ID from JWT token; for masters, use username
            String userIdentifier;
            if (authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_AGENT"))) {
                userIdentifier = getAgentIdFromToken(request);
                if (userIdentifier == null) {
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.error("Unable to extract agent ID from token"));
                }
            } else {
                userIdentifier = authentication.getName(); // For master admin
            }
            
            // Validate issue exists
            Optional<Issue> issueOpt = issueService.getIssueById(issueId);
            if (issueOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Issue issue = issueOpt.get();
            
            // Check access - agents can only see their own issues
            if (authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_AGENT"))) {
                if (!issue.getReportedBy().equals(userIdentifier)) {
                    return ResponseEntity.status(403)
                            .body(ApiResponse.error("Access denied to this issue"));
                }
            }
            
            List<IssueAttachment> attachments = attachmentService.getAttachmentsByIssue(issueId);
            return ResponseEntity.ok(ApiResponse.success(attachments, "Attachments retrieved successfully"));
            
        } catch (Exception e) {
            logger.error("Failed to get attachments: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get attachments: " + e.getMessage()));
        }
    }
    
    /**
     * Delete attachment (only by agent who uploaded it)
     */
    @DeleteMapping("/{attachmentId}")
    @PreAuthorize("hasRole('AGENT')")
    public CompletableFuture<ResponseEntity<ApiResponse<String>>> deleteAttachment(
            @PathVariable Long issueId,
            @PathVariable Long attachmentId,
            Authentication authentication,
            HttpServletRequest request) {

        // Extract agent ID from JWT token before async call
        String agentId = getAgentIdFromToken(request);
        if (agentId == null) {
            return CompletableFuture.completedFuture(
                ResponseEntity.badRequest()
                    .body(ApiResponse.error("Unable to extract agent ID from token"))
            );
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                
                // Validate attachment exists and belongs to agent
                Optional<IssueAttachment> attachmentOpt = attachmentService.getAttachmentById(attachmentId);
                if (attachmentOpt.isEmpty()) {
                    return ResponseEntity.notFound().build();
                }
                
                IssueAttachment attachment = attachmentOpt.get();
                if (!attachment.getUploadedBy().equals(agentId)) {
                    return ResponseEntity.status(403)
                            .body(ApiResponse.error("Access denied to this attachment"));
                }
                
                // Delete from Cloudinary
                if (attachment.getCloudinaryPublicId() != null) {
                    String resourceType = attachment.getFileType() == IssueAttachment.FileType.IMAGE ? "image" : "video";
                    mediaService.deleteMedia(attachment.getCloudinaryPublicId(), resourceType).join();
                }
                
                // Delete attachment record
                attachmentService.deleteAttachment(attachmentId);
                
                logger.info("Agent {} deleted attachment {} from issue {}", agentId, attachmentId, issueId);
                return ResponseEntity.ok(ApiResponse.success("", "Attachment deleted successfully"));
                
            } catch (Exception e) {
                logger.error("Failed to delete attachment: {}", e.getMessage());
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Failed to delete attachment: " + e.getMessage()));
            }
        });
    }
}
