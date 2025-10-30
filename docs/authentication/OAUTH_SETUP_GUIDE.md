# OAuth Setup Guide - Google & Apple Sign-in

Android 및 iPhone 앱을 위한 Google과 Apple OAuth 설정 가이드

## 📱 Overview

Oddiya는 소셜 로그인을 지원합니다:
- ✅ Google Sign-in (Android, iOS)
- ✅ Apple Sign-in (iOS 필수, Android 선택)

---

## 🔐 Google OAuth 설정

### Step 1: Google Cloud Console 설정

**1. 프로젝트 생성:**
```
https://console.cloud.google.com

→ 새 프로젝트 만들기
→ 이름: "Oddiya"
```

**2. OAuth 동의 화면 구성:**
```
APIs & Services → OAuth consent screen

→ User Type: External
→ App name: Oddiya
→ User support email: your-email@gmail.com
→ Developer contact: your-email@gmail.com
→ Save
```

**3. OAuth 클라이언트 ID 생성:**

**For Android:**
```
APIs & Services → Credentials → Create Credentials → OAuth client ID

→ Application type: Android
→ Name: Oddiya Android
→ Package name: com.oddiya.mobile
→ SHA-1 certificate fingerprint: 
  (keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey)
→ Create

→ Copy Client ID
```

**For iOS:**
```
→ Application type: iOS
→ Name: Oddiya iOS
→ Bundle ID: com.oddiya.mobile
→ Create

→ Copy Client ID
```

**For Web (OAuth callback):**
```
→ Application type: Web application
→ Name: Oddiya Web
→ Authorized redirect URIs:
  - http://localhost:8080/oauth2/callback/google
  - https://api.oddiya.com/oauth2/callback/google (production)
→ Create

→ Copy:
  - Client ID
  - Client Secret
```

### Step 2: .env 파일 설정

```bash
# .env.oauth
GOOGLE_CLIENT_ID=your-web-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=GOCSPX-your-client-secret
GOOGLE_ANDROID_CLIENT_ID=your-android-client-id.apps.googleusercontent.com
GOOGLE_IOS_CLIENT_ID=your-ios-client-id.apps.googleusercontent.com
```

### Step 3: Auth Service 업데이트

```yaml
# docker-compose.local.yml
auth-service:
  environment:
    GOOGLE_CLIENT_ID: ${GOOGLE_CLIENT_ID}
    GOOGLE_CLIENT_SECRET: ${GOOGLE_CLIENT_SECRET}
```

---

## 🍎 Apple Sign-in 설정

### Step 1: Apple Developer 계정 설정

**1. App ID 생성:**
```
https://developer.apple.com/account

→ Certificates, Identifiers & Profiles
→ Identifiers → +
→ App IDs
→ Description: Oddiya
→ Bundle ID: com.oddiya.mobile
→ Capabilities: Sign in with Apple ✅
→ Continue → Register
```

**2. Services ID 생성:**
```
→ Identifiers → +
→ Services IDs
→ Description: Oddiya Web Service
→ Identifier: com.oddiya.service
→ Sign in with Apple ✅
→ Configure:
  - Primary App ID: com.oddiya.mobile
  - Domains and Subdomains: api.oddiya.com
  - Return URLs: https://api.oddiya.com/oauth2/callback/apple
→ Continue → Register
```

**3. Key 생성:**
```
→ Keys → +
→ Key Name: Oddiya Sign in with Apple Key
→ Sign in with Apple ✅
→ Configure:
  - Primary App ID: com.oddiya.mobile
→ Continue → Register
→ Download key (.p8 file)
→ Copy Key ID
```

### Step 2: .env 파일 설정

```bash
# .env.oauth
APPLE_TEAM_ID=YOUR_TEAM_ID
APPLE_CLIENT_ID=com.oddiya.service
APPLE_KEY_ID=ABC123DEFG
APPLE_PRIVATE_KEY_PATH=/app/keys/AuthKey_ABC123DEFG.p8
```

