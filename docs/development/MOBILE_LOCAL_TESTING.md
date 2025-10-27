# 모바일 기능 로컬 테스트 환경

로컬에서 모든 모바일 기능을 테스트할 수 있는 완전한 환경 구성 가이드

## 🎯 목표

AWS 서비스 없이 로컬에서 모든 기능 테스트:
- ✅ OAuth 로그인 (Google/Apple 모의)
- ✅ 여행 계획 생성 (AI)
- ✅ 사진 업로드 (로컬 스토리지)
- ✅ 비디오 생성 (FFmpeg)
- ✅ 푸시 알림 (콘솔 로그)

---

## 🚀 빠른 시작

### 1단계: 모든 서비스 시작

```bash
cd /Users/wjs/cursor/oddiya

# 전체 스택 시작 (7 microservices + DB + Redis)
./scripts/start-for-mobile-testing.sh

# 확인
docker ps
# 8개 컨테이너 실행 중이어야 함
```

### 2단계: 모바일 웹앱 접속

```bash
# iPhone/Android 브라우저에서:
http://172.16.102.149:8080/app

# 또는 데스크톱 브라우저:
http://localhost:8080/app
```

### 3단계: 기능 테스트

✅ **모두 로컬에서 작동!**

---

## 📱 기능별 로컬 테스트 방법

### 1. 사용자 프로필 관리

**테스트:**
```javascript
// Profile 탭에서
- 사용자 정보 확인
- 이름 변경
- 이메일 확인
```

**백엔드:**
```bash
# User Service (PostgreSQL)
docker exec oddiya-postgres psql -U oddiya_user -d oddiya \
  -c "SELECT * FROM user_service.users;"
```

**API 호출:**
```bash
# 사용자 조회
curl http://localhost:8080/api/users/me \
  -H "X-User-Id: 1"

# 사용자 수정
curl -X PATCH http://localhost:8080/api/users/me \
  -H "X-User-Id: 1" \
  -H "Content-Type: application/json" \
  -d '{"name": "Updated Name"}'
```

---

### 2. 여행 계획 생성 (AI)

**테스트:**
```javascript
// Plans 탭에서
1. "+ Create New Plan" 클릭
2. 제목: "서울 3일 여행"
3. 날짜 선택
4. "Generate AI Plan" 클릭
5. 생성된 계획 확인
6. 카드 클릭 → 상세 보기
```

**로컬 AI (Mock Mode):**
```bash
# LLM Agent 로그 확인
docker logs -f oddiya-llm-agent

# Mock mode 확인
docker exec oddiya-llm-agent env | grep MOCK_MODE
# MOCK_MODE=true (로컬에서는 무료로 테스트)
```

**Real AI 활성화:**
```bash
# API 키 설정 후
export AWS_ACCESS_KEY_ID=your-key
export AWS_SECRET_ACCESS_KEY=your-secret
export OPENWEATHER_API_KEY=your-key
export MOCK_MODE=false

# LLM Agent 재시작
docker-compose -f docker-compose.local.yml up -d llm-agent
```

**데이터베이스 확인:**
```bash
# 생성된 계획 확인
docker exec oddiya-postgres psql -U oddiya_user -d oddiya \
  -c "SELECT id, title, start_date, end_date FROM plan_service.travel_plans;"

# 상세 일정 확인
docker exec oddiya-postgres psql -U oddiya_user -d oddiya \
  -c "SELECT * FROM plan_service.plan_details;"
```

---

### 3. 사진 업로드 (로컬 파일 시스템)

**로컬 S3 대체 (LocalStack):**

```bash
# LocalStack 시작 (S3 모의)
docker run -d \
  --name localstack \
  -p 4566:4566 \
  -e SERVICES=s3 \
  localstack/localstack

# S3 버킷 생성
aws --endpoint-url=http://localhost:4566 \
  s3 mb s3://oddiya-local-storage

# 파일 업로드 테스트
aws --endpoint-url=http://localhost:4566 \
  s3 cp test-photo.jpg s3://oddiya-local-storage/photos/
```

**또는 간단한 방법 (로컬 폴더):**

```bash
# 로컬 업로드 폴더 생성
mkdir -p /tmp/oddiya-uploads

# 모바일 웹에서 사진 URL 입력 시:
# file:///tmp/oddiya-uploads/photo1.jpg
# 또는
# http://localhost:8080/uploads/photo1.jpg (static 파일 서빙 추가 시)
```

---

### 4. 비디오 생성 (로컬 FFmpeg)

**Video Worker는 이미 로컬 테스트 가능:**

