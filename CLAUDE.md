# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project: Oddiya (v1.3.1 - Streaming + Database Persistence)

**Last Updated:** 2025-11-04

AI-powered mobile travel planner with real-time streaming plan generation and database persistence.

**Critical Rules:**
1. **ALWAYS edit existing files** instead of creating new ones. Read files first before making changes.
2. **NEVER hardcode data** - Use LLM for ALL travel content
3. **Check current status first** - See `docs/CURRENT_IMPLEMENTATION_STATUS.md` for latest state

**📋 Current Implementation:** See **[docs/CURRENT_IMPLEMENTATION_STATUS.md](docs/CURRENT_IMPLEMENTATION_STATUS.md)** for complete system state, code flows, and environment setup.

## 🚫 No Hardcoding Principle

**NEVER hardcode in code:**
- ❌ Travel destinations in switch/case statements
- ❌ Prompts in Java/Python code
- ❌ Restaurant names in if/else chains or YAML files
- ❌ UI messages inline
- ❌ Configuration values
- ❌ Business logic data (travel itineraries, activities, etc.)

**ALWAYS use LLM or configuration:**
- ✅ **LLM (Claude Sonnet)** for ALL travel content (destinations, restaurants, activities)
- ✅ Properties files for settings (`application.yml`)
- ✅ Database for dynamic content
- ✅ Separate prompt files for LLM instructions (`prompts/system_prompts.yaml`)
- ✅ JSON for API responses
- ✅ Environment variables for secrets

**Examples:**

❌ **Bad (Hardcoded travel data):**
```java
if (location.equals("Seoul")) {
    activity = "Visit Gyeongbokgung Palace";
} else if (location.equals("Busan")) {
    activity = "Visit Haeundae Beach";
}
```

❌ **Bad (Hardcoded in YAML):**
```yaml
Seoul:
  day1:
    activity: "Morning: 경복궁 (₩3,000), Evening: 명동교자..."
```

✅ **Good (LLM-Only):**
```java
// Call LLM Agent - Claude generates ALL travel data dynamically
return llmAgentClient.generatePlan(llmRequest)
    .map(llmResponse -> buildTravelPlan(llmResponse))
    .onErrorResume(error -> Mono.error(new ServiceException("AI temporarily unavailable")));
```

✅ **Good (Prompt configuration only):**
```python
# Load instructions for Claude from prompts/system_prompts.yaml
prompt = prompt_loader.get_planning_prompt(location=location)
# Claude generates real restaurants, activities, costs dynamically
```

## Architecture Overview

### Current Implementation (Local Development)

```
Mobile App (React Native 0.75)
    ↓ SSE (Server-Sent Events for streaming)
    ↓
LLM Agent (8000) ← FastAPI + LangChain + LangGraph
    ├→ Google Gemini 2.0 Flash (gemini-2.0-flash-exp)
    └→ Redis (6379) - 1hr cache

Mobile App (REST API)
    ↓
API Gateway (8080) ← Spring Cloud Gateway
    ├→ Auth Service (8081) - OAuth 2.0
    ├→ User Service (8082) - User CRUD
    └→ Plan Service (8083) - Plan CRUD
        ├→ LLM Agent (8000) - Plan generation
        └→ PostgreSQL (5432) - Persistence

Tech Stack:
- Backend: Spring Boot 3.2 + Java 21, FastAPI + Python 3.11
- Database: PostgreSQL 17.0 (schema-per-service)
- Cache: Redis 7.4 (LLM cache + session)
- Mobile: React Native 0.75 + Expo
```

### Critical Data Flows

**✅ Real-time Streaming Plan Generation (Implemented 2025-11-04):**
- Mobile App connects **directly to LLM Agent** (port 8000) via SSE
- LLM Agent uses LangGraph for iterative planning
- Progress events: status → chunk → progress → complete → done
- Real-time progress bar (0-100%), Korean status messages
- LLM output streams to mobile in real-time
- **Cache Hit:** Redis returns plan instantly (<1s)
- **Cache Miss:** Generates with streaming (~6s), saves to Redis

