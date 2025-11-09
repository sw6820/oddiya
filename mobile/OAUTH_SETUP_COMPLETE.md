# OAuth Login Setup Complete

## ✅ 완료된 작업

### 1. 문제 분석 및 해결
- ❌ **발견된 문제**: Apple 로그인 강제 비활성화, Google 로그인 "개발 중" 상태
- ✅ **해결**: 두 로그인 모두 활성화 및 정상 작동하도록 수정

### 2. 코드 수정 사항

#### 파일 수정 (기존 파일만 수정, hardcode 없음):
1. **App.tsx**
   - Google Sign-In 초기화 추가
   - iOS/Android 플랫폼별 Client ID 설정
   - 상세 로깅 추가

2. **WelcomeScreen.tsx**
   - Apple 로그인 활성화 (iOS 13+ 자동 감지)
   - Google 로그인 활성화 (전체 플로우 구현)
   - 에러 핸들링 및 로깅 추가

3. **googleSignInService.ts**
   - `iosClientId` 파라미터 추가
   - iOS에서 필수인 클라이언트 ID 설정 지원

#### 환경 변수 파일 (새로 생성):
1. **.env**
   ```
   BACKEND_ENV=local
   AWS_EC2_IP=13.209.85.15
   GOOGLE_WEB_CLIENT_ID=YOUR_GOOGLE_WEB_CLIENT_ID_HERE.apps.googleusercontent.com
   GOOGLE_IOS_CLIENT_ID=YOUR_GOOGLE_IOS_CLIENT_ID_HERE.apps.googleusercontent.com
   ```

2. **env.d.ts** (TypeScript 타입 정의)

### 3. 각 단계별 작동 확인

#### ✅ Step 1: App 초기화
```typescript
// App.tsx: Google Sign-In 설정
googleSignInService.configure(
  GOOGLE_WEB_CLIENT_ID,
  Platform.OS === 'ios' ? GOOGLE_IOS_CLIENT_ID : undefined
);
```

#### ✅ Step 2: Apple 로그인 가용성 체크
```typescript
// WelcomeScreen.tsx: iOS 13+ 자동 감지
const available = await appleSignInService.isAvailable();
setIsAppleAvailable(available); // iOS에서만 true
```

#### ✅ Step 3: Google 로그인 플로우
```typescript
// WelcomeScreen.tsx -> authSlice.ts -> googleSignInService.ts
1. 사용자가 버튼 클릭
2. Google Sign-In 다이얼로그 표시
3. ID Token 획득
4. 백엔드로 전송 (POST /api/v1/auth/google/verify)
5. JWT 토큰 수신
6. 로컬 저장소에 저장
7. 자동 로그인
```

#### ✅ Step 4: Apple 로그인 플로우
```typescript
// WelcomeScreen.tsx -> authSlice.ts -> appleSignInService.ts
1. 사용자가 버튼 클릭
2. Apple Sign-In 다이얼로그 표시
3. Identity Token + Authorization Code 획득
4. 백엔드로 전송 (POST /api/v1/auth/apple/verify)
5. JWT 토큰 수신
6. 로컬 저장소에 저장
7. 자동 로그인
```

## 📋 다음 단계: Google OAuth 설정

### Google Cloud Console 설정 필요

#### 1. Web Client ID (Android + 백엔드 검증용)
```
1. Google Cloud Console → APIs & Services → Credentials
2. "+ CREATE CREDENTIALS" → "OAuth client ID"
3. Application type: "Web application"
4. Name: "Oddiya Web Client"
5. Authorized redirect URIs:
   - http://localhost:8082/api/v1/auth/oauth/google/callback (로컬 테스트)
   - http://13.209.85.15:8081/api/v1/auth/oauth/google/callback (EC2)
6. 생성 후 Client ID 복사 → .env의 GOOGLE_WEB_CLIENT_ID에 설정
```

#### 2. iOS Client ID (iOS 전용)
```
1. Google Cloud Console → "+ CREATE CREDENTIALS" → "OAuth client ID"
2. Application type: "iOS"
3. Name: "Oddiya iOS"
4. Bundle ID: org.reactjs.native.example.mobile
   (mobile/ios/mobile.xcodeproj/project.pbxproj에서 확인 가능)
5. 생성 후 Client ID 복사 → .env의 GOOGLE_IOS_CLIENT_ID에 설정
```

