-- Fix existing issues that have mobile numbers as reportedBy instead of agent IDs
-- V4__Fix_Issue_ReportedBy_Field.sql

-- Update issues table to use agent IDs instead of mobile numbers for reportedBy field
-- This migration handles the production issue where old issues were stored with mobile numbers

UPDATE issues 
SET reported_by = (
    SELECT a.id 
    FROM agents a 
    WHERE a.mobile = issues.reported_by
    AND a.mobile REGEXP '^[0-9]{10}$'  -- Only update if reported_by looks like a mobile number
)
WHERE reported_by REGEXP '^[0-9]{10}$'  -- Only update records where reported_by is a mobile number
AND EXISTS (
    SELECT 1 
    FROM agents a 
    WHERE a.mobile = issues.reported_by
);

-- Add a comment to track this migration
INSERT INTO migration_log (migration_name, description, executed_at) 
VALUES (
    'V4__Fix_Issue_ReportedBy_Field', 
    'Updated existing issues to use agent IDs instead of mobile numbers in reportedBy field',
    NOW()
) ON DUPLICATE KEY UPDATE executed_at = NOW();

-- Create migration_log table if it doesn't exist
CREATE TABLE IF NOT EXISTS migration_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    migration_name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
