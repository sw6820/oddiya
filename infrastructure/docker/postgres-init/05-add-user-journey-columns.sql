-- User Journey Enhancement: Add columns for complete user journey
-- Phase 1, 2, 3 DB changes

-- Phase 1: Plan status management
ALTER TABLE plan_service.travel_plans
ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'DRAFT',
ADD COLUMN IF NOT EXISTS confirmed_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS completed_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS budget_level VARCHAR(10) DEFAULT 'medium',
ADD COLUMN IF NOT EXISTS total_cost INT DEFAULT 0;

COMMENT ON COLUMN plan_service.travel_plans.status IS 'DRAFT, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED';

-- Phase 2: Plan photos table
CREATE TABLE IF NOT EXISTS plan_service.plan_photos (
    id BIGSERIAL PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    photo_url VARCHAR NOT NULL,
    s3_key VARCHAR NOT NULL,
    upload_order INT,
    uploaded_at TIMESTAMP DEFAULT NOW(),
    CONSTRAINT fk_plan_photos_plan 
        FOREIGN KEY (plan_id) 
        REFERENCES plan_service.travel_plans(id) 
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_plan_photos_plan_id ON plan_service.plan_photos(plan_id);

COMMENT ON TABLE plan_service.plan_photos IS 'Photos uploaded by users for their travel plans';

-- Phase 3: Link videos to plans
ALTER TABLE video_service.video_jobs
ADD COLUMN IF NOT EXISTS plan_id BIGINT,
ADD COLUMN IF NOT EXISTS template_name VARCHAR(50) DEFAULT 'default';

CREATE INDEX IF NOT EXISTS idx_video_jobs_plan_id ON video_service.video_jobs(plan_id);

COMMENT ON COLUMN video_service.video_jobs.plan_id IS 'Link to the travel plan this video belongs to';