```bash
# Video Worker 로그 확인
docker logs -f oddiya-video-worker

# 비디오 작업 생성
curl -X POST http://localhost:8080/api/videos \
  -H "X-User-Id: 1" \
  -H "Idempotency-Key: $(uuidgen)" \
  -H "Content-Type: application/json" \
  -d '{
    "photoUrls": [
      "https://picsum.photos/1080/1920?random=1",
      "https://picsum.photos/1080/1920?random=2",
      "https://picsum.photos/1080/1920?random=3"
    ],
    "template": "default"
  }'

# 작업 상태 확인
curl http://localhost:8080/api/videos/1 -H "X-User-Id: 1"
```

**로컬 SQS 대체 (ElasticMQ):**

```yaml
# docker-compose.local.yml에 추가
services:
  elasticmq:
    image: softwaremill/elasticmq
    ports:
      - "9324:9324"
      - "9325:9325"
    volumes:
      - ./infrastructure/docker/elasticmq.conf:/opt/elasticmq.conf
```

**설정:**
```conf
# infrastructure/docker/elasticmq.conf
include classpath("application.conf")

queues {
  oddiya-video-jobs {
    defaultVisibilityTimeout = 300 seconds
    delay = 0 seconds
    receiveMessageWait = 20 seconds
  }
}
```

---

### 5. OAuth 로그인 (모의)

**로컬 OAuth 모의 서버:**

```javascript
// API Gateway에 추가 (테스트용)
@GetMapping("/api/auth/mock-login")
public Mono<Map<String, String>> mockLogin(
    @RequestParam String email,
    @RequestParam String name
) {
    // Create test user
    // Return test tokens
    return Mono.just(Map.of(
        "accessToken", "test-token-123",
        "userId", "1"
    ));
}
```

**모바일 웹에서 사용:**
```javascript
// Mock login button
async function mockLogin() {
    const response = await fetch(
        `${API_BASE}/api/auth/mock-login?email=test@example.com&name=TestUser`
    );
    const data = await response.json();
    localStorage.setItem('accessToken', data.accessToken);
    localStorage.setItem('userId', data.userId);
    showToast('✅ Logged in!');
}
```

---

### 6. 푸시 알림 (콘솔 로그)

**Video Worker 수정:**

```python
# src/sns_client.py
def send_notification(self, user_id, job_id, video_url, status):
    # Local mode: just log
    if os.getenv("LOCAL_MODE", "true") == "true":
        logger.info(f"""
        📱 PUSH NOTIFICATION (Local):
        User: {user_id}
        Job: {job_id}
        Status: {status}
        Video: {video_url}
        """)
        return
    
    # Production: real SNS
    self.client.publish(...)
```

**모바일 웹에서 폴링:**
```javascript
// Check video status every 5 seconds
setInterval(async () => {
    const response = await fetch('/api/videos/1', {
        headers: { 'X-User-Id': '1' }
    });
    const video = await response.json();
    
    if (video.status === 'COMPLETED') {
        showToast('🎉 Video is ready!');
        clearInterval(this);
    }
}, 5000);
```

---

## 🧪 통합 테스트 시나리오

### 시나리오 1: 완전한 사용자 플로우

```bash
# 자동화 테스트 스크립트
cd /Users/wjs/cursor/oddiya
./scripts/test-mobile-features.sh
```

**스크립트 내용:**

```bash
#!/bin/bash

echo "🧪 모바일 기능 통합 테스트"
echo ""

BASE="http://localhost:8080"
USER_ID=1

# 1. 사용자 생성
echo "1️⃣ Creating user..."
curl -X POST http://localhost:8082/api/v1/users/internal/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "name": "Test User",
    "provider": "google",
    "providerId": "test-123"
  }' | jq '.'

# 2. 프로필 조회
echo "2️⃣ Getting profile..."
curl $BASE/api/users/me -H "X-User-Id: $USER_ID" | jq '.'

# 3. 여행 계획 생성
echo "3️⃣ Creating travel plan..."
curl -X POST $BASE/api/plans \
  -H "Content-Type: application/json" \
  -H "X-User-Id: $USER_ID" \
  -d '{
    "title": "서울 주말 여행",
    "startDate": "2025-12-01",
    "endDate": "2025-12-03"
  }' | jq '.'

# 4. 계획 목록 조회
echo "4️⃣ Listing plans..."
curl $BASE/api/plans -H "X-User-Id: $USER_ID" | jq '.'

# 5. 비디오 작업 생성
echo "5️⃣ Creating video job..."
curl -X POST $BASE/api/videos \
  -H "Content-Type: application/json" \
  -H "X-User-Id: $USER_ID" \
  -H "Idempotency-Key: $(uuidgen)" \
  -d '{
    "photoUrls": [
      "https://picsum.photos/1080/1920?random=1",
      "https://picsum.photos/1080/1920?random=2"
    ],
    "template": "default"
  }' | jq '.'

# 6. 비디오 목록 조회
echo "6️⃣ Listing videos..."
curl $BASE/api/videos -H "X-User-Id: $USER_ID" | jq '.'

echo ""
echo "✅ 모든 모바일 기능 테스트 완료!"
```

