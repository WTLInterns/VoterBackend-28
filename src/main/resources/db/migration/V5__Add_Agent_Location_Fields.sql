-- Add location fields to agents table
ALTER TABLE agents 
ADD COLUMN latitude DOUBLE NULL,
ADD COLUMN longitude DOUBLE NULL,
ADD COLUMN last_location VARCHAR(500) NULL;
