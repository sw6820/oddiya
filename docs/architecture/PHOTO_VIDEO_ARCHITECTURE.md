# Photo & Video Architecture

사진 업로드 및 영상 생성 아키텍처

## 📸 사진 업로드 흐름

```
Mobile (Browser)
    ↓ 1. Select photos
    ↓ 2. Preview
    ↓ 3. Click "Upload"
API Gateway (http://localhost:8080/mobile)
    ↓ 4. POST /api/plans/{id}/photos
Plan Service (http://localhost:8083)
    ↓ 5. PhotoController.addPhoto()
    ↓ 6. PhotoService.addPhoto()
    ↓ 7. PlanPhotoRepository.save()
PostgreSQL
    ↓ 8. INSERT INTO plan_service.plan_photos
    ↓ 9. Return saved photo
Plan Service
    ↓ 10. Return PhotoResponse
API Gateway
    ↓ 11. Return to Mobile
Mobile
    ↓ 12. Show success alert
    ↓ 13. Reload plan detail
    ↓ 14. Fetch /api/plans/{id}/photos
API Gateway
    ↓ 15. Route to Plan Service
Plan Service
    ↓ 16. PhotoController.getPlanPhotos()
    ↓ 17. Return List<PlanPhoto>
Mobile
    ↓ 18. Display photo grid
```

---

## 🎬 영상 생성 흐름

```
Mobile
    ↓ 1. Click "Create Video"
    ↓ 2. Get plan photos
API Gateway
    ↓ 3. POST /api/plans/{id}/create-video
Plan Service
    ↓ 4. Get photo URLs from plan_photos
    ↓ 5. Call Video Service
    ↓ 6. POST /api/videos with plan_id
Video Service
    ↓ 7. Create VideoJob
    ↓ 8. SET plan_id = {plan_id}
    ↓ 9. Publish to SQS
SQS Queue
    ↓ 10. Message: {jobId, userId, photoUrls, planId}
Video Worker (Python)
    ↓ 11. Long-poll SQS
    ↓ 12. Download photos from URLs
    ↓ 13. Generate video with FFmpeg
    ↓ 14. Upload video to S3
    ↓ 15. UPDATE video_jobs SET status='COMPLETED', video_url='...'
    ↓ 16. Send SNS notification
Mobile
    ↓ 17. Poll /api/videos/{id} every 5 seconds
    ↓ 18. When status='COMPLETED', show alert
    ↓ 19. Display video player
```

---

## 📊 데이터베이스 스키마

### plan_service.plan_photos

```sql
CREATE TABLE plan_service.plan_photos (
    id BIGSERIAL PRIMARY KEY,
    plan_id BIGINT NOT NULL,           -- 어느 계획에 속하는지
    photo_url VARCHAR NOT NULL,         -- 사진 URL (S3 또는 임시)
    s3_key VARCHAR NOT NULL,            -- S3 키
    upload_order INT,                   -- 순서 (1, 2, 3...)
    uploaded_at TIMESTAMP DEFAULT NOW() -- 업로드 시간
);
```

**현재 상태:** ❌ 테이블 존재하지 않음  
**문제:** CREATE TABLE 명령이 실행되지 않음

### video_service.video_jobs

```sql
-- 기존 컬럼
id, user_id, status, photo_urls[], template, video_url, idempotency_key

-- 추가된 컬럼
ALTER TABLE video_service.video_jobs
ADD COLUMN plan_id BIGINT,              -- 연결된 계획 ID
ADD COLUMN template_name VARCHAR(50);    -- 템플릿 이름
```

---

## 🔧 현재 문제

### 1. plan_photos 테이블 없음

**원인:**
- Docker 컨테이너 재시작 시 schema 파일 실행 안됨
- `05-add-user-journey-columns.sql` 파일이 실행되지 않음

**해결:**
```sql
-- 수동으로 생성
docker exec -i oddiya-postgres psql -U oddiya_user -d oddiya <<EOF
CREATE TABLE plan_service.plan_photos (
    id BIGSERIAL PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    photo_url VARCHAR NOT NULL,
    s3_key VARCHAR NOT NULL,
    upload_order INT,
    uploaded_at TIMESTAMP DEFAULT NOW()
);
EOF
```

### 2. PhotoController 404

**원인:**
- API Gateway 라우팅에 photo 경로 없음

**해결:**
```yaml
# API Gateway application.yml에 추가
- id: plan-photos
  uri: http://plan-service:8083
  predicates:
    - Path=/api/plans/*/photos,/api/plans/*/photos/**
  filters:
    - RewritePath=/api/plans/(?<planId>[^/]+)/photos(?<segment>/?.*), /api/v1/plans/$\{planId}/photos$\{segment}
```

---

## 🛠️ 즉시 수정

### Step 1: 테이블 생성 (수동)

```bash
docker exec -i oddiya-postgres psql -U oddiya_user -d oddiya <<'SQL'
CREATE TABLE IF NOT EXISTS plan_service.plan_photos (
    id BIGSERIAL PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    photo_url VARCHAR NOT NULL,
    s3_key VARCHAR NOT NULL,
    upload_order INT,
    uploaded_at TIMESTAMP DEFAULT NOW()
);

ALTER TABLE video_service.video_jobs
ADD COLUMN IF NOT EXISTS plan_id BIGINT,
ADD COLUMN IF NOT EXISTS template_name VARCHAR(50) DEFAULT 'default';
SQL
```

### Step 2: API Gateway 라우팅 추가

```yaml
# services/api-gateway/src/main/resources/application.yml
routes:
  # ... 기존 routes
  
  # Photo routes (추가)
  - id: plan-photos
    uri: http://plan-service:8083
    predicates:
      - Path=/api/plans/*/photos,/api/plans/*/photos/**
    filters:
      - RewritePath=/api/plans/([^/]+)/photos(/?.*), /api/v1/plans/$\1/photos$\2
```

### Step 3: 서비스 재시작

```bash
docker-compose -f docker-compose.local.yml restart api-gateway plan-service
```

---

## 📋 완전한 체크리스트

- [ ] plan_photos 테이블 생성
- [ ] video_jobs.plan_id 컬럼 추가
- [ ] API Gateway에 photo 라우팅 추가
- [ ] Plan Service에 PhotoController 확인
- [ ] 서비스 재시작
- [ ] 테스트: 사진 업로드
- [ ] 확인: DB에 저장됨
- [ ] 테스트: 사진 조회
- [ ] 확인: UI에 표시됨
- [ ] 테스트: 영상 생성
- [ ] 확인: plan_id 연결됨

---

**현재 상태:** 테이블 생성됨, 라우팅 수정 필요  
**다음 단계:** API Gateway 라우팅 추가

