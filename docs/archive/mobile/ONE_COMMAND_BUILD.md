# 단 하나의 명령어로 Android & iOS 빌드

## 🎯 목표

**React Native CLI 방식:**
```bash
# Android 빌드 (로컬, 20분)
cd android && ./gradlew assembleRelease

# iOS 빌드 (Mac에서만, 30분)  
cd ios && xcodebuild archive
```
❌ 복잡하고 느림

**Expo 방식:**
```bash
# Android + iOS 동시 빌드 (클라우드, 15분)
eas build --platform all
```
✅ 간단하고 빠름!

---

## 🚀 빠른 시작 (10분)

### 1. Expo 마이그레이션

```bash
cd /Users/wjs/cursor/oddiya/mobile

# 자동 마이그레이션 실행
./scripts/migrate-to-expo.sh
```

### 2. Expo 로그인

```bash
# 계정 생성 (무료)
eas login

# 이메일/비밀번호 입력
```

### 3. EAS Build 설정

```bash
# 프로젝트 초기화
eas build:configure

# eas.json 자동 생성됨
```

### 4. 동시 빌드 실행

```bash
# 🎉 Android + iOS 한 번에!
eas build --platform all

# 또는 자동화 스크립트 사용
./scripts/build-expo.sh
# → 옵션 3 선택 (둘 다)
```

### 5. 빌드 결과 받기

**10-15분 후:**
- 📧 이메일로 다운로드 링크 수신
- 🌐 웹에서 확인: https://expo.dev
- 💾 CLI로 다운로드:
  ```bash
  eas build:download --platform android
  eas build:download --platform ios
  ```

---

## 📱 빌드 결과

### Android
```
✅ app-release.apk (또는 .aab)
📦 크기: ~30MB
🔗 다운로드: https://expo.dev/artifacts/...
```

**설치 방법:**
```bash
# 직접 배포
adb install app-release.apk

# Play Store
eas submit --platform android
```

### iOS
```
✅ app-release.ipa
📦 크기: ~35MB  
🔗 다운로드: https://expo.dev/artifacts/...
```

**배포 방법:**
```bash
# TestFlight (테스트용)
eas submit --platform ios --latest

# App Store (프로덕션)
eas submit --platform ios
```

---

## 💰 비용

| 항목 | 무료 티어 | 유료 ($29/월) |
|------|-----------|---------------|
| **빌드 횟수** | 30회/월 | 무제한 |
| **Android 빌드** | ✅ | ✅ |
| **iOS 빌드** | ✅ | ✅ |
| **동시 빌드** | ✅ | ✅ 우선순위 |
| **빌드 시간** | 10-15분 | 5-10분 |

**스토어 배포 비용:**
- Google Play: $25 (일회성)
- Apple App Store: $99/년 (iOS 배포시)

**총 비용:**
```
월 $0 (무료 티어 30빌드)
또는
월 $29 (무제한 빌드)

+ Apple Developer $99/년 (iOS용)
+ Google Play $25 (일회성)
```

---

## 🔄 전체 워크플로우

### 개발 → 배포 전체 과정

```bash
# 1. 코드 변경
cd /Users/wjs/cursor/oddiya/mobile
# ... 코드 수정 ...

# 2. 테스트
npm test

# 3. 빌드 (Android + iOS 동시)
npm run build:all
# 또는
./scripts/build-expo.sh

# 4. 빌드 완료 대기 (10-15분)
# 이메일로 알림 받음

# 5. 테스트
# Android: adb install app-release.apk
# iOS: TestFlight 설치

# 6. 스토어 배포
npm run submit:android  # Play Store
npm run submit:ios      # App Store

# 끝! 🎉
```

---

## 📊 비교표

### 빌드 방식 비교

| 항목 | React Native CLI | Expo EAS |
|------|------------------|----------|
| **Android 빌드** | Android Studio 필요 | ✅ 클라우드 |
| **iOS 빌드** | Mac + Xcode 필수 | ✅ 클라우드 (Mac 불필요) |
| **동시 빌드** | ❌ 불가 | ✅ 가능 |
| **설정 시간** | 1-2시간 | 5분 |
| **빌드 시간** | 20-30분 | 10-15분 |
| **명령어** | 플랫폼별 복잡 | 단일 명령어 |
| **비용** | 무료 | $0-29/월 |

### 환경 설정 비교

**React Native CLI:**
```bash
# Android
✗ Android Studio 설치 (3GB)
✗ JDK 17 설치
✗ Android SDK 설치
✗ 환경 변수 설정
✗ build.gradle 설정
✗ 서명 키 생성

# iOS
✗ Mac 필수
✗ Xcode 설치 (12GB)
✗ CocoaPods 설치
✗ 인증서 설정
✗ 프로비저닝 프로파일

총 소요 시간: 2-3시간
```

