#!/bin/bash

# Validate environment configuration before deployment

echo "🔍 VALIDATING ENVIRONMENT CONFIGURATION"
echo "========================================"
echo ""

ENV=${1:-.env}

if [ ! -f "$ENV" ]; then
    echo "❌ Environment file not found: $ENV"
    exit 1
fi

# Load environment
set -a
source "$ENV"
set +a

ERRORS=0
WARNINGS=0

check_required() {
    local var_name=$1
    local var_value=${!var_name}
    
    if [ -z "$var_value" ]; then
        echo "❌ MISSING: $var_name"
        ((ERRORS++))
        return 1
    else
        echo "✅ $var_name"
        return 0
    fi
}

check_warning() {
    local var_name=$1
    local var_value=${!var_name}
    local default_value=$2
    
    if [ "$var_value" = "$default_value" ]; then
        echo "⚠️  WARNING: $var_name is using default value"
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
    
    if [[ ! "$var_value" =~ $pattern ]]; then
        echo "❌ INVALID: $var_name - $description"
        ((ERRORS++))
        return 1
    fi
    return 0
}

echo "════════════════════════════════════════"
echo "DATABASE CONFIGURATION"
echo "════════════════════════════════════════"
check_required "DB_HOST"
check_required "DB_PORT"
check_required "DB_NAME"
check_required "DB_USER"
check_required "DB_PASSWORD"
check_warning "DB_PASSWORD" "oddiya_password_dev"
echo ""

echo "════════════════════════════════════════"
echo "REDIS CONFIGURATION"
echo "════════════════════════════════════════"
check_required "REDIS_HOST"
check_required "REDIS_PORT"
echo ""

echo "════════════════════════════════════════"
echo "AWS CONFIGURATION"
echo "════════════════════════════════════════"
check_required "AWS_REGION"
check_required "S3_BUCKET"
check_format "AWS_REGION" "^[a-z]{2}-[a-z]+-[0-9]$" "Invalid AWS region format"
echo ""

echo "════════════════════════════════════════"
echo "OAUTH CONFIGURATION"
echo "════════════════════════════════════════"
check_required "GOOGLE_CLIENT_ID"
check_required "GOOGLE_CLIENT_SECRET"
check_warning "GOOGLE_CLIENT_ID" "test-client-id"
echo ""

echo "════════════════════════════════════════"
echo "JWT CONFIGURATION"
echo "════════════════════════════════════════"
check_required "JWT_ACCESS_TOKEN_VALIDITY"
check_required "JWT_REFRESH_TOKEN_VALIDITY"
echo ""

echo "════════════════════════════════════════"
echo "EXTERNAL APIS"
echo "════════════════════════════════════════"
check_required "KAKAO_LOCAL_API_KEY"
check_warning "KAKAO_LOCAL_API_KEY" "test-kakao-key"
echo ""

echo "════════════════════════════════════════"
echo "VALIDATION SUMMARY"
echo "════════════════════════════════════════"
echo ""
echo "Errors:   $ERRORS"
echo "Warnings: $WARNINGS"
echo ""

if [ $ERRORS -gt 0 ]; then
    echo "❌ Validation failed with $ERRORS error(s)"
    echo "Please fix the errors before deploying"
    exit 1
elif [ $WARNINGS -gt 0 ]; then
    echo "⚠️  Validation passed with $WARNINGS warning(s)"
    echo "Review warnings before deploying to production"
    exit 0
else
    echo "✅ All validations passed!"
    exit 0
fi

