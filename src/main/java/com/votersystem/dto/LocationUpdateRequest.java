package com.votersystem.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class LocationUpdateRequest {
    
    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private Double latitude;
    
    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private Double longitude;
    
    private String location; // Optional address/location description
    private Double accuracy; // GPS accuracy in meters
    private Double altitude;
    private Double speed; // Speed in m/s
    private Double bearing; // Direction in degrees
    private Integer batteryLevel; // Battery percentage (0-100)
    private Boolean isCharging;
    
    // Constructors
    public LocationUpdateRequest() {}
    
    public LocationUpdateRequest(Double latitude, Double longitude, String location) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.location = location;
    }
    
    // Getters and Setters
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
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
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

    @Override
    public String toString() {
        return "LocationUpdateRequest{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", location='" + location + '\'' +
                ", accuracy=" + accuracy +
                ", altitude=" + altitude +
                ", speed=" + speed +
                ", bearing=" + bearing +
                ", batteryLevel=" + batteryLevel +
                ", isCharging=" + isCharging +
                '}';
    }
}
