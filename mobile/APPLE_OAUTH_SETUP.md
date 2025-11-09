# Apple OAuth Setup Guide - iOS & Android

**Date:** 2025-11-09
**Status:** iOS Ready | Android Optional

---

## 📊 Overview

| Platform | Status | Method | UX Quality |
|----------|--------|--------|------------|
| **iOS** | ✅ Implemented | Native SDK | ⭐⭐⭐⭐⭐ Excellent |
| **Android** | ⚠️ Optional | Web-based | ⭐⭐ Poor |

---

## 🍎 iOS Apple Sign-In (RECOMMENDED)

### ✅ Current Status

Your app **already has iOS Apple Sign-In implemented**:

**Code Files:**
- ✅ `appleSignInService.ts` - Native Apple Sign-In wrapper
- ✅ `WelcomeScreen.tsx` - Apple login button (iOS 13+ only)
- ✅ `authSlice.ts` - Redux action `loginWithApple()`
- ✅ `MobileAuthController.java` - Backend `/api/v1/auth/apple/verify`

**User Flow:**
1. User taps "Apple로 계속하기" button (iOS 13+ only)
2. Face ID/Touch ID authentication
3. Apple returns: Identity Token + Authorization Code + User info
4. App sends to backend: `POST /api/v1/auth/apple/verify`
5. Backend verifies token and returns JWT
6. User logged in!

### 🔧 Setup Required

#### Step 1: Enable "Sign in with Apple" in Xcode

Xcode is already open at: `/Users/wjs/cursor/oddiya/mobile/ios/mobile.xcworkspace`

1. **Select Target:**
   - Click "mobile" project in left sidebar
   - Select "mobile" target (not project)

2. **Add Capability:**
   - Go to "Signing & Capabilities" tab
   - Click "+ Capability" button (top left)
   - Search for "Sign in with Apple"
   - Double-click to add

3. **Verify:**
   - You should see "Sign in with Apple" capability listed
   - Xcode auto-configures entitlements

#### Step 2: Configure Apple Developer Portal

**2a. Create Service ID** (for backend verification):

```
1. Go to: https://developer.apple.com/account/resources/identifiers/list/serviceId

2. Click "+" button

3. Select "Services IDs" → Continue

4. Fill in:
   - Description: Oddiya Auth Service
   - Identifier: com.oddiya.auth (or similar, must be unique)

5. Check "Sign in with Apple"

6. Click "Configure" next to "Sign in with Apple":
   - Primary App ID: [Select your app's Bundle ID]
   - Domains and Subdomains: 13.209.85.15
   - Return URLs:
     • http://13.209.85.15:8081/api/v1/auth/apple/callback
     • http://localhost:8081/api/v1/auth/apple/callback (for testing)

7. Save → Continue → Register
```

**2b. Create Key** (for token verification):

```
1. Go to: https://developer.apple.com/account/resources/authkeys/list

2. Click "+" button

3. Fill in:
   - Key Name: Oddiya Apple Sign In Key
   - Check "Sign in with Apple"

4. Click "Configure" → Select your Primary App ID

5. Continue → Register

6. Download the key (.p8 file)
   ⚠️ IMPORTANT: You can only download this ONCE!

7. Save these values securely:
   - Key ID: ABC1234567 (10 characters, shown after creation)
   - Team ID: DEF9876543 (from Membership page)
   - .p8 file contents
```

#### Step 3: Configure Backend

Update your backend `.env` or `application.yml`:

```yaml
# Apple Sign-In Configuration
apple:
  team-id: YOUR_TEAM_ID          # From Apple Developer Membership
  key-id: YOUR_KEY_ID            # From created key
  service-id: com.oddiya.auth    # Service ID from Step 2a
  key-path: /path/to/AuthKey_ABC1234567.p8  # Downloaded .p8 file
```

**Or as environment variables:**

```bash
APPLE_TEAM_ID=YOUR_TEAM_ID
APPLE_KEY_ID=YOUR_KEY_ID
APPLE_SERVICE_ID=com.oddiya.auth
APPLE_KEY_PATH=/path/to/AuthKey_ABC1234567.p8
```

#### Step 4: Test iOS Apple Sign-In

```bash
cd /Users/wjs/cursor/oddiya/mobile
npx react-native run-ios
```

**Test Flow:**
1. Launch app on iOS 13+ device/simulator
2. See "Apple로 계속하기" button
3. Tap button
4. Face ID/Touch ID prompt appears
5. Authenticate
6. First time: Apple asks permission to share email
7. App receives tokens
8. Backend verifies and returns JWT
9. Navigate to Plans screen

