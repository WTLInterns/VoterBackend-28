package com.votersystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Agent Location Entity for real-time tracking
 * Stores current and historical location data for agents
 */
@Entity
@Table(name = "agent_locations")
public class AgentLocation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Agent ID is required")
    @Column(name = "agent_id", nullable = false, length = 20)
    private String agentId;
    
    @NotNull(message = "Latitude is required")
    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "accuracy")
    private Double accuracy; // GPS accuracy in meters

    @Column(name = "altitude")
    private Double altitude;

    @Column(name = "speed")
    private Double speed; // Speed in m/s

    @Column(name = "bearing")
    private Double bearing; // Direction in degrees
    
    @Column(name = "address", length = 500)
    private String address; // Reverse geocoded address
    
    @Column(name = "is_current", nullable = false)
    private Boolean isCurrent = true; // Whether this is the current location
    
    @Column(name = "connection_status", length = 20)
    @Enumerated(EnumType.STRING)
    private ConnectionStatus connectionStatus = ConnectionStatus.ONLINE;
    
    @CreationTimestamp
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "battery_level")
    private Integer batteryLevel; // Battery percentage
    
    @Column(name = "is_charging")
    private Boolean isCharging;
    
    // Connection status enum
    public enum ConnectionStatus {
        ONLINE, OFFLINE, DISCONNECTED
    }
    
    // Constructors
    public AgentLocation() {}
    
    public AgentLocation(String agentId, Double latitude, Double longitude) {
        this.agentId = agentId;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    public AgentLocation(String agentId, Double latitude, Double longitude, Double accuracy) {
        this.agentId = agentId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getAgentId() {
        return agentId;
    }
    
    public void setAgentId(String agentId) {
        this.agentId = agentId;
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
    
    public Boolean getIsCurrent() {
        return isCurrent;
    }
    
    public void setIsCurrent(Boolean isCurrent) {
        this.isCurrent = isCurrent;
    }
    
    public ConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }
    
    public void setConnectionStatus(ConnectionStatus connectionStatus) {
        this.connectionStatus = connectionStatus;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
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
    
    // Helper methods
    public boolean isOnline() {
        return connectionStatus == ConnectionStatus.ONLINE;
    }
    
    public boolean isOffline() {
        return connectionStatus == ConnectionStatus.OFFLINE;
    }
    
    public boolean isDisconnected() {
        return connectionStatus == ConnectionStatus.DISCONNECTED;
    }
    
    @Override
    public String toString() {
        return "AgentLocation{" +
                "id=" + id +
                ", agentId='" + agentId + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", accuracy=" + accuracy +
                ", connectionStatus=" + connectionStatus +
                ", timestamp=" + timestamp +
                '}';
    }
}
