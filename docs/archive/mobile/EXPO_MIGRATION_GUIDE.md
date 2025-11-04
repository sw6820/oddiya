# Expo 마이그레이션 가이드 - Android & iOS 동시 빌드

## 왜 Expo?

### React Native CLI vs Expo

| 기능 | React Native CLI | Expo |
|------|------------------|------|
| Android 빌드 | Android Studio, JDK 설치 필요 | ✅ 클라우드 빌드 (로컬 환경 불필요) |
| iOS 빌드 | Mac + Xcode 필수 | ✅ 클라우드 빌드 (Mac 없이 가능) |
| 동시 빌드 | ❌ 불가능 | ✅ 단일 명령어로 가능 |
| 빌드 시간 | 20-30분 (로컬) | 10-15분 (클라우드) |
| 설정 복잡도 | ⭐⭐⭐⭐ | ⭐ |
| 비용 | 무료 (로컬) | 무료 (Free tier) |

### ✅ Expo의 장점

1. **단일 명령어로 Android + iOS 동시 빌드**
   ```bash
   eas build --platform all
   ```

2. **Mac 없이 iOS 빌드 가능**
   - EAS Build 클라우드에서 자동 빌드

3. **로컬 환경 설정 불필요**
   - Android Studio ❌
   - Xcode ❌
   - JDK 설치 ❌

4. **자동 코드 서명**
   - Apple Developer 계정만 있으면 자동 처리

5. **무료 티어**
   - 월 30회 빌드 무료
   - 추가: $29/월 (무제한)

---

## Step 1: Expo 마이그레이션

### 1.1 Expo 패키지 설치

```bash
cd /Users/wjs/cursor/oddiya/mobile

# Expo SDK 설치
npm install expo

# Expo CLI 설치 (전역)
npm install -g eas-cli

# Expo 필수 패키지
npx expo install expo-dev-client
```

### 1.2 app.json 생성

```bash
cat > app.json << 'APPJSON'
{
  "expo": {
    "name": "Oddiya",
    "slug": "oddiya",
    "version": "1.0.0",
    "orientation": "portrait",
    "icon": "./assets/icon.png",
    "userInterfaceStyle": "light",
    "splash": {
      "image": "./assets/splash.png",
      "resizeMode": "contain",
      "backgroundColor": "#ffffff"
    },
    "assetBundlePatterns": [
      "**/*"
    ],
    "ios": {
      "supportsTablet": true,
      "bundleIdentifier": "com.oddiya.app"
    },
    "android": {
      "adaptiveIcon": {
        "foregroundImage": "./assets/adaptive-icon.png",
        "backgroundColor": "#ffffff"
      },
      "package": "com.oddiya.app"
    },
    "web": {
      "favicon": "./assets/favicon.png"
    },
    "plugins": [
      "expo-router"
    ]
  }
}
APPJSON
```

### 1.3 package.json 스크립트 업데이트

```json
{
  "scripts": {
    "start": "expo start",
    "android": "expo start --android",
    "ios": "expo start --ios",
    "web": "expo start --web",
    "build:all": "eas build --platform all",
    "build:android": "eas build --platform android",
    "build:ios": "eas build --platform ios"
  }
}
```

### 1.4 필수 에셋 생성

```bash
# 아이콘 폴더 생성
mkdir -p assets

# 임시 아이콘 다운로드 (나중에 실제 아이콘으로 교체)
curl -o assets/icon.png https://via.placeholder.com/1024x1024.png
curl -o assets/splash.png https://via.placeholder.com/1242x2688.png
curl -o assets/adaptive-icon.png https://via.placeholder.com/1024x1024.png
```

---

## Step 2: EAS Build 설정

### 2.1 EAS 계정 생성

```bash
# Expo 계정 생성/로그인
eas login

# 이메일과 비밀번호로 가입 (무료)
```

### 2.2 프로젝트 설정

```bash
# EAS 프로젝트 초기화
eas build:configure

# eas.json 파일이 자동 생성됨
```

### 2.3 eas.json 설정 확인

