# Oddiya - 최소 비용 배포 가이드

## 개요

Oddiya를 **최소 비용**으로 AWS와 Android에 배포하는 완전 가이드입니다.

**총 비용: 월 $0** (AWS 프리티어 12개월 기준)

## 📋 목차

1. [AWS 배포 (백엔드)](#aws-배포)
2. [Android 배포 (모바일)](#android-배포)
3. [비용 분석](#비용-분석)
4. [빠른 시작](#빠른-시작)

---

## 🚀 AWS 배포

### 아키텍처

```
Internet
   ↓
EC2 t2.micro (프리티어)
   ↓
Nginx (Port 80)
   ├─ API Gateway (8080) - Frontend & Routing
   ├─ Plan Service (8083) - Travel API
   └─ LLM Agent (8000) - AI Plan Generation
       └─ Redis (6379) - Cache
```

### 단계별 배포

#### 1. EC2 인스턴스 생성

```bash
# AWS Console에서:
# - AMI: Amazon Linux 2023
# - Type: t2.micro (1GB RAM, 1 vCPU) ✅ Free Tier
# - Storage: 8GB gp3
# - Security Group: Port 22, 80, 443 open
```

#### 2. EC2 환경 설정

```bash
# EC2에 SSH 접속
ssh -i your-key.pem ec2-user@<EC2_PUBLIC_IP>

# Setup 스크립트 실행
curl -o setup-ec2.sh https://raw.githubusercontent.com/YOUR_REPO/main/scripts/aws/setup-ec2.sh
chmod +x setup-ec2.sh
./setup-ec2.sh
```

#### 3. 서비스 배포

```bash
# 프로젝트 클론
cd /opt/oddiya
git clone https://github.com/YOUR_REPO/oddiya.git .

# 환경 변수 설정
cat > .env << 'ENVEOF'
GOOGLE_API_KEY=your_gemini_api_key
GEMINI_MODEL=gemini-2.0-flash-exp
ENVEOF

# Docker Compose로 서비스 시작
docker-compose build
docker-compose up -d

# 서비스 확인
docker-compose ps
```

#### 4. 접속 확인

```bash
# EC2 Public IP 확인
curl http://169.254.169.254/latest/meta-data/public-ipv4

# 브라우저에서 접속
http://<EC2_PUBLIC_IP>
```

### 파일 구조

```
oddiya/
├── docker-compose.yml        # ✅ 모든 서비스 정의
├── nginx.conf                 # ✅ 리버스 프록시 설정
├── scripts/aws/
│   └── setup-ec2.sh          # ✅ EC2 초기 설정
├── docs/deployment/
│   └── AWS_DEPLOYMENT_GUIDE.md # 📖 상세 가이드
└── services/
    ├── api-gateway/Dockerfile ✅
    ├── plan-service/Dockerfile ✅
    └── llm-agent/Dockerfile   ✅
```

---

## 📱 Android 배포

### 빌드 프로세스

```bash
# 1. Android 프로젝트 초기화 (최초 1회)
cd mobile
npx react-native init Oddiya --version 0.75.0 --directory temp
cp -r temp/android .
rm -rf temp

# 2. 의존성 설치
npm install

# 3. API URL 설정
# mobile/src/constants/config.ts 파일에서
# API_BASE_URL을 EC2 Public IP로 변경
export const CONFIG = {
  API_BASE_URL: 'http://YOUR_EC2_IP'
};

# 4. 서명 키 생성
cd android/app
keytool -genkeypair -v \
  -storetype PKCS12 \
  -keystore my-release-key.keystore \
  -alias my-key-alias \
  -keyalg RSA -keysize 2048 -validity 10000

# 5. APK 빌드 (자동화 스크립트)
cd mobile
./scripts/build-android.sh
```

### 빌드 결과

```
✅ APK: android/app/build/outputs/apk/release/app-release.apk
✅ AAB: android/app/build/outputs/bundle/release/app-release.aab
```

### 배포 옵션

#### Option 1: 직접 배포 (가장 간단)

```bash
# APK를 Google Drive/Dropbox에 업로드
# 사용자는 다운로드 후 직접 설치
```

**장점:**
- 무료
- 즉시 배포
- 심사 없음

**단점:**
- 사용자가 "알 수 없는 출처" 허용 필요
- 자동 업데이트 없음

#### Option 2: Google Play Store (권장)

```bash
# 1. 개발자 계정 생성 ($25 일회성)
# 2. Play Console에서 앱 생성
# 3. AAB 업로드
# 4. 심사 대기 (1-7일)
```

**장점:**
- 공식 스토어
- 자동 업데이트
- 신뢰도 높음

**비용:** $25 (일회성)

#### Option 3: Firebase App Distribution (테스트용)

```bash
npm install -g firebase-tools
firebase login
firebase appdistribution:distribute \
  android/app/build/outputs/apk/release/app-release.apk \
  --app YOUR_FIREBASE_APP_ID \
  --groups testers
```

**장점:**
- 무료
- 테스터 그룹 관리
- 즉시 배포

### 파일 구조

```
mobile/
├── ANDROID_BUILD_GUIDE.md     # 📖 상세 빌드 가이드
├── scripts/
│   └── build-android.sh       # ✅ 빌드 자동화
├── src/constants/
│   └── config.ts              # ⚙️ API URL 설정
└── android/
    ├── app/
    │   ├── build.gradle       # ⚙️ 빌드 설정
    │   └── my-release-key.keystore # 🔐 서명 키
    └── gradle.properties      # ⚙️ Gradle 설정
```

---

## 💰 비용 분석

### AWS 비용 (프리티어 12개월)

| 항목 | 사양 | 월 비용 | 프리티어 후 |
|------|------|---------|------------|
| EC2 t2.micro | 1GB RAM, 1 vCPU | $0 | ~$8.50 |
| EBS 8GB | gp3 SSD | $0 | ~$0.80 |
| 데이터 전송 | 15GB/월 | $0 | ~$1.40 |
| Gemini API | 15 req/min | $0 | $0 |
| **합계** | | **$0/월** | **~$10/월** |

### Android 배포 비용

| 옵션 | 비용 | 특징 |
|------|------|------|
| 직접 배포 | **$0** | 무료, 즉시 배포 |
| Play Store | **$25** (일회성) | 공식 스토어, 자동 업데이트 |
| Firebase | **$0** | 무료, 테스트용 |

### 총 비용 요약

```
AWS (12개월): $0
Android 배포: $0 (직접) or $25 (Play Store)
────────────────────────
합계: $0 ~ $25 (일회성)

12개월 후:
AWS: ~$10/월
Android: $0 추가 비용
```

---

## ⚡ 빠른 시작

### AWS 배포 (5분)

```bash
# 1. EC2 생성 (AWS Console)
# 2. SSH 접속
ssh -i key.pem ec2-user@<EC2_IP>

# 3. 자동 설정
curl -o setup.sh https://raw.githubusercontent.com/YOUR_REPO/main/scripts/aws/setup-ec2.sh
bash setup.sh

# 4. 프로젝트 클론 & 시작
cd /opt/oddiya
git clone <your-repo> .
echo "GOOGLE_API_KEY=your_key" > .env
docker-compose up -d
```

### Android 빌드 (10분)

```bash
# 1. 환경 설정 (최초 1회)
# - Android Studio 설치
# - JDK 17 설치
# - ANDROID_HOME 설정

# 2. 프로젝트 초기화 (최초 1회)
cd mobile
npx react-native init Oddiya --version 0.75.0 --directory temp
cp -r temp/android .

# 3. API URL 설정
# src/constants/config.ts에서 EC2 IP 설정

# 4. 빌드
./scripts/build-android.sh

# 5. 설치 테스트
adb install android/app/build/outputs/apk/release/app-release.apk
```

---

## 📚 참고 문서

### AWS

- **상세 가이드:** `docs/deployment/AWS_DEPLOYMENT_GUIDE.md`
- **Setup 스크립트:** `scripts/aws/setup-ec2.sh`
- **Docker Compose:** `docker-compose.yml`
- **Nginx 설정:** `nginx.conf`

### Android

- **빌드 가이드:** `mobile/ANDROID_BUILD_GUIDE.md`
- **빌드 스크립트:** `mobile/scripts/build-android.sh`
- **Google OAuth:** `mobile/GOOGLE_OAUTH_ANDROID_SETUP.md`

---

## 🔧 유지보수

### 서비스 업데이트

```bash
# AWS
cd /opt/oddiya
git pull
docker-compose build
docker-compose up -d

# Android
cd mobile
./scripts/build-android.sh
# 새 APK를 배포
```

### 로그 확인

```bash
# AWS 서비스 로그
docker-compose logs -f

# Android 로그
adb logcat | grep ReactNative
```

### 서비스 재시작

```bash
# 전체 재시작
docker-compose restart

# 특정 서비스만
docker-compose restart llm-agent
```

---

## ✅ 체크리스트

### AWS 배포

- [ ] EC2 t2.micro 인스턴스 생성
- [ ] Security Group 설정 (Port 22, 80, 443)
- [ ] Docker & Docker Compose 설치
- [ ] 프로젝트 클론
- [ ] .env 파일 생성 (GOOGLE_API_KEY)
- [ ] docker-compose up -d 실행
- [ ] 브라우저에서 접속 확인

### Android 배포

- [ ] Android Studio 설치
- [ ] JDK 17 설치
- [ ] ANDROID_HOME 환경 변수 설정
- [ ] React Native 프로젝트 초기화
- [ ] API URL을 EC2 IP로 변경
- [ ] 서명 키 생성
- [ ] APK 빌드
- [ ] 실제 기기에서 테스트
- [ ] 배포 방법 선택 (직접/Play Store/Firebase)

---

## 🆘 문제 해결

### AWS

**서비스가 시작되지 않음:**
```bash
docker-compose logs
docker-compose down
docker-compose up -d
```

**메모리 부족 (t2.micro 1GB):**
```bash
# Swap 추가
sudo dd if=/dev/zero of=/swapfile bs=1M count=1024
sudo mkswap /swapfile
sudo swapon /swapfile
```

### Android

**빌드 실패:**
```bash
# Android SDK 확인
echo $ANDROID_HOME

# Clean build
cd android
./gradlew clean
./gradlew assembleRelease
```

**APK 설치 실패:**
```bash
# 기존 앱 제거
adb uninstall com.oddiya

# 재설치
adb install -r app-release.apk
```

---

## 📞 지원

- **GitHub Issues:** https://github.com/YOUR_REPO/oddiya/issues
- **AWS 문서:** `docs/deployment/AWS_DEPLOYMENT_GUIDE.md`
- **Android 문서:** `mobile/ANDROID_BUILD_GUIDE.md`

---

**마지막 업데이트:** 2025-11-03
**작성자:** Claude Code

**총 소요 시간:**
- AWS 배포: ~15분
- Android 빌드: ~20분
- **합계: ~35분**

**총 비용:**
- **첫 12개월: $0**
- **이후: ~$10/월 (AWS)**
- **Play Store (선택): $25 (일회성)**
