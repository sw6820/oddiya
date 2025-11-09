# Personal Team (무료 계정) 개발 가이드

**상태:** Personal Team (Apple ID 무료 계정)
**Team ID:** 88H7CMABS2
**제약:** Sign in with Apple은 디바이스 필요

---

## ✅ Personal Team으로 가능한 것

### 1. Google OAuth (iOS + Android) - 완전 작동!

**iOS Google Sign-In:**
```
✅ Team ID 있음 → Google OAuth Client ID 생성 가능
✅ Simulator에서 테스트 가능
✅ 디바이스 불필요
✅ 제약 없음
```

**Android Google Sign-In:**
```
✅ SHA-1 있음 → Android OAuth Client ID 생성 가능
✅ Emulator에서 테스트 가능
✅ 제약 없음
```

**결론:** Google 로그인만으로 완전한 OAuth 시스템 구축 가능! 🎉

---

## ⚠️ Personal Team의 제약

### Apple Sign-In 제약

**문제:**
```
"Your team has no devices from which to generate a provisioning profile"
```

**원인:**
- Personal Team은 Provisioning Profile 자동 생성 불가
- 실제 디바이스 연결 필수
- Simulator만으로는 "Sign in with Apple" capability 추가 불가

**해결 옵션:**

#### Option A: iPhone/iPad 연결 (가장 쉬움)

```
1. iPhone/iPad를 USB로 Mac에 연결
2. 디바이스 잠금 해제
3. "이 컴퓨터 신뢰" 탭
4. Xcode로 돌아가기
5. 자동으로 디바이스 등록됨
6. Provisioning Profile 자동 생성
7. "Sign in with Apple" capability 추가 가능
```

#### Option B: Apple Developer Program 가입 ($99/년)

**장점:**
```
✅ 디바이스 없이도 Provisioning Profile 생성
✅ "Sign in with Apple" 자동 설정
✅ App Store 배포 가능
✅ TestFlight 베타 테스트 가능
✅ 1년 유효 (7일 제약 없음)
```

**단점:**
```
❌ $99/년 비용
❌ 출시 준비 안 됐으면 불필요
```

#### Option C: Apple Sign-In 없이 개발 (추천!)

**현재 상황:**
```
✅ Google Sign-In으로 충분히 개발 가능
✅ iOS: Google 로그인만
✅ Android: Google 로그인만
✅ 코드는 이미 완벽하게 구현됨
✅ 나중에 Apple Sign-In 추가 가능
```

---

## 🎯 추천 개발 전략

### Phase 1: Google OAuth 완성 (지금!)

**iOS:**
```bash
1. Google Cloud Console에서 iOS OAuth Client ID 생성:
   - Application type: iOS
   - Bundle ID: org.reactjs.native.example.mobile
   - Team ID: 88H7CMABS2

2. .env 파일 업데이트:
   GOOGLE_IOS_CLIENT_ID=xxxxx.apps.googleusercontent.com

3. 테스트:
   npx react-native run-ios
```

**Android:**
```bash
1. Google Cloud Console에서 Android OAuth Client ID 수정:
   - Package name: com.mobile
   - SHA-1: 5E:8F:16:06:2E:A3:CD:2C:4A:0D:54:78:76:BA:A6:F3:8C:AB:F6:25

2. 테스트:
   npx react-native run-android
```

**예상 시간:** 10분
**결과:** 완전히 작동하는 OAuth 로그인 시스템 ✅

---

### Phase 2: Apple Sign-In (나중에)

**언제 추가할지:**

```
Option 1: iPhone/iPad 구입 후
  - 디바이스 연결
  - Personal Team으로 계속 사용
  - 무료!

Option 2: 출시 준비 시
  - Apple Developer Program 가입 ($99)
  - App Store 배포 준비
  - Apple Sign-In 추가

Option 3: 당분간 안 함
  - Google Sign-In만 사용
  - 충분히 기능적
  - 문제 없음
```

---

## 📋 현재 상태 정리

### ✅ 완성 가능 (Personal Team)

| 기능 | iOS | Android | 상태 |
|------|-----|---------|------|
| **Google Sign-In** | ✅ | ✅ | Team ID 있음 |
| **코드 구현** | ✅ | ✅ | 완료 |
| **테스트 환경** | Simulator | Emulator | 사용 가능 |

