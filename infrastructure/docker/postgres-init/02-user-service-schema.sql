-- User Service Schema
-- Users table for storing user profile information

CREATE TABLE IF NOT EXISTS user_service.users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    provider VARCHAR(50) NOT NULL,  -- 'google', 'apple'
    provider_id VARCHAR(255) NOT NULL,  -- External provider user ID
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_provider_user UNIQUE (provider, provider_id)
);

-- Index for fast lookup by email
CREATE INDEX idx_users_email ON user_service.users(email);

-- Index for fast lookup by provider and provider_id
CREATE INDEX idx_users_provider ON user_service.users(provider, provider_id);