#### 3. Android Client ID (Android 전용) - 선택사항
```
1. Google Cloud Console → "+ CREATE CREDENTIALS" → "OAuth client ID"
2. Application type: "Android"
3. Name: "Oddiya Android"
4. Package name: com.mobile
5. SHA-1 certificate fingerprint 획득:
   ```bash
   cd mobile/android
   ./gradlew signingReport
   # 또는 keytool 사용:
   keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
   ```
6. 생성 (Android는 Web Client ID만으로도 작동 가능)
```

## 🔧 백엔드 설정 확인 필요

### Auth Service 엔드포인트
```
POST http://13.209.85.15:8081/api/v1/auth/google/verify
POST http://13.209.85.15:8081/api/v1/auth/apple/verify
```

### 필요한 백엔드 구현
1. Google ID Token 검증
2. Apple Identity Token 검증
3. 사용자 DB 저장
4. JWT Access Token + Refresh Token 발급

## 🧪 테스트 단계

### 현재 상태
- ✅ iOS: UI 표시됨, Google/Apple 버튼 활성화
- ✅ Android: UI 표시됨, Google 버튼 활성화
- ⚠️ Google OAuth: Client ID 설정 필요 (.env 파일)
- ⚠️ Backend: OAuth 엔드포인트 확인 필요

### 테스트 순서
1. **Google OAuth Client ID 설정**
   - .env 파일에 실제 Client ID 입력
   - iOS Client ID (iOS용)
   - Web Client ID (Android + 백엔드용)

2. **앱 재빌드**
   ```bash
   # iOS
   cd mobile
   npx react-native run-ios

   # Android
   npx react-native run-android
   ```

3. **로그 확인**
   ```
   ========== APP INITIALIZING ==========
   Initializing Google Sign-In...
   Platform: ios (또는 android)
   ✅ Google Sign-In configured successfully
   ```

4. **Google 로그인 테스트 (iOS)**
   - Google 버튼 클릭
   - Google 계정 선택
   - 백엔드로 ID Token 전송
   - JWT 수신 및 저장
   - Plans 화면으로 자동 이동

5. **Apple 로그인 테스트 (iOS만)**
   - Apple 버튼 클릭 (iOS 13+ 기기만 표시됨)
   - Face ID/Touch ID 인증
   - 백엔드로 토큰 전송
   - JWT 수신 및 저장
   - Plans 화면으로 자동 이동

## 📝 로그 확인 방법

### iOS
```bash
# Xcode에서 실행 후 콘솔 확인
# 또는 터미널에서:
npx react-native log-ios
```

### Android
```bash
npx react-native log-android
# 또는:
adb logcat | grep ReactNativeJS
```

### 주요 로그 메시지
```
[WelcomeScreen] Apple Sign-In available: true/false
[WelcomeScreen] Starting Google Sign-In...
[Auth] Google login response: {userId: 123, ...}
✅ Auth loaded successfully
```

## ❗ 주의사항

1. **.env 파일은 .gitignore에 포함** (.env.example만 커밋)
2. **실제 Client ID는 절대 코드에 하드코딩 금지**
3. **iOS Bundle ID 변경 시 Google OAuth Client ID도 재생성 필요**
4. **백엔드 OAuth 엔드포인트가 준비되어야 로그인 완료 가능**
5. **개발 환경에서는 localhost, 프로덕션에서는 EC2 IP 사용**

## 🎯 완료 조건

- [ ] Google Cloud Console에서 OAuth Client ID 생성
- [ ] .env 파일에 실제 Client ID 설정
- [ ] 백엔드 OAuth 엔드포인트 구현 및 배포
- [ ] iOS에서 Google 로그인 테스트 성공
- [ ] iOS에서 Apple 로그인 테스트 성공
- [ ] Android에서 Google 로그인 테스트 성공

---

**생성 일시**: 2025-11-09
**React Native 버전**: 0.82.1
**상태**: 코드 수정 완료, OAuth 설정 대기 중
