# 📱 Android 앱 배포 가이드

**플랫폼:** Android
**배포 방식:** Google Play Store
**예상 소요 시간:** 2-3시간 (첫 배포)

---

## 📋 사전 준비

### 필수 항목

- [x] Google OAuth 설정 완료
- [x] AWS 서버 배포 완료 (API 엔드포인트 확인)
- [ ] Google Play Developer 계정 ($25 일회성 비용)
- [ ] Android Studio 설치
- [ ] 앱 서명 키 생성
- [ ] 앱 아이콘 및 스크린샷 준비

---

## Step 1: Google Play Developer 계정 (30분)

### 1.1 계정 등록

```bash
# Google Play Console 가기
open https://play.google.com/console
```

**절차:**
1. Google 계정으로 로그인
2. "계정 만들기" 클릭
3. 개인 또는 조직 선택
4. 개발자 이름 입력: `Oddiya`
5. 이메일 주소 확인
6. 등록 비용 지불: $25 (일회성)
7. 개발자 계약 동의
8. 계정 활성화 대기 (보통 즉시, 최대 48시간)

---

## Step 2: Android 프로젝트 설정 (15분)

### 2.1 API 엔드포인트 업데이트

```bash
# mobile/src/constants/config.ts 파일 수정
cd mobile
```

파일 수정:
```typescript
// mobile/src/constants/config.ts

// AWS EC2 Public IP로 업데이트 (terraform output 확인)
export const API_BASE_URL = 'http://YOUR_EC2_IP:8083';

// 예: export const API_BASE_URL = 'http://43.200.123.45:8083';

// Google OAuth Client ID (이미 설정됨)
export const GOOGLE_CLIENT_ID = '201806680568-34bjg6mnu76939outdakjbf8gmme1r5m.apps.googleusercontent.com';
```

### 2.2 앱 버전 업데이트

```bash
# mobile/app/build.gradle 수정
```

```gradle
android {
    defaultConfig {
        applicationId "com.oddiya.app"
        versionCode 1        // 빌드 번호 (매 배포마다 증가)
        versionName "1.0.0"  // 사용자에게 보이는 버전
    }
}
```

### 2.3 Google OAuth SHA-1 인증서 추가

```bash
# 디버그 키 SHA-1 확인
cd android
./gradlew signingReport

# 출력에서 SHA-1 찾기:
# Variant: debug
# Config: debug
# SHA1: AA:BB:CC:DD:...
```

**Google Console에 추가:**
1. https://console.cloud.google.com/apis/credentials 이동
2. OAuth Client ID 선택
3. "Android" 클라이언트 추가
4. 패키지명: `com.oddiya.app`
5. SHA-1 입력
6. 저장

---

## Step 3: 앱 서명 키 생성 (10분)

### 3.1 Keystore 생성

```bash
# 프로덕션 서명 키 생성
keytool -genkey -v \
  -keystore oddiya-release-key.jks \
  -alias oddiya-release \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000

# 입력 사항:
# Keystore password: [강력한 비밀번호]
# Re-enter password: [재입력]
# What is your first and last name?: Oddiya Team
# What is the name of your organizational unit?: Development
# What is the name of your organization?: Oddiya
# What is the name of your City or Locality?: Seoul
# What is the name of your State or Province?: Seoul
# What is the two-letter country code for this unit?: KR
# Is CN=Oddiya Team ... correct?: yes
```

### 3.2 Keystore 안전하게 보관

```bash
# 안전한 위치로 이동
mv oddiya-release-key.jks ~/.android/

# 권한 설정
chmod 600 ~/.android/oddiya-release-key.jks

# 백업 (중요!)
cp ~/.android/oddiya-release-key.jks ~/Desktop/oddiya-release-key-BACKUP.jks
```

**⚠️ 중요:**
- 이 키를 잃어버리면 앱 업데이트 불가능!
- 안전한 곳에 백업 보관 (1Password, Google Drive 등)
- 비밀번호 기록

### 3.3 Gradle 서명 설정

```bash
# android/app/build.gradle 수정
```