```json
{
  "cli": {
    "version": ">= 5.9.0"
  },
  "build": {
    "development": {
      "developmentClient": true,
      "distribution": "internal"
    },
    "preview": {
      "distribution": "internal",
      "android": {
        "buildType": "apk"
      }
    },
    "production": {
      "android": {
        "buildType": "apk"
      },
      "ios": {
        "simulator": false
      }
    }
  },
  "submit": {
    "production": {}
  }
}
```

---

## Step 3: 동시 빌드 실행

### 3.1 Android + iOS 동시 빌드 (단일 명령어)

```bash
# 🚀 Android APK + iOS IPA 동시 빌드
eas build --platform all --profile production

# 또는 개별 빌드
eas build --platform android --profile production  # Android만
eas build --platform ios --profile production      # iOS만
```

**실행 결과:**
```
✔ Build completed!

Android APK:
📦 Download: https://expo.dev/artifacts/eas/xxx.apk

iOS IPA:
📦 Download: https://expo.dev/artifacts/eas/xxx.ipa
```

### 3.2 빌드 모니터링

```bash
# 빌드 상태 확인
eas build:list

# 특정 빌드 상세 정보
eas build:view <build-id>
```

### 3.3 빌드 다운로드

빌드 완료 후:
1. Expo 웹사이트에서 다운로드: https://expo.dev/accounts/YOUR_USERNAME/projects/oddiya/builds
2. 이메일로 받은 링크에서 다운로드
3. CLI로 다운로드:
   ```bash
   eas build:download --platform android
   eas build:download --platform ios
   ```

---

## Step 4: iOS 빌드 설정 (Apple Developer 필요)

### 4.1 Apple Developer 계정

- **비용:** $99/년
- **등록:** https://developer.apple.com

### 4.2 자동 코드 서명

```bash
# EAS가 자동으로 처리 (Apple ID만 입력)
eas build --platform ios

# 프롬프트에서 Apple ID 입력
# EAS가 자동으로 인증서 생성 및 서명
```

### 4.3 수동 설정 (선택사항)

Apple Developer Portal에서:
1. Certificates → Create Certificate
2. Identifiers → Create App ID
3. Profiles → Create Provisioning Profile

---

## Step 5: 배포

### 5.1 Android - Google Play Store

```bash
# AAB 빌드 (Play Store용)
eas build --platform android --profile production

# Play Console에 업로드
# 1. https://play.google.com/console
# 2. Create app
# 3. Upload AAB
# 4. Review & Publish
```

### 5.2 iOS - App Store

```bash
# IPA 빌드
eas build --platform ios --profile production

# App Store Connect에 자동 업로드
eas submit --platform ios
```

### 5.3 직접 배포 (APK만)

```bash
# 내부 테스트용 APK 빌드
eas build --platform android --profile preview

# 다운로드 링크를 사용자에게 공유
```

---

## 비용 분석

### EAS Build 무료 티어

| 항목 | 무료 | 유료 ($29/월) |
|------|------|---------------|
| Android 빌드 | 30회/월 | 무제한 |
| iOS 빌드 | 30회/월 | 무제한 |
| 빌드 시간 | 10-15분 | 우선순위 (5-10분) |
| 스토리지 | 1GB | 10GB |

### Apple & Google 비용

| 항목 | 비용 | 빈도 |
|------|------|------|
| Apple Developer | $99 | 연간 |
| Google Play Console | $25 | 일회성 |

### 총 비용

```
첫 해:
- EAS Build: $0 (무료 티어)
- Apple Developer: $99 (iOS 배포시)
- Google Play: $25 (Play Store 배포시)
────────────────────────
합계: $0 ~ $124

2년차 이후:
- EAS Build: $0
- Apple Developer: $99/년
────────────────────────
합계: $99/년
```

---

## 빠른 시작 (5분)

```bash
# 1. Expo 설치
cd mobile
npm install expo
npm install -g eas-cli

# 2. Expo 로그인
eas login

# 3. 프로젝트 설정
eas build:configure

# 4. Android + iOS 동시 빌드
eas build --platform all --profile production

# 5. 빌드 완료 대기 (10-15분)
# 6. 다운로드 링크 수신 (이메일)
```

---

## 마이그레이션 체크리스트

