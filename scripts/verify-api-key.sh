#!/bin/bash

# ==========================================
# VERIFY GEMINI API KEY CONFIGURATION
# ==========================================
# This script checks if the Gemini API key is properly configured
# in all required files for Phase 1 deployment
#
# Usage: ./scripts/verify-api-key.sh
# ==========================================

set -e

echo "🔍 Verifying Gemini API Key Configuration..."
echo ""

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Files to check
FILES=(
    ".env"
    "services/llm-agent/.env"
    "infrastructure/terraform/phase1/terraform.tfvars"
)

PLACEHOLDER="PASTE_YOUR_GEMINI_API_KEY_HERE"
ALL_GOOD=true

echo "📁 Checking configuration files:"
echo ""

for FILE in "${FILES[@]}"; do
    if [ ! -f "$FILE" ]; then
        echo -e "${RED}❌ MISSING:${NC} $FILE"
        ALL_GOOD=false
        continue
    fi

    # Check if file contains placeholder
    if grep -q "$PLACEHOLDER" "$FILE" 2>/dev/null; then
        echo -e "${RED}❌ PLACEHOLDER:${NC} $FILE"
        echo "   └─ Still contains: $PLACEHOLDER"
        ALL_GOOD=false
    # Check if file contains an actual API key (starts with AIza)
    elif grep -q "AIza[A-Za-z0-9_-]\{35\}" "$FILE" 2>/dev/null; then
        API_KEY=$(grep -o "AIza[A-Za-z0-9_-]\{35\}" "$FILE" | head -1)
        MASKED_KEY="${API_KEY:0:10}...${API_KEY: -4}"
        echo -e "${GREEN}✅ CONFIGURED:${NC} $FILE"
        echo "   └─ Key found: $MASKED_KEY"
    else
        echo -e "${YELLOW}⚠️  UNKNOWN:${NC} $FILE"
        echo "   └─ No valid API key pattern found"
        ALL_GOOD=false
    fi
done

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

if [ "$ALL_GOOD" = true ]; then
    echo -e "${GREEN}✅ SUCCESS!${NC} All files are properly configured!"
    echo ""
    echo "📝 Next steps:"
    echo "   1. Create SSH key in AWS Seoul region (oddiya-prod)"
    echo "   2. Deploy: cd infrastructure/terraform/phase1 && terraform apply"
    echo ""
    exit 0
else
    echo -e "${RED}❌ INCOMPLETE!${NC} Some files still need configuration."
    echo ""
    echo "📝 To fix this:"
    echo "   1. Get your Gemini API key from: https://ai.google.dev/"
    echo "   2. Edit each file above and replace placeholders"
    echo "   3. Run this script again to verify"
    echo ""
    echo "💡 Quick edit commands:"
    echo "   nano .env"
    echo "   nano services/llm-agent/.env"
    echo "   nano infrastructure/terraform/phase1/terraform.tfvars"
    echo ""
    exit 1
fi