**✅ Database Persistence (Implemented 2025-11-04):**
- After streaming completes, Mobile App calls Plan Service REST API
- Plan Service saves to PostgreSQL (`plan_service.travel_plans`)
- Full CRUD implemented: Create, Read, Update, Delete
- Plans persist across app restarts

**Authentication (RS256 JWT):**
- Auth Service generates RS256 tokens (1hr access, 14-day refresh)
- API Gateway validates JWTs via JWKS
- Refresh tokens stored in Redis

**⚠️ Future: Video Generation (Not Yet Implemented)**
- Async pipeline with SQS + FFmpeg + S3
- Idempotency with UUID keys

## Development Commands

### Quick Start (Local Development)

```bash
# 1. Start LLM Agent (Python FastAPI)
cd services/llm-agent
source venv/bin/activate
python main.py  # Port 8000

# 2. Start Plan Service (Spring Boot)
cd services/plan-service
./gradlew bootRun  # Port 8083

# 3. Start Database & Cache
brew services start redis postgresql

# 4. Run Mobile App
cd mobile
npm run ios  # iOS Simulator
npm run android  # Android Emulator
```

### Service Health Checks

```bash
# Check all services
curl http://localhost:8000/health        # LLM Agent
curl http://localhost:8083/actuator/health  # Plan Service
redis-cli ping                           # Redis → PONG
pg_isready -h localhost -U admin         # PostgreSQL → accepting connections
```

### Java Services (Spring Boot)

```bash
cd services/{service-name}
./gradlew clean build    # Build
./gradlew test           # Unit tests
./gradlew bootRun        # Run locally
```

### Python Services

```bash
cd services/llm-agent
pip install -r requirements.txt
pytest                    # Tests
python main.py            # Run locally
```

### Database Operations

```bash
# Connect to PostgreSQL
PGPASSWORD=4321 psql -h localhost -U admin -d oddiya

# Check plan_service schema
\dt plan_service.*

# View travel plans
SELECT id, title, start_date, end_date, created_at
FROM plan_service.travel_plans
ORDER BY created_at DESC LIMIT 5;
```

### Redis Operations

```bash
# Check cache
redis-cli keys "plan:*"
redis-cli GET "plan:Seoul:2025-11-10:2025-11-12:medium"
redis-cli TTL "plan:Seoul:2025-11-10:2025-11-12:medium"
```

## Project Structure

```
oddiya/
├── docs/                           # Documentation
│   ├── CURRENT_IMPLEMENTATION_STATUS.md  # ⭐ Latest system state
│   ├── architecture/               # System design
│   ├── development/                # Dev guides
│   └── archive/                    # Historical docs
├── services/                       # Backend services
│   ├── llm-agent/                  # ✅ Python FastAPI + LangGraph + Gemini
│   │   ├── src/routes/langgraph_plans.py  # SSE streaming endpoint
│   │   ├── src/services/langgraph_planner.py  # Streaming logic
│   │   └── static/streaming-test.html  # Web test page
│   ├── plan-service/               # ✅ Spring Boot + JPA + PostgreSQL
│   │   ├── src/main/java/com/oddiya/plan/
│   │   │   ├── controller/         # REST API
│   │   │   ├── service/            # Business logic + LLM client
│   │   │   ├── repository/         # JPA repositories (NEW)
│   │   │   ├── entity/             # TravelPlan, PlanDetail
│   │   │   └── dto/                # Request/Response DTOs
│   │   └── src/main/resources/
│   │       └── application.yml     # DB config (JPA enabled)
│   ├── auth-service/               # OAuth 2.0 (future)
│   └── api-gateway/                # Gateway (future)
├── mobile/                         # React Native app
│   ├── src/
│   │   ├── api/
│   │   │   ├── services.ts         # REST API
│   │   │   └── streaming.ts        # ✅ SSE client (NEW)
│   │   ├── screens/
│   │   │   ├── PlansScreen.tsx
│   │   │   └── CreatePlanScreen.tsx  # ✅ Streaming UI (NEW)
│   │   ├── store/slices/
│   │   │   ├── authSlice.ts
│   │   │   └── plansSlice.ts       # Redux state
│   │   └── navigation/
│   │       └── AppNavigator.tsx    # Navigation setup
│   └── package.json
├── scripts/                        # Automation
├── .env                           # Environment variables
├── README.md                      # Project overview
└── CLAUDE.md                      # This file
```

