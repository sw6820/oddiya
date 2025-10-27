-- Video Service Schema
-- Video job management with idempotency support

CREATE TABLE IF NOT EXISTS video_service.video_jobs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',  -- 'PENDING', 'PROCESSING', 'COMPLETED', 'FAILED'
    photo_urls TEXT[] NOT NULL,  -- Array of S3 photo URLs
    template VARCHAR(100),  -- Video template name ('basic', 'fade', etc.)
    video_url VARCHAR(500),  -- Final S3 video URL (null until completed)
    idempotency_key UUID NOT NULL UNIQUE,  -- Client-provided UUID for idempotency
    error_message TEXT,  -- Error details if status is 'FAILED'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT valid_status CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED'))
);

-- Index for fast lookup by user_id
CREATE INDEX idx_video_jobs_user ON video_service.video_jobs(user_id);

-- Index for fast lookup by status (for worker polling)
CREATE INDEX idx_video_jobs_status ON video_service.video_jobs(status) WHERE status = 'PENDING';

-- Index for idempotency key (unique constraint also creates index automatically)
-- Explicit index for idempotency key lookups during job creation
CREATE INDEX idx_video_jobs_idempotency ON video_service.video_jobs(idempotency_key);

