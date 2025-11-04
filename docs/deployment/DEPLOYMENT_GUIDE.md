# Oddiya - 배포 완전 가이드

> AWS + Android + iOS 배포의 모든 것

## 📋 목차

1. [배포 개요](#배포-개요)
2. [AWS 배포 (백엔드)](#aws-배포)
3. [모바일 배포 (Android & iOS)](#모바일-배포)
4. [비용 분석](#비용-분석)
5. [운영 및 모니터링](#운영-및-모니터링)

---

## 배포 개요

### 아키텍처

```
[Mobile Apps]
  Android & iOS
       ↓
[Internet]
       ↓
[AWS EC2 t2.micro] $0 (프리티어 12개월)
  ├─ Nginx (Port 80)
  ├─ API Gateway (8080)
  ├─ Plan Service (8083)
  ├─ LLM Agent (8000) → Gemini API (무료)
  └─ Redis (6379)
```

### 비용 요약

| 항목 | 월 비용 | 연 비용 |
|------|---------|---------|
| AWS EC2 (첫 12개월) | $0 | $0 |
| Gemini API | $0 | $0 |
| Expo EAS Build | $0 | $0 |
| **합계** | **$0** | **$0** |

**12개월 후:** ~$10/월 (EC2만)

**스토어 배포:**
- Google Play: $25 (일회성)
- Apple Store: $99/년 (iOS용)

---

## AWS 배포

### EC2 인스턴스 생성

#### 1. AWS Console 접속

https://console.aws.amazon.com → EC2 → Launch Instance

#### 2. 인스턴스 설정

**Name and tags:**
```
Name: oddiya-production
Environment: production
```

**Application and OS Images:**
- AMI: **Amazon Linux 2023** (Free tier eligible)
- Architecture: 64-bit (x86)

**Instance type:**
- **t2.micro** (1 vCPU, 1GB RAM)
- ✅ Free tier eligible

**Key pair:**
- Create new key pair: `oddiya-key`
- Key pair type: RSA
- Private key file format: .pem
- **Download and save** `oddiya-key.pem`

**Network settings:**
- VPC: Default
- Subnet: No preference
- Auto-assign public IP: **Enable**

**Security Group:**

Create new security group:

```
Security group name: oddiya-sg
Description: Oddiya production security group

Inbound rules:
┌──────┬──────┬────────────┬──────────────────────┐
│ Type │ Port │ Source     │ Description          │
├──────┼──────┼────────────┼──────────────────────┤
│ SSH  │ 22   │ My IP      │ SSH access           │
│ HTTP │ 80   │ 0.0.0.0/0  │ Web traffic          │
│ HTTPS│ 443  │ 0.0.0.0/0  │ Secure web traffic   │
└──────┴──────┴────────────┴──────────────────────┘
```

**Storage:**
- 1 x 8 GB gp3 (Free tier: up to 30GB)

**Advanced details:**
- IAM instance profile: None (선택사항)
- User data: (비워둠)

#### 3. Launch Instance

**Launch** 클릭 → 인스턴스 생성 완료 (1-2분 소요)

### EC2 접속 및 환경 설정

#### 1. SSH 접속

```bash
# 키 파일 권한 설정
chmod 400 oddiya-key.pem

# EC2 접속
ssh -i oddiya-key.pem ec2-user@<EC2_PUBLIC_IP>
```

Public IP 확인: EC2 Console → Instances → 선택한 인스턴스 → Public IPv4 address

#### 2. 자동 설정 스크립트 실행

```bash
# Docker & Docker Compose 설치
sudo yum update -y
sudo yum install -y docker git
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker $USER

# Docker Compose 설치
sudo curl -L \
  "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" \
  -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# 재로그인 (Docker 권한 적용)
exit
ssh -i oddiya-key.pem ec2-user@<EC2_PUBLIC_IP>

# 확인
docker --version
docker-compose --version
```

#### 3. 프로젝트 배포

```bash
# 프로젝트 디렉토리 생성
sudo mkdir -p /opt/oddiya
sudo chown $USER:$USER /opt/oddiya
cd /opt/oddiya

# 저장소 클론
git clone https://github.com/YOUR_REPO/oddiya.git .

# 환경 변수 설정
cat > .env << 'ENVEOF'
# Google Gemini API Key
GOOGLE_API_KEY=your_gemini_api_key_here
GEMINI_MODEL=gemini-2.0-flash-exp

# Redis
REDIS_HOST=redis
REDIS_PORT=6379
ENVEOF

# Docker Compose 빌드 및 시작
docker-compose build
docker-compose up -d

# 서비스 상태 확인
docker-compose ps
```

#### 4. 배포 확인

```bash
# 서비스 상태 확인
docker-compose ps

# 로그 확인
docker-compose logs -f

# 헬스 체크
curl http://localhost/health

# Public IP 확인
curl http://169.254.169.254/latest/meta-data/public-ipv4
```

**브라우저에서 접속:**
```
http://<EC2_PUBLIC_IP>
```

### (선택) 도메인 연결 및 SSL

#### 1. 도메인 DNS 설정

DNS 제공자 (Cloudflare, Namecheap 등)에서:

```
Type: A
Name: @
Value: <EC2_PUBLIC_IP>
TTL: 3600
```

#### 2. SSL 인증서 (Let's Encrypt)

```bash
# Certbot 설치
sudo yum install -y certbot python3-certbot-nginx

# SSL 인증서 발급
sudo certbot --nginx -d yourdomain.com -d www.yourdomain.com

# 자동 갱신 설정
sudo systemctl enable certbot-renew.timer
```

---

## 모바일 배포

### Expo를 사용한 Android & iOS 동시 빌드

#### 전제 조건

- Node.js 18+
- Expo 계정 (무료: https://expo.dev/signup)
- (iOS용) Apple Developer 계정 ($99/년)

#### 1. 환경 준비

```bash
cd /Users/wjs/cursor/oddiya/mobile

# Expo 패키지 설치
npm install expo

# EAS CLI 설치
npm install -g eas-cli

# Expo 로그인
eas login
```

#### 2. API URL 설정

`src/constants/config.ts` 파일 수정:

```typescript
export const CONFIG = {
  API_BASE_URL: __DEV__
    ? 'http://localhost:8080'      // 개발
    : 'http://<EC2_PUBLIC_IP>',    // 프로덕션
  
  GOOGLE_WEB_CLIENT_ID: 'your-client-id.apps.googleusercontent.com',
};
```

#### 3. EAS Build 초기화

```bash
# EAS Build 설정
eas build:configure
```

**프롬프트:**
- Generate Android Keystore? → **Yes**
- Generate iOS credentials? → **Skip** (또는 Yes if Apple Developer 계정 있음)

이 명령은 `eas.json` 파일을 자동 생성합니다.

#### 4. Android 빌드

```bash
# Android APK 빌드
eas build --platform android --profile production
```

**빌드 프로세스:**
1. 코드를 Expo 클라우드에 업로드
2. 클라우드에서 APK 빌드 (10-15분)
3. 완료 후 이메일로 다운로드 링크 전송

**빌드 모니터링:**
```bash
# 빌드 목록 확인
eas build:list

# 특정 빌드 상세 정보
eas build:view <build-id>
```

**다운로드:**
```bash
# APK 다운로드
eas build:download --platform android

# Android 기기에 설치
adb install app-release.apk
```

#### 5. iOS 빌드 (선택)

**요구사항:** Apple Developer 계정 ($99/년)

```bash
# iOS IPA 빌드
eas build --platform ios --profile production
```

**Apple ID 입력:**
- EAS가 자동으로 인증서 및 프로비저닝 프로파일 생성
- 계정 정보 입력 후 자동 처리

**다운로드:**
```bash
# IPA 다운로드
eas build:download --platform ios

# TestFlight로 테스트
eas submit --platform ios --latest
```

#### 6. Android + iOS 동시 빌드 (추천)

```bash
# 한 번에 빌드
eas build --platform all --profile production
```

**결과:**
- Android APK: ~15분 후 완료
- iOS IPA: ~15분 후 완료
- 두 파일 모두 이메일로 다운로드 링크 전송

### 스토어 배포

#### Google Play Store

**1. Play Console 준비**

- 등록: https://play.google.com/console
- 비용: $25 (일회성)
- 계정 인증 (1-2일 소요)

**2. AAB 빌드 (Play Store용)**

```bash
# AAB는 APK보다 15-20% 작음
eas build --platform android --profile production
```

**3. Play Console에 업로드**

1. Play Console → Create app
2. App details 입력
3. Store listing 작성
4. Release → Production → Create release
5. AAB 업로드
6. Review 제출 (1-7일 소요)

**자동 제출 (CLI):**
```bash
eas submit --platform android --latest
```

#### Apple App Store

**1. Apple Developer 계정**

- 등록: https://developer.apple.com
- 비용: $99/년
- 계정 활성화

**2. IPA 빌드**

```bash
eas build --platform ios --profile production
```

**3. App Store Connect 제출**

```bash
# 자동 제출
eas submit --platform ios --latest
```

**4. TestFlight 테스트 (선택)**

- App Store Connect → TestFlight
- 내부 테스터 초대
- 즉시 테스트 가능

**5. App Store 심사**

- App Store Connect → App Store
- 앱 정보 입력
- 스크린샷 업로드
- Submit for Review
- 심사 대기 (1-7일)

---

## 비용 분석

### AWS 비용 (프리티어 12개월)

| 항목 | 사양 | 월 비용 | 프리티어 후 |
|------|------|---------|------------|
| EC2 t2.micro | 1GB RAM, 1 vCPU | $0 | $8.50 |
| EBS 8GB | gp3 SSD | $0 | $0.80 |
| 데이터 전송 | 15GB/월 | $0 | $1.40 |
| **합계** | | **$0** | **$10.70** |

### Gemini API 비용

| 티어 | 요청 | 월 비용 |
|------|------|---------|
| Free | 15 req/min | $0 |
| Free | ~40,000 req/월 | $0 |

**예상 사용량:** 1,000-5,000 req/월 → **무료**

### Expo EAS Build 비용

| 플랜 | 빌드 | 월 비용 |
|------|------|---------|
| Free | 30회/월 | $0 |
| Production | 무제한 | $29 |

**예상 사용량:** 10-20회/월 → **무료 티어 충분**

### 스토어 배포 비용

| 스토어 | 등록비 | 빈도 |
|--------|--------|------|
| Google Play | $25 | 일회성 |
| Apple App Store | $99 | 연간 |

### 총 비용 계산

**첫 12개월:**
```
AWS:           $0/월
Gemini:        $0/월
Expo:          $0/월
────────────────────
합계:          $0/월
```

**12개월 후:**
```
AWS:          $11/월
Gemini:        $0/월
Expo:          $0/월 (또는 $29/월)
────────────────────
합계:         $11/월 (또는 $40/월)
```

**스토어 배포 추가:**
```
Google Play:  $25 (일회성)
Apple Store:  $99/년
```

---

## 운영 및 모니터링

### 서비스 관리

```bash
# 서비스 상태 확인
docker-compose ps

# 로그 확인 (전체)
docker-compose logs -f

# 특정 서비스 로그
docker-compose logs -f llm-agent

# 서비스 재시작
docker-compose restart llm-agent

# 전체 재시작
docker-compose restart

# 서비스 중지
docker-compose down

# 서비스 재시작 (rebuild)
docker-compose up -d --build
```

### 업데이트 배포

```bash
# EC2에서
cd /opt/oddiya

# 최신 코드 가져오기
git pull

# 이미지 재빌드 및 재시작
docker-compose build
docker-compose up -d

# 서비스 확인
docker-compose ps
```

### 로그 모니터링

```bash
# 실시간 로그
docker-compose logs -f

# 최근 100줄
docker-compose logs --tail=100

# 특정 시간 이후 로그
docker-compose logs --since 1h

# 로그를 파일로 저장
docker-compose logs > logs.txt
```

### 백업

```bash
# Redis 데이터 백업
docker-compose exec redis redis-cli SAVE
docker cp oddiya-redis:/data/dump.rdb ./backup/

# 전체 볼륨 백업
docker-compose down
sudo tar -czf backup-$(date +%Y%m%d).tar.gz \
  /var/lib/docker/volumes/oddiya_redis_data
docker-compose up -d
```

### 성능 모니터링

```bash
# 컨테이너 리소스 사용량
docker stats

# 디스크 사용량
df -h

# 메모리 사용량
free -h

# CPU 사용량
top
```

### 문제 해결

#### 서비스가 시작되지 않음

```bash
# 로그 확인
docker-compose logs

# 특정 서비스 재시작
docker-compose restart <service-name>

# 컨테이너 재생성
docker-compose up -d --force-recreate
```

#### 메모리 부족 (t2.micro 1GB)

```bash
# Swap 추가
sudo dd if=/dev/zero of=/swapfile bs=1M count=1024
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
```

#### 포트 충돌

```bash
# 포트 사용 확인
sudo lsof -i :80

# 프로세스 종료
sudo kill -9 <PID>
```

#### Docker 디스크 정리

```bash
# 사용하지 않는 이미지 삭제
docker image prune -a

# 전체 정리
docker system prune -a
```

---

## 보안 설정

### SSH 보안

```bash
# SSH 키 기반 인증만 허용
sudo vi /etc/ssh/sshd_config
# PasswordAuthentication no

# SSH 재시작
sudo systemctl restart sshd
```

### 방화벽 설정

```bash
# firewalld 설치 (Amazon Linux)
sudo yum install -y firewalld
sudo systemctl start firewalld
sudo systemctl enable firewalld

# 필요한 포트만 오픈
sudo firewall-cmd --permanent --add-service=http
sudo firewall-cmd --permanent --add-service=https
sudo firewall-cmd --reload
```

### 자동 업데이트

```bash
# Amazon Linux 자동 보안 업데이트
sudo yum install -y yum-cron
sudo systemctl enable yum-cron
sudo systemctl start yum-cron
```

---

## 요약

### 배포 체크리스트

**AWS 배포:**
- [ ] EC2 t2.micro 인스턴스 생성
- [ ] Security Group 설정 (Port 22, 80, 443)
- [ ] Docker & Docker Compose 설치
- [ ] 프로젝트 클론
- [ ] .env 파일 생성
- [ ] docker-compose up -d 실행
- [ ] 브라우저에서 접속 확인

**모바일 배포:**
- [ ] Expo 계정 생성
- [ ] eas-cli 설치
- [ ] eas login
- [ ] eas build:configure
- [ ] API URL 업데이트 (config.ts)
- [ ] eas build --platform all
- [ ] APK/IPA 다운로드 및 테스트
- [ ] (선택) 스토어 제출

### 핵심 명령어

```bash
# AWS 배포
docker-compose up -d

# 모바일 빌드
eas build --platform all

# 업데이트
git pull && docker-compose up -d --build

# 모니터링
docker-compose logs -f

# 재시작
docker-compose restart
```

---

**마지막 업데이트:** 2025-11-03  
**버전:** 1.0.0

**문의:** [GitHub Issues](https://github.com/YOUR_REPO/oddiya/issues)