**Console Logs to Verify:**
```
[WelcomeScreen] Apple Sign-In available: true
[WelcomeScreen] Starting Apple Sign-In...
✅ Apple Sign-In successful: {user: "...", email: "..."}
[Auth] Apple login response: {userId: 123, ...}
✅ Auth loaded successfully
```

---

## 🤖 Android Apple Sign-In (OPTIONAL)

### ⚠️ Important Notes

1. **Apple does NOT provide native SDK for Android**
2. **Web-based flow only** - opens browser, poor UX
3. **Not required** - Apple only mandates "Sign in with Apple" if you offer other social logins **on iOS**
4. **Your current code already hides Apple button on Android** ✅

### Current Implementation (Recommended)

Your `appleSignInService.ts` already handles this:

```typescript
async isAvailable(): Promise<boolean> {
  if (Platform.OS !== 'ios') {
    return false;  // ✅ Returns false on Android
  }
  // ... iOS check
}
```

**Result:** Apple button **only shows on iOS 13+**, hidden on Android.

This is the **recommended approach** for most apps.

---

### Alternative: Implement Web-based Flow (Complex)

If you **really need** Apple Sign-In on Android:

#### Option A: Use react-native-app-auth (OAuth 2.0)

**1. Install Package:**
```bash
npm install react-native-app-auth
cd ios && pod install && cd ..
```

**2. Update appleSignInService.ts:**

```typescript
import { authorize } from 'react-native-app-auth';
import { Platform } from 'react-native';

async signIn(): Promise<AppleUser> {
  if (Platform.OS === 'ios') {
    // Existing iOS native flow
    // ...
  } else {
    // Android web-based flow
    const config = {
      issuer: 'https://appleid.apple.com',
      clientId: 'com.oddiya.auth', // Your Service ID
      redirectUrl: 'com.mobile://apple-signin-callback',
      scopes: ['email', 'name'],
    };

    const result = await authorize(config);

    // Parse result and return AppleUser format
    // ...
  }
}
```

**3. Configure Apple Service ID:**

In Apple Developer Portal, update Return URLs:
```
com.mobile://apple-signin-callback
```

**4. Update AndroidManifest.xml:**

```xml
<intent-filter>
  <action android:name="android.intent.action.VIEW" />
  <category android:name="android.intent.category.DEFAULT" />
  <category android:name="android.intent.category.BROWSABLE" />
  <data android:scheme="com.mobile" android:host="apple-signin-callback" />
</intent-filter>
```

**Downsides:**
- Opens browser (poor UX)
- Complex error handling
- Users confused why it's not native
- Extra maintenance burden

---

## 🎯 Recommended Strategy

### For Production

| Platform | OAuth Methods |
|----------|---------------|
| **iOS** | ✅ Apple Sign-In (native) + ✅ Google Sign-In |
| **Android** | ✅ Google Sign-In only |

**Why:**
1. **Apple's requirement** only applies to iOS (if you offer other social logins)
2. **Android users expect Google Sign-In** (98% market share)
3. **Better UX** - each platform gets native experience
4. **Less code** - no web-based Apple flow needed
5. **Fewer edge cases** - simpler error handling

### Your Current Implementation ✅

**Already follows best practices:**

```typescript
// WelcomeScreen.tsx - Shows Apple button only on iOS
const [isAppleAvailable, setIsAppleAvailable] = React.useState(false);

// Checks platform and iOS version
const available = await appleSignInService.isAvailable();
setIsAppleAvailable(available); // true on iOS 13+, false on Android

// Button only rendered if available
{isAppleAvailable && (
  <Button onPress={handleAppleSignIn}>
    Apple로 계속하기
  </Button>
)}
```

**Result:**
- iOS: Shows Google + Apple buttons
- Android: Shows Google button only
- No code changes needed!

---

## 🔐 Security Considerations

### Token Verification (Backend)

Your backend must verify Apple Identity Tokens:

**Java Implementation Example:**

```java
@Service
public class AppleTokenVerificationService {

  @Value("${apple.team-id}")
  private String teamId;

  @Value("${apple.key-id}")
  private String keyId;

  @Value("${apple.service-id}")
  private String serviceId;

  public AppleUserInfo verifyIdentityToken(String identityToken) {
    try {
      // 1. Fetch Apple's public keys
      JsonNode keys = fetchApplePublicKeys();

      // 2. Decode JWT header to get key ID
      String kid = getKeyIdFromToken(identityToken);

      // 3. Find matching public key
      PublicKey publicKey = getPublicKey(keys, kid);

      // 4. Verify JWT signature
      DecodedJWT jwt = JWT.require(Algorithm.RSA256((RSAPublicKey) publicKey, null))
        .withIssuer("https://appleid.apple.com")
        .withAudience(serviceId)
        .build()
        .verify(identityToken);

      // 5. Extract user info
      return new AppleUserInfo(
        jwt.getSubject(),        // Apple User ID
        jwt.getClaim("email").asString(),
        jwt.getClaim("email_verified").asBoolean()
      );

    } catch (Exception e) {
      throw new InvalidTokenException("Invalid Apple ID Token");
    }
  }

  private JsonNode fetchApplePublicKeys() throws IOException {
    // GET https://appleid.apple.com/auth/keys
    // Returns Apple's public keys for JWT verification
  }
}
```

