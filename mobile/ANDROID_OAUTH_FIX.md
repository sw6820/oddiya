# Android OAuth Fix - SHA-1 Configuration Required

**Issue:** "A non-recoverable sign in failure occurred"

**Root Cause:** Android requires an Android OAuth Client ID with SHA-1 certificate fingerprint configured in Google Cloud Console.

---

## 📋 Required Information

### Package Name
```
com.mobile
```

### SHA-1 Fingerprint (Debug)
```
5E:8F:16:06:2E:A3:CD:2C:4A:0D:54:78:76:BA:A6:F3:8C:AB:F6:25
```

### SHA-256 Fingerprint (Debug)
```
FA:C6:17:45:DC:09:03:78:6F:B9:ED:E6:2A:96:2B:39:9F:73:48:F0:BB:6F:89:9B:83:32:66:75:91:03:3B:9C
```

---

## 🔧 Fix Steps

### Option 1: Create Android OAuth Client ID (Recommended)

This is the proper way for Android native apps using Google Sign-In SDK.

1. **Go to Google Cloud Console**
   - URL: https://console.cloud.google.com/apis/credentials
   - Select your project (same one with Web Client ID)

2. **Create Android OAuth Client ID**
   - Click **"+ CREATE CREDENTIALS"** → **"OAuth client ID"**
   - Application type: **Android**
   - Name: **Oddiya Android**
   - Package name: `com.mobile`
   - SHA-1 certificate fingerprint: `5E:8F:16:06:2E:A3:CD:2C:4A:0D:54:78:76:BA:A6:F3:8C:AB:F6:25`
   - Click **"CREATE"**

3. **Important Notes**
   - You don't need to add the Android Client ID to .env
   - The Web Client ID is still needed (already configured)
   - Android uses Web Client ID + SHA-1 verification together

4. **Rebuild and Test**
   ```bash
   cd /Users/wjs/cursor/oddiya/mobile
   npx react-native run-android
   ```

### Option 2: Add SHA-1 to Existing Web Client ID (Alternative)

This works but is less secure for production.

1. **Edit Web Client ID**
   - Go to: https://console.cloud.google.com/apis/credentials
   - Find your Web Client ID: `201806680568-5soo17svuu5v8mbg3agg3sci6rcvobo6.apps.googleusercontent.com`
   - Click on it to edit

2. **Add Authorized Origins**
   - Scroll to "Authorized JavaScript origins"
   - Add: `http://localhost`
   - Add: `http://localhost:8081`

3. **Save and Test**
   - Click "SAVE"
   - Wait 5 minutes for changes to propagate
   - Rebuild: `npx react-native run-android`

---

## ✅ Verification Steps

After creating the Android OAuth Client ID:

1. **Wait 5 Minutes**
   - OAuth configuration changes take time to propagate

2. **Rebuild Android App**
   ```bash
   cd /Users/wjs/cursor/oddiya/mobile
   npx react-native run-android
   ```

3. **Test Google Sign-In**
   - Tap "Google로 계속하기" button
   - Google account selection dialog should appear
   - Select account
   - **Should NOT see "non-recoverable sign in failure" error**

4. **Check Console Logs**
   ```bash
   # In another terminal
   npx react-native log-android
   ```

   Expected logs:
   ```
   ========== APP INITIALIZING ==========
   Initializing Google Sign-In...
   Platform: android
   ✅ Google Sign-In configured successfully
   [WelcomeScreen] Starting Google Sign-In...
   ```

   After successful sign-in:
   ```
   [Auth] Google login response: {...}
   ✅ Auth loaded successfully
   ```

---

## 🔍 Understanding the Issue

### Why Web Client ID Alone Doesn't Work

For Android native apps using `@react-native-google-signin/google-signin`:

1. **Web Client ID** - Used for backend token verification
2. **Android OAuth Client ID** - Links package name + SHA-1 fingerprint
3. **Both Required** - Android SDK validates app signature before allowing sign-in

### The Error Chain

```
User clicks Google Sign-In
  ↓
Google Play Services checks app signature
  ↓
Compares SHA-1 with Google Cloud Console
  ↓
❌ No matching Android Client ID found
  ↓
Throws: "A non-recoverable sign in failure occurred"
```

