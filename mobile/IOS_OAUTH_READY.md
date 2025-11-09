# iOS Google OAuth - Ready to Create!

**Date:** 2025-11-09
**Status:** ✅ All information collected

---

## 📋 Your iOS App Information

```
Bundle ID: org.reactjs.native.example.mobile
Team ID: 88H7CMABS2
App Store ID: (Not needed for development)
```

---

## 🚀 Create iOS OAuth Client ID Now

### Step 1: Open Google Cloud Console

URL: https://console.cloud.google.com/apis/credentials

### Step 2: Create iOS Client ID

1. Click **"+ CREATE CREDENTIALS"** → **"OAuth client ID"**

2. Select **Application type: iOS**

3. Fill in the form:
   ```
   Name: Oddiya iOS
   Bundle ID: org.reactjs.native.example.mobile
   Team ID: 88H7CMABS2
   App Store ID: (leave blank)
   ```

4. Click **"CREATE"**

5. **Copy the iOS Client ID** (format: xxxxx-xxxxx.apps.googleusercontent.com)

### Step 3: Update .env File

Add the iOS Client ID to your .env file:

```bash
# iOS Client ID from Google Cloud Console
GOOGLE_IOS_CLIENT_ID=YOUR_IOS_CLIENT_ID_HERE.apps.googleusercontent.com
```

Replace `YOUR_IOS_CLIENT_ID_HERE` with the actual Client ID from Step 2.

### Step 4: Build and Test iOS

```bash
cd /Users/wjs/cursor/oddiya/mobile
npx react-native run-ios
```

**Expected Console Output:**
```
========== APP INITIALIZING ==========
Initializing Google Sign-In...
Platform: ios
✅ Google Sign-In configured successfully
```

**Test Google Sign-In:**
1. App launches on iOS Simulator
2. Tap "Google로 계속하기" button
3. Google account selection dialog appears
4. Sign in completes successfully

---

## 📱 Apple Sign-In Setup (Later)

Apple Sign-In requires a physical iOS device or paid Apple Developer account.

**When to set up:**
- When you have an iPhone/iPad to connect
- Or when you upgrade to paid Apple Developer Program ($99/year)
- Or when you're ready for production release

**For now:**
- iOS will show Google Sign-In only ✅
- Android shows Google Sign-In only ✅
- This is perfectly fine for development!

---

## 🎯 Current Priority

1. **✅ DONE:** Team ID confirmed (88H7CMABS2)
2. **⏳ NOW:** Create iOS OAuth Client ID in Google Cloud Console
3. **⏳ NEXT:** Update .env with iOS Client ID
4. **⏳ THEN:** Test iOS Google Sign-In
5. **🔜 LATER:** Android Google OAuth (SHA-1 + package name fix)
6. **🔜 LATER:** Apple Sign-In (when device available)

---

## 📝 Quick Commands

### Verify current configuration:
```bash
./scripts/verify-oauth-config.sh
```

### Test iOS:
```bash
npx react-native run-ios
```

### Test Android:
```bash
npx react-native run-android
```

---

**Next Step:** Create iOS OAuth Client ID using the information above! 🚀