### Service Structure Pattern
```
services/{service-name}/
├── src/
│   └── main/
│       ├── java/com/oddiya/{service}/
│       │   ├── controller/
│       │   ├── service/
│       │   ├── repository/
│       │   ├── entity/
│       │   ├── dto/
│       │   └── config/
│       └── resources/
│           ├── application.yml
│           └── db/migration/
├── tests/
├── Dockerfile
├── build.gradle              # Java
├── requirements.txt          # Python
└── README.md
```

## Code Patterns

### Spring Boot Services
- Package: `com.oddiya.{service}.{layer}`
- Config: `application.yml` (NOT .properties)
- DB connections: Use environment variables for t2.micro private IPs
- Error handling: `@ControllerAdvice` for global exceptions
- Validation: Jakarta Bean Validation (`@Valid`)
- Testing: JUnit 5 + Mockito + Testcontainers

### Python Services
- FastAPI with Pydantic validation
- Async/await for I/O operations
- Black for formatting
- Testing: pytest + pytest-asyncio
- Environment: python-dotenv

### Database Schema-per-Service
```sql
-- Each service owns its schema
user_service.users
plan_service.travel_plans
plan_service.plan_details
video_service.video_jobs (with idempotency_key UUID UNIQUE)
```

### Kubernetes Resources
- Deployments (NOT StatefulSets) - all stateless
- Liveness + Readiness probes required
- ConfigMaps for config, Secrets for credentials
- HPA for auto-scaling
- Service discovery: `http://{service-name}:{port}`

## Environment Variables

### Required (Local Development)

```bash
# Google Gemini (Required - FREE tier)
GOOGLE_API_KEY=AIzaSyDlMvCLaGNMbPJXvnNkpjf_d4gOQOr5Hbk
GEMINI_MODEL=gemini-2.0-flash-exp

# Redis (Required for caching)
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_CACHE_TTL=3600  # 1 hour

# PostgreSQL (Required for persistence)
DB_HOST=localhost
DB_PORT=5432
DB_NAME=oddiya
DB_USER=admin
DB_PASSWORD=4321
```

### Optional (Future Features)

```bash
# OAuth (Auth Service)
GOOGLE_CLIENT_ID=***
GOOGLE_CLIENT_SECRET=***

# AWS (Video Service - not yet implemented)
AWS_REGION=ap-northeast-2
S3_BUCKET=oddiya-storage
SQS_QUEUE_URL=https://sqs...
SNS_TOPIC_ARN=arn:aws:sns...
```

**See:** [.env.example](.env.example) for full configuration template

## Implementation Status (2025-11-04)

### ✅ Completed Features

1. **Real-time Streaming Plan Generation**
   - SSE (Server-Sent Events) streaming
   - LangGraph iterative planning
   - Real-time progress bar (0-100%)
   - Korean status messages
   - LLM chunk display

2. **Redis Caching**
   - 1-hour TTL
   - 99% cost savings
   - Instant response (<1s) on cache hit

3. **Database Persistence**
   - PostgreSQL storage
   - Full CRUD operations
   - User-scoped plans
   - Survives app restart

4. **Mobile App**
   - React Native 0.75 + Expo
   - SSE client implementation
   - CreatePlanScreen with streaming UI
   - Redux state management

### 🚧 Next Steps

