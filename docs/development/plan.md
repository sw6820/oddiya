# Oddiya Development Plan - Chain of Thought with Ultrathink

**Created:** 2025-01-27  
**Approach:** Local-First Development → Deploy  
**Git Strategy:** Commit by Feature/Module for Easy Rollback

---

## 🧠 Chain of Thought (CoT) Reasoning

### Problem Decomposition
We have 7 microservices to build in 8 weeks with resource constraints. The key insight is:
1. **Dependencies must be respected** (Auth before Gateway, LLM before Plan)
2. **Local testing before cloud** (save time and money)
3. **Modular commits** (enable rollback if errors appear)
4. **Accept bottleneck** (t2.micro PostgreSQL is slow by design)

### Sequential Ultrathink Planning Strategy

**Ultrathink Stage 1: Foundation (Week 1)**
- Goal: Get basic infrastructure running locally without errors
- Why: Validates environment, catches issues early
- Risk: If this fails, everything fails
- Validation: Can connect to local PostgreSQL and Redis

**Ultrathink Stage 2: Auth Flow (Week 1-2)**
- Goal: Complete OAuth login and JWT generation
- Why: Without auth, no other features can be tested
- Risk: JWT validation complexity, OAuth provider integration
- Validation: Can login with Google, get valid JWT, verify signature

**Ultrathink Stage 3: Protected Resources (Week 2-3)**
- Goal: API Gateway validates JWTs and routes to services
- Why: Establishes security boundary for all future services
- Risk: JWKS caching, token refresh flow
- Validation: Unauthorized requests blocked, valid tokens accepted

**Ultrathink Stage 4: Core Business Logic (Week 3-5)**
- Goal: AI-powered travel planning works end-to-end
- Why: This is the P1 priority feature (core value proposition)
- Risk: Bedrock costs, external API rate limits, Redis caching
- Validation: Can generate real travel plan with Kakao data + Bedrock

**Ultrathink Stage 5: Async Processing (Week 6-7)**
- Goal: Video generation pipeline works asynchronously
- Why: Demonstrates production-ready async patterns
- Risk: SQS integration, FFmpeg configuration, SNS notifications
- Validation: Video job completes without blocking, notifications sent

**Ultrathink Stage 6: Production Hardening (Week 8)**
- Goal: Documented, tested, load-tested, ready for real users
- Why: Ensures system can handle production reality
- Risk: t2.micro bottleneck exposed, need to document trade-offs
- Validation: Load test shows bottleneck, but system stable

---

## 📋 Detailed Task Breakdown

### PHASE 0: Git Repository Setup (Day 1)

**Goal:** Initialize version control with proper gitignore and initial structure

**Tasks:**
1. Initialize git repository
2. Create comprehensive `.gitignore` (Java, Python, Docker, K8s, IDE files)
3. Commit initial documentation files
4. Create `develop` branch from `main`

**Git Commits (for rollback):**
```bash
git init
git commit -m "chore: initial project documentation (TechSpecPRD.md, CLAUDE.md)"
git commit -m "chore: add .gitignore for Java, Python, Docker, Kubernetes"
git commit -m "chore: create project directory structure"
```

**Validation:**
- ✅ Git repo initialized
- ✅ Can see commits with `git log`
- ✅ `.gitignore` covers all necessary files

---

### PHASE 1: Local Development Infrastructure (Day 1-2)

**Goal:** Local Docker Compose environment that mimics production (without AWS services)

**Strategy:** Use Docker Compose to run PostgreSQL and Redis locally. For AWS services layers:
- S3 → Use LocalStack or mock with file uploads to local directory
- SQS → Use LocalStack or mock with in-memory queue
- SNS → Mock with console logging (will connect to real SNS in production)
- Bedrock → Use mock responses in development LIMIT TO 1-2 BEDROCK CALLS PER HOUR!

**Tasks:**

1. **Create `docker-compose.yml`** in project root
   - PostgreSQL 17.0 service
   - Redis 7.4 service
   - Set appropriate environment variables
   - Create init scripts for schema creation

