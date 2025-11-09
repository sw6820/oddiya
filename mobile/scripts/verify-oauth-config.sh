#!/bin/bash

# OAuth Configuration Verification Script
# Checks all OAuth setup requirements

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MOBILE_DIR="$(dirname "$SCRIPT_DIR")"
IOS_DIR="$MOBILE_DIR/ios"
PROJECT_FILE="$IOS_DIR/mobile.xcodeproj/project.pbxproj"
ENV_FILE="$MOBILE_DIR/.env"

echo "========================================="
echo "OAuth Configuration Status"
echo "========================================="
echo ""

# Check Bundle ID
BUNDLE_ID=$(grep -A 1 "PRODUCT_BUNDLE_IDENTIFIER" "$PROJECT_FILE" | grep -v "PRODUCT_BUNDLE_IDENTIFIER" | head -1 | sed 's/.*= //; s/;//; s/ //g')
echo "📱 Bundle ID: $BUNDLE_ID"

# Check Team ID
TEAM_ID=$(grep "DevelopmentTeam" "$PROJECT_FILE" | head -1 | sed 's/.*= //; s/;//; s/ //g' || echo "")
if [ -z "$TEAM_ID" ]; then
    echo "❌ Team ID: Not configured"
    TEAM_STATUS="❌"
else
    echo "✅ Team ID: $TEAM_ID"
    TEAM_STATUS="✅"
fi

echo ""

# Check .env file
if [ ! -f "$ENV_FILE" ]; then
    echo "❌ .env file not found"
    exit 1
fi

echo "🔐 Environment Variables:"
echo "-------------------------------------------"

# Check Web Client ID
WEB_CLIENT_ID=$(grep "GOOGLE_WEB_CLIENT_ID=" "$ENV_FILE" | cut -d'=' -f2)
if [[ "$WEB_CLIENT_ID" == *"YOUR_GOOGLE"* ]] || [ -z "$WEB_CLIENT_ID" ]; then
    echo "❌ GOOGLE_WEB_CLIENT_ID: Not configured"
    WEB_STATUS="❌"
else
    echo "✅ GOOGLE_WEB_CLIENT_ID: ${WEB_CLIENT_ID:0:30}..."
    WEB_STATUS="✅"
fi

# Check iOS Client ID
IOS_CLIENT_ID=$(grep "GOOGLE_IOS_CLIENT_ID=" "$ENV_FILE" | cut -d'=' -f2)
if [[ "$IOS_CLIENT_ID" == *"YOUR_GOOGLE"* ]] || [ -z "$IOS_CLIENT_ID" ]; then
    echo "❌ GOOGLE_IOS_CLIENT_ID: Not configured"
    IOS_STATUS="❌"
else
    echo "✅ GOOGLE_IOS_CLIENT_ID: ${IOS_CLIENT_ID:0:30}..."
    IOS_STATUS="✅"
fi

# Check Backend
BACKEND_ENV=$(grep "BACKEND_ENV=" "$ENV_FILE" | cut -d'=' -f2)
AWS_EC2_IP=$(grep "AWS_EC2_IP=" "$ENV_FILE" | cut -d'=' -f2)
echo "✅ BACKEND_ENV: $BACKEND_ENV"
echo "✅ AWS_EC2_IP: $AWS_EC2_IP"

echo ""
echo "========================================="
echo "Summary"
echo "========================================="
echo ""

# Overall status
ALL_GOOD=true

echo "$TEAM_STATUS Team ID configured in Xcode"
[ "$TEAM_STATUS" = "❌" ] && ALL_GOOD=false

echo "$WEB_STATUS Google Web Client ID configured"
[ "$WEB_STATUS" = "❌" ] && ALL_GOOD=false

echo "$IOS_STATUS Google iOS Client ID configured"
[ "$IOS_STATUS" = "❌" ] && ALL_GOOD=false

echo ""

if [ "$ALL_GOOD" = true ]; then
    echo "✅ All OAuth configurations complete!"
    echo ""
    echo "Ready to test:"
    echo "  cd $MOBILE_DIR"
    echo "  npx react-native run-ios"
    echo ""
    exit 0
else
    echo "⚠️  Some configurations are missing"
    echo ""
    echo "Next steps:"

    if [ "$TEAM_STATUS" = "❌" ]; then
        echo "  1. Configure Team ID in Xcode"
        echo "     open $IOS_DIR/mobile.xcworkspace"
    fi

    if [ "$WEB_STATUS" = "❌" ]; then
        echo "  2. Create Web OAuth Client ID in Google Cloud Console"
        echo "     https://console.cloud.google.com/apis/credentials"
    fi

    if [ "$IOS_STATUS" = "❌" ]; then
        echo "  3. Run iOS OAuth setup script:"
        echo "     $SCRIPT_DIR/setup-ios-oauth.sh"
    fi

    echo ""
    exit 1
fi
