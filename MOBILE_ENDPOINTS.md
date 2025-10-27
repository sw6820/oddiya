# 모바일 앱 엔드포인트 가이드

## ⚠️ 중요: 루트 경로는 404입니다

`http://172.16.102.149:8080/` 으로 접속하면 404 에러가 나타납니다.
이것은 **정상**입니다. API 엔드포인트를 사용해야 합니다.

## ✅ 올바른 엔드포인트

### Base URL
```
http://172.16.102.149:8080
```

### 1. Health Check (헬스 체크)

```
GET http://172.16.102.149:8080/actuator/health
```

**응답:**
```json
{"status":"UP"}
```

**모바일 브라우저에서 이 URL을 테스트하세요!**

### 2. 사용자 프로필

**조회:**
```
GET http://172.16.102.149:8080/api/users/me
Headers:
  X-User-Id: 1
```

**수정:**
```
PATCH http://172.16.102.149:8080/api/users/me
Headers:
  X-User-Id: 1
  Content-Type: application/json
Body:
{
  "name": "새로운 이름"
}
```

### 3. 여행 계획

**목록 조회:**
```
GET http://172.16.102.149:8080/api/plans
Headers:
  X-User-Id: 1
```

**생성:**
```
POST http://172.16.102.149:8080/api/plans
Headers:
  X-User-Id: 1
  Content-Type: application/json
Body:
{
  "title": "서울 주말 여행",
  "startDate": "2025-12-01",
  "endDate": "2025-12-03"
}
```

**상세 조회:**
```
GET http://172.16.102.149:8080/api/plans/1
Headers:
  X-User-Id: 1
```

### 4. 비디오

**목록 조회:**
```
GET http://172.16.102.149:8080/api/videos
Headers:
  X-User-Id: 1
```

**생성:**
```
POST http://172.16.102.149:8080/api/videos
Headers:
  X-User-Id: 1
  Idempotency-Key: 550e8400-e29b-41d4-a716-446655440000
  Content-Type: application/json
Body:
{
  "photoUrls": [
    "https://example.com/photo1.jpg",
    "https://example.com/photo2.jpg"
  ],
  "template": "default"
}
```

## 모바일 브라우저에서 테스트

### iPhone Safari에서:

1. Safari 열기
2. 주소창에 입력:
```
http://172.16.102.149:8080/actuator/health
```
3. 결과 확인: `{"status":"UP"}`

### 주의사항

❌ **잘못된 URL (404 에러 발생):**
- `http://172.16.102.149:8080/` (루트)
- `http://172.16.102.149:8080/index.html`

✅ **올바른 URL:**
- `http://172.16.102.149:8080/actuator/health`
- `http://172.16.102.149:8080/api/users/me`
- `http://172.16.102.149:8080/api/plans`

## 모바일 앱 개발 시

### iOS (Swift)
```swift
let baseURL = "http://172.16.102.149:8080"

// Health check
let healthURL = "\(baseURL)/actuator/health"

// Get user profile
let userURL = "\(baseURL)/api/users/me"
```

### Android (Kotlin)
```kotlin
const val BASE_URL = "http://172.16.102.149:8080"

// Health check
val healthURL = "$BASE_URL/actuator/health"

// Get plans
val plansURL = "$BASE_URL/api/plans"
```

### React Native
```javascript
const BASE_URL = 'http://172.16.102.149:8080';

// Fetch user profile
fetch(`${BASE_URL}/api/users/me`, {
  headers: {
    'X-User-Id': '1'
  }
})
```

## 빠른 테스트 스크립트

```bash
# 모든 엔드포인트 테스트
./scripts/test-mobile-api.sh
```

이 스크립트가 모든 API를 자동으로 테스트합니다.

## 문제 해결

### 404 에러가 계속 나타나면

1. 올바른 경로를 사용하는지 확인
2. `/actuator/health` 로 시작하여 테스트
3. 서비스가 실행 중인지 확인:
```bash
docker ps
```

### 연결 거부 에러가 나타나면

1. 서비스가 시작되었는지 확인
2. 같은 WiFi 네트워크인지 확인
3. 방화벽 설정 확인

---

**완전한 API 문서:** `docs/api/MOBILE_API_TESTING.md`