2. **Create database migration scripts**
   - `user_service/users.sql`
   - `plan_service/travel_plans.sql` + `plan_details.sql`
   - `video_service/video_jobs.sql`
   - Schema-per-service pattern

3. **Create local environment config**
   - `.env.local` template
   - Default credentials for local dev
   - Instructions for AWS credential setup

**File Structure to Create:**
```
oddiya/
├── docker-compose.yml          # Local infrastructure
├── .env.local.example          # Environment template
├── scripts/
│   ├── init-db.sh              # Database initialization
│   └── local-setup.sh          # One-command local setup
├── infrastructure/
│   └── docker/
│       ├── postgres-init/      # DB init scripts
│       └── redis-init/         # Redis config if needed
└── .gitignore                  # Excludes .env.local
```

**Git Commits:**
```bash
git commit -m "infra: add docker-compose for local PostgreSQL and Redis"
git commit -m "infra: add database schema migrations for all services"
git commit -m "infra: add local setup scripts and documentation"
```

**Validation:**
- ✅ Run `docker-compose up` successfully
- ✅ Can connect to PostgreSQL with `psql`
- ✅ Can connect to Redis with `redis-cli`
- ✅ All schemas created successfully
- ✅ `docker-compose down` cleans up properly

**Common Issues to Watch:**
- Port conflicts (5432, 6379)
- Volume persistence
- Network connectivity between services

---

### PHASE 2: Auth Service & User Service (Week 1-2)

**Goal:** OAuth 2.0 flow working with RS256 JWT generation

**Dependency:** Phase 1 must be complete (PostgreSQL + Redis running)

**Auth Service Tasks:**

1. **Project Setup**
   - Create `services/auth-service/` directory
   - Initialize Spring Boot 3.2 with Java 21
   - Add dependencies: Spring Security OAuth2, JJWT for RS256, Spring Data Redis
   - Create `application.yml` with local PostgreSQL and Redis config

2. **Core Authentication Logic**
   - **Controller Layer:** `OAuthController` with endpoints:
     - `GET /oauth2/authorize/{provider}` - Initiate OAuth flow
     - `POST /oauth2/callback/{provider}` - Handle OAuth callback
     - `POST /oauth2/token` - Token refresh
     - `GET /.well-known/jwks.json` - Public key for JWT validation
   - **Service Layer:** `OAuthService`, `JwtService`, `UserLookupService`
   - **Repository Layer:** Redis for refresh tokens
   - **Security Config:** OAuth2 client configuration for Google/Apple

3. **RS256 JWT Implementation**
   - Generate RSA key pair (store in config class)
   - Create JWKS endpoint with public key
   - Generate access tokens (1hr expiry, RS256)
   - Generate refresh tokens (UUID, 14-day expiry)
   - Store refresh tokens in Redis: `refresh_token:{uuid}` → `{user_id}`

4. **Integration with User Service**
   - Call `POST /internal/users` on User Service during first login
   - Handle user not found → create new user
   - Handle user found → return existing user

5. **Error Handling**
   - Create `GlobalExceptionHandler` with `@ControllerAdvice`
   - Handle OAuth errors (invalid code, network issues)
   - Handle JWT errors (expired, invalid signature)
   - Return proper HTTP status codes

**User Service Tasks:**

1. **Project Setup**
   - Create `services/user-service/` directory
   - Initialize Spring Boot 3.2 with Java 21
   - Add dependencies: Spring Data JPA, PostgreSQL driver
   - Configure schema: `user_service.users` table

2. **Core User Management**
   - **Controller Layer:** `UserController` with endpoints:
     - `GET /users/me` - Get current user (requires JWT)
     - `PATCH /users/me` - Update current user (requires JWT)
     - `POST /internal/users` - Create/find user (INTERNAL, no JWT required)
   - **Service Layer:** `UserService` with business logic
   - **Repository Layer:** JPA repository for user entity
   - **Entity:** `User` with fields (id, email, name, provider, provider_id)