### Step 3: Private Key 저장

```bash
# services/auth-service/keys/
mkdir -p services/auth-service/keys
cp ~/Downloads/AuthKey_*.p8 services/auth-service/keys/

# .gitignore에 추가
echo "services/auth-service/keys/*.p8" >> .gitignore
```

---

## 🔄 OAuth Flow

### Google Sign-in Flow:

```
Mobile App
  ↓ 1. User taps "Google로 계속하기"
  ↓ 2. Google Sign-in SDK
Google OAuth
  ↓ 3. User approves
  ↓ 4. Returns authorization code
Mobile App
  ↓ 5. POST /oauth2/callback/google
  ↓    Body: {code, platform: "android"}
Auth Service
  ↓ 6. Exchange code for tokens
  ↓ 7. Get user info from Google
  ↓ 8. Check if user exists
  ↓ 9. If not, call User Service (POST /internal/users)
  ↓ 10. Generate JWT (access + refresh)
  ↓ 11. Return tokens
Mobile App
  ↓ 12. Store tokens
  ↓ 13. Navigate to main screen
```

### Apple Sign-in Flow:

```
Mobile App (iOS)
  ↓ 1. User taps "Apple로 계속하기"
  ↓ 2. Apple Sign-in (native)
Apple
  ↓ 3. User approves with Face ID
  ↓ 4. Returns identity token
Mobile App
  ↓ 5. POST /oauth2/callback/apple
  ↓    Body: {identityToken, user: {...}}
Auth Service
  ↓ 6. Verify identity token with Apple
  ↓ 7. Extract user info
  ↓ 8. Check if user exists
  ↓ 9. If not, create user
  ↓ 10. Generate JWT
  ↓ 11. Return tokens
Mobile App
  ↓ 12. Store tokens
```

---

## 📱 Mobile App 통합

### React Native - Google Sign-in

**1. 패키지 설치:**
```bash
cd mobile
npm install @react-native-google-signin/google-signin
```

**2. iOS 설정 (ios/Podfile):**
```ruby
pod 'GoogleSignIn'
```

**3. Android 설정 (android/app/build.gradle):**
```gradle
dependencies {
    implementation 'com.google.android.gms:play-services-auth:20.7.0'
}
```

**4. 코드 구현:**
```javascript
// mobile/src/services/AuthService.js
import { GoogleSignin } from '@react-native-google-signin/google-signin';

// Configure
GoogleSignin.configure({
  webClientId: 'your-web-client-id.apps.googleusercontent.com',
  iosClientId: 'your-ios-client-id.apps.googleusercontent.com',
  offlineAccess: true,
});

// Sign in
export const signInWithGoogle = async () => {
  try {
    await GoogleSignin.hasPlayServices();
    const userInfo = await GoogleSignin.signIn();
    
    // Send to backend
    const response = await fetch('http://localhost:8080/oauth2/callback/google', {
      method: 'POST',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify({
        code: userInfo.serverAuthCode,
        platform: Platform.OS,
      }),
    });
    
    const {accessToken, refreshToken} = await response.json();
    
    // Store tokens
    await AsyncStorage.setItem('accessToken', accessToken);
    await AsyncStorage.setItem('refreshToken', refreshToken);
    
    return true;
  } catch (error) {
    console.error('Google Sign-in error:', error);
    return false;
  }
};
```

### React Native - Apple Sign-in

**1. 패키지 설치:**
```bash
npm install @invertase/react-native-apple-authentication
```

**2. iOS 설정:**
```
Xcode → Signing & Capabilities → + Capability
→ Sign in with Apple
```

