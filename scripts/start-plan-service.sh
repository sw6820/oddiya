#!/bin/bash

# Load environment variables from .env file
export $(grep -v '^#' /Users/wjs/cursor/oddiya/.env | xargs)

# Navigate to plan service directory
cd /Users/wjs/cursor/oddiya/services/plan-service

# Start the service
./gradlew bootRun > /tmp/plan-service.log 2>&1 &

echo "Plan Service starting with environment variables:"
echo "DB_HOST: $DB_HOST"
echo "DB_PORT: $DB_PORT"
echo "DB_NAME: $DB_NAME"
echo "LLM_AGENT_URL: $LLM_AGENT_URL"