3. **Internal API Security**
   - Allow only requests from Auth Service
   - Use simple API key or internal service mesh secret
   - Document in API spec

**Testing Strategy:**

1. **Unit Tests**
   - Mock external dependencies (Google OAuth, User Service)
   - Test JWT generation and parsing
   - Test OAuth callback flow

2. **Integration Tests**
   - Use Testcontainers for PostgreSQL and Redis
   - Test full OAuth flow (mock Google provider)
   - Test JWKS endpoint
   - Test token refresh flow

3. **Manual Testing (with real Google OAuth)**
   - Get Google Client ID and Secret from Google Cloud Console
   - Test redirect flow
   - Inspect JWT token with jwt.io
   - Verify JWKS endpoint returns correct public key

**File Structure:**
```
services/
├── auth-service/
│   ├── build.gradle
│   ├── src/
│   │   └── main/
│   │       ├── java/com/oddiya/auth/
│   │       │   ├── controller/OAuthController.java
│   │       │   ├── service/{OAuthService, JwtService, UserLookupService}.java
│   │       │   ├── config/{SecurityConfig, JwtConfig}.java
│   │       │   ├── dto/{TokenRequest, TokenResponse}.java
│   │       │   └── exception/GlobalExceptionHandler.java
│   │       └── resources/
│   │           └── application.yml
│   ├── tests/
│   ├── Dockerfile
│   └──女主
```

**Git Commits (Separate by Feature):**
```bash
git commit -m "feat(auth): initialize Spring Boot project with dependencies"
git commit -m "feat(auth): add OAuth2 controller with Google provider"
git commit -m "feat(auth): implement RS256 JWT generation with JWKS endpoint"
git commit -m "feat(auth): add refresh token storage in Redis"
git commit -m "feat(auth): add error handling and validation"
git commit -m "test(auth): add unit and integration tests"

git commit -m "feat(user): iterative Spring Boot project structure"
git commit -m "feat(user): implement user CRUD endpoints"
git commit -m "feat(user): add internal API for Auth Service"
git commit -m "test(user): add tests for user service"
```

**Validation Checklist:**
- ✅ Auth Service starts without errors
- ✅ Can visit `GET /oauth2/authorize/google` and redirects to Google
- ✅ After OAuth, receives authorization code
- ✅ Calling `POST /oauth2/callback/google` with code returns JWT
- ✅ JWT is valid RS256 format (verify with jwt.io)
- ✅ `GET /.well-known/jwks.json` returns public key
- ✅ Refresh token flow works
- ✅ User Service endpoints work
- ✅ Internal API allows Auth Service to create users

---

### PHASE 3: API Gateway (Week 2)

**Goal:** Spring Cloud Gateway validates JWTs and routes to backend services

**Dependency:** Auth Service must be running with JWKS endpoint

**Tasks:**

1. **Project Setup**
   - Create `services/api-gateway/` directory
   - Initialize Spring Cloud Gateway project
   - Add dependencies: Spring Cloud Gateway, Spring Data Redis, OAuth2 client

2. **JWT Validation Filter**
   - Create custom `JwtValidationFilter`
   - Fetch public key from Auth Service `/jwks.json` endpoint
   - Cache public key in Redis (TTL: 1hr)
   - Validate RS256 signature on every request
   - Extract user_id from JWT and add to request headers
   - Block requests without valid JWT (except public endpoints)

3. **Routing Configuration**
   - Configure routes in `application.yml`:
     - `/api/auth/**` → Auth Service (8081)
     - `/api/users/**` → User Service (8082)
     - `/api/plans/**` → Plan Service (8083) [future]
     - `/api/videos/**` → Video Service (8084) [future]
   - Add rate limiting (requests per IP per minute)

4. **CORS Configuration**
   - Allow mobile app origin
   - Configure allowed methods and headers

5. **Health Checks**
   - Expose `/actuator/health` endpoint
   - Check connectivity to backend services

**Testing Strategy:**

1. **Test JWT Validation**
   - Send request without JWT → expect 401
   - Send request with invalid JWT → expect 401
   - Send request with expired JWT → expect 401
   - Send request with valid JWT → route to backend