### After Fix

```
User clicks Google Sign-In
  ↓
Google Play Services checks app signature
  ↓
✅ Finds matching Android Client ID (com.mobile + SHA-1)
  ↓
Shows account selection dialog
  ↓
Returns ID Token to app
  ↓
App sends to backend with Web Client ID for verification
```

---

## 📊 Current OAuth Configuration

### Created ✅
| Type | Client ID | Purpose |
|------|-----------|---------|
| Web | 201806680568-5soo17svuu5v8mbg3agg3sci6rcvobo6.apps.googleusercontent.com | Backend verification |

### To Create ⏳
| Type | Package/Bundle | SHA-1/Team ID | Purpose |
|------|----------------|---------------|---------|
| Android | com.mobile | 5E:8F:16:06:2E:A3:CD:2C:4A:0D:54:78:76:BA:A6:F3:8C:AB:F6:25 | Android app signature |
| iOS | org.reactjs.native.example.mobile | [Pending Team ID] | iOS app signing |

---

## 🎯 Quick Command Reference

### Get SHA-1 Fingerprint (for future reference)
```bash
cd /Users/wjs/cursor/oddiya/mobile/android
./gradlew signingReport | grep "SHA1:"
```

### Get Package Name
```bash
grep "applicationId" /Users/wjs/cursor/oddiya/mobile/android/app/build.gradle
```

### Rebuild Android App
```bash
cd /Users/wjs/cursor/oddiya/mobile
npx react-native run-android
```

### View Android Logs
```bash
npx react-native log-android
# Or filter for errors:
~/Library/Android/sdk/platform-tools/adb logcat | grep -E "ERROR|Exception"
```

---

## 🆘 Troubleshooting

### Error Persists After Creating Android Client ID

**Wait and Retry:**
```bash
# Wait 5 minutes for OAuth config to propagate
sleep 300

# Clear app data
~/Library/Android/sdk/platform-tools/adb shell pm clear com.mobile

# Rebuild
npx react-native run-android
```

### Different Error: "DEVELOPER_ERROR"

This means SHA-1 mismatch. Verify:
```bash
# Get current SHA-1
cd /Users/wjs/cursor/oddiya/mobile/android
./gradlew signingReport | grep "SHA1:" | head -1

# Compare with Google Cloud Console
# They must match exactly!
```

### Error: "API not enabled"

Enable Google Sign-In API:
1. Go to: https://console.cloud.google.com/apis/library
2. Search: "Google Sign-In API"
3. Click "ENABLE"
4. Wait 1-2 minutes
5. Retry

---

## 📝 Production Considerations

For production release (not needed now):

1. **Generate Release Keystore**
   ```bash
   keytool -genkey -v -keystore release.keystore -alias release \
     -keyalg RSA -keysize 2048 -validity 10000
   ```

2. **Get Release SHA-1**
   ```bash
   keytool -list -v -keystore release.keystore -alias release
   ```

3. **Create Another Android OAuth Client ID**
   - Same package: `com.mobile`
   - Different SHA-1: [Release SHA-1]
   - Name: "Oddiya Android (Release)"

4. **Update build.gradle**
   Configure release signing in `android/app/build.gradle`

---

## ✅ Success Checklist

After creating Android OAuth Client ID:

- [ ] Android Client ID created in Google Cloud Console
- [ ] Package name: `com.mobile`
- [ ] SHA-1: `5E:8F:16:06:2E:A3:CD:2C:4A:0D:54:78:76:BA:A6:F3:8C:AB:F6:25`
- [ ] Waited 5 minutes for propagation
- [ ] Rebuilt Android app
- [ ] Tested Google Sign-In
- [ ] No "non-recoverable" error
- [ ] Account selection dialog appears

---

**Next:** After Android works, proceed with iOS setup using `./scripts/setup-ios-oauth.sh`

**Estimated Time:** 10 minutes (5 min setup + 5 min propagation)

---

**Last Updated:** 2025-11-09
**Status:** Android OAuth Client ID Required
