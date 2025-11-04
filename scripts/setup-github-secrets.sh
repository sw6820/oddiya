#!/bin/bash

# GitHub Secrets Setup Helper for Oddiya
# This script helps configure GitHub Secrets from local configuration files
# Usage: ./scripts/setup-github-secrets.sh

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

echo "=================================================="
echo "GitHub Secrets Setup Helper for Oddiya"
echo "=================================================="
echo ""

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

echo -e "${GREEN}✅ GitHub CLI is ready${NC}"
echo ""

# Function to extract value from .env file
get_env_value() {
    local key=$1
    local file=$2

    if [ -f "$file" ]; then
        grep "^${key}=" "$file" 2>/dev/null | cut -d'=' -f2- | tr -d '"' | tr -d "'"
    fi
}

# Function to set secret from value or file
set_secret() {
    local secret_name=$1
    local value=$2
    local description=$3

    if [ -z "$value" ] || [[ "$value" == PASTE_YOUR_* ]] || [ "$value" == "your-"* ]; then
        echo -e "${YELLOW}⏭️  Skipping${NC} ${secret_name}"
        echo -e "   ${BLUE}└─${NC} ${description}"
        echo -e "   ${YELLOW}└─${NC} No value found - set manually later"
        return 1
    fi

    echo -e "${CYAN}🔧 Setting${NC} ${secret_name}"
    echo -e "   ${BLUE}└─${NC} ${description}"

    if echo "$value" | gh secret set "$secret_name"; then
        echo -e "   ${GREEN}└─ ✅ Success${NC}"
        return 0
    else
        echo -e "   ${RED}└─ ❌ Failed${NC}"
        return 1
    fi
}

echo "=================================================="
echo "Step 1: AWS Deployment Secrets"
echo "=================================================="
echo ""

# Load from .env and terraform.tfvars
ENV_FILE=".env"
TFVARS_FILE="infrastructure/terraform/phase1/terraform.tfvars"

echo "Reading from local configuration files..."
echo "  • $ENV_FILE"
echo "  • $TFVARS_FILE"
echo ""

success_count=0
skip_count=0

# AWS Credentials (must be set manually)
echo -e "${YELLOW}⚠️  AWS credentials must be set manually${NC}"
echo "   Get from: https://console.aws.amazon.com/iam/"
echo "   Then run:"
echo "     gh secret set AWS_ACCESS_KEY_ID"
echo "     gh secret set AWS_SECRET_ACCESS_KEY"
echo ""
((skip_count+=2))

# SSH Key Name
if value=$(get_env_value "key_pair_name" "$TFVARS_FILE"); then
    set_secret "SSH_KEY_NAME" "$value" "SSH key pair name in AWS" && ((success_count++)) || ((skip_count++))
fi

# SSH Private Key
if [ -f ~/.ssh/oddiya-prod.pem ]; then
    echo -e "${CYAN}🔧 Setting${NC} SSH_PRIVATE_KEY"
    echo -e "   ${BLUE}└─${NC} From ~/.ssh/oddiya-prod.pem"
    if cat ~/.ssh/oddiya-prod.pem | gh secret set SSH_PRIVATE_KEY; then
        echo -e "   ${GREEN}└─ ✅ Success${NC}"
        ((success_count++))
    else
        echo -e "   ${RED}└─ ❌ Failed${NC}"
        ((skip_count++))
    fi
else
    echo -e "${YELLOW}⏭️  Skipping${NC} SSH_PRIVATE_KEY"
    echo -e "   ${YELLOW}└─${NC} File not found: ~/.ssh/oddiya-prod.pem"
    ((skip_count++))
fi

# Admin IP
if value=$(get_env_value "admin_ip_whitelist" "$TFVARS_FILE"); then
    # Remove brackets and quotes
    value=$(echo "$value" | tr -d '[]"' | cut -d'/' -f1)
    set_secret "ADMIN_IP" "$value" "Your IP address for security group" && ((success_count++)) || ((skip_count++))
fi

# Database Password
if value=$(get_env_value "DB_PASSWORD" "$ENV_FILE"); then
    set_secret "DB_PASSWORD" "$value" "PostgreSQL database password" && ((success_count++)) || ((skip_count++))
fi

# Gemini API Key
if value=$(get_env_value "GOOGLE_API_KEY" "$ENV_FILE"); then
    set_secret "GEMINI_API_KEY" "$value" "Google Gemini API key for LLM" && ((success_count++)) || ((skip_count++))
fi

# Google OAuth
if value=$(get_env_value "GOOGLE_CLIENT_ID" "$ENV_FILE"); then
    set_secret "GOOGLE_CLIENT_ID" "$value" "Google OAuth Client ID" && ((success_count++)) || ((skip_count++))
fi

if value=$(get_env_value "GOOGLE_CLIENT_SECRET" "$ENV_FILE"); then
    set_secret "GOOGLE_CLIENT_SECRET" "$value" "Google OAuth Client Secret" && ((success_count++)) || ((skip_count++))
fi

echo ""
echo "=================================================="
echo "Step 2: Expo/EAS Mobile Secrets"
echo "=================================================="
echo ""

# These need to be set manually or from specific files
echo -e "${YELLOW}⚠️  Mobile secrets require manual configuration:${NC}"
echo ""
echo "  ${CYAN}EXPO_TOKEN${NC} - Get from: https://expo.dev/accounts/[account]/settings/access-tokens"
echo "    gh secret set EXPO_TOKEN"
echo ""
echo "  ${CYAN}EAS_PROJECT_ID${NC} - Get from: mobile/app.json after running 'eas init'"
echo "    gh secret set EAS_PROJECT_ID"
echo ""
echo "  ${CYAN}API_BASE_URL${NC} - Your production API URL (after AWS deployment)"
echo "    gh secret set API_BASE_URL"
echo ""
echo "  ${CYAN}GOOGLE_CLIENT_ID_IOS${NC} - iOS OAuth Client ID from Google Cloud Console"
echo "    gh secret set GOOGLE_CLIENT_ID_IOS"
echo ""
echo "  ${CYAN}GOOGLE_SERVICES_JSON${NC} - From Firebase console (android/app/google-services.json)"
echo "    cat mobile/android/app/google-services.json | gh secret set GOOGLE_SERVICES_JSON"
echo ""
echo "  ${CYAN}Android Keystore${NC} - After generating keystore:"
echo "    base64 ~/.android/oddiya-release-key.jks | tr -d '\\n' | gh secret set ANDROID_KEYSTORE_BASE64"
echo "    gh secret set KEYSTORE_PASSWORD"
echo "    gh secret set KEY_ALIAS"
echo "    gh secret set KEY_PASSWORD"
echo ""

((skip_count+=9))

echo "=================================================="
echo "Summary"
echo "=================================================="
echo ""
echo -e "${GREEN}✅ Successfully set: ${success_count} secrets${NC}"
echo -e "${YELLOW}⏭️  Skipped: ${skip_count} secrets${NC}"
echo ""
echo "Next steps:"
echo "  1. Set remaining secrets manually (see above)"
echo "  2. Validate configuration:"
echo "     ./scripts/validate-github-secrets.sh"
echo "  3. View all secrets:"
echo "     gh secret list"
echo ""
echo "Documentation: See GITHUB_ACTIONS_SETUP.md"
echo "=================================================="
