# Oddiya User Journey

완전한 사용자 여정 설계

## 🗺️ 전체 여정 흐름

```
1. 여행 계획 📝
   ↓
2. 계획 확정 ✅
   ↓
3. 여행 중/후 사진 업로드 📸
   ↓
4. 숏폼 영상 자동 생성 🎬
   ↓
5. 프로필에서 여행 모아보기 📚
```

---

## Step 1: 여행 계획 📝

### UI/UX:
```
Plans 탭
  → "+ 새 여행 계획" 버튼
  → 폼 입력:
     • 여행지 선택 (서울/부산/제주)
     • 여행 제목
     • 시작일/종료일
     • 예산 수준
  → "🤖 AI 여행 계획 생성" 클릭
  → AI가 일정 생성
  → 생성된 계획 카드 표시
```

### 데이터베이스:
```sql
-- 계획 저장
INSERT INTO plan_service.travel_plans 
(user_id, title, start_date, end_date, status)
VALUES (1, '서울 주말 여행', '2025-12-01', '2025-12-03', 'DRAFT');

-- 상세 일정 저장
INSERT INTO plan_service.plan_details
(plan_id, day, location, activity)
VALUES (1, 1, '경복궁 & 북촌', '...');
```

### API:
```
POST /api/plans
→ Plan Service
→ LLM Agent (AI 생성)
→ PostgreSQL 저장
→ Response: Plan with status='DRAFT'
```

---

## Step 2: 계획 확정 ✅

### UI/UX:
```
계획 상세 화면
  → 일정 검토
  → "✅ 이 계획으로 확정" 버튼
  → 상태 변경: DRAFT → CONFIRMED
  → 알림: "여행이 확정되었습니다!"
  → 여행 날짜에 리마인더 설정
```

### 데이터베이스:
```sql
-- 계획 상태 변경
UPDATE plan_service.travel_plans
SET status = 'CONFIRMED', confirmed_at = NOW()
WHERE id = 1;
```

### API:
```
PATCH /api/plans/{id}/confirm
→ Plan Service
→ Update status to 'CONFIRMED'
→ (Optional) Send notification
```

### 새 테이블 스키마:
```sql
ALTER TABLE plan_service.travel_plans
ADD COLUMN status VARCHAR(20) DEFAULT 'DRAFT',
ADD COLUMN confirmed_at TIMESTAMP,
ADD COLUMN completed_at TIMESTAMP;

-- 상태: DRAFT, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED
```

---

## Step 3: 여행 중/후 사진 업로드 📸

### UI/UX:
```
계획 상세 화면
  → (여행 날짜 이후)
  → "📸 여행 사진 추가" 버튼
  → 사진 선택 (최대 10장)
  → 업로드 진행률 표시
  → S3에 업로드
  → "사진이 추가되었습니다!"
```

### 데이터베이스:
```sql
-- 새 테이블: 여행 사진
CREATE TABLE plan_service.plan_photos (
    id BIGSERIAL PRIMARY KEY,
    plan_id BIGINT NOT NULL REFERENCES plan_service.travel_plans(id),
    photo_url VARCHAR NOT NULL,
    upload_order INT,
    uploaded_at TIMESTAMP DEFAULT NOW(),
    FOREIGN KEY (plan_id) REFERENCES plan_service.travel_plans(id) ON DELETE CASCADE
);
```

### API:
```
1. Get pre-signed URL:
   POST /api/plans/{id}/photos/presigned-url
   → Plan Service
   → S3 Client
   → Response: signed URL for upload

2. Upload to S3:
   PUT https://s3.amazonaws.com/oddiya-storage/photos/...
   → Direct to S3 (from mobile)

3. Confirm upload:
   POST /api/plans/{id}/photos
   Body: {photoUrl, order}
   → Plan Service
   → Save to plan_photos table
```

---

## Step 4: 숏폼 영상 자동 생성 🎬

### UI/UX:
```
계획 상세 화면
  → (사진 업로드 완료 후)
  → "🎬 영상 만들기" 버튼 자동 활성화
  → 클릭 시:
     • 템플릿 선택 (기본/감성/활발)
     • "영상 생성 시작" 버튼
  → 영상 생성 시작
  → 상태: PENDING → PROCESSING → COMPLETED
  → 푸시 알림: "영상이 완성되었습니다!"
```

