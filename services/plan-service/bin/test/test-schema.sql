-- Create schema for testing
CREATE SCHEMA IF NOT EXISTS plan_service;

-- Create travel_plans table
CREATE TABLE IF NOT EXISTS plan_service.travel_plans (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Create plan_details table
CREATE TABLE IF NOT EXISTS plan_service.plan_details (
    id BIGSERIAL PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    day INT NOT NULL,
    location VARCHAR NOT NULL,
    activity TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    FOREIGN KEY (plan_id) REFERENCES plan_service.travel_plans(id) ON DELETE CASCADE
);