2. **Test JWKS Caching**
   - Verify first request fetches from Auth Service
   - Verify subsequent requests use Redis cache
   - Test cache expiration

3. **Test Routing**
   - Verify requests reach correct backend services
   - Verify 502 errors if backend is down

**File Structure:**
```
services/api-gateway/
├── build.gradle
├── src/main/
│   ├── java/com/oddiya/gateway/
│   │   ├── filter/JwtValidationFilter.java
│   │   ├── config/GatewayConfig.java
│   │   └── service/JwksService.java
│   └── resources/application.yml
├── Dockerfile
└── README.md
```

**Git Commits:**
```bash
git commit -m "feat(gateway): initialize Spring Cloud Gateway project"
git commit -m "feat(gateway): implement JWT validation filter with JWKS caching"
git commit -m "feat(gateway): add routing configuration for all services"
git commit -m "feat(gateway): add rate limiting and CORS configuration"
git commit -m "test(gateway): add JWT validation and routing tests"
```

**Validation Checklist:**
- ✅ API Gateway starts on port 8080
- ✅ Unauthorized requests blocked (401)
- ✅ Valid JWT allows request through
- ✅ Requests routed to correct backend services
- ✅ JWKS cached in Redis

---

### PHASE 4: LLM Agent Service (Week 3-4)

**Goal:** FastAPI service that orchestrates AI plan generation using Bedrock

**Dependency:** Redis must be running (for caching)

**⚠️ COST WARNING:** AWS Bedrock is not free. Use MOCK in development except for 1-2 real test calls.

**Tasks:**

1. **Project Setup**
   - Create `services/llm-agent/` directory
   - Initialize FastAPI with Python 3.11
   - Add dependencies: boto3 (for Bedrock), httpx (for external APIs), redis-py
   - Create `requirements.txt` with versions

2. **Core LLM Integration**
   - Create `BedrockService` class to call Claude Sonnet
   - Implement Function Calling pattern (structured output)
   - Design prompt template for travel planning
   - Handle Bedrock API errors and retries
   - **CRITICAL:** Add mock mode for development (avoid Bedrock costs)

3. **External API Integration (Priority 1)**
   - Integrate **Kakao Local API** for place search
   - Get Kakao API key from Kakao Developers
   - Create `KakaoApiService` for place search
   - Handle rate limits and errors

4. **Caching Layer**
   - Create `CacheService` with Redis
   - Cache LLM responses (1hr TTL)
   - Cache key: `llm_plan:{location}:{dates}`

5. **API Endpoint**
   - `POST /api/v1/generate-plan`
   - Request: `{location, start_date, end_date, preferences}`
   - Response: `{daily_plans: [{day, locations, activities, restaurants}]}`
   - Use Pydantic models for validation

6. **Error Handling**
   - Handle Bedrock errors
   - Handle external API failures (degrade gracefully)
   - Return meaningful error messages

**Development Strategy (Mock Mode):**

Create a `BedrockMockService` that returns realistic but hardcoded responses:
```python
class BedrockMockService:
    def generate_plan(self, location, dates, preferences):
        # Return realistic JSON without calling Bedrock
        return {
            "daily_plans": [
                {"day": 1, "locations": [...], "activities": [...], "restaurants": [...]}
            ]
        }
```

Switch between mock and real using environment variable: `USE_BEDROCK_MOCK=true`

**Testing Strategy:**

1. **Unit Tests**
   - Test Bedrock service (mock responses)
   - Test Kakao API integration (mock with httpx mock)
   - Test caching logic

2. **Integration Tests (with 1 real Bedrock call)**
   - Make 1 real call to Bedrock to validate integration
   - Cache result for subsequent tests
   - Test full flow with Kakao API

3. **Mock Mode Testing**
   - Verify mock returns realistic responses
   - Verify faster than real Bedrock calls

