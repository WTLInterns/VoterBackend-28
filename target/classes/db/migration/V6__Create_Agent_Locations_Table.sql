-- Migration V6: Create Agent Locations Table for Real-time Tracking
-- This migration creates the agent_locations table for storing real-time location data

-- Create agent_locations table
CREATE TABLE agent_locations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    agent_id VARCHAR(20) NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    accuracy DOUBLE DEFAULT NULL COMMENT 'GPS accuracy in meters',
    altitude DOUBLE DEFAULT NULL COMMENT 'Altitude in meters',
    speed DOUBLE DEFAULT NULL COMMENT 'Speed in m/s',
    bearing DOUBLE DEFAULT NULL COMMENT 'Direction in degrees',
    address VARCHAR(500) DEFAULT NULL COMMENT 'Reverse geocoded address',
    is_current BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Whether this is the current location',
    connection_status ENUM('ONLINE', 'OFFLINE', 'DISCONNECTED') NOT NULL DEFAULT 'ONLINE',
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    battery_level INT DEFAULT NULL COMMENT 'Battery percentage 0-100',
    is_charging BOOLEAN DEFAULT NULL COMMENT 'Whether device is charging',
    
    -- Indexes for performance
    INDEX idx_agent_locations_agent_id (agent_id),
    INDEX idx_agent_locations_current (is_current),
    INDEX idx_agent_locations_status (connection_status),
    INDEX idx_agent_locations_timestamp (timestamp),
    INDEX idx_agent_locations_agent_current (agent_id, is_current),
    INDEX idx_agent_locations_agent_status (agent_id, connection_status),
    
    -- Spatial index for location queries (if MySQL supports it)
    INDEX idx_agent_locations_coordinates (latitude, longitude),
    
    -- Foreign key constraint
    CONSTRAINT fk_agent_locations_agent_id 
        FOREIGN KEY (agent_id) REFERENCES agents(id) 
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Add location tracking fields to existing agents table if they don't exist
-- These fields store the last known location for quick access
ALTER TABLE agents
ADD COLUMN IF NOT EXISTS latitude DOUBLE DEFAULT NULL COMMENT 'Last known latitude',
ADD COLUMN IF NOT EXISTS longitude DOUBLE DEFAULT NULL COMMENT 'Last known longitude',
ADD COLUMN IF NOT EXISTS last_location VARCHAR(500) DEFAULT NULL COMMENT 'Last known address/location';

-- Create indexes on agents table for location fields
CREATE INDEX IF NOT EXISTS idx_agents_latitude ON agents(latitude);
CREATE INDEX IF NOT EXISTS idx_agents_longitude ON agents(longitude);
CREATE INDEX IF NOT EXISTS idx_agents_coordinates ON agents(latitude, longitude);

-- Insert sample data for testing (optional - can be removed in production)
-- This creates some sample location data for existing agents
INSERT INTO agent_locations (agent_id, latitude, longitude, accuracy, address, is_current, connection_status)
SELECT 
    id as agent_id,
    28.6139 + (RAND() - 0.5) * 0.1 as latitude,  -- Random locations around Delhi
    77.2090 + (RAND() - 0.5) * 0.1 as longitude,
    ROUND(5 + RAND() * 15, 2) as accuracy,       -- Random accuracy between 5-20 meters
    CONCAT('Sample Location for ', id) as address,
    TRUE as is_current,
    'OFFLINE' as connection_status
FROM agents 
WHERE id IS NOT NULL
LIMIT 5;  -- Only insert for first 5 agents to avoid too much test data

-- Update agents table with the same location data
UPDATE agents a
JOIN agent_locations al ON a.id = al.agent_id
SET 
    a.latitude = al.latitude,
    a.longitude = al.longitude,
    a.last_location = al.address
WHERE al.is_current = TRUE;

-- Create a view for easy access to current agent locations with agent details
CREATE OR REPLACE VIEW current_agent_locations AS
SELECT 
    al.id as location_id,
    al.agent_id,
    a.first_name,
    a.last_name,
    a.mobile,
    al.latitude,
    al.longitude,
    al.accuracy,
    al.altitude,
    al.speed,
    al.bearing,
    al.address,
    al.connection_status,
    al.timestamp as last_update,
    al.battery_level,
    al.is_charging,
    a.created_by as admin_username
FROM agent_locations al
JOIN agents a ON al.agent_id = a.id
WHERE al.is_current = TRUE;

-- Create a procedure to clean up old location history (optional)
DELIMITER //
CREATE PROCEDURE CleanupOldLocationHistory(IN days_to_keep INT)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    
    START TRANSACTION;
    
    DELETE FROM agent_locations 
    WHERE is_current = FALSE 
    AND timestamp < DATE_SUB(NOW(), INTERVAL days_to_keep DAY);
    
    COMMIT;
END //
DELIMITER ;

-- Add comments to the table
ALTER TABLE agent_locations COMMENT = 'Real-time location tracking data for agents';

-- Grant necessary permissions (adjust as needed for your setup)
-- GRANT SELECT, INSERT, UPDATE, DELETE ON agent_locations TO 'voter_app_user'@'%';
-- GRANT SELECT ON current_agent_locations TO 'voter_app_user'@'%';

-- Migration completed successfully
-- This migration adds real-time location tracking capabilities to the voter system
