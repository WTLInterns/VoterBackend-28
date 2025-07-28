package com.votersystem.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.votersystem.dto.AgentLocationResponse;
import com.votersystem.dto.LocationUpdateRequest;
import com.votersystem.service.LocationTrackingService;

/**
 * WebSocket controller for handling real-time location updates
 */
@Controller
public class LocationWebSocketController {

    @Autowired
    private LocationTrackingService locationTrackingService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Handle location updates from mobile agents
     */
    @MessageMapping("/location/update")
    public void updateLocation(@Payload LocationUpdateRequest locationUpdate, 
                              SimpMessageHeaderAccessor headerAccessor,
                              Principal principal) {
        try {
            // Get agent ID from session
            if (headerAccessor.getSessionAttributes() == null) {
                System.err.println("No session attributes found for location update");
                return;
            }

            String username = (String) headerAccessor.getSessionAttributes().get("username"); // Mobile number
            String agentId = (String) headerAccessor.getSessionAttributes().get("userId");    // Actual agent ID
            String userType = (String) headerAccessor.getSessionAttributes().get("userType");

            if (agentId == null || !"AGENT".equals(userType)) {
                System.err.println("Unauthorized location update attempt - username: " + username + ", agentId: " + agentId + ", userType: " + userType);
                return;
            }
            
            System.out.println("Received location update from agent: " + agentId + " (username: " + username + ")");
            System.out.println("Location: " + locationUpdate.getLatitude() + ", " + locationUpdate.getLongitude());
            System.out.println("Location update payload: " + locationUpdate.toString());
            
            // Update agent location
            AgentLocationResponse response = locationTrackingService.updateAgentLocation(agentId, locationUpdate);
            
            if (response != null) {
                // Broadcast location update to all admin subscribers
                messagingTemplate.convertAndSend("/topic/location/updates", response);
                
                // Send confirmation back to agent
                messagingTemplate.convertAndSendToUser(
                    agentId, 
                    "/queue/location/confirmation", 
                    "Location updated successfully"
                );
                
                System.out.println("Location update broadcasted for agent: " + agentId);
            } else {
                // Send error back to agent
                messagingTemplate.convertAndSendToUser(
                    agentId, 
                    "/queue/location/error", 
                    "Failed to update location"
                );
            }
            
        } catch (Exception e) {
            System.err.println("Error processing location update: " + e.getMessage());
            e.printStackTrace();
            
            // Send error back to agent if possible
            if (principal != null) {
                messagingTemplate.convertAndSendToUser(
                    principal.getName(), 
                    "/queue/location/error", 
                    "Error processing location update: " + e.getMessage()
                );
            }
        }
    }

    /**
     * Handle agent connection status updates
     */
    @MessageMapping("/location/status")
    public void updateConnectionStatus(@Payload String status, 
                                     SimpMessageHeaderAccessor headerAccessor) {
        try {
            if (headerAccessor.getSessionAttributes() == null) {
                System.err.println("No session attributes found for status update");
                return;
            }

            String username = (String) headerAccessor.getSessionAttributes().get("username"); // Mobile number
            String agentId = (String) headerAccessor.getSessionAttributes().get("userId");    // Actual agent ID
            String userType = (String) headerAccessor.getSessionAttributes().get("userType");

            if (agentId == null || !"AGENT".equals(userType)) {
                System.err.println("Unauthorized status update attempt - username: " + username + ", agentId: " + agentId + ", userType: " + userType);
                return;
            }
            
            System.out.println("Received status update from agent: " + agentId + " - Status: " + status);
            
            // Update connection status
            boolean updated = locationTrackingService.updateConnectionStatus(agentId, status);
            
            if (updated) {
                // Broadcast status update to admins
                AgentLocationResponse response = locationTrackingService.getAgentCurrentLocation(agentId);
                if (response != null) {
                    messagingTemplate.convertAndSend("/topic/location/status", response);
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error processing status update: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle agent heartbeat/ping messages
     */
    @MessageMapping("/location/ping")
    public void handlePing(SimpMessageHeaderAccessor headerAccessor) {
        try {
            if (headerAccessor.getSessionAttributes() == null) {
                System.err.println("No session attributes found for ping");
                return;
            }

            String username = (String) headerAccessor.getSessionAttributes().get("username"); // Mobile number
            String agentId = (String) headerAccessor.getSessionAttributes().get("userId");    // Actual agent ID
            String userType = (String) headerAccessor.getSessionAttributes().get("userType");

            if (agentId == null || !"AGENT".equals(userType)) {
                return;
            }
            
            // Update last seen timestamp
            locationTrackingService.updateLastSeen(agentId);
            
            // Send pong back to agent
            messagingTemplate.convertAndSendToUser(agentId, "/queue/location/pong", "pong");
            
        } catch (Exception e) {
            System.err.println("Error processing ping: " + e.getMessage());
        }
    }
}
