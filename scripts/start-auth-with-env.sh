#!/bin/bash

# Load environment variables from .env file
export $(grep -v '^#' /Users/wjs/cursor/oddiya/.env | xargs)

# Set OAuth redirect URI for ngrok tunnel
export OAUTH_REDIRECT_URI="https://ff192510c9b9.ngrok-free.app/api/v1/auth/oauth/google/callback"

# Navigate to auth service directory
cd /Users/wjs/cursor/oddiya/services/auth-service

# Start the service
./gradlew bootRun > /tmp/auth-service.log 2>&1 &

echo "Auth service starting with environment variables:"
echo "GOOGLE_CLIENT_ID: ${GOOGLE_CLIENT_ID:0:20}..."
echo "OAUTH_REDIRECT_URI: $OAUTH_REDIRECT_URI"