### 데이터베이스:
```sql
-- video_jobs에 plan_id 추가
ALTER TABLE video_service.video_jobs
ADD COLUMN plan_id BIGINT,
ADD FOREIGN KEY (plan_id) REFERENCES plan_service.travel_plans(id);

-- 계획에서 비디오 조회 가능
SELECT vj.* FROM video_service.video_jobs vj
WHERE vj.plan_id = 1;
```

### API Flow:
```
POST /api/plans/{id}/create-video
→ Plan Service:
   • Get photos from plan_photos
   • Extract photo URLs
   • Call Video Service
→ Video Service:
   • Create job with plan_id
   • Publish to SQS
   • Return job_id
→ Video Worker:
   • Download photos from S3
   • Generate video with FFmpeg
   • Upload video to S3
   • Update job status
   • Send push notification
→ Mobile:
   • Receive push notification
   • Show "영상 완성!" alert
   • Navigate to video
```

---

## Step 5: 프로필에서 여행 모아보기 📚

### UI/UX:
```
Profile 탭 개선
  ├── 사용자 정보
  │   • 이름, 이메일
  │   • 가입일
  │
  ├── 📊 여행 통계
  │   • 총 여행 횟수: 5회
  │   • 방문한 도시: 서울, 부산, 제주
  │   • 총 영상: 8개
  │   • 총 여행 일수: 23일
  │
  ├── 🗺️ 완료된 여행
  │   [카드] 서울 주말 여행 (2025.12.01-03)
  │          • 사진: 8장
  │          • 영상: ▶️ 보기
  │          • 예산: ₩285,000
  │
  │   [카드] 부산 힐링 여행 (2025.11.15-17)
  │          • 사진: 12장
  │          • 영상: ▶️ 보기
  │          • 예산: ₩320,000
  │
  └── 📝 다가오는 여행
      [카드] 제주도 여행 (2025.12.20-23)
             • 상태: 확정됨
             • D-25일
```

### API:
```
GET /api/profile/trips
→ User Service
→ Join plans + photos + videos
→ Response: {
    statistics: {
        total_trips: 5,
        cities_visited: ['Seoul', 'Busan', 'Jeju'],
        total_videos: 8,
        total_days: 23
    },
    completed_trips: [
        {
            plan: {...},
            photos: [...],
            video: {...}
        }
    ],
    upcoming_trips: [...]
}
```

---

## 데이터베이스 스키마 변경

### 1. Travel Plans 테이블 확장

```sql
ALTER TABLE plan_service.travel_plans
ADD COLUMN status VARCHAR(20) DEFAULT 'DRAFT',
ADD COLUMN confirmed_at TIMESTAMP,
ADD COLUMN completed_at TIMESTAMP,
ADD COLUMN budget_level VARCHAR(10),
ADD COLUMN total_cost INT;

-- 상태 값:
-- DRAFT: 초안
-- CONFIRMED: 확정
-- IN_PROGRESS: 여행 중
-- COMPLETED: 완료
-- CANCELLED: 취소
```

### 2. Plan Photos 테이블 (새로 생성)

```sql
CREATE TABLE plan_service.plan_photos (
    id BIGSERIAL PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    photo_url VARCHAR NOT NULL,
    s3_key VARCHAR NOT NULL,
    upload_order INT,
    uploaded_at TIMESTAMP DEFAULT NOW(),
    FOREIGN KEY (plan_id) REFERENCES plan_service.travel_plans(id) ON DELETE CASCADE
);

CREATE INDEX idx_plan_photos_plan_id ON plan_service.plan_photos(plan_id);
```

### 3. Video Jobs 테이블 확장

```sql
ALTER TABLE video_service.video_jobs
ADD COLUMN plan_id BIGINT,
ADD COLUMN template_name VARCHAR(50) DEFAULT 'default';

-- plan_id로 조회 가능
CREATE INDEX idx_video_jobs_plan_id ON video_service.video_jobs(plan_id);
```

---

## API 엔드포인트 추가

### Plan Service:

