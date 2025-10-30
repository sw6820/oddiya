-- Add passwordHash column to users table for email/password authentication
-- This column is nullable because OAuth users don't have passwords

ALTER TABLE user_service.users
ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255);

-- Add comment to explain the field
COMMENT ON COLUMN user_service.users.password_hash IS 'BCrypt hashed password for email/password authentication. NULL for OAuth users.';

-- Note: Email uniqueness is already enforced by the existing UNIQUE constraint on email column