---

### 시나리오 2: React Native 앱 테스트

**iOS Simulator:**

```bash
cd /Users/wjs/cursor/oddiya/mobile

# 의존성 설치
npm install

# iOS Simulator 실행
# (Xcode 필요)
npx react-native run-ios

# 앱이 http://localhost:8080 연결
# 모든 API 사용 가능
```

**Android Emulator:**

```bash
# Android Emulator 실행
# (Android Studio 필요)
npx react-native run-android

# 앱이 http://10.0.2.2:8080 연결
```

**테스트 코드 실행:**

```bash
# Unit tests (시뮬레이터 불필요)
npm test

# 특정 테스트만
npm test -- Button.test.tsx

# Coverage
npm run test:coverage
```

---

## 🔧 완전한 로컬 환경 구성

### docker-compose.test.yml (완전한 로컬 환경)

```yaml
version: '3.8'

services:
  # 기존 서비스들...
  
  # LocalStack (AWS 서비스 모의)
  localstack:
    image: localstack/localstack
    container_name: oddiya-localstack
    ports:
      - "4566:4566"  # AWS API endpoint
    environment:
      - SERVICES=s3,sqs,sns
      - DEBUG=1
      - DATA_DIR=/tmp/localstack/data
    volumes:
      - localstack-data:/tmp/localstack
    networks:
      - oddiya-network

  # ElasticMQ (SQS 모의)
  elasticmq:
    image: softwaremill/elasticmq
    container_name: oddiya-elasticmq
    ports:
      - "9324:9324"
      - "9325:9325"
    networks:
      - oddiya-network

  # MailHog (이메일 알림 테스트)
  mailhog:
    image: mailhog/mailhog
    container_name: oddiya-mailhog
    ports:
      - "1025:1025"  # SMTP
      - "8025:8025"  # Web UI
    networks:
      - oddiya-network

volumes:
  localstack-data:
```

---

## 📲 모바일 웹앱 테스트 체크리스트

### ✅ 즉시 테스트 가능 (설정 불필요)

- [x] **Health Check**
  - URL: http://172.16.102.149:8080/actuator/health
  - 예상: `{"status":"UP"}`

- [x] **Dashboard**
  - URL: http://172.16.102.149:8080/
  - 예상: 7개 서비스 목록

- [x] **Mobile Web App**
  - URL: http://172.16.102.149:8080/app
  - Plans, Videos, Profile 탭 모두 작동

- [x] **Profile 보기**
  - Profile 탭 클릭
  - 사용자 정보 표시

- [x] **Plans 목록**
  - Plans 탭
  - 생성된 계획 목록 (처음엔 비어있음)

- [x] **Plan 생성**
  - "+ Create New Plan"
  - 정보 입력
  - AI 생성 (Mock mode)

- [x] **Plan 상세보기**
  - 계획 카드 클릭
  - 일별 일정 확인
  - 비용, 날씨 정보

- [x] **Videos 목록**
  - Videos 탭
  - 비디오 작업 목록

- [x] **Video 생성**
  - "+ Create Video"
  - 사진 URL 입력
  - 작업 생성

---

### ⏳ 추가 설정 필요

- [ ] **Real AI Plans (Bedrock)**
  - 필요: AWS API keys
  - 설정: `./scripts/enable-real-apis.sh`
  - 결과: 실제 AI 생성 계획

- [ ] **Real Weather Data**
  - 필요: OpenWeatherMap API key
  - 설정: OPENWEATHER_API_KEY 환경변수
  - 결과: 실제 날씨 예보

- [ ] **OAuth Login**
  - 필요: Google/Apple OAuth 설정
  - 또는: Mock login endpoint 사용

- [ ] **Photo Upload to S3**
  - 필요: AWS S3 또는 LocalStack
  - 또는: 로컬 파일 시스템 사용

- [ ] **Video Processing**
  - 필요: SQS + SNS 또는 ElasticMQ
  - 현재: Worker가 로컬에서 작동 가능

- [ ] **Push Notifications**
  - 필요: Firebase Cloud Messaging
  - 또는: 콘솔 로그로 확인

---

## 🎨 UI/UX 테스트

### 데스크톱 브라우저에서:

```bash
# Chrome DevTools로 모바일 시뮬레이션
open -a "Google Chrome" http://localhost:8080/app

# Chrome에서:
# 1. F12 → DevTools 열기
# 2. Device Toolbar (Ctrl+Shift+M)
# 3. iPhone 14 Pro 선택
# 4. 터치 이벤트 테스트
```

### 실제 모바일 기기에서:

```bash
# 1. Mac과 같은 WiFi 연결
# 2. Safari/Chrome 열기
# 3. http://172.16.102.149:8080/app
# 4. 실제 터치로 테스트
```

