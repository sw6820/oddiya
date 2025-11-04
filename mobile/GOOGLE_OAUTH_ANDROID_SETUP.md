# 🔐 Google OAuth Setup for Android - Complete Guide

**Implementation Date:** October 30, 2025
**Status:** ✅ Code Complete | ⏳ Configuration Needed

---

## ✅ What's Already Implemented

### Mobile App (Complete!)
- ✅ Google Sign-In library installed (`@react-native-google-signin/google-signin`)
- ✅ GoogleSignInService wrapper created
- ✅ Redux auth slice updated with Google OAuth
- ✅ WelcomeScreen updated with "Continue with Google" button
- ✅ Beautiful Google-branded button with loading state
- ✅ Error handling and user feedback
- ✅ Token storage in secure storage (Keychain/KeyStore)

### What You Need to Do
1. Get Google OAuth credentials from Google Cloud Console
2. Configure Android project (generate if needed)
3. Add credentials to your app
4. Build and test!

---

## 📋 Step-by-Step Setup

### STEP 1: Create Google Cloud Project (15 minutes)

#### 1.1 Go to Google Cloud Console
Visit: https://console.cloud.google.com/

#### 1.2 Create New Project
1. Click "Select a project" → "New Project"
2. Project name: **"Oddiya"**
3. Click "Create"
4. Wait for project creation (30 seconds)

#### 1.3 Enable Google+ API
1. In left sidebar: **APIs & Services** → **Library**
2. Search: **"Google+ API"**
3. Click **"Google+ API"**
4. Click **"Enable"**

---

### STEP 2: Configure OAuth Consent Screen (10 minutes)

#### 2.1 Go to OAuth Consent Screen
1. Left sidebar: **APIs & Services** → **OAuth consent screen**
2. User Type: Select **"External"**
3. Click **"Create"**

#### 2.2 Fill Out App Information
**OAuth consent screen (Page 1):**
- App name: `Oddiya`
- User support email: `your-email@example.com`
- App logo: (optional, upload 120x120px logo)
- Application home page: `https://oddiya.com` (or leave empty)
- Authorized domains: (leave empty for testing)
- Developer contact: `your-email@example.com`
- Click **"Save and Continue"**

**Scopes (Page 2):**
- Click **"Add or Remove Scopes"**
- Select:
  - ✅ `.../auth/userinfo.email`
  - ✅ `.../auth/userinfo.profile`
  - ✅ `openid`
- Click **"Update"**
- Click **"Save and Continue"**

**Test users (Page 3):**
- Click **"Add Users"**
- Add your email: `your-email@gmail.com`
- Click **"Add"**
- Click **"Save and Continue"**

**Summary (Page 4):**
- Review and click **"Back to Dashboard"**

---

### STEP 3: Create OAuth Credentials (10 minutes)

#### 3.1 Create Android OAuth Client ID

1. Left sidebar: **APIs & Services** → **Credentials**
2. Click **"+ CREATE CREDENTIALS"** → **"OAuth client ID"**

3. **Application type**: Select **"Android"**

4. **Name**: `Oddiya Android App`

5. **Package name**: `com.oddiya` (or your package name from `android/app/build.gradle`)

6. **SHA-1 certificate fingerprint**: Get it by running:
   ```bash
   cd android
   ./gradlew signingReport
   ```
   Look for **"SHA1"** under **"Variant: debug"**
   Copy the SHA-1 hash (looks like: `AA:BB:CC:DD:...`)

   **Alternative (using keytool):**
   ```bash
   keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
   ```

7. Paste the SHA-1 fingerprint

8. Click **"Create"**

#### 3.2 Create Web OAuth Client ID (Required!)

You also need a **Web Client ID** for the mobile app:

1. Click **"+ CREATE CREDENTIALS"** → **"OAuth client ID"**

2. **Application type**: Select **"Web application"**

3. **Name**: `Oddiya Web (for mobile)`

4. **Authorized JavaScript origins**: (leave empty)

5. **Authorized redirect URIs**: (leave empty)

6. Click **"Create"**

7. **IMPORTANT**: Copy the **Client ID** (looks like `xxx.apps.googleusercontent.com`)
   - You'll need this for Step 4!

---

### STEP 4: Generate Android Project (if needed)

If you don't have an `android/` directory in your mobile app:

```bash
cd /Users/wjs/cursor/oddiya/mobile

# Generate Android project
npx react-native init OddiyaMobile --version 0.75.0 --skip-install

# Copy the android directory
cp -r OddiyaMobile/android ./

# Clean up
rm -rf OddiyaMobile

# Verify
ls -la android/
```

---

### STEP 5: Configure Android Project (15 minutes)

#### 5.1 Update `android/build.gradle`

Open `android/build.gradle` and add Google Services:

```gradle
buildscript {
    ext {
        buildToolsVersion = "34.0.0"
        minSdkVersion = 23
        compileSdkVersion = 34
        targetSdkVersion = 34
        ndkVersion = "26.1.10909125"
        kotlinVersion = "1.9.22"
    }
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.1")
        classpath("com.facebook.react:react-native-gradle-plugin")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin")
        // ADD THIS LINE:
        classpath("com.google.gms:google-services:4.4.0")
    }
}
```