```java
// 계획 확정
@PatchMapping("/{id}/confirm")
public ResponseEntity<PlanResponse> confirmPlan(
    @PathVariable Long id,
    @RequestHeader("X-User-Id") Long userId
) {
    return ResponseEntity.ok(planService.confirmPlan(id, userId));
}

// 사진 업로드 URL 발급
@PostMapping("/{id}/photos/presigned-url")
public ResponseEntity<PresignedUrlResponse> getPresignedUrl(
    @PathVariable Long id,
    @RequestHeader("X-User-Id") Long userId,
    @RequestBody PhotoUploadRequest request
) {
    // S3 pre-signed URL 생성
    String url = s3Service.generatePresignedUrl(request.getFileName());
    return ResponseEntity.ok(new PresignedUrlResponse(url));
}

// 사진 업로드 확인
@PostMapping("/{id}/photos")
public ResponseEntity<PhotoResponse> addPhoto(
    @PathVariable Long id,
    @RequestHeader("X-User-Id") Long userId,
    @RequestBody AddPhotoRequest request
) {
    return ResponseEntity.ok(planService.addPhoto(id, userId, request));
}

// 계획으로부터 영상 생성
@PostMapping("/{id}/create-video")
public ResponseEntity<VideoJobResponse> createVideoFromPlan(
    @PathVariable Long id,
    @RequestHeader("X-User-Id") Long userId,
    @RequestBody CreateVideoRequest request
) {
    // 1. 계획의 사진들 가져오기
    List<String> photoUrls = planService.getPhotoUrls(id);
    
    // 2. Video Service 호출
    VideoJobResponse video = videoService.createVideo(
        userId, 
        UUID.randomUUID(), 
        photoUrls, 
        request.getTemplate()
    );
    
    // 3. video_job에 plan_id 연결
    videoService.linkToPlan(video.getId(), id);
    
    return ResponseEntity.ok(video);
}

// 프로필 - 여행 모아보기
@GetMapping("/profile/trips")
public ResponseEntity<TripCollectionResponse> getUserTrips(
    @RequestHeader("X-User-Id") Long userId
) {
    return ResponseEntity.ok(planService.getUserTripsWithMedia(userId));
}
```

---

## 모바일 웹 UI 변경

### 1. Plans 화면에 상태 표시

```javascript
// Plan card에 상태 배지 추가
<div class="card" onclick="showPlanDetails(${plan.id})">
    <div class="card-title">${plan.title}</div>
    <div class="status-badge status-${plan.status.toLowerCase()}">
        ${plan.status === 'DRAFT' ? '📝 초안' : 
          plan.status === 'CONFIRMED' ? '✅ 확정' :
          plan.status === 'COMPLETED' ? '✨ 완료' : plan.status}
    </div>
    ...
</div>
```

### 2. 계획 상세 화면 개선

```javascript
function showPlanDetails(planId) {
    // 계획 상태에 따라 다른 버튼 표시
    
    if (plan.status === 'DRAFT') {
        // 초안 상태
        buttons = `
            <button class="button" onclick="confirmPlan(${planId})">
                ✅ 이 계획으로 확정하기
            </button>
            <button class="button-secondary" onclick="editPlan(${planId})">
                ✏️ 수정하기
            </button>
        `;
    } else if (plan.status === 'CONFIRMED') {
        // 확정됨 - 여행 전
        const daysUntil = Math.ceil((new Date(plan.startDate) - new Date()) / (1000*60*60*24));
        buttons = `
            <div class="alert-info">
                🎒 여행까지 D-${daysUntil}일
            </div>
            <button class="button" onclick="showPackingList(${planId})">
                🎒 짐 싸기 리스트
            </button>
        `;
    } else if (plan.status === 'IN_PROGRESS' || isPastDate(plan.endDate)) {
        // 여행 중 또는 여행 후
        buttons = `
            <button class="button" onclick="showPhotoUpload(${planId})">
                📸 여행 사진 추가하기
            </button>
            ${plan.photos && plan.photos.length > 0 ? `
                <button class="button" onclick="createVideoFromPlan(${planId})">
                    🎬 사진으로 영상 만들기 (${plan.photos.length}장)
                </button>
            ` : ''}
        `;
    } else if (plan.status === 'COMPLETED' && plan.video) {
        // 완료됨 - 영상까지 있음
        buttons = `
            <button class="button" onclick="playVideo(${plan.video.id})">
                ▶️ 여행 영상 보기
            </button>
            <button class="button-secondary" onclick="shareVideo(${plan.video.id})">
                📤 공유하기
            </button>
        `;
    }
}
```