```gradle
android {
    signingConfigs {
        release {
            storeFile file('/Users/wjs/.android/oddiya-release-key.jks')
            storePassword 'YOUR_KEYSTORE_PASSWORD'
            keyAlias 'oddiya-release'
            keyPassword 'YOUR_KEY_PASSWORD'
        }
    }

    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

---

## Step 4: 릴리스 빌드 (15분)

### 4.1 의존성 설치

```bash
cd mobile

# Node 모듈 설치
npm install

# Android 의존성 동기화
cd android
./gradlew clean
```

### 4.2 AAB (Android App Bundle) 빌드

```bash
# Release 빌드
./gradlew bundleRelease

# 빌드 성공 확인
ls -lh app/build/outputs/bundle/release/
# app-release.aab 파일 생성 확인
```

### 4.3 빌드 검증

```bash
# APK 추출 테스트
bundletool build-apks \
  --bundle=app/build/outputs/bundle/release/app-release.aab \
  --output=oddiya.apks \
  --mode=universal

# 설치 테스트 (연결된 기기 필요)
bundletool install-apks --apks=oddiya.apks
```

---

## Step 5: Google Play Console 설정 (30분)

### 5.1 앱 만들기

1. **Play Console** 이동: https://play.google.com/console
2. **"앱 만들기"** 클릭
3. **설정:**
   - 앱 이름: `Oddiya`
   - 기본 언어: 한국어
   - 앱/게임: 앱
   - 무료/유료: 무료
4. **"앱 만들기"** 클릭

### 5.2 스토어 등록 정보

**앱 세부정보:**
- 간단한 설명 (80자):
  ```
  AI 기반 맞춤형 한국 여행 플래너 - 서울, 부산, 제주 여행 계획을 자동으로 생성해드립니다
  ```

- 자세한 설명 (4000자):
  ```
  🌏 Oddiya - AI 여행 플래너

  한국 여행을 계획하시나요? Oddiya가 AI로 당신만의 완벽한 여행 일정을 만들어 드립니다!

  ✨ 주요 기능:
  • AI 기반 맞춤형 여행 계획
  • 서울, 부산, 제주 등 인기 여행지
  • 예산에 맞는 일정 추천
  • 실시간 여행 정보 업데이트
  • Google 로그인으로 간편하게

  🎯 이런 분들께 추천합니다:
  • 한국 여행을 처음 계획하시는 분
  • 시간이 부족한 바쁜 여행자
  • 맞춤형 일정을 원하시는 분
  • 최신 여행 트렌드를 따르고 싶은 분

  📱 사용 방법:
  1. Google 계정으로 로그인
  2. 여행지, 날짜, 예산 입력
  3. AI가 자동으로 일정 생성
  4. 필요에 따라 수정 및 저장

  💡 Oddiya는 Google Gemini AI를 사용하여 최적의 여행 계획을 제공합니다.
  ```

**그래픽:**
- 앱 아이콘: 512 x 512 px
- 스크린샷: 최소 2개 (1080 x 1920 px 권장)
- 프로모션 이미지: 1024 x 500 px (선택사항)

### 5.3 앱 카테고리 및 연락처

- **카테고리:** 여행 및 지역정보
- **이메일:** your-email@example.com
- **개인정보처리방침 URL:** (나중에 추가 가능)

### 5.4 콘텐츠 등급

1. "콘텐츠 등급 설정" 클릭
2. 설문조사 작성:
   - 폭력: 없음
   - 성적 콘텐츠: 없음
   - 욕설: 없음
   - 등등
3. 등급 받기 (보통 "전체 이용가")

### 5.5 앱 액세스 권한

**앱에서 요청하는 권한:**
- 인터넷 액세스
- 네트워크 상태 확인
- Google OAuth 로그인

**설명 제공**

---

## Step 6: 릴리스 업로드 (15분)

### 6.1 내부 테스트 트랙 설정

1. **"릴리스" → "테스트" → "내부 테스트"**
2. **"새 릴리스 만들기"**
3. **AAB 업로드:**
   - `app/build/outputs/bundle/release/app-release.aab` 선택
4. **릴리스 이름:** v1.0.0
5. **릴리스 노트:**
   ```
   초기 출시 버전
   - AI 기반 여행 계획 생성
   - Google 로그인
   - 서울, 부산, 제주 여행지 지원
   ```
6. **"검토 및 출시"** 클릭

### 6.2 테스터 추가

1. **"테스터" 탭**
2. **이메일 목록 만들기**
3. **테스터 이메일 추가**
4. **저장**

### 6.3 테스트 링크 공유

- 테스터들에게 opt-in 링크 공유
- 테스터가 앱 설치 및 테스트
- 피드백 수집 (1-2주)

---

## Step 7: 프로덕션 출시 (심사 시간: 1-7일)

### 7.1 프로덕션 트랙으로 승급

1. **테스트 완료 후**
2. **"릴리스" → "프로덕션"**
3. **"내부 테스트에서 승급"** 선택
4. **출시 비율:** 100% (전체 출시)
5. **"검토 및 출시"**

### 7.2 Google 심사 대기

- 일반적으로 1-3일 소요
- 최대 7일
- 이메일로 결과 통보

### 7.3 출시 완료

**앱이 승인되면:**
- Google Play Store에서 검색 가능
- 전 세계 사용자 다운로드 가능
- 통계 및 리뷰 확인 가능

---

## 🎉 배포 완료!

### 앱 정보

- **패키지명:** com.oddiya.app
- **버전:** 1.0.0
- **최소 Android 버전:** 6.0 (API 23)
- **타겟 Android 버전:** 14 (API 34)

### Play Store 링크

```
https://play.google.com/store/apps/details?id=com.oddiya.app
```

---

## 📊 배포 후 모니터링

### Google Play Console

1. **설치 수 확인**
2. **충돌 및 ANR 모니터링**
3. **사용자 리뷰 응답**
4. **평점 관리**

### 업데이트 배포

```bash
# 1. 버전 증가
# build.gradle:
# versionCode 2
# versionName "1.0.1"