### ⏳ 대기 중 (디바이스 또는 유료 계정 필요)

| 기능 | 필요 사항 | 우선순위 |
|------|----------|---------|
| **Apple Sign-In (iOS)** | iPhone/iPad 또는 $99 | 낮음 |
| **App Store 배포** | $99 | 출시 시 |
| **TestFlight** | $99 | 베타 테스트 시 |

---

## 💡 Personal Team 활용 팁

### 1. 개발 단계에서는 충분함

```
✅ Google OAuth로 전체 시스템 개발
✅ Simulator/Emulator에서 테스트
✅ 백엔드 연동 테스트
✅ UI/UX 개발
✅ 기능 완성
```

### 2. 7일 제약 우회 방법

**문제:** Personal Team 앱은 7일 후 만료

**해결:**
```bash
# 7일 후 재설치
cd /Users/wjs/cursor/oddiya/mobile
npx react-native run-ios --device
```

**또는:**
```
Xcode에서 "Build and Run" (⌘R)
자동으로 재서명 및 설치
```

### 3. 여러 디바이스 사용

**가능:**
- iPhone 1개: 가능
- iPhone + iPad: 가능
- 친구/가족 디바이스: 가능 (최대 100개)

**제약:**
- 디바이스마다 7일 후 재설치 필요

---

## 🎯 바로 다음 할 일 (Personal Team 상태)

### 1. Google OAuth 완성 (5-10분)

**iOS:**
```
1. Google Cloud Console 열기:
   https://console.cloud.google.com/apis/credentials

2. iOS OAuth Client ID 생성:
   - Type: iOS
   - Name: Oddiya iOS
   - Bundle ID: org.reactjs.native.example.mobile
   - Team ID: 88H7CMABS2 (클립보드에 복사됨)

3. .env 업데이트:
   GOOGLE_IOS_CLIENT_ID=[생성된 Client ID]

4. 테스트:
   npx react-native run-ios
```

**Android:**
```
1. Google Cloud Console에서 기존 Android Client ID 수정:
   - Package name: com.oddiya → com.mobile

2. 테스트:
   npx react-native run-android
```

### 2. Apple Sign-In 결정

**Option A: 디바이스 있음**
```
→ USB 연결 후 진행
→ 무료로 Apple Sign-In 추가 가능
```

**Option B: 디바이스 없음**
```
→ 일단 Google만 사용
→ 나중에 추가
→ 문제 없음!
```

**Option C: 프로덕션 준비**
```
→ Apple Developer Program 가입 ($99)
→ 모든 기능 사용 가능
```

---

## ❓ FAQ - Personal Team

### Q1: Personal Team으로 배포 가능한가요?

**A:** 아니요. App Store 배포는 Apple Developer Program ($99/년) 필요.

### Q2: Google Sign-In은 Personal Team으로 충분한가요?

**A:** 네! **완전히 작동합니다.** 제약 없음.

### Q3: 언제 유료 계정이 필요한가요?

**A:**
- App Store 배포 준비 시
- TestFlight 베타 테스트 필요 시
- 7일 재설치가 귀찮을 때
- Apple Sign-In을 디바이스 없이 테스트하고 싶을 때

### Q4: Personal Team으로 몇 대까지 테스트 가능한가요?

**A:** 최대 100대 디바이스 (iPhone, iPad 합산)

### Q5: Simulator에서 Apple Sign-In 테스트 불가능한가요?

**A:** Personal Team은 불가능. 유료 계정이면 가능.

---

## ✅ 결론

**Personal Team (무료)으로도:**
- ✅ Google OAuth 완벽 작동
- ✅ iOS/Android 개발 가능
- ✅ Simulator/Emulator 테스트 가능
- ✅ 백엔드 연동 가능
- ✅ 전체 시스템 완성 가능

**제약:**
- ⚠️ Apple Sign-In은 디바이스 필요
- ⚠️ App Store 배포 불가
- ⚠️ 7일마다 재설치

**권장:**
1. 지금: Google OAuth 완성
2. 나중: 디바이스 구입 또는 유료 계정 가입
3. 출시 시: Apple Developer Program 가입

**Google Sign-In만으로도 완전한 앱 개발이 가능합니다!** 🚀

---

**Last Updated:** 2025-11-09
**Team Type:** Personal Team (Free)
**Next Step:** Google OAuth 완성