#### 5.2 Update `android/app/build.gradle`

Add Google Sign-In dependency:

```gradle
dependencies {
    implementation("com.facebook.react:react-android")

    // ADD THESE LINES:
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // ... rest of dependencies
}

// ADD THIS AT THE BOTTOM:
apply plugin: "com.google.gms.google-services"
```

#### 5.3 Create `android/app/google-services.json`

Create a minimal `google-services.json` file:

```json
{
  "project_info": {
    "project_number": "123456789",
    "project_id": "oddiya-XXXXX",
    "storage_bucket": "oddiya-XXXXX.appspot.com"
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "1:123456789:android:XXXXX",
        "android_client_info": {
          "package_name": "com.oddiya"
        }
      },
      "oauth_client": [
        {
          "client_id": "YOUR_WEB_CLIENT_ID.apps.googleusercontent.com",
          "client_type": 3
        },
        {
          "client_id": "YOUR_ANDROID_CLIENT_ID.apps.googleusercontent.com",
          "client_type": 1,
          "android_info": {
            "package_name": "com.oddiya",
            "certificate_hash": "YOUR_SHA1_HASH"
          }
        }
      ],
      "api_key": [
        {
          "current_key": "YOUR_API_KEY"
        }
      ],
      "services": {
        "appinvite_service": {
          "other_platform_oauth_client": [
            {
              "client_id": "YOUR_WEB_CLIENT_ID.apps.googleusercontent.com",
              "client_type": 3
            }
          ]
        }
      }
    }
  ],
  "configuration_version": "1"
}
```

**OR Download from Firebase:**
1. Go to https://console.firebase.google.com/
2. Add your Google Cloud project
3. Add Android app
4. Download `google-services.json`
5. Move to `android/app/google-services.json`

#### 5.4 Update `App.tsx` with Web Client ID

Open `/Users/wjs/cursor/oddiya/mobile/App.tsx`:

```typescript
// REPLACE THIS LINE:
const GOOGLE_WEB_CLIENT_ID = 'YOUR_WEB_CLIENT_ID.apps.googleusercontent.com';

// WITH YOUR ACTUAL WEB CLIENT ID FROM STEP 3.2:
const GOOGLE_WEB_CLIENT_ID = '123456789-abcdef.apps.googleusercontent.com';
```

---

### STEP 6: Build and Run (5 minutes)

#### 6.1 Install Dependencies
```bash
cd /Users/wjs/cursor/oddiya/mobile

# Install npm packages
npm install

# Install Android dependencies
cd android
./gradlew clean
cd ..
```

#### 6.2 Run on Android
```bash
# Start Metro bundler
npm start

# In another terminal, run Android app
npm run android
```

Or open in Android Studio:
```bash
cd android
open -a "Android Studio" .
```

---

### STEP 7: Test Google Sign-In! 🎉

1. **App opens** → See Welcome screen
2. **Click "Continue with Google"** → Google Sign-In dialog appears
3. **Select Google account** → Grant permissions
4. **Success!** → Navigate to main app

---

## 🔧 Configuration Reference

### File Structure
```
mobile/
├── android/
│   ├── app/
│   │   ├── google-services.json ← Google OAuth config
│   │   └── build.gradle ← Add Google dependencies
│   └── build.gradle ← Add Google Services plugin
├── App.tsx ← Set GOOGLE_WEB_CLIENT_ID
└── src/
    ├── services/
    │   └── googleSignInService.ts ← Google Sign-In wrapper
    └── screens/
        └── WelcomeScreen.tsx ← Google button
```

### Important IDs You Need

1. **Web Client ID** (from Step 3.2)
   - Format: `xxx.apps.googleusercontent.com`
   - Used in: `App.tsx` → `GOOGLE_WEB_CLIENT_ID`
   - Used in: `google-services.json` → `oauth_client[0].client_id`

2. **Android Client ID** (from Step 3.1)
   - Format: `xxx.apps.googleusercontent.com`
   - Used in: `google-services.json` → `oauth_client[1].client_id`

3. **SHA-1 Fingerprint** (from Step 3.1)
   - Format: `AA:BB:CC:DD:EE:FF:...`
   - Used in: Google Cloud Console → Android OAuth Client
   - Used in: `google-services.json` → `android_info.certificate_hash`

---

## 🐛 Troubleshooting

### Error: "DEVELOPER_ERROR"

**Problem**: Google Sign-In shows "DEVELOPER_ERROR"

**Solutions**:
1. **Check SHA-1 fingerprint** matches:
   ```bash
   cd android && ./gradlew signingReport
   ```
   Compare with Google Cloud Console

2. **Check package name** matches:
   ```bash
   grep "applicationId" android/app/build.gradle
   ```
   Should be `com.oddiya`

