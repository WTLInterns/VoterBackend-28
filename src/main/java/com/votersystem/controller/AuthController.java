package com.votersystem.controller;

import com.votersystem.dto.LoginRequest;
import com.votersystem.dto.LoginResponse;
import com.votersystem.dto.MobileLoginRequest;
import com.votersystem.dto.UpdateProfileRequest;
import com.votersystem.service.AuthService;
import com.votersystem.util.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {
        
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        
        LoginResponse response = authService.login(loginRequest, ipAddress, userAgent);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    // Mobile login for agents using phone number
    @PostMapping("/mobile-login")
    public ResponseEntity<ApiResponse<LoginResponse>> mobileLogin(
            @Valid @RequestBody MobileLoginRequest mobileLoginRequest,
            HttpServletRequest request) {

        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");

        LoginResponse response = authService.mobileLogin(mobileLoginRequest, ipAddress, userAgent);

        return ResponseEntity.ok(ApiResponse.success(response, "Mobile login successful"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        authService.logout(token);
        return ResponseEntity.ok(ApiResponse.success(null, "Logout successful"));
    }
    
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Object>> getCurrentUser(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        Object userInfo = authService.getCurrentUserInfo(token);
        return ResponseEntity.ok(ApiResponse.success(userInfo, "User information retrieved"));
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        LoginResponse response = authService.refreshToken(token);
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
    }
    
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @RequestBody ChangePasswordRequest request,
            HttpServletRequest httpRequest) {
        
        String token = extractTokenFromRequest(httpRequest);
        authService.changePassword(token, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
    }
    
    // Helper methods
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    // Inner class for change password request
    public static class ChangePasswordRequest {
        private String currentPassword;
        private String newPassword;
        
        public String getCurrentPassword() {
            return currentPassword;
        }
        
        public void setCurrentPassword(String currentPassword) {
            this.currentPassword = currentPassword;
        }
        
        public String getNewPassword() {
            return newPassword;
        }
        
        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }

    // Update profile endpoint for Master Admin
    @PutMapping("/update-profile")
    public ResponseEntity<ApiResponse<LoginResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            HttpServletRequest httpRequest) {
        try {
            // Extract current user from JWT token
            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Authorization header missing"));
            }

            String token = authHeader.substring(7);
            LoginResponse response = authService.updateProfile(token, request);

            if (response != null) {
                return ResponseEntity.ok(ApiResponse.success(response, "Profile updated successfully"));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Failed to update profile. Please check your current password."));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Update failed: " + e.getMessage()));
        }
    }
}
