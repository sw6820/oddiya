#!/bin/bash
# ==========================================
# ODDIYA - ENVIRONMENT VALIDATION SCRIPT
# ==========================================
# Validates .env configuration before deployment
# Usage: ./validate-env.sh [path-to-env-file]
#
# Last updated: 2025-11-03
# ==========================================

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔍 ODDIYA ENVIRONMENT VALIDATION${NC}"
echo "========================================"
echo ""

ENV=${1:-.env}

if [ ! -f "$ENV" ]; then
    echo -e "${RED}❌ Environment file not found: $ENV${NC}"
    echo ""
    echo "Create it by copying the example:"
    echo "  cp .env.example .env"
    echo ""
    exit 1
fi

echo "Validating: $ENV"
echo ""

# Load environment
set -a
source "$ENV"
set +a

ERRORS=0
WARNINGS=0
INFO=0

# Check functions
check_required() {
    local var_name=$1
    local var_value=${!var_name}
    local description=$2

    if [ -z "$var_value" ] || [ "$var_value" = "your_"* ] || [ "$var_value" = "CHANGE_ME" ]; then
        echo -e "${RED}❌ MISSING: $var_name${NC}"
        [ -n "$description" ] && echo -e "   ${YELLOW}→ $description${NC}"
        ((ERRORS++))
        return 1
    else
        echo -e "${GREEN}✅ $var_name${NC}"
        return 0
    fi
}

check_optional() {
    local var_name=$1
    local var_value=${!var_name}
    local description=$2

    if [ -z "$var_value" ] || [ "$var_value" = "your_"* ]; then
        echo -e "${YELLOW}⚠️  OPTIONAL: $var_name (not set)${NC}"
        [ -n "$description" ] && echo -e "   ${YELLOW}→ $description${NC}"
        ((INFO++))
        return 1
    else
        echo -e "${GREEN}✅ $var_name${NC}"
        return 0
    fi
}

check_warning() {
    local var_name=$1
    local var_value=${!var_name}
    local bad_value=$2
    local message=$3

    if [ "$var_value" = "$bad_value" ]; then
        echo -e "${YELLOW}⚠️  WARNING: $var_name${NC}"
        echo -e "   ${YELLOW}→ $message${NC}"
        ((WARNINGS++))
        return 1
    fi
    return 0
}

check_format() {
    local var_name=$1
    local var_value=${!var_name}
    local pattern=$2
    local description=$3

    if [ -n "$var_value" ] && [[ ! "$var_value" =~ $pattern ]]; then
        echo -e "${RED}❌ INVALID: $var_name${NC}"
        echo -e "   ${YELLOW}→ $description${NC}"
        ((ERRORS++))
        return 1
    fi
    return 0
}

# ==========================================
# 🚀 REQUIRED CONFIGURATION
# ==========================================
echo -e "${BLUE}════════════════════════════════════════${NC}"
echo -e "${BLUE}🚀 REQUIRED FOR BASIC OPERATION${NC}"
echo -e "${BLUE}════════════════════════════════════════${NC}"

check_required "GOOGLE_API_KEY" "Get from: https://makersuite.google.com/app/apikey"
check_warning "GOOGLE_API_KEY" "AIzaSyDlMvCLaGNMbPJXvnNkpjf_d4gOQOr5Hbk" "Using example API key - replace with your own!"
check_required "GEMINI_MODEL" "Recommended: gemini-2.0-flash-exp"
check_required "REDIS_HOST" "localhost for local, redis for Docker"
check_required "REDIS_PORT" "Default: 6379"

echo ""

# ==========================================
# 🔐 AUTHENTICATION (Optional but Recommended)
# ==========================================
echo -e "${BLUE}════════════════════════════════════════${NC}"
echo -e "${BLUE}🔐 AUTHENTICATION (Optional)${NC}"
echo -e "${BLUE}════════════════════════════════════════${NC}"

check_optional "GOOGLE_CLIENT_ID" "Required for OAuth login"
check_optional "GOOGLE_CLIENT_SECRET" "Required for OAuth login"

if [ -n "$GOOGLE_CLIENT_ID" ]; then
    check_format "GOOGLE_CLIENT_ID" "\.apps\.googleusercontent\.com$" "Should end with .apps.googleusercontent.com"
    check_warning "GOOGLE_CLIENT_ID" "201806680568-34bjg6mnu76939outdakjbf8gmme1r5m.apps.googleusercontent.com" "Using example Client ID - replace with your own!"
fi

if [ -n "$GOOGLE_CLIENT_SECRET" ]; then
    check_format "GOOGLE_CLIENT_SECRET" "^GOCSPX-" "Should start with GOCSPX-"
    check_warning "GOOGLE_CLIENT_SECRET" "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" "Using example Secret - replace with your own!"
fi

check_optional "OAUTH_REDIRECT_URI" "OAuth callback URL"
check_optional "JWT_ACCESS_TOKEN_VALIDITY" "Default: 3600 (1 hour)"
check_optional "JWT_REFRESH_TOKEN_VALIDITY" "Default: 1209600 (14 days)"

echo ""

# ==========================================
# 🏗️ INFRASTRUCTURE (Optional - Full Stack)
# ==========================================
echo -e "${BLUE}════════════════════════════════════════${NC}"
echo -e "${BLUE}🏗️ INFRASTRUCTURE (Optional)${NC}"
echo -e "${BLUE}════════════════════════════════════════${NC}"
echo -e "${YELLOW}Only needed if running docker-compose.local.yml${NC}"

