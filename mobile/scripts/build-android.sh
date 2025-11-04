#!/bin/bash
# Oddiya Android Build Automation Script

set -e

echo "🚀 Oddiya Android Build Script"
echo "================================"

# Navigate to mobile directory
cd "$(dirname "$0")/.."

# Check if Android folder exists
if [ ! -d "android" ]; then
    echo "❌ Error: android folder not found"
    echo "Please run the initialization steps first:"
    echo "  npx react-native init Oddiya --version 0.75.0 --directory temp_project"
    echo "  cp -r temp_project/android ."
    echo "  rm -rf temp_project"
    exit 1
fi

# Install dependencies
echo ""
echo "📦 Installing dependencies..."
npm install

# Navigate to Android directory
cd android

# Clean previous builds
echo ""
echo "🧹 Cleaning previous builds..."
./gradlew clean

# Build Release APK
echo ""
echo "🔨 Building Release APK..."
./gradlew assembleRelease

# Build Release AAB (for Play Store)
echo ""
echo "📦 Building Release AAB (Play Store)..."
./gradlew bundleRelease

# Show results
echo ""
echo "✅ Build Complete!"
echo "================================"
echo ""
echo "📱 APK Location:"
echo "   $(pwd)/app/build/outputs/apk/release/app-release.apk"
echo ""
echo "📦 AAB Location (Play Store):"
echo "   $(pwd)/app/build/outputs/bundle/release/app-release.aab"
echo ""
echo "Size:"
APK_SIZE=$(du -h app/build/outputs/apk/release/app-release.apk 2>/dev/null | awk '{print $1}' || echo "N/A")
AAB_SIZE=$(du -h app/build/outputs/bundle/release/app-release.aab 2>/dev/null | awk '{print $1}' || echo "N/A")
echo "   APK: $APK_SIZE"
echo "   AAB: $AAB_SIZE"
echo ""
echo "Install command:"
echo "   adb install app/build/outputs/apk/release/app-release.apk"
echo ""
