# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project: Oddiya (v1.3 - Hybrid Self-Hosted)

AI-powered mobile travel planner with automated short-form video generation. 8-week MVP targeting real-world deployment on AWS EKS with resource-constrained infrastructure.

**Critical Rules:**
1. **ALWAYS edit existing files** instead of creating new ones. Read files first before making changes.
2. **NEVER hardcode data** - Use configuration files (YAML/JSON/properties) instead

**Documentation:** All docs are now in `docs/` directory with organized structure. See `docs/README.md` for index.

## 🚫 No Hardcoding Principle

**NEVER hardcode in code:**
- ❌ Travel destinations in switch/case statements
- ❌ Prompts in Java/Python code
- ❌ Restaurant names in if/else chains
- ❌ UI messages inline
- ❌ Configuration values
- ❌ Business logic data

**ALWAYS use configuration:**
- ✅ YAML files for structured data (`default-activities.yaml`)
- ✅ Properties files for settings (`application.yml`)
- ✅ Database for dynamic content
- ✅ Separate prompt files (`prompts/system_prompts.yaml`)
- ✅ JSON for API responses
- ✅ Environment variables for secrets

**Examples:**

❌ **Bad (Hardcoded):**
```java
if (location.equals("Seoul")) {
    activity = "Visit Gyeongbokgung Palace";
} else if (location.equals("Busan")) {
    activity = "Visit Haeundae Beach";
}
```

✅ **Good (Configuration):**
```java
// Load from YAML file
Map<String, String> activity = activityLoader.getActivity(location);
```

❌ **Bad (Hardcoded):**
```python
prompt = "Create a travel plan for Seoul with these attractions: Gyeongbokgung, Myeongdong..."
```

✅ **Good (Configuration):**
```python
# Load from prompts/planning.yaml
prompt = prompt_loader.get_planning_prompt(location="Seoul")
```

## Architecture Overview

### Microservices (7 Total)
The system uses a hybrid model: **stateless services on EKS (t3.medium Spot)**, **stateful components on dedicated EC2s (2x t2.micro)**.

```
Mobile App → ALB → Nginx Ingress → API Gateway (8080)
                                      ├→ Auth Service (8081) → Redis (t2.micro)
                                      ├→ User Service (8082) → PostgreSQL (t2.micro)
                                      ├→ Plan Service (8083) → PostgreSQL + LLM Agent (8000)
                                      └→ Video Service (8084) → SQS → Video Worker → S3/SNS

All Java services: Spring Boot 3.2 + Java 21
Python services: FastAPI (LLM Agent), Python 3.11 (Video Worker)
Database: PostgreSQL 17.0 schema-per-service model
Cache: Redis 7.4 for refresh tokens + LLM cache
```

### Critical Data Flows

**Authentication (RS256 JWT):**
- Auth Service generates RS256 tokens (1hr access, 14-day refresh)
- Provides `/.well-known/jwks.json` for public key
- API Gateway validates JWTs by fetching JWKS (cached in Redis)
- Refresh tokens stored in Redis: `refresh_token:{uuid}` → `{user_id}`

**Video Generation (Async with Idempotency):**
- Client generates UUID for `Idempotency-Key` header
- Video Service saves job (status: PENDING) + publishes to SQS → returns 202
- Video Worker polls SQS → checks DB status → processes (FFmpeg) → uploads to S3 → SNS push
- NO client polling - use push notifications

**AI Planning (LLM-Only Strategy):**
- Plan Service → LLM Agent (sync REST)
- LLM Agent → **AWS Bedrock Claude Sonnet ONLY** (no external APIs)
- Smart prompt engineering with user preferences (destination, dates, budget, interests)
- Comprehensive Korea travel knowledge built into Claude Sonnet
- Responses cached in Redis (1hr TTL) for 90%+ cost savings

### Resource Constraints

**⚠️ CRITICAL BOTTLENECK:** PostgreSQL on t2.micro (1GB RAM) - extremely slow, accepted for cost/learning.

- EKS Node: 1x t3.medium Spot (4GB RAM) - stateless only
- DB/Cache: 2x t2.micro (1GB RAM each) - performance bottleneck
- Conservative resource limits: CPU 200m-500m, Memory 256Mi-512Mi
- Security Groups: EKS nodes MUST reach t2.micro ports 5432/6379

## Development Commands

### Local Development
```bash
# Java services (Spring Boot)
cd services/{service-name}
./gradlew clean build
./gradlew test
./gradlew bootRun

# Python services
cd services/{service-name}
pip install -r requirements.txt
pytest
uvicorn main:app --reload  # FastAPI
python main.py             # Worker

# Docker build
docker build -t oddiya/{service-name}:latest .
docker-compose up  # For local dev with PostgreSQL/Redis
```

### Kubernetes Operations
```bash
# Deploy service
kubectl apply -f infrastructure/kubernetes/{service-name}/

# Check status
kubectl get pods -n oddiya
kubectl get svc -n oddiya

# Debug
kubectl logs -f deployment/{service-name} -n oddiya
kubectl describe pod {pod-name} -n oddiya
kubectl exec -it {pod-name} -n oddiya -- /bin/bash

# Port forward for testing
kubectl port-forward svc/api-gateway 8080:8080 -n oddiya
kubectl port-forward svc/auth-service 8081:8081 -n oddiya
```

