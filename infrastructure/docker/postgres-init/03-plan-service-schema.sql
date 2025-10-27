-- Plan Service Schema
-- Travel plans and plan details

CREATE TABLE IF NOT EXISTS plan_service.travel_plans (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    location VARCHAR(255),  -- Main destination
    preferences JSONB,  -- User preferences (budget, interests, etc.)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT valid_date_range CHECK (end_date >= start_date)
);

CREATE TABLE IF NOT EXISTS plan_service.plan_details (
    id BIGSERIAL PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    day INT NOT NULL,  -- Day number in the trip
    location VARCHAR(255) NOT NULL,  -- Specific location for this day
    activity TEXT,  -- Activities planned
    restaurant TEXT,  -- Restaurant recommendations
    weather_info JSONB,  -- Weather forecast data (nullable)
    time_slot VARCHAR(50),  -- 'morning', 'afternoon', 'evening', 'all_day'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_plan FOREIGN KEY (plan_id) REFERENCES plan_service.travel_plans(id) ON DELETE CASCADE
);

-- Index for fast lookup by user_id
CREATE INDEX idx_travel_plans_user ON plan_service.travel_plans(user_id);

-- Index for fast lookup by plan_id
CREATE INDEX idx_plan_details_plan ON plan_service.plan_details(plan_id);

-- Index for date range queries
CREATE INDEX idx_travel_plans_dates ON plan_service.travel_plans(start_date, end_date);

