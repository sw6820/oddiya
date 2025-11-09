# OAuth Setup Status and Next Steps

**Last Updated:** 2025-11-09

## Current Status

### ✅ Completed

1. **Code Implementation**
   - ✅ App.tsx: Google Sign-In initialization with platform-specific config
   - ✅ WelcomeScreen.tsx: Google and Apple login flows re-enabled
   - ✅ googleSignInService.ts: iOS client ID support added
   - ✅ appleSignInService.ts: Already implemented
   - ✅ authSlice.ts: Redux async thunks for OAuth
   - ✅ Backend endpoints: `/api/v1/auth/google/verify` and `/api/v1/auth/apple/verify`

2. **Configuration Files**
   - ✅ .env file created with environment variables
   - ✅ env.d.ts TypeScript types defined
   - ✅ GOOGLE_WEB_CLIENT_ID configured: `201806680568-5soo17svuu5v8mbg3agg3sci6rcvobo6.apps.googleusercontent.com`

3. **Automation Scripts**
   - ✅ `scripts/setup-ios-oauth.sh` - Interactive iOS OAuth setup
   - ✅ `scripts/verify-oauth-config.sh` - Configuration verification

4. **Documentation**
   - ✅ IOS_GOOGLE_OAUTH_SETUP.md - Complete iOS setup guide
   - ✅ OAUTH_SETUP_COMPLETE.md - Implementation summary

### ⏳ Pending

1. **Team ID Configuration**
   - ❌ Not configured in Xcode
   - Required for iOS OAuth Client ID creation
   - **Action:** Xcode is open → Go to Signing & Capabilities → Select Team

2. **iOS OAuth Client ID**
   - ❌ Not created in Google Cloud Console
   - Required after Team ID is configured
   - **Action:** Run `./scripts/setup-ios-oauth.sh` after setting Team ID

## Quick Start

### Verify Current Configuration

```bash
cd /Users/wjs/cursor/oddiya/mobile
./scripts/verify-oauth-config.sh
```

### Configure iOS OAuth (Step-by-Step)

```bash
# Step 1: Xcode is already open
# In Xcode:
# 1. Select 'mobile' project in navigator
# 2. Go to 'Signing & Capabilities' tab
# 3. Select your Apple Developer account in 'Team' dropdown
# 4. Note the Team ID (e.g., ABCD123456)

# Step 2: Run automated setup script
cd /Users/wjs/cursor/oddiya/mobile
./scripts/setup-ios-oauth.sh

# This script will:
# - Detect your Bundle ID (org.reactjs.native.example.mobile)
# - Detect your Team ID from Xcode
# - Guide you through creating iOS OAuth Client ID in Google Cloud Console
# - Update .env with the iOS Client ID
# - Clean and rebuild the app
```

### Test Android OAuth (Ready Now!)

Android OAuth can be tested immediately since Web Client ID is already configured:

```bash
cd /Users/wjs/cursor/oddiya/mobile
npx react-native run-android
# App should build and launch on Android emulator
# Test Google login button
```

### Test iOS OAuth (After Setup)

```bash
cd /Users/wjs/cursor/oddiya/mobile
npx react-native run-ios
# Check console for: ✅ Google Sign-In configured successfully
# Test Google login button
```

## Configuration Details

### Current Bundle ID
```
org.reactjs.native.example.mobile
```
**Note:** This is the default React Native bundle ID. Consider changing to `com.oddiya.mobile` for production.

### Environment Variables (.env)

```bash
# Backend
BACKEND_ENV=local
AWS_EC2_IP=13.209.85.15

# Google OAuth
GOOGLE_WEB_CLIENT_ID=201806680568-5soo17svuu5v8mbg3agg3sci6rcvobo6.apps.googleusercontent.com
GOOGLE_IOS_CLIENT_ID=YOUR_GOOGLE_IOS_CLIENT_ID_HERE.apps.googleusercontent.com  # TODO
```

### Google Cloud Console Setup

#### Web Client ID (Already Created) ✅
- **Type:** Web application
- **Name:** Oddiya Web Client
- **Client ID:** 201806680568-5soo17svuu5v8mbg3agg3sci6rcvobo6.apps.googleusercontent.com
- **Used for:** Android + Backend verification