### 3. 사진 업로드 화면

```javascript
function showPhotoUpload(planId) {
    container.innerHTML = `
        <div class="card">
            <h3>📸 여행 사진 추가</h3>
            <p>여행의 추억을 사진으로 남겨보세요!</p>
            
            <input type="file" 
                   id="photo-input" 
                   multiple 
                   accept="image/*"
                   onchange="handlePhotoSelect(event, ${planId})">
            
            <div id="photo-preview"></div>
            
            <button class="button" onclick="uploadPhotos(${planId})">
                ☁️ 사진 업로드 (최대 10장)
            </button>
        </div>
    `;
}

async function uploadPhotos(planId) {
    const files = document.getElementById('photo-input').files;
    
    for (let i = 0; i < files.length; i++) {
        // 1. Get presigned URL
        const urlResponse = await fetch(
            `${API_BASE}/api/plans/${planId}/photos/presigned-url`,
            {
                method: 'POST',
                headers: {'X-User-Id': USER_ID, 'Content-Type': 'application/json'},
                body: JSON.stringify({fileName: files[i].name})
            }
        );
        const {uploadUrl, photoKey} = await urlResponse.json();
        
        // 2. Upload to S3
        await fetch(uploadUrl, {
            method: 'PUT',
            body: files[i],
            headers: {'Content-Type': files[i].type}
        });
        
        // 3. Confirm upload
        await fetch(`${API_BASE}/api/plans/${planId}/photos`, {
            method: 'POST',
            headers: {'X-User-Id': USER_ID, 'Content-Type': 'application/json'},
            body: JSON.stringify({
                photoUrl: `https://s3.amazonaws.com/oddiya-storage/${photoKey}`,
                s3Key: photoKey,
                order: i + 1
            })
        });
        
        showToast(`✅ 사진 ${i+1}/${files.length} 업로드 완료`);
    }
    
    showToast('🎉 모든 사진이 업로드되었습니다!');
    showPlanDetails(planId);  // Refresh
}
```

### 4. 영상 생성 화면

```javascript
function createVideoFromPlan(planId) {
    showDialog(`
        <h3>🎬 여행 영상 만들기</h3>
        <p>${photoCount}장의 사진으로 멋진 영상을 만들어드립니다!</p>
        
        <div class="form-group">
            <label>템플릿 선택</label>
            <select id="video-template">
                <option value="default">📹 기본 (사진 슬라이드)</option>
                <option value="emotional">💫 감성 (부드러운 전환)</option>
                <option value="energetic">⚡ 활발 (빠른 전환)</option>
            </select>
        </div>
        
        <button class="button" onclick="generateVideo(${planId})">
            🎬 영상 생성 시작 (약 2-3분 소요)
        </button>
    `);
}

async function generateVideo(planId) {
    const template = document.getElementById('video-template').value;
    
    // 영상 생성 요청
    const response = await fetch(
        `${API_BASE}/api/plans/${planId}/create-video`,
        {
            method: 'POST',
            headers: {'X-User-Id': USER_ID, 'Content-Type': 'application/json'},
            body: JSON.stringify({template})
        }
    );
    
    const video = await response.json();
    
    showToast('🎬 영상 생성이 시작되었습니다!');
    
    // 상태 폴링 (실제로는 Push Notification)
    pollVideoStatus(video.id);
}

