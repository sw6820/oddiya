#!/bin/bash
# Restart Auth Service with environment variables from .env

# Load environment variables
set -a
source /Users/wjs/cursor/oddiya/.env
set +a

# Kill existing Auth Service
echo "Stopping Auth Service..."
pkill -f "auth-service"
sleep 2

# Start Auth Service
echo "Starting Auth Service..."
cd /Users/wjs/cursor/oddiya/services/auth-service

# Export required variables
export DB_HOST
export DB_PORT
export DB_NAME
export DB_USER
export DB_PASSWORD
export REDIS_HOST
export REDIS_PORT
export GOOGLE_CLIENT_ID
export GOOGLE_CLIENT_SECRET
export OAUTH_REDIRECT_URI

nohup ./gradlew bootRun > /tmp/auth-service.log 2>&1 &

echo "Waiting for Auth Service to start..."
sleep 5

# Check if service is running
if lsof -i :8081 > /dev/null 2>&1; then
    echo "✅ Auth Service is running on port 8081"
    tail -10 /tmp/auth-service.log
else
    echo "❌ Auth Service failed to start"
    tail -20 /tmp/auth-service.log
fi