# 2. 빌드
./gradlew bundleRelease

# 3. Play Console 업로드
# 4. 릴리스 노트 작성
# 5. 출시
```

---

## 🔧 문제 해결

### 문제 1: 서명 오류

**증상:**
```
Execution failed for task ':app:validateSigningRelease'
```

**해결:**
- Keystore 경로 확인
- 비밀번호 확인
- alias 이름 확인

### 문제 2: API 연결 실패

**증상:** 앱에서 서버 연결 안 됨

**해결:**
```typescript
// config.ts 확인
// EC2 Public IP 정확한지 확인
export const API_BASE_URL = 'http://CORRECT_IP:8083';
```

### 문제 3: Google OAuth 실패

**증상:** 로그인 버튼이 작동하지 않음

**해결:**
1. Google Console에서 SHA-1 등록 확인
2. 패키지명이 일치하는지 확인
3. OAuth Client ID 확인

---

## 💡 Pro Tips

### 테스트

- **내부 테스트:** 최소 1주일
- **베타 테스트:** 더 많은 사용자로 2주
- **단계별 출시:** 10% → 50% → 100%

### ASO (App Store Optimization)

- **키워드 최적화**
- **스크린샷 품질**
- **정기적인 업데이트**
- **사용자 리뷰 응답**

### 버전 관리

```
Major.Minor.Patch
1.0.0 → 초기 출시
1.0.1 → 버그 수정
1.1.0 → 새 기능
2.0.0 → 대규모 변경
```

---

## 다음 단계

### ✅ Android 배포 완료 후:

1. **iOS 앱 배포**
   - Apple Developer 계정 ($99/년)
   - TestFlight 베타 테스트
   - App Store 제출

2. **마케팅**
   - 소셜 미디어
   - 블로그 포스트
   - 사용자 확보 전략

3. **지속적인 개선**
   - 사용자 피드백 반영
   - 기능 추가
   - 성능 최적화

---

**Status:** Android 배포 가이드 완료
**플랫폼:** Google Play Store
**예상 심사 기간:** 1-7일
