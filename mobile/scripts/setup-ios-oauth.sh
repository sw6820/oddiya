#!/bin/bash

# iOS Google OAuth Setup Script
# This script helps configure iOS Google OAuth step-by-step

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MOBILE_DIR="$(dirname "$SCRIPT_DIR")"
IOS_DIR="$MOBILE_DIR/ios"
XCODE_PROJECT="$IOS_DIR/mobile.xcworkspace"
PROJECT_FILE="$IOS_DIR/mobile.xcodeproj/project.pbxproj"
ENV_FILE="$MOBILE_DIR/.env"

echo "========================================="
echo "iOS Google OAuth Setup"
echo "========================================="
echo ""

# Step 1: Check Bundle ID
echo "📱 Step 1: Bundle Identifier"
echo "-------------------------------------------"
BUNDLE_ID=$(grep -A 1 "PRODUCT_BUNDLE_IDENTIFIER" "$PROJECT_FILE" | grep -v "PRODUCT_BUNDLE_IDENTIFIER" | head -1 | sed 's/.*= //; s/;//; s/ //g')
echo "✅ Current Bundle ID: $BUNDLE_ID"
echo ""

# Step 2: Check Team ID
echo "👤 Step 2: Apple Developer Team ID"
echo "-------------------------------------------"
TEAM_ID=$(grep "DevelopmentTeam" "$PROJECT_FILE" | head -1 | sed 's/.*= //; s/;//; s/ //g' || echo "")

if [ -z "$TEAM_ID" ]; then
    echo "❌ Team ID not configured"
    echo ""
    echo "To configure Team ID:"
    echo "  1. Open Xcode: open $XCODE_PROJECT"
    echo "  2. Select 'mobile' project in navigator"
    echo "  3. Go to 'Signing & Capabilities' tab"
    echo "  4. Select your Apple Developer account in 'Team' dropdown"
    echo "  5. Team ID will be auto-filled (e.g., ABCD123456)"
    echo ""
    read -p "Press Enter after configuring Team ID in Xcode, or 'q' to quit: " response
    if [ "$response" = "q" ]; then
        exit 0
    fi

    # Re-check Team ID
    TEAM_ID=$(grep "DevelopmentTeam" "$PROJECT_FILE" | head -1 | sed 's/.*= //; s/;//; s/ //g' || echo "")
    if [ -z "$TEAM_ID" ]; then
        echo "⚠️  Team ID still not found. Please configure manually in Xcode."
        exit 1
    fi
fi

echo "✅ Team ID: $TEAM_ID"
echo ""

# Step 3: Google Cloud Console Instructions
echo "☁️  Step 3: Create iOS OAuth Client ID"
echo "-------------------------------------------"
echo "Open Google Cloud Console and create iOS OAuth Client ID:"
echo ""
echo "  1. Go to: https://console.cloud.google.com/apis/credentials"
echo "  2. Click '+ CREATE CREDENTIALS' → 'OAuth client ID'"
echo "  3. Select Application type: 'iOS'"
echo "  4. Fill in:"
echo "     - Name: Oddiya iOS"
echo "     - Bundle ID: $BUNDLE_ID"
echo "     - Team ID: $TEAM_ID"
echo "     - App Store ID: (leave blank for development)"
echo "  5. Click 'CREATE'"
echo "  6. Copy the iOS Client ID (format: xxxxx-xxxxx.apps.googleusercontent.com)"
echo ""
read -p "Enter your iOS Client ID: " IOS_CLIENT_ID

if [ -z "$IOS_CLIENT_ID" ]; then
    echo "❌ iOS Client ID cannot be empty"
    exit 1
fi

# Step 4: Update .env file
echo ""
echo "📝 Step 4: Update .env file"
echo "-------------------------------------------"

if [ ! -f "$ENV_FILE" ]; then
    echo "❌ .env file not found at: $ENV_FILE"
    exit 1
fi

# Update iOS Client ID in .env
if grep -q "GOOGLE_IOS_CLIENT_ID=" "$ENV_FILE"; then
    sed -i '' "s|GOOGLE_IOS_CLIENT_ID=.*|GOOGLE_IOS_CLIENT_ID=$IOS_CLIENT_ID|" "$ENV_FILE"
    echo "✅ Updated GOOGLE_IOS_CLIENT_ID in .env"
else
    echo "GOOGLE_IOS_CLIENT_ID=$IOS_CLIENT_ID" >> "$ENV_FILE"
    echo "✅ Added GOOGLE_IOS_CLIENT_ID to .env"
fi

echo ""
echo "Current .env configuration:"
grep "GOOGLE.*CLIENT_ID" "$ENV_FILE"
echo ""

# Step 5: Clean and rebuild
echo "🔨 Step 5: Rebuild iOS App"
echo "-------------------------------------------"
echo "Cleaning and rebuilding is required after .env changes"
echo ""
read -p "Run clean build now? (y/n): " rebuild

if [ "$rebuild" = "y" ]; then
    echo "Cleaning iOS build..."
    cd "$IOS_DIR"
    rm -rf Pods Podfile.lock
    pod install
    cd "$MOBILE_DIR"

    echo ""
    echo "✅ Clean complete. Now run: npx react-native run-ios"
fi

# Step 6: Testing instructions
echo ""
echo "========================================="
echo "✅ Setup Complete!"
echo "========================================="
echo ""
echo "Next steps:"
echo "  1. Run: cd $MOBILE_DIR && npx react-native run-ios"
echo "  2. Check console for: '✅ Google Sign-In configured successfully'"
echo "  3. Tap 'Google로 계속하기' button"
echo "  4. Select Google account"
echo "  5. App should receive JWT token and navigate to Plans screen"
echo ""
echo "Troubleshooting:"
echo "  - View setup guide: cat $MOBILE_DIR/IOS_GOOGLE_OAUTH_SETUP.md"
echo "  - Check logs: npx react-native log-ios"
echo ""
