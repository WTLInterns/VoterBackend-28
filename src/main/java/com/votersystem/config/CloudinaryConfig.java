package com.votersystem.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cloudinary Configuration for Media Storage
 * Configures Cloudinary for storing images and videos from issue reports
 */
@Configuration
public class CloudinaryConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(CloudinaryConfig.class);
    
    @Value("${cloudinary.cloud-name:}")
    private String cloudName;
    
    @Value("${cloudinary.api-key:}")
    private String apiKey;
    
    @Value("${cloudinary.api-secret:}")
    private String apiSecret;
    
    @Value("${cloudinary.secure:true}")
    private boolean secure;
    
    @Bean
    public Cloudinary cloudinary() {
        try {
            if (cloudName.isEmpty() || apiKey.isEmpty() || apiSecret.isEmpty()) {
                logger.warn("Cloudinary credentials not configured. Media upload will be disabled.");
                logger.info("Please set cloudinary.cloud-name, cloudinary.api-key, and cloudinary.api-secret in application.properties");
                return null;
            }
            
            Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudName,
                    "api_key", apiKey,
                    "api_secret", apiSecret,
                    "secure", secure
            ));
            
            logger.info("Cloudinary initialized successfully for cloud: {}", cloudName);
            return cloudinary;
            
        } catch (Exception e) {
            logger.error("Failed to initialize Cloudinary: {}", e.getMessage());
            return null;
        }
    }
}
