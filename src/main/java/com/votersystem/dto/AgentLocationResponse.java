package com.votersystem.dto;

import com.votersystem.entity.AgentLocation;
import com.votersystem.entity.Agent;

import java.time.LocalDateTime;

/**
 * DTO for agent location responses to admin dashboard
 */
public class AgentLocationResponse {
    
    private String agentId;
    private String agentFirstName;
    private String agentLastName;
    private String agentMobile;
    private String lastLocation;
    private Double latitude;
    private Double longitude;
    private Double accuracy;
    private Double altitude;
    private Double speed;
    private Double bearing;
    private String address;
    private String connectionStatus;
    private LocalDateTime lastUpdate;
    private Integer batteryLevel;
    private Boolean isCharging;
    private Boolean isOnline;
    
    // Constructors
    public AgentLocationResponse() {}
    
    public AgentLocationResponse(AgentLocation location, Agent agent) {
        this.agentId = location.getAgentId();
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        this.accuracy = location.getAccuracy();
        this.altitude = location.getAltitude();
        this.speed = location.getSpeed();
        this.bearing = location.getBearing();
        this.address = location.getAddress();
        this.connectionStatus = location.getConnectionStatus().name();
        this.lastUpdate = location.getTimestamp();
        this.batteryLevel = location.getBatteryLevel();
        this.isCharging = location.getIsCharging();
        this.isOnline = location.isOnline();
        
        if (agent != null) {
            this.agentFirstName = agent.getFirstName();
            this.agentLastName = agent.getLastName();
            this.agentMobile = agent.getMobile();
            this.lastLocation = agent.getLastLocation();
        }
    }
    
    // Static factory method
    public static AgentLocationResponse from(AgentLocation location, Agent agent) {
        return new AgentLocationResponse(location, agent);
    }
    
    // Getters and Setters
    public String getAgentId() {
        return agentId;
    }
    
    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }
    
    public String getAgentFirstName() {
        return agentFirstName;
    }
    
    public void setAgentFirstName(String agentFirstName) {
        this.agentFirstName = agentFirstName;
    }
    
    public String getAgentLastName() {
        return agentLastName;
    }
    
    public void setAgentLastName(String agentLastName) {
        this.agentLastName = agentLastName;
    }
    
    public String getAgentMobile() {
        return agentMobile;
    }
    
    public void setAgentMobile(String agentMobile) {
        this.agentMobile = agentMobile;
    }
    
    public String getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(String lastLocation) {
        this.lastLocation = lastLocation;
    }
    
    public Double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    
    public Double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    
    public Double getAccuracy() {
        return accuracy;
    }
    
    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }
    
    public Double getAltitude() {
        return altitude;
    }
    
    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }
    
    public Double getSpeed() {
        return speed;
    }
    
    public void setSpeed(Double speed) {
        this.speed = speed;
    }
    
    public Double getBearing() {
        return bearing;
    }
    
    public void setBearing(Double bearing) {
        this.bearing = bearing;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getConnectionStatus() {
        return connectionStatus;
    }
    
    public void setConnectionStatus(String connectionStatus) {
        this.connectionStatus = connectionStatus;
    }
    
    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }
    
    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
    
    public Integer getBatteryLevel() {
        return batteryLevel;
    }
    
    public void setBatteryLevel(Integer batteryLevel) {
        this.batteryLevel = batteryLevel;
    }
    
    public Boolean getIsCharging() {
        return isCharging;
    }
    
    public void setIsCharging(Boolean isCharging) {
        this.isCharging = isCharging;
    }
    
    public Boolean getIsOnline() {
        return isOnline;
    }
    
    public void setIsOnline(Boolean isOnline) {
        this.isOnline = isOnline;
    }
    
    // Helper methods
    public String getAgentFullName() {
        if (agentFirstName != null && agentLastName != null) {
            return agentFirstName + " " + agentLastName;
        }
        return agentFirstName != null ? agentFirstName : agentLastName;
    }
    
    public String getAgentLocation() {
        return lastLocation != null ? lastLocation : "Location not available";
    }
    
    public boolean hasValidLocation() {
        return latitude != null && longitude != null;
    }
    
    public boolean hasBatteryInfo() {
        return batteryLevel != null;
    }
    
    @Override
    public String toString() {
        return "AgentLocationResponse{" +
                "agentId='" + agentId + '\'' +
                ", agentFirstName='" + agentFirstName + '\'' +
                ", agentLastName='" + agentLastName + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", connectionStatus='" + connectionStatus + '\'' +
                ", lastUpdate=" + lastUpdate +
                ", isOnline=" + isOnline +
                '}';
    }
}