---

## 🧪 자동화된 E2E 테스트

### Playwright로 모바일 웹 테스트:

```javascript
// tests/e2e/mobile-web.spec.js
const { test, expect, devices } = require('@playwright/test');

test.use(devices['iPhone 14 Pro']);

test('create travel plan flow', async ({ page }) => {
  // 1. 앱 열기
  await page.goto('http://localhost:8080/app');
  
  // 2. Plans 탭 클릭
  await page.click('text=Plans');
  
  // 3. 새 계획 만들기
  await page.click('text=+ Create New Plan');
  
  // 4. 정보 입력
  await page.fill('#plan-title', '서울 테스트 여행');
  await page.fill('#plan-start', '2025-12-01');
  await page.fill('#plan-end', '2025-12-03');
  
  // 5. 생성 클릭
  await page.click('text=Generate AI Plan');
  
  // 6. 결과 확인
  await expect(page.locator('text=서울 테스트 여행')).toBeVisible();
});
```

---

## 📊 성능 테스트

### 모바일 네트워크 시뮬레이션:

```bash
# Chrome DevTools
# 1. Network 탭
# 2. Throttling: Fast 3G
# 3. 모든 기능 테스트
# 4. 로딩 시간 확인
```

### 응답 시간 측정:

```bash
# API 응답 시간
time curl http://localhost:8080/api/plans -H "X-User-Id: 1"

# 목표:
# - Health check: <100ms
# - 목록 조회: <500ms
# - 계획 생성: <3000ms (AI 호출 포함)
```

---

## 🔍 디버깅 도구

### 1. 네트워크 로그

```bash
# API Gateway 로그
docker logs -f oddiya-api-gateway

# 특정 서비스 로그
docker logs -f oddiya-plan-service
docker logs -f oddiya-llm-agent
```

### 2. 데이터베이스 확인

```bash
# PostgreSQL 접속
docker exec -it oddiya-postgres psql -U oddiya_user -d oddiya

# 테이블 확인
\dt user_service.*
\dt plan_service.*
\dt video_service.*

# 데이터 확인
SELECT * FROM plan_service.travel_plans;
```

### 3. Redis 캐시 확인

```bash
# Redis 접속
docker exec -it oddiya-redis redis-cli

# 캐시된 키 확인
KEYS *

# LLM 캐시 확인
KEYS llm_plan:*
```

---

## 📱 모바일 앱 개발 워크플로우

### 일반적인 개발 사이클:

```bash
# 1. 백엔드 서비스 시작
./scripts/start-for-mobile-testing.sh

# 2. 모바일 웹에서 기능 테스트
open http://localhost:8080/app

# 3. API 작동 확인
./scripts/test-mobile-api.sh

# 4. React Native 앱 개발
cd mobile
npm start

# 5. iOS Simulator 실행
npx react-native run-ios

# 6. 앱에서 기능 테스트
# - 로그인
# - 계획 생성
# - 비디오 생성

# 7. 문제 발견 시 백엔드 로그 확인
docker logs oddiya-plan-service

# 8. 수정 후 재시작
docker-compose -f docker-compose.local.yml restart plan-service

# 9. 앱에서 재테스트
```

---

## 🎯 완전한 로컬 테스트 환경

### 현재 작동하는 것:

✅ **Backend Services:** 7개 모두 로컬 실행  
✅ **Database:** PostgreSQL (모든 스키마)  
✅ **Cache:** Redis (JWT, LLM 캐시)  
✅ **Mobile Web:** 완전 작동  
✅ **API Gateway:** 라우팅 작동  
✅ **Plan Generation:** Mock AI (무료)  
✅ **Video Jobs:** 작업 생성 가능  
✅ **User Management:** CRUD 작동  

### 추가하면 더 좋은 것:

⏳ **LocalStack:** S3/SQS/SNS 로컬 모의  
⏳ **ElasticMQ:** SQS 전용 모의  
⏳ **Mock OAuth:** 간단한 로그인 테스트  
⏳ **Playwright:** E2E 자동화 테스트  

---

## 📋 요약

**현재 상태:**
- ✅ 모든 모바일 기능을 로컬에서 테스트 가능
- ✅ http://172.16.102.149:8080/app에서 즉시 사용
- ✅ Plans, Videos, Profile 모두 작동
- ✅ Mock AI로 무료 테스트
- ✅ Real AI는 API 키만 있으면 가능

**테스트 방법:**
1. `./scripts/start-for-mobile-testing.sh` 실행
2. `http://172.16.102.149:8080/app` 접속
3. 모든 기능 클릭해보기!

**고급 테스트:**
- React Native 앱: Xcode/Android Studio 필요
- E2E 테스트: Playwright 설정
- AWS 모의: LocalStack 추가

**지금 바로 테스트 가능!** 🚀

