# iOS Google OAuth Setup Guide

## 📱 Current iOS Project Information

### Bundle Identifier
```
org.reactjs.native.example.mobile
```
**위치**: `mobile/ios/mobile.xcodeproj/project.pbxproj`

### Team ID
**현재 상태**: ❌ 설정되지 않음

**설정 방법**:
1. Xcode에서 프로젝트 열기:
   ```bash
   open /Users/wjs/cursor/oddiya/mobile/ios/mobile.xcworkspace
   ```
2. 프로젝트 네비게이터에서 "mobile" 프로젝트 선택
3. "Signing & Capabilities" 탭 선택
4. "Team" 드롭다운에서 Apple Developer 계정 선택
5. Team ID는 자동으로 설정됨 (예: `ABCD123456`)

### App Store ID
**현재 상태**: ❌ 필요 없음 (개발 단계)

**언제 필요한가?**:
- App Store Connect에 앱을 등록한 후
- App Store에 앱을 제출할 때
- 현재 개발/테스트 단계에서는 필요 없음

---

## 🔧 Google Cloud Console 설정 단계

### Step 1: Google Cloud Console 접속
1. https://console.cloud.google.com/ 접속
2. 프로젝트 선택 또는 새로 만들기 (예: "Oddiya")

### Step 2: OAuth Consent Screen 설정
```
1. APIs & Services → OAuth consent screen
2. User Type: External 선택
3. App information:
   - App name: Oddiya
   - User support email: your-email@gmail.com
   - Developer contact information: your-email@gmail.com
4. Scopes: email, profile (기본값)
5. Test users: 테스트할 Gmail 계정 추가
6. "SAVE AND CONTINUE"
```

### Step 3: iOS OAuth Client ID 생성

#### 3-1. Credentials 페이지로 이동
```
APIs & Services → Credentials → + CREATE CREDENTIALS → OAuth client ID
```

#### 3-2. Application Type 선택
```
Application type: iOS
```

#### 3-3. 필수 정보 입력

**Name** (앱 이름):
```
Oddiya iOS
```

**Bundle ID** (현재 프로젝트):
```
org.reactjs.native.example.mobile
```

**App Store ID** (선택사항):
```
비워두기 (아직 App Store에 등록하지 않음)
```

**Team ID** (Apple Developer):
```
[Xcode에서 확인한 Team ID 입력]
예: ABCD123456
```

#### 3-4. 생성 완료
```
"CREATE" 클릭 → iOS Client ID가 생성됨
형식: 123456789-abcdefg.apps.googleusercontent.com
```

### Step 4: Web Client ID도 생성 (백엔드 검증용)

#### 4-1. 다시 Credentials 페이지
```
+ CREATE CREDENTIALS → OAuth client ID
```

#### 4-2. Application Type 선택
```
Application type: Web application
```

#### 4-3. 필수 정보 입력

**Name**:
```
Oddiya Web Client
```

**Authorized redirect URIs**:
```
http://localhost:8082/api/v1/auth/oauth/google/callback
http://13.209.85.15:8081/api/v1/auth/oauth/google/callback
```

#### 4-4. 생성 완료
```
"CREATE" 클릭 → Web Client ID가 생성됨
```

---

## 📝 .env 파일에 추가

생성된 Client ID들을 `.env` 파일에 복사:

```bash
# Oddiya Mobile App - Environment Variables

# Backend Configuration
BACKEND_ENV=local
AWS_EC2_IP=13.209.85.15

# Google OAuth Configuration
# Web Client ID: OAuth 2.0 Client ID for "Web application" type
GOOGLE_WEB_CLIENT_ID=123456789-abc.apps.googleusercontent.com

# iOS Client ID: OAuth 2.0 Client ID for "iOS" type
GOOGLE_IOS_CLIENT_ID=123456789-xyz.apps.googleusercontent.com
```

**⚠️ 주의**: 실제 Client ID로 교체하세요!

---

## 🧪 테스트 단계

### 1. 앱 재빌드
```bash
cd /Users/wjs/cursor/oddiya/mobile
npx react-native run-ios
```

