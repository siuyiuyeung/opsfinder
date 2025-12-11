-- OpsFinder Database Reset Script
-- This script drops and recreates the opsfinder database
-- Run this with any PostgreSQL client connected to: 192.168.31.107:5432

-- IMPORTANT: This will DELETE ALL DATA in the database!
-- Only run this in development/testing environments

-- Connect as postgres user or a superuser first
-- Then execute these commands:

-- Disconnect all active connections to the database
SELECT pg_terminate_backend(pg_stat_activity.pid)
FROM pg_stat_activity
WHERE pg_stat_activity.datname = 'opsfinder'
  AND pid <> pg_backend_pid();

-- Drop the database
DROP DATABASE IF EXISTS opsfinder;

-- Recreate the database
CREATE DATABASE opsfinder
    WITH
    OWNER = opsuser
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.utf8'
    LC_CTYPE = 'en_US.utf8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE opsfinder TO opsuser;

-- Connect to opsfinder database and grant schema privileges
\c opsfinder

GRANT ALL ON SCHEMA public TO opsuser;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO opsuser;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO opsuser;

-- Done! Now restart your Spring Boot application
-- Liquibase will automatically create all tables with correct schema