### Key Points

1. **Always verify tokens server-side** - never trust client
2. **Check issuer** - must be `https://appleid.apple.com`
3. **Check audience** - must match your Service ID
4. **Verify signature** - using Apple's public keys
5. **Check expiration** - tokens expire after 10 minutes
6. **Handle "Hide My Email"** - Apple may provide relay email

---

## 📋 Testing Checklist

### iOS Testing

- [ ] Xcode: "Sign in with Apple" capability added
- [ ] Apple Developer: Service ID created and configured
- [ ] Apple Developer: Key (.p8) created and downloaded
- [ ] Backend: Apple configuration added
- [ ] Run: `npx react-native run-ios`
- [ ] App shows Apple button (iOS 13+ only)
- [ ] Tap button → Face ID/Touch ID prompt
- [ ] First time: Apple permission dialog
- [ ] Token sent to backend
- [ ] Backend verification succeeds
- [ ] JWT returned and stored
- [ ] Navigate to Plans screen

### Android Testing

- [ ] Run: `npx react-native run-android`
- [ ] App does NOT show Apple button
- [ ] Only Google button visible
- [ ] Google Sign-In works (after SHA-1 fix)

---

## 🆘 Troubleshooting

### iOS: "Apple Sign-In button not showing"

**Check:**
```typescript
// Console logs
[WelcomeScreen] Apple Sign-In available: false
```

**Solutions:**
1. Test on iOS 13+ device (not iOS 12 or earlier)
2. Verify capability added in Xcode
3. Check `appleSignInService.isAvailable()` returns true

### iOS: "The operation couldn't be completed"

**Cause:** Missing or incorrect Service ID configuration

**Fix:**
1. Verify Service ID in Apple Developer Portal
2. Check Return URLs match backend endpoints
3. Ensure Primary App ID is correctly selected

### iOS: Backend returns "Invalid token"

**Check:**
1. Backend has correct Team ID
2. Backend has correct Key ID
3. .p8 file loaded correctly
4. Service ID matches audience in token

### Android: Users asking for Apple Sign-In

**Response:**
"Apple Sign-In is available on iOS. Android users can sign in with Google."

**Alternative:**
Implement web-based flow (see "Option A" above)

---

## 📚 Resources

### Official Documentation

- **Apple:** https://developer.apple.com/sign-in-with-apple/
- **iOS SDK:** https://github.com/invertase/react-native-apple-authentication
- **Token Verification:** https://developer.apple.com/documentation/sign_in_with_apple/sign_in_with_apple_rest_api/verifying_a_user

### Your Implementation

- **iOS Service:** `/Users/wjs/cursor/oddiya/mobile/src/services/appleSignInService.ts`
- **UI Component:** `/Users/wjs/cursor/oddiya/mobile/src/screens/WelcomeScreen.tsx`
- **Redux Action:** `/Users/wjs/cursor/oddiya/mobile/src/store/slices/authSlice.ts`
- **Backend Endpoint:** `MobileAuthController.java` - `/api/v1/auth/apple/verify`

---

## 🎯 Quick Start Summary

### iOS (Required Setup)

1. **Xcode:** Add "Sign in with Apple" capability ✅
2. **Apple Developer:** Create Service ID and Key 🔑
3. **Backend:** Configure Apple credentials 🔧
4. **Test:** `npx react-native run-ios` 🧪

**Time:** ~15 minutes

### Android (Already Done)

1. **Current Implementation:** Apple button hidden on Android ✅
2. **User Experience:** Google Sign-In only ✅
3. **No action needed** 🎉

**Time:** 0 minutes (already done!)

---

## ✅ Recommended Next Steps

1. **First:** Fix Android Google OAuth (SHA-1 issue from previous message)
2. **Then:** Setup iOS Apple Sign-In (this guide)
3. **Finally:** Test both platforms end-to-end

**Priority Order:**
1. ⚠️ Android Google OAuth (fix package name → create OAuth Client ID)
2. 📱 iOS Apple Sign-In (add capability → configure Apple Developer)
3. 🍎 iOS Google OAuth (create iOS Client ID after Team ID)

---

**Last Updated:** 2025-11-09
**iOS Status:** Ready for Apple Developer setup
**Android Status:** ✅ Correctly shows Google only