function pollVideoStatus(videoId) {
    const interval = setInterval(async () => {
        const response = await fetch(
            `${API_BASE}/api/videos/${videoId}`,
            {headers: {'X-User-Id': USER_ID}}
        );
        const video = await response.json();
        
        if (video.status === 'COMPLETED') {
            clearInterval(interval);
            showToast('🎉 영상이 완성되었습니다!');
            showVideoPlayer(video.videoUrl);
        } else if (video.status === 'FAILED') {
            clearInterval(interval);
            showToast('❌ 영상 생성에 실패했습니다');
        }
    }, 5000);  // 5초마다 확인
}
```

### 5. Profile - 여행 모아보기

```javascript
async function loadTripCollection() {
    const response = await fetch(
        `${API_BASE}/api/profile/trips`,
        {headers: {'X-User-Id': USER_ID}}
    );
    const data = await response.json();
    
    container.innerHTML = `
        <!-- 통계 카드 -->
        <div class="stats-card">
            <div class="stat-item">
                <div class="stat-value">${data.statistics.total_trips}</div>
                <div class="stat-label">총 여행</div>
            </div>
            <div class="stat-item">
                <div class="stat-value">${data.statistics.cities_visited.length}</div>
                <div class="stat-label">방문 도시</div>
            </div>
            <div class="stat-item">
                <div class="stat-value">${data.statistics.total_videos}</div>
                <div class="stat-label">여행 영상</div>
            </div>
        </div>
        
        <!-- 완료된 여행들 -->
        <h3>✨ 완료된 여행</h3>
        ${data.completed_trips.map(trip => `
            <div class="trip-memory-card">
                ${trip.video ? `
                    <video src="${trip.video.videoUrl}" 
                           poster="${trip.photos[0]?.url}"
                           controls
                           style="width: 100%; border-radius: 12px;">
                    </video>
                ` : `
                    <img src="${trip.photos[0]?.url}" 
                         style="width: 100%; border-radius: 12px;">
                `}
                
                <div class="trip-info">
                    <h4>${trip.plan.title}</h4>
                    <div class="trip-meta">
                        📅 ${formatDate(trip.plan.startDate)} - ${formatDate(trip.plan.endDate)}
                    </div>
                    <div class="trip-meta">
                        📸 사진 ${trip.photos.length}장
                        ${trip.video ? '• 🎬 영상 완성' : ''}
                    </div>
                    <div class="trip-meta">
                        💰 총 경비: ₩${trip.plan.total_cost?.toLocaleString()}
                    </div>
                </div>
                
                <button class="button" onclick="showPlanDetails(${trip.plan.id})">
                    자세히 보기
                </button>
            </div>
        `).join('')}
        
        <!-- 다가오는 여행 -->
        <h3>🎒 다가오는 여행</h3>
        ${data.upcoming_trips.map(trip => `
            <div class="upcoming-trip-card">
                <h4>${trip.title}</h4>
                <div class="countdown">D-${trip.daysUntil}일</div>
                <button onclick="showPlanDetails(${trip.id})">
                    일정 확인
                </button>
            </div>
        `).join('')}
    `;
}
```

---

## 구현 우선순위

### Phase 1: 계획 상태 관리 (1-2일)
- [ ] DB 스키마 변경 (status 컬럼 추가)
- [ ] 계획 확정 API
- [ ] UI에 상태 표시

### Phase 2: 사진 업로드 (2-3일)
- [ ] plan_photos 테이블 생성
- [ ] S3 presigned URL API
- [ ] 사진 업로드 UI
- [ ] 사진 목록 표시

### Phase 3: 영상 생성 연동 (1-2일)
- [ ] plan_id를 video_jobs에 추가
- [ ] 계획에서 영상 생성 API
- [ ] 영상 상태 폴링 UI
- [ ] 완성 알림

### Phase 4: 프로필 개선 (2-3일)
- [ ] 여행 통계 API
- [ ] 완료된 여행 목록 API
- [ ] 프로필 UI 재디자인
- [ ] 여행 모아보기 화면

---

## 예상 데이터 흐름

```
사용자: "서울 주말 여행" 계획 생성
  ↓
DB: travel_plans (status='DRAFT')
  ↓
사용자: 확정 버튼 클릭
  ↓
DB: status='CONFIRMED'
  ↓
(여행 다녀옴)
  ↓
사용자: 사진 8장 업로드
  ↓
DB: plan_photos (8 rows)
S3: photos/user1/plan1/photo1.jpg ~ photo8.jpg
  ↓
사용자: "영상 만들기" 클릭
  ↓
DB: video_jobs (plan_id=1, status='PENDING')
SQS: Video job published
  ↓
Video Worker: FFmpeg 처리
  ↓
S3: videos/user1/plan1/output.mp4
DB: video_jobs (status='COMPLETED')
  ↓
Push: "영상이 완성되었습니다!"
  ↓
사용자: Profile → 여행 모아보기
  ↓
화면: 여행 목록 + 영상 + 사진
```

---

## 다음 단계

이 사용자 여정을 구현하시겠습니까?

1. **전체 구현** (5-7일 작업)
2. **Phase 1만 먼저** (계획 상태 관리)
3. **프로토타입** (Mock UI만 먼저)

선택해주시면 바로 구현하겠습니다!