**3. 코드 구현:**
```javascript
import appleAuth from '@invertase/react-native-apple-authentication';

export const signInWithApple = async () => {
  try {
    const appleAuthRequestResponse = await appleAuth.performRequest({
      requestedOperation: appleAuth.Operation.LOGIN,
      requestedScopes: [appleAuth.Scope.EMAIL, appleAuth.Scope.FULL_NAME],
    });
    
    const {identityToken, email, fullName} = appleAuthRequestResponse;
    
    // Send to backend
    const response = await fetch('http://localhost:8080/oauth2/callback/apple', {
      method: 'POST',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify({
        identityToken,
        user: {
          email,
          firstName: fullName?.givenName,
          lastName: fullName?.familyName,
        },
      }),
    });
    
    const {accessToken, refreshToken} = await response.json();
    
    // Store tokens
    await AsyncStorage.setItem('accessToken', accessToken);
    await AsyncStorage.setItem('refreshToken', refreshToken);
    
    return true;
  } catch (error) {
    console.error('Apple Sign-in error:', error);
    return false;
  }
};
```

---

## 🧪 테스트

### Local 테스트 (Mock):

```bash
# Mock Google login
curl -X POST http://localhost:8081/oauth2/test/google \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@gmail.com",
    "name": "Test User"
  }'

# Returns:
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "uuid...",
  "userId": 1
}
```

### Production 테스트:

**Android:**
```
1. Build APK with release key
2. Get SHA-1 fingerprint
3. Update Google Console
4. Test login on device
```

**iOS:**
```
1. Configure Bundle ID
2. Enable Sign in with Apple capability
3. Test on device or simulator
```

---

## 🔒 보안

### Token 저장:

```javascript
// Secure storage
import * as Keychain from 'react-native-keychain';

// Store
await Keychain.setGenericPassword('accessToken', token);

// Retrieve
const credentials = await Keychain.getGenericPassword();
const token = credentials.password;
```

### Token Refresh:

```javascript
const refreshAccessToken = async () => {
  const refreshToken = await AsyncStorage.getItem('refreshToken');
  
  const response = await fetch('http://localhost:8080/oauth2/refresh', {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({refreshToken}),
  });
  
  const {accessToken} = await response.json();
  await AsyncStorage.setItem('accessToken', accessToken);
  
  return accessToken;
};
```

---

## 📋 체크리스트

### Google OAuth:
- [ ] Google Cloud 프로젝트 생성
- [ ] OAuth 동의 화면 구성
- [ ] Android OAuth Client ID
- [ ] iOS OAuth Client ID
- [ ] Web OAuth Client ID & Secret
- [ ] .env.oauth 파일 설정
- [ ] React Native 패키지 설치
- [ ] 코드 구현
- [ ] 테스트

### Apple Sign-in:
- [ ] Apple Developer 계정
- [ ] App ID 생성 (Sign in with Apple 활성화)
- [ ] Services ID 생성
- [ ] Key 생성 및 다운로드
- [ ] .env.oauth 파일 설정
- [ ] React Native 패키지 설치
- [ ] iOS Capability 추가
- [ ] 코드 구현
- [ ] 테스트

---

## 🎯 Quick Start

### 1. Get Credentials:

```bash
# Google
Google Cloud Console → OAuth Client IDs

# Apple
Apple Developer → Keys → Download .p8
```

### 2. Configure:

```bash
cd /Users/wjs/cursor/oddiya
cp .env.oauth.example .env.oauth
# Edit .env.oauth with your credentials
```

### 3. Start Services:

```bash
docker-compose -f docker-compose.local.yml --env-file .env.oauth up -d
```

### 4. Test:

```bash
# Mobile app
npm run android  # or ios
# Tap "Google로 계속하기"
```

---

## 📚 References

- **Google OAuth:** https://developers.google.com/identity/sign-in/android
- **Apple Sign-in:** https://developer.apple.com/sign-in-with-apple/
- **React Native Google:** https://github.com/react-native-google-signin/google-signin
- **React Native Apple:** https://github.com/invertase/react-native-apple-authentication

---

**모든 설정이 완료되면 Android와 iPhone에서 소셜 로그인이 작동합니다!** 🚀