**Expo EAS:**
```bash
# 전부
✓ npm install expo
✓ eas login
✓ eas build:configure

총 소요 시간: 5분
```

---

## 🎨 실제 사용 예시

### 시나리오 1: 첫 배포

```bash
# 1. Expo 마이그레이션 (최초 1회)
./scripts/migrate-to-expo.sh

# 2. 로그인
eas login

# 3. 빌드
eas build --platform all

# 4. 다운로드 (15분 후)
eas build:download --platform android
eas build:download --platform ios

# 5. 테스트 및 배포
adb install app-release.apk
eas submit --platform all
```

### 시나리오 2: 업데이트 배포

```bash
# 1. 코드 수정
# ... editing ...

# 2. 버전 업데이트 (app.json)
# "version": "1.0.1"

# 3. 빌드
npm run build:all

# 4. 자동 배포
npm run submit:all

# 끝!
```

### 시나리오 3: 긴급 버그 수정

```bash
# 1. 버그 수정
# ... fix bug ...

# 2. 즉시 빌드 (우선순위)
eas build --platform all --priority high

# 3. 빌드 모니터링
eas build:list

# 4. 완료되면 즉시 배포
eas submit --platform all --latest
```

---

## 🛠️ 자동화 스크립트

### `scripts/migrate-to-expo.sh`
Expo로 자동 마이그레이션

```bash
./scripts/migrate-to-expo.sh
```

### `scripts/build-expo.sh`
대화형 빌드 스크립트

```bash
./scripts/build-expo.sh
# → 1) Android만
# → 2) iOS만  
# → 3) 둘 다 ⭐
```

### package.json 스크립트

```json
{
  "scripts": {
    "build:all": "eas build --platform all",
    "build:android": "eas build --platform android",
    "build:ios": "eas build --platform ios",
    "submit:all": "eas submit --platform all --latest",
    "submit:android": "eas submit --platform android --latest",
    "submit:ios": "eas submit --platform ios --latest"
  }
}
```

---

## ❓ FAQ

### Q: Mac 없이 iOS 앱을 만들 수 있나요?
**A:** 네! Expo EAS Build는 클라우드에서 빌드하므로 Windows/Linux에서도 iOS 앱 빌드가 가능합니다.

### Q: 무료로 사용할 수 있나요?
**A:** 네! 월 30회 빌드까지 무료입니다. 대부분의 개인 프로젝트에 충분합니다.

### Q: 빌드 시간은 얼마나 걸리나요?
**A:** Android + iOS 동시 빌드 시 10-15분 정도 소요됩니다.

### Q: 기존 React Native CLI 프로젝트를 그대로 쓸 수 있나요?
**A:** 네! 마이그레이션 스크립트로 쉽게 전환할 수 있습니다.

### Q: Google Play / App Store에 바로 올릴 수 있나요?
**A:** 네! `eas submit` 명령어로 자동 업로드가 가능합니다.

### Q: 로컬에서 빌드하는 것보다 느리지 않나요?
**A:** 오히려 더 빠릅니다! 클라우드의 강력한 서버를 사용하기 때문입니다.

---

## 📚 추가 리소스

- **상세 가이드:** `EXPO_MIGRATION_GUIDE.md`
- **Expo 공식 문서:** https://docs.expo.dev
- **EAS Build 문서:** https://docs.expo.dev/build/introduction
- **대시보드:** https://expo.dev

---

## ✅ 체크리스트

### 마이그레이션
- [ ] `./scripts/migrate-to-expo.sh` 실행
- [ ] `eas login` 완료
- [ ] `eas build:configure` 완료
- [ ] `app.json` 확인
- [ ] `assets/` 폴더에 아이콘 추가

### 첫 빌드
- [ ] `eas build --platform all` 실행
- [ ] 빌드 완료 대기 (10-15분)
- [ ] APK 다운로드 및 테스트
- [ ] IPA 다운로드 (iOS 배포시)

### 배포
- [ ] Google Play Console 준비 ($25)
- [ ] Apple Developer 계정 준비 ($99/년)
- [ ] `eas submit` 실행
- [ ] 스토어 심사 대기

---

## 🎉 결론

**Before (React Native CLI):**
```
Android: 설정 1시간 + 빌드 20분
iOS: Mac 필수 + 설정 2시간 + 빌드 30분
════════════════════════════════════
총: 3시간+ (Mac 있어야 함)
```

**After (Expo):**
```
설정: 5분
Android + iOS 동시 빌드: 15분
════════════════════════════════════
총: 20분 (어디서나 가능)
```

**명령어 하나로 끝:**
```bash
eas build --platform all
```

---

**작성:** 2025-11-03  
**업데이트:** Expo SDK 51 기준
