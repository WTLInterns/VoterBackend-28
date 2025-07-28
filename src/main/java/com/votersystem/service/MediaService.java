package com.votersystem.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Cloudinary Media Service
 * Handles uploading images and videos for issue reporting system
 */
@Service
public class MediaService {
    
    private static final Logger logger = LoggerFactory.getLogger(MediaService.class);
    
    @Autowired(required = false)
    private Cloudinary cloudinary;
    
    // Maximum file sizes (in bytes)
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final long MAX_VIDEO_SIZE = 100 * 1024 * 1024; // 100MB
    
    /**
     * Upload image to Cloudinary
     */
    public CompletableFuture<MediaUploadResult> uploadImage(MultipartFile file, String folder) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (cloudinary == null) {
                    logger.warn("Cloudinary not configured - image upload failed");
                    return new MediaUploadResult(false, null, "Cloudinary not configured", null);
                }
                
                // Validate file
                if (file.isEmpty()) {
                    return new MediaUploadResult(false, null, "File is empty", null);
                }
                
                if (file.getSize() > MAX_IMAGE_SIZE) {
                    return new MediaUploadResult(false, null, "Image file too large (max 10MB)", null);
                }
                
                if (!isImageFile(file)) {
                    return new MediaUploadResult(false, null, "Invalid image format", null);
                }
                
                // Upload to Cloudinary
                Map<String, Object> uploadParams = ObjectUtils.asMap(
                        "folder", folder,
                        "resource_type", "image",
                        "quality", "auto",
                        "fetch_format", "auto"
                );
                
                Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
                
                String publicId = (String) uploadResult.get("public_id");
                String url = (String) uploadResult.get("secure_url");
                
                logger.info("Image uploaded successfully: {}", publicId);
                return new MediaUploadResult(true, url, "Upload successful", publicId);
                
            } catch (IOException e) {
                logger.error("Failed to upload image: {}", e.getMessage());
                return new MediaUploadResult(false, null, "Upload failed: " + e.getMessage(), null);
            }
        });
    }
    
    /**
     * Upload video to Cloudinary
     */
    public CompletableFuture<MediaUploadResult> uploadVideo(MultipartFile file, String folder) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (cloudinary == null) {
                    logger.warn("Cloudinary not configured - video upload failed");
                    return new MediaUploadResult(false, null, "Cloudinary not configured", null);
                }
                
                // Validate file
                if (file.isEmpty()) {
                    return new MediaUploadResult(false, null, "File is empty", null);
                }
                
                if (file.getSize() > MAX_VIDEO_SIZE) {
                    return new MediaUploadResult(false, null, "Video file too large (max 100MB)", null);
                }
                
                if (!isVideoFile(file)) {
                    return new MediaUploadResult(false, null, "Invalid video format", null);
                }
                
                // Upload to Cloudinary
                Map<String, Object> uploadParams = ObjectUtils.asMap(
                        "folder", folder,
                        "resource_type", "video",
                        "quality", "auto"
                );
                
                Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
                
                String publicId = (String) uploadResult.get("public_id");
                String url = (String) uploadResult.get("secure_url");
                
                logger.info("Video uploaded successfully: {}", publicId);
                return new MediaUploadResult(true, url, "Upload successful", publicId);
                
            } catch (IOException e) {
                logger.error("Failed to upload video: {}", e.getMessage());
                return new MediaUploadResult(false, null, "Upload failed: " + e.getMessage(), null);
            }
        });
    }
    
    /**
     * Delete media from Cloudinary
     */
    public CompletableFuture<Boolean> deleteMedia(String publicId, String resourceType) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (cloudinary == null) {
                    logger.warn("Cloudinary not configured - media deletion failed");
                    return false;
                }
                
                Map<String, Object> deleteParams = ObjectUtils.asMap(
                        "resource_type", resourceType
                );
                
                Map deleteResult = cloudinary.uploader().destroy(publicId, deleteParams);
                String result = (String) deleteResult.get("result");
                
                boolean success = "ok".equals(result);
                if (success) {
                    logger.info("Media deleted successfully: {}", publicId);
                } else {
                    logger.warn("Failed to delete media: {}, result: {}", publicId, result);
                }
                
                return success;
                
            } catch (Exception e) {
                logger.error("Failed to delete media {}: {}", publicId, e.getMessage());
                return false;
            }
        });
    }
    
    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }
    
    private boolean isVideoFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("video/");
    }
    
    /**
     * Result class for media upload operations
     */
    public static class MediaUploadResult {
        private final boolean success;
        private final String url;
        private final String message;
        private final String publicId;
        
        public MediaUploadResult(boolean success, String url, String message, String publicId) {
            this.success = success;
            this.url = url;
            this.message = message;
            this.publicId = publicId;
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getUrl() { return url; }
        public String getMessage() { return message; }
        public String getPublicId() { return publicId; }
    }
}
