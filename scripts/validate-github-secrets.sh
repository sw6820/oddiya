#!/bin/bash

# GitHub Secrets Validation Script for Oddiya
# This script helps verify all required GitHub Secrets are configured
# Usage: ./scripts/validate-github-secrets.sh

set -e

echo "=================================================="
echo "GitHub Secrets Validation for Oddiya Deployment"
echo "=================================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Check if gh CLI is installed
if ! command -v gh &> /dev/null; then
    echo -e "${RED}❌ ERROR: GitHub CLI (gh) is not installed${NC}"
    echo ""
    echo "Install it with:"
    echo "  brew install gh"
    echo "  gh auth login"
    exit 1
fi

# Check if authenticated
if ! gh auth status &> /dev/null; then
    echo -e "${RED}❌ ERROR: Not authenticated with GitHub CLI${NC}"
    echo ""
    echo "Authenticate with:"
    echo "  gh auth login"
    exit 1
fi

echo -e "${GREEN}✅ GitHub CLI is installed and authenticated${NC}"
echo ""

# Function to check if a secret exists
check_secret() {
    local secret_name=$1
    local description=$2
    local validation_pattern=$3

    # GitHub doesn't allow reading secret values via CLI
    # So we just check if the secret exists in the list
    if gh secret list 2>/dev/null | grep -q "^${secret_name}"; then
        echo -e "${GREEN}✅${NC} ${secret_name}"
        echo -e "   ${BLUE}└─${NC} ${description}"
        return 0
    else
        echo -e "${RED}❌${NC} ${secret_name}"
        echo -e "   ${YELLOW}└─${NC} ${description}"
        echo -e "   ${YELLOW}└─${NC} MISSING - Please configure this secret"
        return 1
    fi
}

echo "Checking AWS Deployment Secrets (9 required):"
echo "--------------------------------------------"

missing_count=0

check_secret "AWS_ACCESS_KEY_ID" "AWS Access Key for Terraform" || ((missing_count++))
check_secret "AWS_SECRET_ACCESS_KEY" "AWS Secret Access Key" || ((missing_count++))
check_secret "SSH_KEY_NAME" "SSH key pair name (e.g., oddiya-prod)" || ((missing_count++))
check_secret "SSH_PRIVATE_KEY" "SSH private key for EC2 access" || ((missing_count++))
check_secret "ADMIN_IP" "Your IP address for security group" || ((missing_count++))
check_secret "DB_PASSWORD" "PostgreSQL database password" || ((missing_count++))
check_secret "GEMINI_API_KEY" "Google Gemini API key for LLM" || ((missing_count++))
check_secret "GOOGLE_CLIENT_ID" "Google OAuth Client ID" || ((missing_count++))
check_secret "GOOGLE_CLIENT_SECRET" "Google OAuth Client Secret" || ((missing_count++))

echo ""
echo "Checking Expo/EAS Mobile Deployment Secrets (7 required):"
echo "---------------------------------------------------------"

check_secret "EXPO_TOKEN" "Expo authentication token" || ((missing_count++))
check_secret "EAS_PROJECT_ID" "EAS project ID from expo.dev" || ((missing_count++))
check_secret "API_BASE_URL" "Production API base URL" || ((missing_count++))
check_secret "GOOGLE_CLIENT_ID_IOS" "Google OAuth iOS Client ID" || ((missing_count++))
check_secret "GOOGLE_SERVICES_JSON" "google-services.json for Android" || ((missing_count++))
check_secret "ANDROID_KEYSTORE_BASE64" "Base64 encoded Android keystore" || ((missing_count++))
check_secret "KEYSTORE_PASSWORD" "Android keystore password" || ((missing_count++))
check_secret "KEY_ALIAS" "Android key alias" || ((missing_count++))
check_secret "KEY_PASSWORD" "Android key password" || ((missing_count++))

echo ""
echo "Checking Optional Secrets (Apple OAuth):"
echo "----------------------------------------"

check_secret "APPLE_CLIENT_ID" "Apple Sign In Service ID" || echo -e "   ${BLUE}└─${NC} Optional - only needed for Apple OAuth"
check_secret "APPLE_TEAM_ID" "Apple Developer Team ID" || echo -e "   ${BLUE}└─${NC} Optional - only needed for Apple OAuth"
check_secret "APPLE_KEY_ID" "Apple Sign In Key ID" || echo -e "   ${BLUE}└─${NC} Optional - only needed for Apple OAuth"
check_secret "APPLE_PRIVATE_KEY" "Apple Sign In Private Key" || echo -e "   ${BLUE}└─${NC} Optional - only needed for Apple OAuth"

echo ""
echo "=================================================="
echo "Summary"
echo "=================================================="

if [ $missing_count -eq 0 ]; then
    echo -e "${GREEN}✅ All required secrets are configured!${NC}"
    echo ""
    echo "You can now run automated deployments:"
    echo "  • AWS Deployment: Push to main or manually trigger"
    echo "  • Mobile Build: Manual trigger via GitHub Actions"
    echo ""
    echo "GitHub Actions: https://github.com/$(gh repo view --json nameWithOwner -q .nameWithOwner)/actions"
else
    echo -e "${RED}❌ Missing ${missing_count} required secret(s)${NC}"
    echo ""
    echo "To configure missing secrets:"
    echo "  1. Go to: https://github.com/$(gh repo view --json nameWithOwner -q .nameWithOwner)/settings/secrets/actions"
    echo "  2. Click 'New repository secret'"
    echo "  3. Add each missing secret from the list above"
    echo ""
    echo "Or use GitHub CLI:"
    echo "  gh secret set SECRET_NAME"
    echo ""
    echo "Detailed instructions: See GITHUB_ACTIONS_SETUP.md"
fi

echo ""
echo "=================================================="
