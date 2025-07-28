package com.votersystem.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/config")
@CrossOrigin(origins = "*")
public class ConfigController {

    @Value("${google.maps.api.key:}")
    private String googleMapsApiKey;

    @GetMapping("/google-maps-key")
    @PreAuthorize("hasRole('MASTER') or hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<?> getGoogleMapsApiKey() {
        try {
            Map<String, Object> response = new HashMap<>();
            
            if (googleMapsApiKey == null || googleMapsApiKey.trim().isEmpty() || 
                googleMapsApiKey.equals("YOUR_GOOGLE_MAPS_API_KEY_HERE")) {
                response.put("success", false);
                response.put("message", "Google Maps API key not configured");
                response.put("data", null);
            } else {
                response.put("success", true);
                response.put("message", "Google Maps API key retrieved successfully");
                response.put("data", Map.of("apiKey", googleMapsApiKey));
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to retrieve Google Maps API key: " + e.getMessage());
            errorResponse.put("data", null);
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/app-config")
    @PreAuthorize("hasRole('MASTER') or hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<?> getAppConfig() {
        try {
            Map<String, Object> config = new HashMap<>();
            config.put("googleMapsEnabled", googleMapsApiKey != null && 
                      !googleMapsApiKey.trim().isEmpty() && 
                      !googleMapsApiKey.equals("YOUR_GOOGLE_MAPS_API_KEY_HERE"));
            config.put("locationTrackingEnabled", true);
            config.put("fileUploadEnabled", true);
            config.put("realTimeUpdatesEnabled", true);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "App configuration retrieved successfully");
            response.put("data", config);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to retrieve app configuration: " + e.getMessage());
            errorResponse.put("data", null);
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