#### iOS Client ID (To Be Created) ⏳
- **Type:** iOS
- **Name:** Oddiya iOS
- **Bundle ID:** org.reactjs.native.example.mobile
- **Team ID:** [From Xcode - Pending]
- **App Store ID:** Leave blank (development)
- **Used for:** iOS app

## Testing Checklist

### Android Testing
- [ ] Build app: `npx react-native run-android`
- [ ] Check console: `========== APP INITIALIZING ==========`
- [ ] Check console: `Platform: android`
- [ ] Check console: `✅ Google Sign-In configured successfully`
- [ ] Tap "Google로 계속하기" button
- [ ] Google account selection dialog appears
- [ ] Select account and consent
- [ ] App receives JWT token
- [ ] Navigate to Plans screen

### iOS Testing (After iOS Client ID Setup)
- [ ] Build app: `npx react-native run-ios`
- [ ] Check console: `========== APP INITIALIZING ==========`
- [ ] Check console: `Platform: ios`
- [ ] Check console: `✅ Google Sign-In configured successfully`
- [ ] Tap "Google로 계속하기" button
- [ ] Google account selection dialog appears
- [ ] Select account and consent
- [ ] App receives JWT token
- [ ] Navigate to Plans screen
- [ ] (iOS 13+) Test "Apple로 계속하기" button
- [ ] Face ID/Touch ID authentication
- [ ] App receives JWT token
- [ ] Navigate to Plans screen

## Troubleshooting

### Error: "failed to determine clientID"
**iOS Only**
- Verify GOOGLE_IOS_CLIENT_ID in .env is not a placeholder
- Rebuild: `npx react-native run-ios`
- Check console for successful initialization

### Error: "DEVELOPER_ERROR"
**iOS Only**
- Bundle ID mismatch between Xcode and Google Cloud Console
- Verify Bundle ID in Xcode matches Google Cloud Console
- Recreate iOS Client ID if needed

### Error: "API not enabled"
- Google Sign-In API not enabled in Google Cloud Console
- Go to APIs & Services → Library → Search "Google Sign-In API" → Enable

### Error: "Play Services not available"
**Android Only**
- Update Google Play Services in Android emulator
- Or test on physical device

## Backend Endpoints

### Auth Service (Port 8081)

**Google OAuth:**
```
POST http://13.209.85.15:8081/api/v1/auth/google/verify

Request Body:
{
  "idToken": "eyJhbGciOiJSUzI1NiIsImtpZCI..."
}

Response:
{
  "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI...",
  "refreshToken": "...",
  "expiresIn": 3600,
  "userId": 123
}
```

**Apple OAuth:**
```
POST http://13.209.85.15:8081/api/v1/auth/apple/verify

Request Body:
{
  "identityToken": "eyJraWQiOiJBSURPUEsxIiwiYWxn...",
  "authorizationCode": "c1234567890abcdef..."
}

Response:
{
  "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI...",
  "refreshToken": "...",
  "expiresIn": 3600,
  "userId": 123
}
```

## Project Information

**Bundle ID:** `org.reactjs.native.example.mobile` (mobile/ios/mobile.xcodeproj/project.pbxproj)
**Team ID:** Not configured (Xcode → Signing & Capabilities)
**App Store ID:** Not needed for development

**React Native Version:** 0.82.1
**Platform:** iOS 13+ and Android 5.0+

## Next Actions

1. **In Xcode (Already Open):**
   - Navigate to Signing & Capabilities tab
   - Select your Apple Developer Team
   - Note the Team ID

2. **Run Setup Script:**
   ```bash
   cd /Users/wjs/cursor/oddiya/mobile
   ./scripts/setup-ios-oauth.sh
   ```

3. **Test on Android:**
   ```bash
   npx react-native run-android
   ```
   Android OAuth should work immediately!

4. **Test on iOS (After Setup):**
   ```bash
   npx react-native run-ios
   ```

## Resources

- **Setup Guide:** [IOS_GOOGLE_OAUTH_SETUP.md](IOS_GOOGLE_OAUTH_SETUP.md)
- **Implementation Summary:** [OAUTH_SETUP_COMPLETE.md](OAUTH_SETUP_COMPLETE.md)
- **Google Cloud Console:** https://console.cloud.google.com/apis/credentials
- **Apple Developer Portal:** https://developer.apple.com/account/

---

**Status:** Ready for Team ID configuration and iOS Client ID creation
**Android OAuth:** ✅ Ready to test
**iOS OAuth:** ⏳ Pending Team ID + iOS Client ID