check_optional "DB_HOST" "PostgreSQL host"
check_optional "DB_PORT" "PostgreSQL port"
check_optional "DB_NAME" "Database name"
check_optional "DB_USER" "Database user"
check_optional "DB_PASSWORD" "Database password"

if [ -n "$DB_PASSWORD" ]; then
    check_warning "DB_PASSWORD" "4321" "WEAK PASSWORD! Use strong password in production"
    check_warning "DB_PASSWORD" "change_me_in_production" "Default password - change it!"

    # Check password strength
    if [ ${#DB_PASSWORD} -lt 8 ]; then
        echo -e "${YELLOW}⚠️  WARNING: DB_PASSWORD is too short (< 8 characters)${NC}"
        ((WARNINGS++))
    fi
fi

check_optional "REDIS_PASSWORD" "Redis password (optional)"
check_optional "REDIS_CACHE_TTL" "Cache TTL in seconds"

echo ""

# ==========================================
# 🔗 SERVICE URLS (Optional)
# ==========================================
echo -e "${BLUE}════════════════════════════════════════${NC}"
echo -e "${BLUE}🔗 SERVICE URLS${NC}"
echo -e "${BLUE}════════════════════════════════════════${NC}"

check_optional "USER_SERVICE_URL" "User service endpoint"
check_optional "AUTH_SERVICE_URL" "Auth service endpoint"
check_optional "PLAN_SERVICE_URL" "Plan service endpoint"
check_optional "VIDEO_SERVICE_URL" "Video service endpoint"
check_optional "LLM_AGENT_URL" "LLM Agent endpoint"

echo ""

# ==========================================
# ☁️ AWS SERVICES (Optional)
# ==========================================
echo -e "${BLUE}════════════════════════════════════════${NC}"
echo -e "${BLUE}☁️ AWS SERVICES (Optional)${NC}"
echo -e "${BLUE}════════════════════════════════════════${NC}"
echo -e "${YELLOW}Only needed for video generation and production${NC}"

check_optional "AWS_REGION" "AWS region"
check_optional "AWS_ACCESS_KEY_ID" "AWS credentials"
check_optional "AWS_SECRET_ACCESS_KEY" "AWS credentials"
check_optional "S3_BUCKET" "S3 bucket for storage"
check_optional "SQS_QUEUE_URL" "SQS queue for video jobs"
check_optional "SNS_TOPIC_ARN" "SNS topic for notifications"

if [ -n "$AWS_REGION" ]; then
    check_format "AWS_REGION" "^[a-z]{2}-[a-z]+-[0-9]$" "Invalid AWS region format (e.g., us-east-1)"
fi

if [ -n "$AWS_ACCESS_KEY_ID" ]; then
    check_warning "AWS_ACCESS_KEY_ID" "BedrockAPIKey-yr8r-at-501544476367" "Example AWS key detected - replace with your own!"
fi

echo ""

# ==========================================
# 🌐 EXTERNAL APIS (Optional)
# ==========================================
echo -e "${BLUE}════════════════════════════════════════${NC}"
echo -e "${BLUE}🌐 EXTERNAL APIS (Optional)${NC}"
echo -e "${BLUE}════════════════════════════════════════${NC}"

check_optional "OPENWEATHER_API_KEY" "Weather data"
check_optional "EXCHANGERATE_API_KEY" "Currency conversion"

echo ""

# ==========================================
# 🛠️ APPLICATION SETTINGS
# ==========================================
echo -e "${BLUE}════════════════════════════════════════${NC}"
echo -e "${BLUE}🛠️ APPLICATION SETTINGS${NC}"
echo -e "${BLUE}════════════════════════════════════════${NC}"

check_optional "ENVIRONMENT" "Current environment"
check_optional "LOG_LEVEL" "Logging level"
check_optional "MOCK_MODE" "Mock LLM responses"

echo ""

# ==========================================
# 📊 VALIDATION SUMMARY
# ==========================================
echo -e "${BLUE}════════════════════════════════════════${NC}"
echo -e "${BLUE}📊 VALIDATION SUMMARY${NC}"
echo -e "${BLUE}════════════════════════════════════════${NC}"
echo ""
echo -e "Errors:   ${RED}$ERRORS${NC}"
echo -e "Warnings: ${YELLOW}$WARNINGS${NC}"
echo -e "Info:     ${BLUE}$INFO${NC}"
echo ""

if [ $ERRORS -gt 0 ]; then
    echo -e "${RED}❌ Validation failed with $ERRORS error(s)${NC}"
    echo ""
    echo "Fix required variables before continuing:"
    echo "  1. Edit .env file: nano .env"
    echo "  2. Get Gemini API key: https://makersuite.google.com/app/apikey"
    echo "  3. Run validation again: ./scripts/validate-env.sh"
    echo ""
    exit 1
elif [ $WARNINGS -gt 0 ]; then
    echo -e "${YELLOW}⚠️  Validation passed with $WARNINGS warning(s)${NC}"
    echo ""
    echo "Review warnings before deploying to production"
    echo "Optional features may not work without proper configuration"
    echo ""
    exit 0
else
    echo -e "${GREEN}✅ All validations passed!${NC}"
    echo ""
    echo "Your environment is properly configured."
    echo "Start services: docker-compose up -d"
    echo ""
    exit 0
fi

