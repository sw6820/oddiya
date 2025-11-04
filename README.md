# Oddiya - AI 여행 플래너

**버전:** v1.3 - Streaming + Database Persistence
**최종 업데이트:** 2025-11-04
**상태:** ✅ 실시간 스트리밍 & 데이터베이스 영속성 구현 완료

---

## 📋 최신 문서

**⭐ [현재 구현 상태](docs/CURRENT_IMPLEMENTATION_STATUS.md)** - 전체 시스템 상태, 코드 플로우, 환경 변수

---

## 🚀 빠른 시작

### 로컬 개발 환경 실행

```bash
# 1. LLM Agent (AI 엔진) - Port 8000
cd services/llm-agent
source venv/bin/activate
python main.py

# 2. Plan Service (플랜 관리) - Port 8083
cd services/plan-service
./gradlew bootRun

# 3. 데이터베이스 & 캐시
brew services start redis postgresql

# 4. 모바일 앱
cd mobile
npm run ios
```

### 헬스 체크

```bash
curl http://localhost:8000/health        # LLM Agent
curl http://localhost:8083/actuator/health  # Plan Service
redis-cli ping                           # Redis
pg_isready                               # PostgreSQL
```

---

## ✨ 주요 기능

### 1. 실시간 스트리밍 플랜 생성 🎬
- ChatGPT 스타일 점진적 표시
- 실시간 진행률 & 한글 상태 메시지
- LLM 생성 과정 실시간 표시
- **구현:** 2025-11-04 ✅

### 2. 스마트 캐싱 ⚡
- Redis 기반 1시간 캐싱
- 동일 요청 즉시 응답 (<1초)
- 99% 비용 절감

### 3. 데이터베이스 영속성 💾
- PostgreSQL 저장
- 사용자별 플랜 관리
- 앱 재시작 후에도 유지
- **구현:** 2025-11-04 ✅

### 4. 완전한 CRUD 📝
- 생성, 조회, 수정, 삭제 전체 구현

---

## 🏗️ 시스템 아키텍처

```
Mobile App (React Native 0.75)
    ↓ SSE (스트리밍)
LLM Agent (8000) ← Python FastAPI + LangChain + Gemini
    ↓
Redis (6379) ← 캐시
    ↓
Plan Service (8083) ← Spring Boot + JPA
    ↓
PostgreSQL (5432) ← 영속성
```

---

## 📊 기술 스택

### Backend
- Java 21 + Spring Boot 3.2
- Python 3.11 + FastAPI
- LangChain + LangGraph
- PostgreSQL 17.0 + Redis 7.4

### Frontend
- React Native 0.75 + Expo
- Redux Toolkit
- Server-Sent Events (SSE)

### AI
- Google Gemini 2.0 Flash (무료)

---

## 🔄 데이터 플로우

### 플랜 생성 (첫 요청)

```
[Mobile] 폼 입력 → Generate 버튼 탭
    ↓
[LLM Agent] SSE 스트리밍 시작
    → 날씨 정보 수집 (10%)
    → AI 플랜 생성 (20-60%)
    → 검증 & 개선 (60-95%)
    → 최종 완성 (100%)
    ↓
[Plan Service] PostgreSQL에 저장
    ↓
[Mobile] Plans 리스트 새로고침
```

### 캐시 히트 (동일 요청)

```
[LLM Agent] Redis에서 즉시 조회 (<1초)
    ↓
[Mobile] "💾 Cached" 배지 표시
```

---

## 📁 프로젝트 구조

```
oddiya/
├── docs/                           # 📚 문서
│   ├── CURRENT_IMPLEMENTATION_STATUS.md  # ⭐ 최신 상태
│   ├── architecture/               # 시스템 설계
│   ├── development/                # 개발 가이드
│   └── archive/                    # 이전 버전
├── services/                       # 🔧 백엔드
│   ├── llm-agent/                  # Python AI 엔진
│   ├── plan-service/               # Java 플랜 서비스
│   ├── auth-service/               # OAuth 인증
│   └── api-gateway/                # API 게이트웨이
├── mobile/                         # 📱 모바일
│   ├── src/
│   │   ├── api/
│   │   │   ├── services.ts         # REST API
│   │   │   └── streaming.ts        # SSE 스트리밍
│   │   ├── screens/
│   │   │   ├── PlansScreen.tsx    # 플랜 목록
│   │   │   └── CreatePlanScreen.tsx # 스트리밍 UI
│   │   └── store/slices/
│   │       └── plansSlice.ts       # Redux
│   └── package.json
├── scripts/                        # 🛠️ 자동화
├── .env                           # 환경 변수
└── README.md                      # 이 파일
```

