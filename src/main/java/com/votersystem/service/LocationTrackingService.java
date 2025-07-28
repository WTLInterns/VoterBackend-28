package com.votersystem.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.votersystem.dto.AgentLocationResponse;
import com.votersystem.dto.LocationUpdateRequest;
import com.votersystem.entity.Agent;
import com.votersystem.entity.AgentLocation;
import com.votersystem.repository.AgentLocationRepository;
import com.votersystem.repository.AgentRepository;

/**
 * Service for handling agent location tracking operations
 */
@Service
public class LocationTrackingService {

    @Autowired
    private AgentLocationRepository agentLocationRepository;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Update agent location from mobile app (PUT approach - update existing record)
     */
    @Transactional
    public AgentLocationResponse updateAgentLocation(String agentId, LocationUpdateRequest request) {
        try {
            // Validate agent exists
            Optional<Agent> agentOpt = agentRepository.findById(agentId);
            if (agentOpt.isEmpty()) {
                System.err.println("Agent not found: " + agentId);
                return null;
            }

            Agent agent = agentOpt.get();

            // TRUE UPDATE approach - update existing record or create if not exists
            Optional<AgentLocation> existingLocationOpt = agentLocationRepository.findCurrentLocationByAgentId(agentId);
            AgentLocation location;

            if (existingLocationOpt.isPresent()) {
                // UPDATE existing record (same ID, just update fields)
                location = existingLocationOpt.get();
                System.out.println("✅ UPDATING existing location for agent: " + agentId + " (keeping ID: " + location.getId() + ")");
            } else {
                // CREATE new record (first time only)
                location = new AgentLocation();
                location.setAgentId(agentId);
                location.setIsCurrent(true);
                System.out.println("🆕 CREATING first location record for agent: " + agentId);
            }

            // Update location data
            location.setLatitude(request.getLatitude());
            location.setLongitude(request.getLongitude());
            location.setAccuracy(request.getAccuracy());
            location.setAltitude(request.getAltitude());
            location.setSpeed(request.getSpeed());
            location.setBearing(request.getBearing());
            location.setAddress(request.getLocation());
            location.setBatteryLevel(request.getBatteryLevel());
            location.setIsCharging(request.getIsCharging());
            location.setConnectionStatus(AgentLocation.ConnectionStatus.ONLINE);
            location.setTimestamp(LocalDateTime.now()); // Update timestamp

            // Save location (UPDATE or INSERT)
            AgentLocation savedLocation = agentLocationRepository.save(location);

            // Update agent's last location fields
            agent.setLatitude(request.getLatitude());
            agent.setLongitude(request.getLongitude());
            agent.setLastLocation(request.getLocation());
            agentRepository.save(agent);

            // Create response
            AgentLocationResponse response = AgentLocationResponse.from(savedLocation, agent);

            System.out.println("Location updated for agent: " + agentId + 
                             " at " + request.getLatitude() + ", " + request.getLongitude());

            return response;

        } catch (Exception e) {
            System.err.println("Error updating agent location: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Update agent connection status
     */
    @Transactional
    public boolean updateConnectionStatus(String agentId, String status) {
        try {
            AgentLocation.ConnectionStatus connectionStatus;
            switch (status.toUpperCase()) {
                case "ONLINE":
                    connectionStatus = AgentLocation.ConnectionStatus.ONLINE;
                    break;
                case "OFFLINE":
                    connectionStatus = AgentLocation.ConnectionStatus.OFFLINE;
                    break;
                case "DISCONNECTED":
                    connectionStatus = AgentLocation.ConnectionStatus.DISCONNECTED;
                    break;
                default:
                    System.err.println("Invalid connection status: " + status);
                    return false;
            }

            int updated = agentLocationRepository.updateConnectionStatus(agentId, connectionStatus, LocalDateTime.now());
            System.out.println("Updated connection status for agent " + agentId + " to " + status +
                             " (affected rows: " + updated + ") with timestamp: " + LocalDateTime.now());
            
            return updated > 0;

        } catch (Exception e) {
            System.err.println("Error updating connection status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update last seen timestamp for agent
     */
    @Transactional
    public void updateLastSeen(String agentId) {
        try {
            Optional<AgentLocation> currentLocation = agentLocationRepository.findCurrentLocationByAgentId(agentId);
            if (currentLocation.isPresent()) {
                AgentLocation location = currentLocation.get();
                location.setTimestamp(LocalDateTime.now());
                agentLocationRepository.save(location);
            }
        } catch (Exception e) {
            System.err.println("Error updating last seen for agent " + agentId + ": " + e.getMessage());
        }
    }

    /**
     * Get current location for a specific agent
     */
    public AgentLocationResponse getAgentCurrentLocation(String agentId) {
        try {
            Optional<AgentLocation> locationOpt = agentLocationRepository.findCurrentLocationByAgentId(agentId);
            if (locationOpt.isEmpty()) {
                return null;
            }

            Optional<Agent> agentOpt = agentRepository.findById(agentId);
            if (agentOpt.isEmpty()) {
                return null;
            }

            return AgentLocationResponse.from(locationOpt.get(), agentOpt.get());

        } catch (Exception e) {
            System.err.println("Error getting agent current location: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get all current locations (for master admin)
     */
    public List<AgentLocationResponse> getAllCurrentLocations() {
        try {
            List<AgentLocation> locations = agentLocationRepository.findAllCurrentLocations();
            return locations.stream()
                    .map(location -> {
                        Optional<Agent> agent = agentRepository.findById(location.getAgentId());
                        return agent.map(a -> AgentLocationResponse.from(location, a)).orElse(null);
                    })
                    .filter(response -> response != null)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Error getting all current locations: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Get current locations for specific agents (for sub-admin)
     */
    public List<AgentLocationResponse> getCurrentLocationsByAgentIds(List<String> agentIds) {
        try {
            List<AgentLocation> locations = agentLocationRepository.findCurrentLocationsByAgentIds(agentIds);
            return locations.stream()
                    .map(location -> {
                        Optional<Agent> agent = agentRepository.findById(location.getAgentId());
                        return agent.map(a -> AgentLocationResponse.from(location, a)).orElse(null);
                    })
                    .filter(response -> response != null)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Error getting current locations by agent IDs: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Get online agents only
     */
    public List<AgentLocationResponse> getOnlineAgents() {
        try {
            List<AgentLocation> locations = agentLocationRepository.findAllOnlineAgents();
            return locations.stream()
                    .map(location -> {
                        Optional<Agent> agent = agentRepository.findById(location.getAgentId());
                        return agent.map(a -> AgentLocationResponse.from(location, a)).orElse(null);
                    })
                    .filter(response -> response != null)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Error getting online agents: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Get online agents by IDs (for sub-admin)
     */
    public List<AgentLocationResponse> getOnlineAgentsByIds(List<String> agentIds) {
        try {
            List<AgentLocation> locations = agentLocationRepository.findOnlineAgentsByIds(agentIds);
            return locations.stream()
                    .map(location -> {
                        Optional<Agent> agent = agentRepository.findById(location.getAgentId());
                        return agent.map(a -> AgentLocationResponse.from(location, a)).orElse(null);
                    })
                    .filter(response -> response != null)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Error getting online agents by IDs: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Handle agent disconnection
     */
    @Transactional
    public void handleAgentDisconnection(String agentId) {
        try {
            updateConnectionStatus(agentId, "DISCONNECTED");
            System.out.println("Agent disconnected: " + agentId);

            // Broadcast disconnection to admins
            AgentLocationResponse response = getAgentCurrentLocation(agentId);
            if (response != null) {
                messagingTemplate.convertAndSend("/topic/location/disconnection", response);
            }

        } catch (Exception e) {
            System.err.println("Error handling agent disconnection: " + e.getMessage());
        }
    }

    /**
     * Count online agents
     */
    public long countOnlineAgents() {
        try {
            return agentLocationRepository.countOnlineAgents();
        } catch (Exception e) {
            System.err.println("Error counting online agents: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Count online agents by IDs (for sub-admin)
     */
    public long countOnlineAgentsByIds(List<String> agentIds) {
        try {
            return agentLocationRepository.countOnlineAgentsByIds(agentIds);
        } catch (Exception e) {
            System.err.println("Error counting online agents by IDs: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Add sample location data for testing purposes
     */
    @Transactional
    public void addSampleLocationData() {
        try {
            // Get first few agents from database
            List<Agent> agents = agentRepository.findAll().stream().limit(3).collect(Collectors.toList());

            if (agents.isEmpty()) {
                System.out.println("No agents found in database for sample data");
                return;
            }

            // Sample locations around a central point (you can adjust these coordinates)
            double[][] sampleCoordinates = {
                {19.0760, 72.8777}, // Mumbai coordinates
                {19.0896, 72.8656}, // Slightly different location
                {19.0625, 72.8972}  // Another nearby location
            };

            for (int i = 0; i < Math.min(agents.size(), sampleCoordinates.length); i++) {
                Agent agent = agents.get(i);
                double[] coords = sampleCoordinates[i];

                // Mark previous locations as not current
                agentLocationRepository.markPreviousLocationsAsNotCurrent(agent.getId());

                // Create new location record
                AgentLocation location = new AgentLocation();
                location.setAgentId(agent.getId());
                location.setLatitude(coords[0]);
                location.setLongitude(coords[1]);
                location.setAccuracy(10.0);
                location.setAltitude(50.0);
                location.setSpeed(0.0);
                location.setBearing(0.0);
                location.setAddress("Sample Location " + (i + 1));
                location.setIsCurrent(true);
                location.setConnectionStatus(AgentLocation.ConnectionStatus.ONLINE);
                location.setBatteryLevel(85);
                location.setTimestamp(LocalDateTime.now());

                agentLocationRepository.save(location);

                // Update agent's last known location
                agent.setLatitude(coords[0]);
                agent.setLongitude(coords[1]);
                agent.setLastLocation("Sample Location " + (i + 1));
                agentRepository.save(agent);

                System.out.println("Added sample location for agent: " + agent.getId());
            }

            System.out.println("Sample location data added successfully");

        } catch (Exception e) {
            System.err.println("Error adding sample location data: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