### Database
```bash
# Connect to PostgreSQL (from bastion or EKS pod)
psql -h 10.0.x.x -U oddiya_user -d oddiya

# Run migrations (per service)
cd services/{service-name}/src/main/resources/db/migration
flyway migrate  # Or use Spring Boot auto-migration
```

## Project Structure

```
oddiya/
├── infrastructure/
│   ├── kubernetes/           # Deployments, Services, Ingress, HPA
│   │   ├── api-gateway/
│   │   ├── auth-service/
│   │   └── ...
│   └── terraform/            # EKS, EC2, VPC, Security Groups
├── services/
│   ├── api-gateway/          # Spring Cloud Gateway, JWT validation
│   ├── auth-service/         # OAuth 2.0, RS256 JWT, JWKS endpoint
│   ├── user-service/         # User CRUD, internal API for Auth
│   ├── plan-service/         # Travel plan CRUD, calls LLM Agent
│   ├── llm-agent/            # FastAPI, Bedrock, Kakao API
│   ├── video-service/        # Job API, SQS producer, idempotency
│   └── video-worker/         # SQS consumer, FFmpeg, S3, SNS
├── scripts/                  # DevOps automation
├── docs/                     # API specs, architecture diagrams
├── TechSpecPRD.md           # Complete technical specification
└── .cursorrules             # Detailed development guidelines
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

All services require:
```bash
# Database (t2.micro PostgreSQL private IP)
DB_HOST=10.0.x.x
DB_PORT=5432
DB_NAME=oddiya
DB_USER=admin
DB_PASSWORD=4321

# Redis (t2.micro private IP)
REDIS_HOST=10.0.x.x
REDIS_PORT=6379

# AWS
AWS_REGION=ap-northeast-2
S3_BUCKET=oddiya-storage
SQS_QUEUE_URL=https://sqs.ap-northeast-2.amazonaws.com/{account}/oddiya-video-jobs
SNS_TOPIC_ARN=arn:aws:sns:ap-northeast-2:{account}:oddiya-notifications

# Bedrock (LLM Agent only) - Claude Sonnet 4.5
BEDROCK_MODEL_ID=anthropic.claude-3-5-sonnet-20241022-v2:0
BEDROCK_REGION=us-east-1

# OAuth (Auth Service only)
GOOGLE_CLIENT_ID=***
GOOGLE_CLIENT_SECRET=***
APPLE_CLIENT_ID=***
APPLE_PRIVATE_KEY=***
```

## Development Priorities

1. **P1: Core Flow** (Week 1-5) - OAuth Login → AI-Powered Travel Planning
2. **P2: K8s Operations** (Week 1-2, 8) - EKS deployment + operations
3. **P3: Video Generation** (Week 6-7) - Async video pipeline

### Week 1-2: Infrastructure + Auth
- EKS cluster + 2x t2.micro EC2s (PostgreSQL/Redis)
- Auth Service (OAuth 2.0, RS256 JWT, JWKS)
- User Service (internal API)
- API Gateway (JWT validation)

### Week 3-5: AI Planning
- LLM Agent (Bedrock + Kakao Local API)
- Plan Service (CRUD + LLM integration)
- Redis caching (1hr TTL)

### Week 6-7: Video Pipeline
- Video Service (API + SQS producer)
- Video Worker (SQS consumer + FFmpeg)
- One template (Priority 1), SNS notifications

### Week 8: Testing + Ops
- Locust load tests (will expose t2.micro bottleneck)
- HPA configuration
- Documentation

## Testing

```bash
# Unit tests
./gradlew test                    # Spring Boot
pytest                            # Python

# Integration tests (with Testcontainers)
./gradlew integrationTest

# Load tests
cd tests/load
locust -f locustfile.py --host=http://api.oddiya.com
```

## Important Implementation Details

1. **API Gateway JWT Validation:** Fetches JWKS from Auth Service `/.well-known/jwks.json`, caches public key in Redis
2. **User Service Internal API:** `POST /internal/users` only called by Auth Service during OAuth callback
3. **Video Idempotency:** Client generates UUID, Video Service checks DB before SQS publish, Worker checks before processing
4. **Service Communication:** Internal REST calls use K8s Service DNS (e.g., `http://user-service:8082`)
5. **Database Connection Pooling:** Keep pool size low due to t2.micro RAM constraints
6. **Spot Instance Handling:** t3.medium may terminate, use node affinity for critical workloads
7. **External API Priority:** Kakao Local (P1), OpenWeatherMap/ExchangeRate (P2 - if time permits)

## Git Strategy

- Commit by feature/module for easy rollback
- Test locally (Docker Compose) before K8s deployment
- Branch naming: `feature/{service}-{feature}`, `infra/{component}`

## References

- **[Architecture Overview](docs/architecture/overview.md)** - Complete technical specification with architecture diagrams
- **[Development Plan](docs/development/plan.md)** - Detailed phased development strategy
- **[Testing Guide](docs/development/testing.md)** - Testing standards and practices
- **[.cursorrules](.cursorrules)** - Detailed coding guidelines and patterns

## Documentation Structure

All documentation is organized in the `docs/` directory:

- `docs/architecture/` - System design and architecture
- `docs/development/` - Development guides and plans
- `docs/deployment/` - CI/CD and infrastructure deployment
- `docs/api/` - API integrations and external services
- `docs/archive/` - Historical progress tracking documents
