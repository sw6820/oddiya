# Changelog

All notable changes to the Oddiya project.

## [Unreleased]

### Added - Latest Changes (Commits 80-95)
- Complete user journey implementation (plan → confirm → photos → video → profile)
- No Hardcoding principle enforced across project
- YAML-based configuration for travel activities (`default-activities.yaml`)
- Prompt management system separated from code (`prompts/system_prompts.yaml`)
- Free text location input (any Korean city, not just Seoul/Busan/Jeju)
- Specific real place names (restaurants, attractions)
- Korean language UI throughout mobile web
- Profile statistics and trip collection API
- Photo upload with S3 presigned URLs
- Plan status management (DRAFT, CONFIRMED, IN_PROGRESS, COMPLETED)
- Video-plan linking (video_jobs.plan_id)
- LangChain + LangGraph + LangSmith integration
- Prompt management comparison guide (YAML vs Text)
- User journey documentation
- Service communication architecture docs

### Changed
- Location selection from dropdown to free text input
- Travel activities from hardcoded switch/case to YAML configuration
- LLM prompts from inline code to external YAML files
- Plan detail UI with structured layout and Korean labels
- API endpoints to support complete user journey

### Removed
- Hardcoded travel activities from PlanService.java (100+ lines)
- Kakao Local API dependency (Claude has built-in Korea knowledge)
- TechSpecPRD.md from root (moved to docs/architecture/overview.md)

## [1.0.0] - 2025-10-28

### Backend Services (Commits 1-40)
- Auth Service with OAuth 2.0 and RS256 JWT
- API Gateway with Spring Cloud Gateway
- User Service with profile management
- Plan Service with travel plan CRUD
- Video Service with SQS integration
- Video Worker with FFmpeg
- LLM Agent with Bedrock integration

### Infrastructure (Commits 40-60)
- Docker Compose for local development
- PostgreSQL with schema-per-service design
- Redis for caching and sessions
- GitHub Actions CI/CD pipeline
- Terraform VPC foundation
- Dockerfiles for all 7 services

### Testing (Commits 60-70)
- Unit tests for all services
- Integration tests with Testcontainers
- Load testing with Locust
- Mobile API testing scripts
- End-to-end testing scenarios

### Mobile (Commits 70-80)
- React Native project setup
- Redux store with 4 slices
- API client with auto token refresh
- Reusable component library
- Mobile web app with interactive UI

### Documentation (Commits 40-95)
- 30+ comprehensive documents
- Architecture guides
- API setup guides
- Testing guides
- User journey documentation
- No Hardcoding guide

---

## File Structure Changes

### Moved/Reorganized:
- `TechSpecPRD.md` → `docs/architecture/overview.md`
- `DEVELOPMENT_PLAN.md` → `docs/development/plan.md`
- `TESTING_GUIDE.md` → `docs/development/testing.md`
- Various progress docs → `docs/archive/`

### Added:
- `services/plan-service/src/main/resources/default-activities.yaml`
- `services/llm-agent/prompts/system_prompts.yaml`
- `services/llm-agent/prompts/planning.txt`
- `docs/development/NO_HARDCODING_GUIDE.md`
- `docs/architecture/USER_JOURNEY.md`
- `docs/architecture/SERVICE_COMMUNICATION.md`
- `docs/architecture/LANGGRAPH_WORKFLOW.md`
- `docs/architecture/PROMPT_MANAGEMENT_COMPARISON.md`

---

## Statistics

- **Total Commits:** 95+
- **Total Files:** 280+
- **Services:** 7 microservices
- **Documentation:** 30+ files
- **Tests:** Unit + Integration + Load
- **Lines of Code:** 15,000+

---

**Status:** MVP Complete, Ready for Real AI Integration and AWS Deployment