1. **PlanDetail Screen** - View full plan details
2. **Plan Edit/Delete UI** - Update/remove plans
3. **Error Handling** - Network errors, LLM failures
4. **OAuth Integration** - Complete authentication
5. **Video Generation** - Async video pipeline (future)

## Testing

### Quick Tests

```bash
# Service health
curl http://localhost:8000/health
curl http://localhost:8083/actuator/health
redis-cli ping
pg_isready

# API test (plan generation)
curl -X POST http://localhost:8083/api/v1/plans \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -d '{"destination":"Seoul","startDate":"2025-11-10","endDate":"2025-11-12","budget":100000}'

# Mobile app test
cd mobile && npm run ios
# Then: Plans → "+ New Plan" → Fill form → Generate
```

### Unit Tests

```bash
# Spring Boot
cd services/plan-service
./gradlew test

# Python
cd services/llm-agent
pytest
```

## Important Implementation Details

### Current (v1.3.1)

1. **Direct SSE Connection:** Mobile app connects directly to LLM Agent (port 8000) for streaming, bypassing API Gateway for performance

2. **Two-Step Save:** Streaming completes → Mobile calls Plan Service REST API → Saves to PostgreSQL

3. **Cache Strategy:** Redis key format `plan:{location}:{startDate}:{endDate}:{budget}`, 1-hour TTL

4. **Database Schema:** `plan_service.travel_plans` + `plan_service.plan_details` with foreign key relationship

5. **Timer Bug Fixed:** Used `startTimestamp` constant instead of recalculating `Date.now()` each interval

6. **Budget Mapping:** Mobile converts level (low/medium/high) to daily amount (50k/100k/200k) × days

### Future

7. **JWT Validation:** API Gateway will validate JWTs via JWKS from Auth Service
8. **Video Idempotency:** UUID-based idempotency keys for video generation
9. **External APIs:** No external APIs currently used (Kakao removed, using Gemini only)

## Git Strategy

- Commit by feature/module for easy rollback
- Test locally before pushing
- Branch naming: `feature/{service}-{feature}`, `fix/{issue}`

## Key References

### ⭐ Start Here
- **[Current Implementation Status](docs/CURRENT_IMPLEMENTATION_STATUS.md)** - Complete system state, all flows, environment setup

### Recent Implementations (2025-11-04)
- **[Database Persistence](DATABASE_PERSISTENCE_COMPLETE.md)** - PostgreSQL integration details
- **[Mobile Streaming Test](MOBILE_STREAMING_TEST_GUIDE.md)** - Mobile app testing guide
- **[Quick Test Summary](READY_TO_TEST_SUMMARY.md)** - Fast test checklist

### Development Guides
- **[No Hardcoding Guide](docs/development/NO_HARDCODING_GUIDE.md)** - LLM-first development
- **[Environment Variables](docs/development/ENVIRONMENT_VARIABLES.md)** - Configuration guide
- **[OAuth Setup](docs/development/OAUTH_ONLY_SETUP.md)** - Google/Apple OAuth

### API & Architecture
- **[Architecture Overview](docs/architecture/overview.md)** - System design
- **[Mobile API Testing](docs/api/MOBILE_API_TESTING.md)** - API reference

### Change History
- **[CHANGELOG_2025-11-04.md](CHANGELOG_2025-11-04.md)** - Today's changes
- **[docs/archive/](docs/archive/)** - Historical documents

## Documentation Structure

```
docs/
├── CURRENT_IMPLEMENTATION_STATUS.md  # ⭐ Main reference
├── README.md                         # Documentation index
├── architecture/                     # System design
├── development/                      # Development guides
├── deployment/                       # CI/CD guides
├── api/                             # API references
└── archive/                         # Historical docs
    └── 2025-11-04-streaming-implementation/
```

---

**Last Updated:** 2025-11-04
**Version:** v1.3.1
**Status:** ✅ Streaming + Database Persistence Complete
