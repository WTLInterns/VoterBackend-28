-- Add new fields to users table for better voter identification
-- V3__Add_Vidhansabha_Vibhagh_Fields.sql

ALTER TABLE users 
ADD COLUMN vidhansabha_no VARCHAR(10) NULL COMMENT 'Assembly Constituency Number',
ADD COLUMN vibhagh_kramank VARCHAR(10) NULL COMMENT 'Division/Section Number';

-- Add indexes for better search performance
CREATE INDEX idx_users_vidhansabha_no ON users(vidhansabha_no);
CREATE INDEX idx_users_vibhagh_kramank ON users(vibhagh_kramank);

-- Add composite index for combined searches
CREATE INDEX idx_users_vidhansabha_vibhagh ON users(vidhansabha_no, vibhagh_kramank);