---

## 🌍 환경 변수

```bash
# Google Gemini (필수)
GOOGLE_API_KEY=your_api_key
GEMINI_MODEL=gemini-2.0-flash-exp

# Redis (필수)
REDIS_HOST=localhost
REDIS_PORT=6379

# PostgreSQL (필수)
DB_HOST=localhost
DB_PORT=5432
DB_NAME=oddiya
DB_USER=admin
DB_PASSWORD=4321
```

전체 설정: [.env.example](.env.example)

---

## 🧪 테스트

### API 테스트

```bash
# 플랜 생성
curl -X POST http://localhost:8083/api/v1/plans \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -d '{"destination":"Seoul","startDate":"2025-11-10","endDate":"2025-11-12","budget":100000}'

# 플랜 조회
curl http://localhost:8083/api/v1/plans -H "X-User-Id: 1"
```

### 모바일 테스트

```bash
cd mobile && npm run ios

# 앱에서:
# 1. Plans → "+ New Plan"
# 2. Seoul, 2025-11-10~12, Medium 입력
# 3. Generate → 스트리밍 확인 (~6초)
# 4. Plans 리스트에 추가 확인
# 5. 동일 파라미터 재생성 → 즉시 (<1초)
```

---

## 📊 데이터베이스

### 스키마

```sql
-- plan_service.travel_plans
id, user_id, title, start_date, end_date,
budget_level, status, created_at, updated_at

-- plan_service.plan_details
id, plan_id, day, location, activity, created_at
```

### 조회

```bash
PGPASSWORD=4321 psql -h localhost -U admin -d oddiya

SELECT * FROM plan_service.travel_plans
ORDER BY created_at DESC LIMIT 5;
```

---

## 📚 문서

| 문서 | 설명 |
|------|------|
| **[현재 구현 상태](docs/CURRENT_IMPLEMENTATION_STATUS.md)** | 전체 시스템 상태 |
| [데이터베이스 영속성](DATABASE_PERSISTENCE_COMPLETE.md) | DB 저장 구현 |
| [모바일 테스트](MOBILE_STREAMING_TEST_GUIDE.md) | 모바일 테스트 가이드 |
| [빠른 테스트](READY_TO_TEST_SUMMARY.md) | 체크리스트 |

---

## 📝 로그

```bash
# LLM Agent
tail -f /tmp/llm-agent.log | grep -E "Streaming|Cache"

# Plan Service
tail -f /tmp/plan-service.log | grep "PlanService"

# 성공 로그:
# [PlanService] ✅ Plan saved to database: id=1
# [PlanService] Found 1 plans for user=1
```

---

## 🚧 다음 단계

### 즉시 구현 가능
- [ ] PlanDetail 화면
- [ ] 플랜 수정/삭제 UI
- [ ] 오류 처리 개선

### 추후 구현
- [ ] OAuth 인증 완성
- [ ] 오프라인 지원
- [ ] 비디오 생성

---

## 💰 비용

| 항목 | 비용 |
|------|------|
| Gemini API | **무료** |
| AWS EC2 (12개월) | **무료** |
| Redis/PostgreSQL | **무료** |
| **총 (12개월 후)** | ~$10/월 |

---

## 🔧 개발 가이드

### 코딩 규칙
1. **LLM-First** - 여행 데이터는 LLM 생성, 하드코딩 금지
2. **Edit First** - 기존 파일 수정 우선
3. **Test** - 변경 후 항상 테스트

### 기여
1. [CLAUDE.md](CLAUDE.md) 읽기
2. 브랜치: `feature/service-feature`
3. PR 생성

---

## 📞 문의

- GitHub Issues
- 문서: [docs/](docs/)

---

**License:** MIT
**Last Updated:** 2025-11-04
**Version:** v1.3
