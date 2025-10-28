# Session Context - Oddiya Project

**당신이 읽어야 할 이유:** 이 파일은 새 Claude 채팅에서 프로젝트를 즉시 이해하고 긴 출력(8192 tokens)을 생성하기 위한 최소 컨텍스트입니다.

---

## 🎯 현재 상태 (2025-10-28)

**프로젝트:** Oddiya v1.3 - AI 여행 플래너 + 자동 영상 생성
**진행도:** 문서 작성 완료, 코드 구현 시작 전
**전략 변경:** ~~외부 API 통합~~ → **AWS Bedrock Claude 3.5 Sonnet 전용**

---

## 🏗️ 아키텍처 (7 Microservices)

```
Mobile App (React Native)
    ↓
AWS ALB → Nginx Ingress → API Gateway (8080)
    ├─ Auth Service (8081) → Redis [refresh tokens]
    ├─ User Service (8082) → PostgreSQL
    ├─ Plan Service (8083) → PostgreSQL + LLM Agent
    ├─ LLM Agent (8000) → AWS Bedrock Sonnet → Redis [cache]
    └─ Video Service (8084) → SQS → Video Worker → S3/SNS
```

**인프라:**
- EKS: 1x t3.medium Spot (stateless)
- EC2: 2x t2.micro (PostgreSQL 17.0 + Redis 7.4)
- ⚠️ **Bottleneck:** PostgreSQL 1GB RAM

---

## 🚨 핵심 전략 변경

### ❌ 제거됨
- Kakao Local API
- OpenWeatherMap API
- ExchangeRate-API
- Google Places API
- 모든 외부 API 호출

### ✅ 새 전략: LLM 전용
```
User Input: "서울 3일, 2명, 중급 호텔, 예산 100만원"
    ↓
AWS Bedrock Claude 3.5 Sonnet (1회 호출)
    ↓
Output: 일정 + 장소 + 예산 + 교통 + 팁 (JSON)
    ↓
Redis Cache (1hr TTL, 90%+ hit rate 목표)
```

**이유:**
- 개발 속도 3x 빠름 (외부 API 통합 불필요)
- 비용 절감 ($0 외부 API vs Bedrock만)
- Claude는 한국 여행 지식 충분
- 캐싱으로 비용 90% 절감

---

## 📂 프로젝트 구조

```
oddiya/
├── services/
│   ├── api-gateway/      # Spring Cloud Gateway
│   ├── auth-service/     # OAuth 2.0, RS256 JWT, JWKS
│   ├── user-service/     # User CRUD, internal API
│   ├── plan-service/     # Plan CRUD, calls LLM
│   ├── llm-agent/        # FastAPI, Bedrock only
│   ├── video-service/    # Job API, SQS, idempotency
│   └── video-worker/     # SQS consumer, FFmpeg
├── infrastructure/
│   ├── kubernetes/       # Deployments, Services
│   ├── terraform/        # EKS, EC2, VPC
│   └── docker-compose.yml # 로컬 개발
├── docs/                 # 상세 문서
├── scripts/              # 개발 스크립트
├── .cursorrules          # Claude Code 가이드
├── CLAUDE.md             # 빠른 레퍼런스
└── SESSION_CONTEXT.md    # 이 파일
```

---

## 🔑 핵심 기술 스택

| 구분 | 기술 |
|------|------|
| Java | Spring Boot 3.2, Java 21 |
| Python | FastAPI, Python 3.11 |
| DB | PostgreSQL 17.0 (schema-per-service) |
| Cache | Redis 7.4 |
| AI | **AWS Bedrock Claude 3.5 Sonnet** |
| Queue | AWS SQS + DLQ |
| Storage | AWS S3 |
| Notify | AWS SNS |
| K8s | EKS 1.28, HPA |

---

## 🎬 주요 플로우

### 1. OAuth 인증 (RS256 JWT)
```
Mobile → Auth Service → Google/Apple OAuth
       → User Service (internal API)
       → RS256 JWT (1hr) + Refresh Token (14d)
       → Redis refresh_token:{uuid}
API Gateway → JWKS fetch → JWT validation
```

### 2. AI 여행 계획 (LLM 전용)
```
Mobile → Plan Service → LLM Agent
LLM Agent:
  - Redis cache check
  - If miss: Bedrock invoke (prompt engineering)
  - Cache response (1hr)
  - Return JSON
→ PostgreSQL save → Mobile
```

### 3. 비디오 생성 (Async)
```
Mobile → Video Service (Idempotency-Key UUID)
       → PostgreSQL (status: PENDING)
       → SQS publish
       → 202 Accepted
Video Worker:
  - SQS poll
  - DB idempotency check
  - FFmpeg process
  - S3 upload
  - SNS push notification
```

---

## 🛠️ 개발 전략

### 원칙
1. **로컬 우선:** Docker Compose로 모든 서비스 로컬 개발
2. **테스트 후 배포:** 로컬 에러 없으면 EKS 배포
3. **Git 커밋:** 기능/모듈별 커밋 (롤백 용이)
4. **단순화:** 외부 API 없음, LLM만 사용

### 우선순위 (8주)
- **Week 1-2:** Infrastructure + Auth (P1/P2)
- **Week 3-5:** AI Planning - LLM Agent (P1)
- **Week 6-7:** Video Pipeline (P3)
- **Week 8:** Testing + Ops (P2)

---

## 📋 다음 작업

**즉시 시작:** 새 TODO 리스트 실행 (별도 파일 참고)

1. **Phase 0:** Git repo 생성, 기본 구조
2. **Phase 1:** 로컬 인프라 (Docker Compose)
3. **Phase 2:** Auth + User (로컬)
4. **Phase 3:** LLM Agent (Bedrock 통합)
5. **Phase 4:** Plan Service
6. **Phase 5:** Video Pipeline
7. **Phase 6:** EKS 배포

---

## 💡 중요 참고사항

### LLM 프롬프트 예시
```python
system_prompt = """
You are a Korea travel planning expert. Generate a detailed travel plan
based on user preferences. Include:
- Daily itinerary with specific places
- Estimated budget per item (KRW)
- Transportation recommendations
- Local tips
Output in JSON format.
"""

user_input = {
    "destination": "Seoul",
    "days": 3,
    "people": 2,
    "budget": 1000000,  # KRW
    "interests": ["culture", "food", "shopping"]
}
```

### 환경 변수 (핵심만)
```bash
# Bedrock (LLM Agent)
BEDROCK_MODEL_ID=anthropic.claude-sonnet-4-5-20250929-v1:0
BEDROCK_REGION=us-east-1

# DB/Cache (t2.micro private IPs)
DB_HOST=10.0.x.x
REDIS_HOST=10.0.x.x

# AWS
AWS_REGION=ap-northeast-2
S3_BUCKET=oddiya-storage
SQS_QUEUE_URL=https://sqs...
```

---

## 📚 전체 문서 위치

- **`.cursorrules`** - 코딩 가이드라인 (가장 상세)
- **`CLAUDE.md`** - 빠른 레퍼런스 (명령어 포함)
- **`README.md`** - 프로젝트 개요
- **`docs/`** - API 스펙, 아키텍처 다이어그램

---

## 🚀 새 채팅 시작 명령어

```
새로운 Claude에게 이렇게 시작하세요:

"SESSION_CONTEXT.md를 읽고, 현재 TODO 리스트부터 시작해줘.
로컬 개발 우선, Bedrock Sonnet 전용 전략으로 진행."
```

---

**마지막 업데이트:** 2025-10-28
**다음 단계:** TODO 리스트 실행 → 로컬 개발 시작
