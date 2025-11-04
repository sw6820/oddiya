# Android APK Build Guide

## Prerequisites

- Node.js 18+
- Java Development Kit (JDK) 17
- Android Studio or Android SDK
- React Native CLI

## Step 1: Setup Android Environment

### 1.1 Install Android Studio

Download from: https://developer.android.com/studio

### 1.2 Install Android SDK

In Android Studio:
1. Preferences → Appearance & Behavior → System Settings → Android SDK
2. Install:
   - Android 13 (API Level 33) ✅ Recommended
   - Android SDK Platform-Tools
   - Android SDK Build-Tools 33.0.0

### 1.3 Set Environment Variables

Add to `~/.zshrc` or `~/.bash_profile`:

```bash
export ANDROID_HOME=$HOME/Library/Android/sdk
export PATH=$PATH:$ANDROID_HOME/emulator
export PATH=$PATH:$ANDROID_HOME/platform-tools
export PATH=$PATH:$ANDROID_HOME/tools
export PATH=$PATH:$ANDROID_HOME/tools/bin
```

Reload shell:
```bash
source ~/.zshrc
```

### 1.4 Verify Setup

```bash
java -version  # Should show JDK 17
node -v        # Should show Node 18+
adb --version  # Should show Android Debug Bridge
```

## Step 2: Initialize Android Project

Since the project doesn't have an `android` folder yet, we need to generate it:

```bash
cd /Users/wjs/cursor/oddiya/mobile

# Install dependencies
npm install

# Generate Android project structure
npx react-native init Oddiya --version 0.75.0 --directory temp_project

# Copy Android folder to current project
cp -r temp_project/android .
cp temp_project/android/gradle.properties android/
rm -rf temp_project

echo "Android project structure created"
```

## Step 3: Configure Android Project

### 3.1 Update Package Name

Edit `android/app/build.gradle`:

```gradle
android {
    namespace "com.oddiya"
    compileSdk 34

    defaultConfig {
        applicationId "com.oddiya"
        minSdkVersion 23
        targetSdkVersion 34
        versionCode 1
        versionName "1.0.0"
    }

    signingConfigs {
        release {
            // Generate keystore in Step 4
            storeFile file('my-release-key.keystore')
            storePassword System.getenv("KEYSTORE_PASSWORD") ?: "oddiya123"
            keyAlias System.getenv("KEY_ALIAS") ?: "my-key-alias"
            keyPassword System.getenv("KEY_PASSWORD") ?: "oddiya123"
        }
    }

    buildTypes {
        release {
            minifyEnabled false
            signingConfig signingConfigs.release
        }
    }
}
```

### 3.2 Update API URL

Edit `mobile/src/constants/config.ts`:

```typescript
export const CONFIG = {
  API_BASE_URL: __DEV__
    ? 'http://localhost:8080' // Development
    : 'http://YOUR_EC2_IP',    // Production - Update with your EC2 IP
  
  GOOGLE_WEB_CLIENT_ID: 'your-google-client-id.apps.googleusercontent.com',
  GOOGLE_ANDROID_CLIENT_ID: 'your-android-client-id.apps.googleusercontent.com',
};
```

### 3.3 Configure Google OAuth

Follow instructions in `mobile/GOOGLE_OAUTH_ANDROID_SETUP.md` to:
1. Create OAuth 2.0 Client ID for Android
2. Add SHA-1 fingerprint
3. Configure `android/app/google-services.json`

## Step 4: Generate Signing Key

Release APK must be signed with a keystore:

```bash
cd android/app

# Generate keystore
keytool -genkeypair -v \
  -storetype PKCS12 \
  -keystore my-release-key.keystore \
  -alias my-key-alias \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

**Enter password:** `oddiya123` (or choose your own)

**Important:** Keep this keystore file safe! You cannot update your app without it.

## Step 5: Build APK

### 5.1 Development Build (Unsigned)

```bash
cd /Users/wjs/cursor/oddiya/mobile

# Start Metro bundler in one terminal
npx react-native start

# Build and install on connected device (another terminal)
npx react-native run-android
```

### 5.2 Production Build (Signed APK)

```bash
cd /Users/wjs/cursor/oddiya/mobile/android

# Clean build
./gradlew clean

# Build release APK
./gradlew assembleRelease

# APK location:
# android/app/build/outputs/apk/release/app-release.apk
```

### 5.3 Build Android App Bundle (AAB) for Play Store

```bash
cd /Users/wjs/cursor/oddiya/mobile/android

# Build AAB
./gradlew bundleRelease

# AAB location:
# android/app/build/outputs/bundle/release/app-release.aab
```

## Step 6: Test APK

### 6.1 Install on Device

```bash
# Connect Android device via USB with USB Debugging enabled
adb devices  # Verify device is connected

