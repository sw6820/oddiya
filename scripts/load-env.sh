#!/bin/bash

# Environment Configuration Loader
# Loads the appropriate .env file based on environment

set -e

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Determine environment
ENV=${1:-local}

echo "🔧 LOADING ENVIRONMENT: $ENV"
echo "=============================="
echo ""

# Map environment to .env file
case "$ENV" in
    local|development|dev)
        ENV_FILE=".env.local"
        ;;
    staging|stage)
        ENV_FILE=".env.staging"
        ;;
    production|prod)
        ENV_FILE=".env.production"
        ;;
    *)
        ENV_FILE=".env"
        ;;
esac

# Check if file exists
if [ ! -f "$ENV_FILE" ]; then
    print_error "Environment file not found: $ENV_FILE"
    echo ""
    echo "Available options:"
    echo "  local      - Use .env.local"
    echo "  staging    - Use .env.staging"
    echo "  production - Use .env.production"
    echo "  custom     - Use .env"
    echo ""
    echo "Create the file from template:"
    echo "  cp .env.example $ENV_FILE"
    exit 1
fi

# Load environment variables
set -a
source "$ENV_FILE"
set +a

print_success "Loaded: $ENV_FILE"
echo ""

# Validate required variables
echo "Validating configuration..."
MISSING_VARS=()

# Database
[ -z "$DB_HOST" ] && MISSING_VARS+=("DB_HOST")
[ -z "$DB_PORT" ] && MISSING_VARS+=("DB_PORT")
[ -z "$DB_NAME" ] && MISSING_VARS+=("DB_NAME")
[ -z "$DB_USER" ] && MISSING_VARS+=("DB_USER")
[ -z "$DB_PASSWORD" ] && MISSING_VARS+=("DB_PASSWORD")

# Redis
[ -z "$REDIS_HOST" ] && MISSING_VARS+=("REDIS_HOST")
[ -z "$REDIS_PORT" ] && MISSING_VARS+=("REDIS_PORT")

# AWS (only required in production)
if [ "$ENVIRONMENT" = "production" ]; then
    [ -z "$AWS_ACCESS_KEY_ID" ] && MISSING_VARS+=("AWS_ACCESS_KEY_ID")
    [ -z "$AWS_SECRET_ACCESS_KEY" ] && MISSING_VARS+=("AWS_SECRET_ACCESS_KEY")
    [ -z "$S3_BUCKET" ] && MISSING_VARS+=("S3_BUCKET")
fi

if [ ${#MISSING_VARS[@]} -gt 0 ]; then
    print_error "Missing required variables:"
    for var in "${MISSING_VARS[@]}"; do
        echo "  - $var"
    done
    echo ""
    echo "Please update $ENV_FILE with the required values"
    exit 1
fi

print_success "All required variables present"
echo ""

# Display configuration summary
echo "════════════════════════════════════════"
echo "📋 CONFIGURATION SUMMARY"
echo "════════════════════════════════════════"
echo ""
echo "Environment:    $ENVIRONMENT"
echo "Database:       $DB_HOST:$DB_PORT/$DB_NAME"
echo "Redis:          $REDIS_HOST:$REDIS_PORT"
echo "AWS Region:     $AWS_REGION"
echo "S3 Bucket:      $S3_BUCKET"
echo "Mock Mode:      ${MOCK_MODE:-false}"
echo "Log Level:      ${LOG_LEVEL:-INFO}"
echo ""

# Export for docker-compose
export ENV_FILE

print_success "Environment loaded successfully!"
echo ""
echo "To use with docker-compose:"
echo "  docker-compose --env-file $ENV_FILE up -d"
echo ""