### 기본 설정
- [ ] `npm install expo` 실행
- [ ] `eas-cli` 전역 설치
- [ ] `app.json` 생성
- [ ] `assets/` 폴더에 아이콘 추가
- [ ] `eas login` 완료

### Android 빌드
- [ ] `eas build:configure` 실행
- [ ] `eas build --platform android` 성공
- [ ] APK 다운로드 및 테스트

### iOS 빌드 (선택)
- [ ] Apple Developer 계정 준비 ($99/년)
- [ ] `eas build --platform ios` 실행
- [ ] Apple ID 입력
- [ ] IPA 다운로드 및 TestFlight 테스트

### 배포
- [ ] Google Play Console 준비 ($25)
- [ ] APK/AAB 업로드
- [ ] (선택) App Store Connect 업로드

---

## 자동화 스크립트

### `scripts/build-expo.sh` 생성

```bash
#!/bin/bash
# Expo Build Automation Script

set -e

echo "🚀 Oddiya Expo Build"
echo "===================="
echo ""
echo "Select build target:"
echo "1) Android only"
echo "2) iOS only"
echo "3) Both (Android + iOS)"
read -p "Enter choice [1-3]: " choice

case $choice in
  1)
    echo "Building Android..."
    eas build --platform android --profile production
    ;;
  2)
    echo "Building iOS..."
    eas build --platform ios --profile production
    ;;
  3)
    echo "Building Android + iOS..."
    eas build --platform all --profile production
    ;;
  *)
    echo "Invalid choice"
    exit 1
    ;;
esac

echo ""
echo "✅ Build submitted!"
echo "Check status: https://expo.dev/accounts/YOUR_USERNAME/projects/oddiya/builds"
```

실행:
```bash
chmod +x scripts/build-expo.sh
./scripts/build-expo.sh
```

---

## 로컬 vs 클라우드 빌드 비교

### 로컬 빌드 (React Native CLI)

**장점:**
- 무료
- 오프라인 가능
- 완전한 제어

**단점:**
- Android Studio + Xcode 설치 필요
- Mac 필수 (iOS)
- 빌드 시간 20-30분
- 환경 설정 복잡
- Android/iOS 따로 빌드

### 클라우드 빌드 (Expo EAS)

**장점:**
- ✅ 로컬 환경 설정 불필요
- ✅ Mac 없이 iOS 빌드
- ✅ 단일 명령어로 동시 빌드
- ✅ 자동 코드 서명
- ✅ 빌드 시간 10-15분

**단점:**
- 유료 (무료 티어: 30회/월)
- 인터넷 필요
- 제한적인 커스터마이징

---

## 트러블슈팅

### 빌드 실패: 잘못된 자격 증명

```bash
# Expo 로그아웃/로그인
eas logout
eas login
```

### iOS 빌드 실패: Apple ID

```bash
# Apple ID 재설정
eas credentials
# Select iOS → Apple ID → Update
```

### 빌드 큐 대기 시간이 긴 경우

```bash
# Priority Build (유료 플랜)
# 또는 한가한 시간대에 빌드
```

---

## React Native CLI와 병행 사용

Expo로 마이그레이션해도 기존 방식 사용 가능:

```bash
# Expo 방식
npm run start        # Expo Go

# 기존 방식 (필요시)
npx react-native run-android
npx react-native run-ios
```

---

## 요약

### Before (React Native CLI)
```bash
# Android 빌드 (로컬)
cd android && ./gradlew assembleRelease
# → 소요 시간: 20분
# → 필요: Android Studio, JDK

# iOS 빌드 (Mac에서만 가능)
cd ios && xcodebuild ...
# → 소요 시간: 30분
# → 필요: Mac, Xcode
```

### After (Expo)
```bash
# Android + iOS 동시 빌드 (클라우드)
eas build --platform all
# → 소요 시간: 10-15분
# → 필요: 인터넷만
# → 어디서나 가능 (Windows, Linux, Mac)
```

---

**최종 추천:**

프로덕션 배포에는 **Expo EAS Build**를 사용하세요:
- ✅ 간단함
- ✅ 빠름
- ✅ 안정적
- ✅ 무료 (30빌드/월)

---

**Last Updated:** 2025-11-03