### 2. 앱 실행 확인
```
========== APP INITIALIZING ==========
Initializing Google Sign-In...
Platform: ios
✅ Google Sign-In configured successfully
```

### 3. Google 로그인 테스트
1. "Google로 계속하기" 버튼 클릭
2. Google 계정 선택 화면 표시
3. 계정 선택
4. 권한 동의 화면 표시 (처음만)
5. 백엔드로 ID Token 전송
6. 로그인 성공!

### 4. 에러 발생 시 확인
```bash
# iOS 로그 확인
npx react-native log-ios

# 주요 에러 메시지:
- "failed to determine clientID" → .env 파일 확인
- "DEVELOPER_ERROR" → Bundle ID 불일치
- "API not enabled" → Google Cloud에서 API 활성화 필요
```

---

## 🔍 Bundle ID 변경 필요 시

현재 Bundle ID (`org.reactjs.native.example.mobile`)를 변경하려면:

### 1. Xcode에서 변경
```
1. Xcode 열기: open mobile/ios/mobile.xcworkspace
2. 프로젝트 선택 → General 탭
3. Bundle Identifier 변경 (예: com.oddiya.mobile)
```

### 2. Google Cloud Console에서 재생성
```
새로운 Bundle ID로 iOS OAuth Client ID 재생성 필요
```

### 3. .env 파일 업데이트
```
새로운 GOOGLE_IOS_CLIENT_ID로 교체
```

---

## 📚 추가 정보

### Team ID 찾는 다른 방법

**방법 1: Xcode**
```
Xcode → Preferences (⌘,) → Accounts → Apple ID 선택 → Team ID 확인
```

**방법 2: Apple Developer Portal**
```
https://developer.apple.com/account/
→ Membership → Team ID
```

**방법 3: 터미널**
```bash
security find-identity -v -p codesigning
```

### App Store ID 찾는 방법 (앱 제출 후)

```
1. App Store Connect 접속: https://appstoreconnect.apple.com/
2. "My Apps" → 앱 선택
3. App Information → General Information → Apple ID
4. 형식: 1234567890 (10자리 숫자)
```

### Google OAuth API 활성화

```
Google Cloud Console → APIs & Services → Library
→ "Google+ API" 검색 → ENABLE
```

---

## ✅ 체크리스트

### Google Cloud Console
- [ ] OAuth Consent Screen 설정 완료
- [ ] iOS OAuth Client ID 생성 완료
- [ ] Web OAuth Client ID 생성 완료
- [ ] Test users 추가 완료

### iOS 프로젝트
- [ ] Xcode에서 Team ID 설정 완료
- [ ] Bundle Identifier 확인 완료
- [ ] .env 파일에 Client ID 추가 완료

### 테스트
- [ ] 앱 재빌드 성공
- [ ] Google Sign-In 초기화 로그 확인
- [ ] Google 로그인 버튼 클릭 가능
- [ ] Google 계정 선택 화면 표시
- [ ] 백엔드 연동 확인 (JWT 수신)

---

## 🆘 자주 발생하는 에러

### Error 1: "failed to determine clientID"
**원인**: .env 파일에 Client ID가 없거나 잘못됨
**해결**: .env 파일 확인 및 앱 재빌드

### Error 2: "DEVELOPER_ERROR"
**원인**: Bundle ID가 Google Cloud Console과 일치하지 않음
**해결**:
1. Xcode에서 Bundle ID 확인
2. Google Cloud Console에서 iOS Client ID 재생성

### Error 3: "API not enabled"
**원인**: Google+ API가 활성화되지 않음
**해결**: Google Cloud Console → APIs & Services → Library → Google+ API → ENABLE

### Error 4: "invalid_client"
**원인**: Client ID가 잘못되었거나 만료됨
**해결**: Google Cloud Console에서 새로운 Client ID 생성

---

**작성 일시**: 2025-11-09
**프로젝트**: Oddiya Mobile (React Native 0.82.1)
**상태**: 설정 대기 중
