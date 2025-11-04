# Mobile Build Scripts

Automation scripts for building and deploying the Oddiya mobile app.

## 📱 Available Scripts

### build-expo.sh

**Purpose:** Interactive Expo build for Android and/or iOS

**What it does:**
- Prompts for platform selection (Android / iOS / Both)
- Runs `eas build` with selected platform
- Uses production profile

**Usage:**
```bash
./build-expo.sh
```

**Interactive prompts:**
```
빌드 옵션을 선택하세요:
1) Android만
2) iOS만
3) 둘 다 (Android + iOS) ⭐ 추천
선택 [1-3]: 3
```

**Prerequisites:**
- Expo account created
- EAS CLI installed globally: `npm install -g eas-cli`
- Logged in: `eas login`
- Project configured: `eas build:configure`

**Output:**
- Build progress URL
- Download links sent to email
- APK/IPA files ready in ~15 minutes

---

### build-android.sh

**Purpose:** Build Android APK using React Native CLI (Legacy)

⚠️ **Deprecated:** This script uses React Native CLI. We now use Expo EAS Build for cloud-based builds.

**Legacy usage:**
```bash
./build-android.sh
```

**Why deprecated:**
- Requires Android Studio setup
- Requires Java 11 configuration
- Mac/Windows only
- Slow local builds (30+ minutes)

**Recommended alternative:** Use `build-expo.sh` instead

---

### migrate-to-expo.sh

**Purpose:** Automated migration from React Native CLI to Expo

**What it does:**
- Installs Expo packages
- Creates/updates `app.json` configuration
- Creates `eas.json` for build configuration
- Removes React Native CLI dependencies
- Creates placeholder assets (icon, splash)

**Usage:**
```bash
./migrate-to-expo.sh
```

**Changes made:**
- ✅ Adds `expo` package
- ✅ Adds `eas-cli` globally
- ✅ Creates `app.json` with bundle identifiers
- ✅ Creates `eas.json` with build profiles
- ✅ Creates placeholder images in `assets/`
- ✅ Updates `package.json` scripts

**After migration:**
1. Login: `eas login`
2. Configure: `eas build:configure`
3. Build: `eas build --platform all`

---

## 🚀 Quick Start Guide

### First Time Setup

```bash
# 1. Navigate to mobile folder
cd /Users/wjs/cursor/oddiya/mobile

# 2. Install dependencies (if not done)
npm install

# 3. Login to Expo
eas login

# 4. Configure EAS Build (first time only)
eas build:configure

# When prompted:
# - "Generate a new Android Keystore?" → Yes
# - "Generate credentials for iOS?" → Skip for now (or Yes if you have Apple Developer account)
```

### Build for Production

```bash
# Option 1: Interactive build (Recommended)
./scripts/build-expo.sh

# Option 2: Direct command
eas build --platform all --profile production
```

### Development Build

```bash
# Build development version (faster, includes dev tools)
eas build --platform android --profile development

# Install on device
adb install downloaded-app.apk
```

---

## 📋 Build Profiles

Configured in `eas.json`:

### Production Profile
- **APK:** Ready for Play Store
- **IPA:** Ready for App Store
- **Optimized:** Minified, obfuscated
- **Build time:** ~15 minutes per platform

### Development Profile
- **APK/IPA:** Includes dev tools and debugging
- **Not optimized:** Faster build time (~10 minutes)
- **For testing:** Internal testing only

### Preview Profile
- **APK/IPA:** Between production and development
- **Some optimizations:** Good for beta testing
- **Build time:** ~12 minutes

---

## 🎯 Common Use Cases

### Build Both Android + iOS

```bash
eas build --platform all --profile production
```

**Result:**
- Android APK (ready for Play Store)
- iOS IPA (ready for App Store)
- Total time: ~15 minutes (builds in parallel)
- Download links sent to email

### Build Android Only

```bash
eas build --platform android --profile production
```

**Use when:**
- Testing Android-specific features
- Don't have Apple Developer account
- Quick iteration

### Build iOS Only

```bash
eas build --platform ios --profile production
```

**Prerequisites:**
- Apple Developer account ($99/year)
- Apple ID configured in EAS

### Check Build Status

```bash
# List all builds
eas build:list

# View specific build
eas build:view <BUILD_ID>

# Follow build logs
eas build:view <BUILD_ID> --logs
```

### Download Builds

```bash
# Download latest Android build
eas build:download --platform android --latest

# Download latest iOS build
eas build:download --platform ios --latest

# Download specific build
eas build:download --id <BUILD_ID>
```

---

## 🔧 Configuration Files

### app.json

Main Expo configuration:

```json
{
  "expo": {
    "name": "Oddiya",
    "slug": "oddiya",
    "version": "1.0.0",
    "ios": {
      "bundleIdentifier": "com.oddiya.app"
    },
    "android": {
      "package": "com.oddiya.app"
    }
  }
}
```

### eas.json

Build configuration:

```json
{
  "build": {
    "production": {
      "android": {
        "buildType": "apk"
      },
      "ios": {
        "buildConfiguration": "Release"
      }
    }
  }
}
```

---

## 💰 Build Costs

### Free Tier
- **30 builds/month** - Free
- **Both platforms** count as 2 builds
- **Development builds** also count

### Paid Plans
- **Unlimited builds** - $29/month
- **Priority queue** - Faster builds
- **More parallel builds** - Build multiple platforms simultaneously

### Optimization Tips

To stay within free tier:
1. Build both platforms together (`--platform all`) instead of separately
2. Use development profile for testing
3. Only build production when ready for release
4. Use `eas update` for OTA updates (doesn't count as build)

---

## 🚨 Troubleshooting

### "Not logged in"

```bash
eas logout
eas login
```

### "Project not configured"

```bash
eas build:configure
```

### Build failed

```bash
# Check logs
eas build:list
# Click failed build to see error

# Common issues:
# - Missing environment variables
# - Invalid bundle identifier
# - Expired certificates (iOS)
```

### "Build limit exceeded"

You've used all 30 free builds this month:
- Wait until next month
- Upgrade to paid plan ($29/mo)

### APK won't install

```bash
# Enable "Install unknown apps" on Android device
# Settings → Security → Install unknown apps → Enable for your file manager
```

---

## 📚 Related Documentation

- [Mobile Quick Start](../QUICK_START.md) - Step-by-step build guide
- [Mobile README](../README.md) - Mobile app overview
- [Getting Started](../../docs/GETTING_STARTED.md) - Complete project setup

---

## 🔗 External Resources

- [Expo Documentation](https://docs.expo.dev)
- [EAS Build Documentation](https://docs.expo.dev/build/introduction/)
- [Expo Application Services](https://expo.dev/eas)

---

**Last Updated:** 2025-11-03