# Install APK
adb install android/app/build/outputs/apk/release/app-release.apk
```

### 6.2 Test on Emulator

```bash
# List emulators
emulator -list-avds

# Start emulator
emulator -avd Pixel_5_API_33

# Install APK
adb install android/app/build/outputs/apk/release/app-release.apk
```

## Step 7: Distribute APK

### Option 1: Direct Distribution (Easiest & Free)

1. Upload `app-release.apk` to:
   - Google Drive
   - Dropbox
   - Your website
   - GitHub Releases

2. Share link with users

3. Users must enable "Install from Unknown Sources" in Android settings

### Option 2: Google Play Store (Recommended)

#### 7.1 Create Developer Account

- Cost: $25 one-time fee
- Register at: https://play.google.com/console

#### 7.2 Prepare Store Listing

Required assets:
- App icon: 512x512 PNG
- Feature graphic: 1024x500 PNG
- Screenshots: At least 2 (phone)
- Privacy Policy URL
- App description (Korean & English)

#### 7.3 Upload AAB

```bash
# Build production AAB
cd android
./gradlew bundleRelease

# Upload to Play Console
# File: android/app/build/outputs/bundle/release/app-release.aab
```

#### 7.4 Review Process

- Internal testing: Available immediately
- Alpha/Beta testing: 1-2 hours review
- Production: 1-7 days review

### Option 3: Firebase App Distribution (Free Testing)

```bash
# Install Firebase CLI
npm install -g firebase-tools

# Login
firebase login

# Deploy to Firebase
firebase appdistribution:distribute \
  android/app/build/outputs/apk/release/app-release.apk \
  --app YOUR_FIREBASE_APP_ID \
  --groups testers
```

## Build Automation Script

Create `mobile/scripts/build-android.sh`:

```bash
#!/bin/bash
set -e

echo "=== Oddiya Android Build Script ==="

cd "$(dirname "$0")/.."

# Install dependencies
echo "Installing dependencies..."
npm install

# Clean Android build
echo "Cleaning Android build..."
cd android
./gradlew clean

# Build release APK
echo "Building release APK..."
./gradlew assembleRelease

# Build release AAB (for Play Store)
echo "Building release AAB..."
./gradlew bundleRelease

echo ""
echo "=== Build Complete ==="
echo "APK: android/app/build/outputs/apk/release/app-release.apk"
echo "AAB: android/app/build/outputs/bundle/release/app-release.aab"
echo ""
echo "Install APK: adb install android/app/build/outputs/apk/release/app-release.apk"
```

Make executable:
```bash
chmod +x mobile/scripts/build-android.sh
```

Run:
```bash
./mobile/scripts/build-android.sh
```

## Troubleshooting

### Build Failed: SDK not found

```bash
# Set ANDROID_HOME
export ANDROID_HOME=$HOME/Library/Android/sdk
```

### Build Failed: Java version

```bash
# Install JDK 17
brew install openjdk@17
sudo ln -sfn /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk \
  /Library/Java/JavaVirtualMachines/openjdk-17.jdk
```

### Unable to load script from assets

```bash
# Bundle JavaScript
npx react-native bundle \
  --platform android \
  --dev false \
  --entry-file index.js \
  --bundle-output android/app/src/main/assets/index.android.bundle \
  --assets-dest android/app/src/main/res
```

### APK size too large

1. Enable ProGuard minification in `build.gradle`:
```gradle
buildTypes {
    release {
        minifyEnabled true
        shrinkResources true
        proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
    }
}
```

2. Enable App Bundle (AAB) instead of APK - reduces size by 15-20%

## Size Optimization

**Current APK size:** ~30-40MB (typical React Native app)

To reduce size:
1. Use AAB instead of APK (-15%)
2. Enable ProGuard (-20%)
3. Remove unused resources
4. Optimize images (use WebP)

**Target:** 25MB or less

## CI/CD Setup (Optional)

### GitHub Actions

Create `.github/workflows/android.yml`:

```yaml
name: Android Build

on:
  push:
    branches: [ main ]
    paths:
      - 'mobile/**'

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
      
      - name: Setup JDK
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Install dependencies
        working-directory: mobile
        run: npm install
      
      - name: Build APK
        working-directory: mobile/android
        run: ./gradlew assembleRelease
        env:
          KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
      
      - name: Upload APK
        uses: actions/upload-artifact@v3
        with:
          name: app-release
          path: mobile/android/app/build/outputs/apk/release/app-release.apk
```

## Quick Reference

```bash
# Development
npx react-native run-android

# Production build
cd android && ./gradlew assembleRelease

# Install APK
adb install app-release.apk

# Check build
adb logcat | grep ReactNative
```

---

**Last Updated:** 2025-11-03