**File Structure:**
```
services/llm-agent/
├── main.py                     # FastAPI app
├── services/
│   ├── bedrock_service.py      # Bedrock + mock
│   ├── kakao_service.py        # Kakao Local API
│   └── cache_service.py        # Redis caching
├── models/
│   ├── request.py              # Pydantic request models
│   └── response.py             # Pydantic response models
├── requirements.txt
├── Dockerfile
└── README.md
```

**Git Commits:**
```bash
git commit -m "feat(llm-agent): initialize FastAPI project with dependencies"
git commit -m "feat(llm-agent): add Bedrock service with mock mode for development"
git commit -m "feat(llm-agent): integrate Kakao Local API for place search"
git commit -m "feat(llm-agent): implement Redis caching layer"
git commit -m "feat(llm-agent): add generate-plan endpoint with Pydantic models"
git commit -m "test(llm-agent): add unit and integration tests with 1 real Bedrock call"
```

**Validation Checklist:**
- ✅ LLM Agent starts on port 8000
- ✅ Mock mode returns realistic responses
- ✅ Kakao API integration works
- ✅ Redis caching works (1hr TTL)
- ✅ Made 1 real Bedrock call to validate (check AWS bill!)
- ✅ Error handling works

---

### PHASE 5: Plan Service (Week 4-5)

**Goal:** Travel plan CRUD with LLM Agent integration

**Dependency:** LLM Agent must be running

**Tasks:**

1. **Project Setup**
   - Create `services/plan-service/` directory
   - Initialize Spring Boot 3.2 with Java 21
   - Add dependencies: Spring Data JPA, RestTemplate for HTTP calls
   - Configure schemas: `plan_service.travel_plans` + `plan_details`

2. **Core Plan Management**
   - **Controller Layer:** `PlanController` with endpoints:
     - `POST /api/v1/plans` - Create new plan (with AI generation)
     - `GET /api/v1/plans` - List user's plans
     - `GET /api/v1/plans/{id}` - Get single plan
     - `PATCH /api/v1/plans/{id}` - Update plan
     - `DELETE /api/v1/plans/{id}` - Delete plan
   - **Service Layer:** `PlanService` with business logic
   - **Repository Layer:** JPA repositories for plans and details
   - **Entities:** `TravelPlan`, `PlanDetail`

3. **AI Plan Generation Integration**
   - Create `LlmClientService` to call LLM Agent via REST
   - Call `POST /api/v1/generate-plan` on LLM Agent
   - Parse response and save to database
   - Handle LLM Agent errors (return error to client)

4. **User Context**
   - Extract user_id from JWT (injected by API Gateway)
   - Ensure users can only access their own plans

5. **Validation**
   - Validate plan dates (end_date >= start_date)
   - Validate required fields

**Testing Strategy:**

1. **Unit Tests**
   - Mock LLM Agent calls
   - Test plan CRUD logic
   - Test validation

2. **Integration Tests**
   - Test with real LLM Agent (mock mode)
   - Test database operations with Testcontainers

**File Structure:**
```
services/plan-service/
├── build.gradle
├── src/main/
│   ├── java/com/oddiya/plan/
│   │   ├── controller/PlanController.java
│   │   ├── service/{PlanService, LlmClientService}.java
│   │   ├── repository/{PlanRepository, PlanDetailRepository}.java
│   │   ├── entity/{TravelPlan, PlanDetail}.java
│   │   └── dto/{PlanRequest, PlanResponse}.java
│   └── resources/application.yml
├── Dockerfile
└── README.md
```

**Git Commits:**
```bash
git commit -m "feat(plan): initialize Spring Boot project with database schema"
git commit -m "feat(plan): implement travel plan CRUD endpoints"
git commit -m "feat(plan): integrate LLM Agent for AI plan generation"
git commit -m "feat(plan): add user context and authorization"
git commit -m "test(plan): add unit and integration tests"
```

**Validation Checklist:**
- ✅ Plan Service starts without errors
- ✅ Can create plan with AI generation
- ✅ AI response saved to database correctly
- ✅ User can only access own plans
- ✅ Date validation works

---

### PHASE 6: Video Services (Week 6-7)

