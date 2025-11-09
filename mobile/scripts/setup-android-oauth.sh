#!/bin/bash

# Android OAuth Setup Helper
# Displays required information and opens Google Cloud Console

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MOBILE_DIR="$(dirname "$SCRIPT_DIR")"
ANDROID_DIR="$MOBILE_DIR/android"

echo "========================================="
echo "Android OAuth Client ID Setup"
echo "========================================="
echo ""

# Get package name
PACKAGE_NAME=$(grep "applicationId" "$ANDROID_DIR/app/build.gradle" | sed 's/.*applicationId "//' | sed 's/".*//')
echo "📦 Package Name: $PACKAGE_NAME"

# Get SHA-1
echo ""
echo "🔑 Getting SHA-1 Fingerprint..."
cd "$ANDROID_DIR"
SHA1=$(./gradlew signingReport 2>/dev/null | grep "SHA1:" | head -1 | sed 's/.*SHA1: //')
SHA256=$(./gradlew signingReport 2>/dev/null | grep "SHA-256:" | head -1 | sed 's/.*SHA-256: //')

echo "✅ SHA-1: $SHA1"
echo "✅ SHA-256: $SHA256"

echo ""
echo "========================================="
echo "Setup Instructions"
echo "========================================="
echo ""
echo "1. Opening Google Cloud Console..."
echo "   URL: https://console.cloud.google.com/apis/credentials"
echo ""
echo "2. Click '+ CREATE CREDENTIALS' → 'OAuth client ID'"
echo ""
echo "3. Fill in the form:"
echo "   - Application type: Android"
echo "   - Name: Oddiya Android"
echo "   - Package name: $PACKAGE_NAME"
echo "   - SHA-1 certificate fingerprint: $SHA1"
echo ""
echo "4. Click 'CREATE'"
echo ""
echo "5. Wait 5 minutes for changes to propagate"
echo ""
echo "6. Rebuild and test:"
echo "   cd $MOBILE_DIR"
echo "   npx react-native run-android"
echo ""
echo "========================================="
echo ""

# Copy SHA-1 to clipboard (macOS)
echo "$SHA1" | pbcopy
echo "✅ SHA-1 copied to clipboard!"
echo ""

read -p "Press Enter to open Google Cloud Console... "

# Open Google Cloud Console
open "https://console.cloud.google.com/apis/credentials"

echo ""
echo "📋 Summary copied to clipboard:"
echo "-------------------------------------------"
echo "Package: $PACKAGE_NAME"
echo "SHA-1: $SHA1"
echo "-------------------------------------------"
echo ""
echo "After creating the Android OAuth Client ID:"
echo "  1. Wait 5 minutes"
echo "  2. Run: npx react-native run-android"
echo "  3. Test Google Sign-In button"
echo ""
echo "For detailed troubleshooting, see:"
echo "  $MOBILE_DIR/ANDROID_OAUTH_FIX.md"
echo ""
