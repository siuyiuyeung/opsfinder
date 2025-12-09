-- OpsFinder Database Initialization
-- This script ensures the database is properly configured

-- Enable required PostgreSQL extensions
CREATE EXTENSION IF NOT EXISTS pg_trgm;  -- For full-text search performance

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE opsfinder TO opsuser;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO opsuser;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO opsuser;

-- Set default privileges for future tables
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO opsuser;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO opsuser;