**Goal:** Asynchronous video processing pipeline with SQS and SNS

**Dependency:** PostgreSQL must be running

**⚠️ AWS Cost Alert:** SQS and SNS have free tiers. Use LocalStack for local development.

**Video Service Tasks:**

1. **Project Setup**
   - Create `services/video-service/` directory
   - Initialize Spring Boot 3.2 with Java 21
   - Add dependencies: AWS SDK for SQS, Spring Data JPA
   - Configure schema: `video_service.video_jobs` with idempotency_key

2. **Core Video Job Management**
   - **Controller Layer:** `VideoController` with endpoints:
     - `POST /api/v1/videos` - Create video job (with Idempotency-Key header)
     - `GET /api/v1/videos/{id}` - Get job status
   - **Request strictly requires:** `photo_urls[]`, `template`, `Idempotency-Key` header
   - **Response:** `{job_id, status, created_at}` (202 Accepted)

3. **Idempotency Implementation**
   - Check if `idempotency_key` exists in database
   - If exists, return existing job (don't create duplicate)
   - If new, create job with status `PENDING`
   - Publish to SQS queue
   - Return `202 Accepted` immediately

4. **SQS Integration**
   - Create SQS client
   - Publish job message with job_id
   - Handle SQS errors

**Video Worker Tasks:**

1. **Project Setup**
   - Create `services/video-worker/` directory
   - Initialize Python 3.11 project
   - Add dependencies: boto3 (SQS, S3, SNS), FFmpeg-python, Pillow
   - Create `requirements.txt`

2. **SQS Consumer**
   - Long-poll SQS queue (20s wait)
   - Receive messages (JSON with job_id)
   - Delete message after processing

3. **Idempotency Check**
   - Fetch job from database
   - Check if status == 'PENDING'
   - If not PENDING, skip (already processed)
   - Update status to 'PROCESSING'

4. **Video Generation**
   - Download photos from S3 URLs
   - Create temporary directory
   - Use FFmpeg to generate video (start with 1 basic template)
   - Basic template: concatenate photos with fade transition (3s each)
   - Command: `ffmpeg -i photo1.jpg -i photo2.jpg ... -filter_complex "..." output.mp4`

5. **Upload and Notify**
   - Upload generated video to S3
   - Update database: status='COMPLETED', video_url=s3_url
   - Publish to SNS topic (job completed)

6. **Error Handling**
   - Update status='FAILED' on any error
   - Log error details
   - Consider retry logic (exponential backoff)

**Development Strategy:**

For local development without AWS:
- **Video Service:** Mock SQS publishing (use in-memory queue or LocalStack)
- **Video Worker:** Use LocalStack for SQS/S3, use local file system for S3

**Testing Strategy:**

1. **Unit Tests**
   - Test idempotency logic
   - Mock SQS calls
   - Mock FFmpeg calls

2. **Integration Tests**
   - Test full flow with LocalStack
   - Test video generation with real photos
   - Test idempotency (duplicate requests)

**File Structure:**
```
services/
├── video-service/
│   ├── build.gradle
│   ├── src/main/java/com/oddiya/video/...
│   └── Dockerfile
└── video-worker/
    ├── main.py
    ├── video_processor.py
    ├── requirements.txt
    └── Dockerfile
```

**Git Commits:**
```bash
git commit -m "feat(video): initialize Video Service with database schema"
git commit -m "feat(video): implement idempotency with UUID header"
git commit -m "feat(video): add SQS integration for job queue"
git commit -m "feat(worker): initialize Video Worker with SQS consumer"
git commit -m "feat(worker): implement FFmpeg video generation (basic template)"
git commit -m "feat(worker): add S3 upload and SNS notification"
git commit -m "test: add integration tests for video pipeline"
```

**Validation Checklist:**
- ✅ Video Service accepts job with Idempotency-Key
- ✅ Duplicate requests return same job_id
- ✅ Video Worker picks up job from SQS
- ✅ Video generated successfully
- ✅ Video uploaded to S3
- ✅ SNS notification sent (check console log)
- ✅ GET /api/v1/videos/{id} returns video_url

---

### PHASE 7: Testing & Deployment Preparation (Week 8)

**Goal:** System is documented, tested, and ready for production deployment

**Tasks:**

1. **Comprehensive Testing**
   - Run all unit tests across all services
   - Run integration tests
   - Fix any failing tests

2. **Load Testing**
   - Set up Locust (load testing framework)
   - Create test scenarios:
     - OAuth login flow
     - Plan generation
     - Video job creation
   - Run load test (expect to see t2.micro bottleneck)
   - Document results

3. **Docker & Kubernetes Preparation**
   - Ensure all services have Dockerfiles
   - Test Docker builds locally
   - Create Kubernetes manifests:
     - Deployments
     - Services
     - ConfigMaps
     - Secrets
   - Create deployment scripts

4. **Documentation**
   - Write README.md for each service
   - Create API documentation (OpenAPI/Swagger)
   - Document deployment process
   - Document known issues (t2.micro bottleneck)

5. **Git Tag Release**
   - Tag v0.1.0 (MVP)
   - Create release notes

**Git Commits:**
```bash
git commit -m "test: add comprehensive load testing with Locust"
git commit -m "docs: add README for each service"
git commit -m "infra: add Kubernetes manifests and deployment scripts"
git commit -m "docs: create deployment guide and known issues"
git commit -m "chore: prepare v0.1.0 release"
```

**Validation Checklist:**
- ✅ All tests pass
- ✅ Load test completed (results documented)
- ✅ All services build in Docker
- ✅ Kubernetes manifests ready
- ✅ Documentation complete
- ✅ Git tagged v0.1.0

---

## 🚀 Deployment Strategy

### Local → Production Flow

1. **Local Development** (Phases 0-6)
   - Use Docker Compose
   - Mock AWS services (LocalStack or mock implementations)
   - Test thoroughly

2. **Local AWS Testing** (Optional)
   - Connect to real AWS services
   - Test with 1-2 real Bedrock calls
   - Test SQS/SNS integration

3. **Kubernetes Deployment** (Week 8)
   - Deploy to EKS cluster
   - Set up load balancer
   - Configure DNS
   - Monitor and debug

### Rollback Strategy

Since we're committing by feature, rollback is simple:

```bash
# View commits
git log --oneline

# Rollback specific feature
git revert <commit-hash>

# Rollback to specific point
git reset --hard <commit-hash>
```

---

## 📝 Gemini MCP Note

**Current Status:** I don't have direct access to Gemini MCP tools in this environment. However, I can:
1. Generate code following the specifications
2. Create architecture discussions in comments
3. Debate technical decisions with you directly

**If you have Gemini MCP access:**
- You could use Gemini to review this plan
- Debate architectural decisions
- Get second opinions on technology choices

**My Rationale for This Plan:**
- Local-first reduces AWS costs during development
- Commit by feature enables safe rollback
- Mock Bedrock in development (except 1-2 validation calls)
- Use LocalStack for AWS services in local dev
- Sequential ultrathink ensures dependencies are respected

---

## ✅ Success Criteria

**Phase 0-7 Complete When:**
1. ✅ Git repository initialized and organized
2. ✅ All 7 microservices built and tested locally
3. ✅ Full OAuth flow works end-to-end
4. ✅ AI plan generation works (with mock or limited real calls)
5. ✅ Video generation pipeline works (local file system)
6. ✅ All tests pass
7. ✅ Documentation complete
8. ✅ Ready for Kubernetes deployment

**No Blockers for Deployment When:**
1. ✅ No compile errors
2. ✅ No runtime errors in local testing
3. ✅ Database migrations work
4. ✅ Environment variables documented
5. ✅ Kubernetes manifests ready

---

## 🎯 Next Steps (Start Here!)

**Immediate Action (Today):**
1. Run: `git init`
2. Create `.gitignore` (I'll help with this)
3. Stage and commit existing files
4. Create project directory structure

Ready to begin? Let's start with Phase 0!

