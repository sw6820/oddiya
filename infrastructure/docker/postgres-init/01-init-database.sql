-- Initialize schemas for schema-per-service model
-- This file runs automatically when PostgreSQL container first starts

-- Create schemas for each service
CREATE SCHEMA IF NOT EXISTS user_service;
CREATE SCHEMA IF NOT EXISTS plan_service;
CREATE SCHEMA IF NOT EXISTS video_service;

-- Grant permissions to the application user
GRANT ALL PRIVILEGES ON SCHEMA user_service TO oddiya_user;
GRANT ALL PRIVILEGES ON SCHEMA plan_service TO oddiya_user;
GRANT ALL PRIVILEGES ON SCHEMA video_service TO oddiya_user;

-- Set default privileges for future tables
ALTER DEFAULT PRIVILEGES IN SCHEMA user_service GRANT ALL ON TABLES TO oddiya_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA plan_service GRANT ALL ON TABLES TO oddiya_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA video_service GRANT ALL ON TABLES TO oddiya_user;