3. **Check Web Client ID** is correct in `App.tsx`

4. **Wait 5-10 minutes** after creating OAuth credentials (Google propagation delay)

### Error: "SIGN_IN_CANCELLED"

**Problem**: User cancelled sign-in

**Solution**: This is normal user behavior, no fix needed

### Error: "PLAY_SERVICES_NOT_AVAILABLE"

**Problem**: Google Play Services not installed/outdated

**Solutions**:
1. **On emulator**: Use Google Play system image
2. **On device**: Update Google Play Services from Play Store

### Error: "Module not found: @react-native-google-signin"

**Problem**: Package not installed properly

**Solution**:
```bash
cd /Users/wjs/cursor/oddiya/mobile
rm -rf node_modules
npm install
cd android && ./gradlew clean && cd ..
npm run android
```

### Build Error: "Duplicate class"

**Problem**: Conflicting Google library versions

**Solution**: Add to `android/gradle.properties`:
```properties
android.useAndroidX=true
android.enableJetifier=true
```

---

## 📱 Backend Integration

### Backend Endpoint Needed

The mobile app sends the Google ID Token to:
```
POST /api/auth/google
Content-Type: application/json

{
  "idToken": "eyJhbGc..."
}
```

**Backend should**:
1. Verify ID token with Google
2. Extract user info (email, name)
3. Create/find user in database
4. Generate JWT tokens
5. Return tokens to mobile app

---

## 🎯 Complete Flow Diagram

```
Mobile App                 Google                Backend
   │                         │                      │
   │ 1. Click "Continue      │                      │
   │    with Google"         │                      │
   ├────────────────────────>│                      │
   │                         │                      │
   │ 2. Google Sign-In       │                      │
   │    Dialog               │                      │
   │<────────────────────────┤                      │
   │                         │                      │
   │ 3. User selects         │                      │
   │    account              │                      │
   ├────────────────────────>│                      │
   │                         │                      │
   │ 4. ID Token received    │                      │
   │<────────────────────────┤                      │
   │                         │                      │
   │ 5. Send ID Token        │                      │
   ├─────────────────────────┼─────────────────────>│
   │                         │                      │
   │                         │ 6. Verify ID Token   │
   │                         │<─────────────────────┤
   │                         │                      │
   │                         │ 7. User info         │
   │                         ├─────────────────────>│
   │                         │                      │
   │                         │ 8. Create/find user  │
   │                         │      in database     │
   │                         │                      │
   │ 9. JWT Tokens           │                      │
   │<─────────────────────────┼──────────────────────┤
   │                         │                      │
   │ 10. Store tokens        │                      │
   │     (SecureStore)       │                      │
   │                         │                      │
   │ 11. Navigate to         │                      │
   │     Main App ✅         │                      │
```

---

## ✅ Checklist

### Google Cloud Console
- [ ] Created Google Cloud project
- [ ] Enabled Google+ API
- [ ] Configured OAuth consent screen
- [ ] Created Android OAuth client ID
- [ ] Created Web OAuth client ID (for mobile)
- [ ] Added test users
- [ ] Copied Web Client ID

### Android Project
- [ ] Generated/have android directory
- [ ] Updated `android/build.gradle` with Google Services
- [ ] Updated `android/app/build.gradle` with dependencies
- [ ] Created `android/app/google-services.json`
- [ ] Updated `App.tsx` with Web Client ID

### Testing
- [ ] Built Android app successfully
- [ ] Clicked "Continue with Google"
- [ ] Google Sign-In dialog appeared
- [ ] Successfully signed in
- [ ] Navigated to main app
- [ ] Tokens stored securely

---

## 📚 Additional Resources

- **Google Sign-In Android**: https://developers.google.com/identity/sign-in/android
- **React Native Google Sign-In**: https://github.com/react-native-google-signin/google-signin
- **Google Cloud Console**: https://console.cloud.google.com/
- **Firebase Console**: https://console.firebase.google.com/

---

## 🔐 Security Best Practices

### DO ✅
- ✅ Use Web Client ID (not Android Client ID) in `App.tsx`
- ✅ Store `google-services.json` in `.gitignore`
- ✅ Use different OAuth clients for dev/staging/prod
- ✅ Verify ID tokens on backend
- ✅ Use HTTPS for production backend

### DON'T ❌
- ❌ Commit `google-services.json` to git
- ❌ Share OAuth credentials publicly
- ❌ Use same credentials for dev and prod
- ❌ Trust ID token without verification
- ❌ Store sensitive data in AsyncStorage

---

## 🎉 Success!

Once you complete all steps:

1. **Open mobile app**
2. **See "Continue with Google" button**
3. **Click button**
4. **Sign in with Google**
5. **Navigate to main app**
6. **You're logged in!** ✅

**Status**: All code is ready! Just add your Google OAuth credentials and test! 🚀

---

**Questions?** Check the troubleshooting section above or refer to the official documentation.

**Next Steps**: After Android works, you can set up iOS (similar process but uses iOS bundle ID instead of package name).
