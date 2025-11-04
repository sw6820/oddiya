# Oddiya - 시작 가이드

> 이 문서 하나로 Oddiya를 처음부터 배포까지 완료할 수 있습니다.

## 📋 목차

1. [사전 준비](#사전-준비)
2. [로컬 개발 환경 설정](#로컬-개발-환경-설정)
3. [모바일 앱 빌드](#모바일-앱-빌드)
4. [AWS 배포](#aws-배포)
5. [다음 단계](#다음-단계)

---

## 사전 준비

### 필수 소프트웨어

```bash
# Node.js 18+
node -v

# Docker & Docker Compose
docker -v
docker-compose -v

# Git
git -v
```

설치되지 않았다면:
- **Node.js:** https://nodejs.org (LTS 버전)
- **Docker Desktop:** https://www.docker.com/products/docker-desktop
- **Git:** https://git-scm.com

### 필수 계정

1. **Google API Key** (무료)
   - https://makersuite.google.com/app/apikey
   - Gemini AI 사용을 위한 API 키

2. **Expo 계정** (무료)
   - https://expo.dev/signup
   - 모바일 앱 빌드용

3. **(선택) Google OAuth** 
   - https://console.cloud.google.com
   - 소셜 로그인용

---

## 로컬 개발 환경 설정

### Step 1: 저장소 클론

```bash
git clone https://github.com/YOUR_REPO/oddiya.git
cd oddiya
```

### Step 2: 환경 변수 설정

```bash
# .env 파일 생성
cat > .env << 'ENVEOF'
# Google Gemini API Key (필수)
GOOGLE_API_KEY=your_gemini_api_key_here

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# Google OAuth (선택)
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
ENVEOF
```

### Step 3: 서비스 시작

```bash
# Docker Compose로 모든 서비스 시작
docker-compose up -d

# 서비스 확인
docker-compose ps
```

**예상 결과:**
```
NAME                   STATUS    PORTS
oddiya-nginx           Up        0.0.0.0:80->80/tcp
oddiya-api-gateway     Up        0.0.0.0:8080->8080/tcp
oddiya-plan-service    Up        0.0.0.0:8083->8083/tcp
oddiya-llm-agent       Up        0.0.0.0:8000->8000/tcp
oddiya-redis           Up        0.0.0.0:6379->6379/tcp
```

### Step 4: 접속 확인

```bash
# 웹 브라우저에서 접속
open http://localhost:8080

# API 테스트
curl http://localhost:8080/health
```

**✅ 로컬 개발 환경 완료!**

---

## 모바일 앱 빌드

### Step 1: Expo 패키지 설치

```bash
cd mobile

# Expo 설치
npm install expo

# EAS CLI 설치 (전역)
npm install -g eas-cli
```

### Step 2: Expo 로그인

```bash
# Expo 계정으로 로그인
eas login

# 이메일/비밀번호 입력
```

### Step 3: EAS Build 초기화

```bash
# EAS Build 설정
eas build:configure
```

**프롬프트 응답:**
- "Generate a new Android Keystore?" → **Yes**
- "Generate credentials for iOS?" → **Skip for now**

### Step 4: Android 빌드

```bash
# Android APK 빌드 (10-15분 소요)
eas build --platform android --profile production
```

**빌드 진행:**
1. 코드를 Expo 클라우드에 업로드
2. 클라우드에서 APK 빌드
3. 완료 후 이메일로 다운로드 링크 전송

**빌드 모니터링:**
- 웹: https://expo.dev/accounts/YOUR_USERNAME/projects/oddiya/builds
- CLI: `eas build:list`

### Step 5: APK 다운로드 및 테스트

```bash
# APK 다운로드
eas build:download --platform android

# Android 기기에 설치
adb install app-release.apk
```

### (선택) iOS 빌드

**요구사항:** Apple Developer 계정 ($99/년)

```bash
# iOS IPA 빌드
eas build --platform ios --profile production
```

### (추천) Android + iOS 동시 빌드

```bash
# 한 번에 빌드
eas build --platform all --profile production
```

**✅ 모바일 앱 빌드 완료!**

---

## AWS 배포

### Step 1: EC2 인스턴스 생성

1. **AWS Console** → EC2 → Launch Instance
2. **설정:**
   - Name: `oddiya-server`
   - AMI: Amazon Linux 2023
   - Instance Type: **t2.micro** (프리티어)
   - Storage: 8GB gp3
   - Key Pair: 새로 생성 또는 기존 선택

3. **Security Group:**
   | Type  | Port | Source    |
   |-------|------|-----------|
   | SSH   | 22   | My IP     |
   | HTTP  | 80   | 0.0.0.0/0 |
   | HTTPS | 443  | 0.0.0.0/0 |

### Step 2: EC2 환경 설정

```bash
# SSH 접속
ssh -i your-key.pem ec2-user@<EC2_PUBLIC_IP>

# 자동 설정 스크립트 실행
curl -o setup.sh \
  https://raw.githubusercontent.com/YOUR_REPO/main/scripts/aws/setup-ec2.sh
chmod +x setup.sh
./setup.sh
```

### Step 3: 프로젝트 배포

```bash
# 프로젝트 디렉토리로 이동
cd /opt/oddiya

# 저장소 클론
git clone https://github.com/YOUR_REPO/oddiya.git .

# 환경 변수 설정
cat > .env << 'ENVEOF'
GOOGLE_API_KEY=your_gemini_api_key_here
GEMINI_MODEL=gemini-2.0-flash-exp
REDIS_HOST=redis
REDIS_PORT=6379
ENVEOF

# Docker Compose로 서비스 시작
docker-compose build
docker-compose up -d
```

### Step 4: 배포 확인

```bash
# 서비스 상태 확인
docker-compose ps

# Public IP 확인
curl http://169.254.169.254/latest/meta-data/public-ipv4

# 브라우저에서 접속
# http://<EC2_PUBLIC_IP>
```

### Step 5: 모바일 앱 API 연결

```bash
# 모바일 프로젝트로 이동 (로컬)
cd /Users/wjs/cursor/oddiya/mobile

# API URL 업데이트
# src/constants/config.ts 파일 수정
export const CONFIG = {
  API_BASE_URL: 'http://<EC2_PUBLIC_IP>',  // EC2 IP로 변경
};

# 모바일 앱 재빌드
eas build --platform all
```

**✅ AWS 배포 완료!**

---

## 다음 단계

### 1. 도메인 연결 (선택)

```bash
# DNS 설정
Type: A
Name: @
Value: <EC2_PUBLIC_IP>

# SSL 인증서 (Let's Encrypt)
sudo certbot --nginx -d yourdomain.com
```

### 2. 스토어 배포

#### Google Play Store

1. **Play Console:** https://play.google.com/console
2. **계정 등록:** $25 (일회성)
3. **앱 생성** → AAB 업로드
4. **심사 제출** (1-7일 소요)

```bash
# AAB 빌드
eas build --platform android --profile production

# 자동 제출
eas submit --platform android --latest
```

#### Apple App Store

1. **Apple Developer:** https://developer.apple.com ($99/년)
2. **App Store Connect** 접속
3. **앱 생성** → IPA 업로드
4. **심사 제출** (1-7일 소요)

```bash
# IPA 빌드
eas build --platform ios --profile production

# 자동 제출
eas submit --platform ios --latest
```

### 3. 모니터링 설정

```bash
# 로그 확인
docker-compose logs -f

# 서비스 재시작
docker-compose restart llm-agent
```

---

## 문제 해결

### 로컬 서비스가 시작되지 않음

```bash
# 로그 확인
docker-compose logs

# 서비스 재시작
docker-compose down
docker-compose up -d
```

### 모바일 빌드 실패

```bash
# EAS 로그 확인
eas build:list
# 실패한 빌드 클릭하여 에러 확인

# 재로그인
eas logout
eas login
```

### AWS 배포 후 접속 안됨

```bash
# Security Group 확인 (Port 80 오픈되었는지)
# 서비스 상태 확인
docker-compose ps

# 로그 확인
docker-compose logs nginx
```

---

## 요약

### 전체 소요 시간

```
로컬 개발 설정:  5분
모바일 앱 빌드:  15분
AWS 배포:        20분
─────────────────────
총:              40분
```

### 총 비용

```
첫 12개월 (프리티어):
- AWS EC2:      $0
- Gemini API:   $0
- Expo Build:   $0 (30회/월)
─────────────────────
합계:           $0/월

스토어 배포 (선택):
- Google Play:  $25 (일회성)
- Apple Store:  $99/년
```

### 핵심 명령어

```bash
# 로컬 개발
docker-compose up -d

# 모바일 빌드
eas build --platform all

# AWS 배포
docker-compose up -d  # EC2에서
```

---

## 추가 문서

### 배포
- [AWS EC2 상세 가이드](deployment/AWS_EC2_SETUP.md)
- [모바일 빌드 상세 가이드](deployment/MOBILE_BUILD.md)
- [배포 완전 가이드](deployment/DEPLOYMENT_GUIDE.md)

### 개발
- [로컬 개발 환경](development/LOCAL_DEVELOPMENT.md)
- [환경 변수 가이드](development/ENVIRONMENT_VARS.md)
- [API 문서](development/API_DOCUMENTATION.md)

### 아키텍처
- [시스템 아키텍처](architecture/SYSTEM_OVERVIEW.md)
- [AI 플래닝 워크플로우](architecture/AI_PLANNING_FLOW.md)

---

**처음 시작한다면 이 문서만 따라하세요!** ⭐

**문의:** [GitHub Issues](https://github.com/YOUR_REPO/oddiya/issues)
