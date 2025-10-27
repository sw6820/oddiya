# Remaining Tasks - Oddiya Project

**Last Updated:** 2025-01-27  
**Current Progress:** Phase 0-4 Complete (3/7 services)

## 📊 Status Overview

- ✅ **Completed:** Auth Service, API Gateway, LLM Agent
- ⏳ **In Progress:** Plan Service (entities created)
- ❌ **Pending:** User Service, Video Services, Deployment

## 🎯 Tasks by Phase & Priority

### P1: Core Flow (Priority 1) - 60% Complete

#### ✅ Completed
- [x] OAuth 2.0 login flow (Auth Service)
- [x] JWT validation (Auth Service + Gateway)
- [x] AI plan generation foundation (LLM Agent)

#### ⏳ Remaining (Week 3-5)
- [ ] **Plan Service** - Service layer implementation
  - [ ] Implement PlanService with business logic
  - [ ] Create PlanController with CRUD endpoints
  - [ ] Integrate with LLM Agent for AI plan generation
  - [ ] Add minimum unit tests
  - [ ] Add integration tests
- [ ] **User Service** - User profile management
  - [ ] Build complete User Service (similar to Auth Service)
  - [ ] User profile CRUD operations
  - [ ] Internal API for Auth Service integration
  - [ ] Add minimum unit tests

### P2: K8s Operations (Priority 2) - 30% Complete

#### ✅ Completed
- [x] Local infrastructure (Docker Compose)
- [x] CI/CD pipeline (GitHub Actions)
- [x] Terraform foundation (VPC, networking)

#### ⏳ Remaining
- [ ] **Terraform Modules**
  - [ ] EKS cluster configuration
  - [ ] EC2 instances for PostgreSQL (t2.micro)
  - [ ] EC2 instances for Redis (t2.micro)
  - [ ] Security groups configuration
  - [ ] AWS resources (S3, SQS, SNS)
  - [ ] Application Load Balancer (ALB)
- [ ] **Kubernetes Manifests**
  - [ ] Deployment manifests for all 7 services
  - [ ] Service manifests
  - [ ] Ingress configuration
  - [ ] ConfigMaps and Secrets
  - [ ] Health check probes

### P3: Video Generation (Priority 3) - 0% Complete

#### ❌ Not Started (Week 6-7)
- [ ] **Video Service** - Spring Boot
  - [ ] Project setup and structure
  - [ ] Video job CRUD API
  - [ ] Idempotency implementation (UUID handling)
  - [ ] SQS producer integration
  - [ ] Add minimum unit tests
- [ ] **Video Worker** - Python
  - [ ] SQS consumer implementation
  - [ ] FFmpeg integration
  - [ ] Basic video template (photo slideshow)
  - [ ] S3 upload/download
  - [ ] SNS notification trigger
  - [ ] Add minimum unit tests

### Phase 7: Testing & Deployment - 0% Complete

#### ❌ Not Started (Week 8)
- [ ] **Integration Tests**
  - [ ] Testcontainers setup for PostgreSQL
  - [ ] Testcontainers setup for Redis
  - [ ] End-to-end OAuth flow testing
  - [ ] LLM Agent integration testing
  - [ ] Video pipeline integration testing
- [ ] **Load Testing**
  - [ ] Locust test scenarios
  - [ ] Performance baseline measurement
  - [ ] t2.micro bottleneck documentation
- [ ] **Deployment Documentation**
  - [ ] Deployment runbook
  - [ ] Troubleshooting guide
  - [ ] Production readiness checklist
  - [ ] Rollback procedures

## 📋 Detailed Task Breakdown

### Immediate Next Tasks (Phase 5)

#### 1. Complete Plan Service ⏳ **IN PROGRESS**

**Priority:** High (P1)  
**Estimated Time:** 2-3 days  
**Blockers:** None

**Tasks:**
- [ ] Create PlanService class
  - [ ] Plan CRUD operations
  - [ ] LLM Agent client integration
  - [ ] User authorization checks
- [ ] Create PlanController
  - [ ] POST /api/v1/plans - Create plan with AI generation
  - [ ] GET /api/v1/plans - List user's plans
  - [ ] GET /api/v1/plans/{id} - Get single plan
  - [ ] PATCH /api/v1/plans/{id} - Update plan
  - [ ] DELETE /api/v1/plans/{id} - Delete plan
- [ ] Add error handling
- [ ] Add input validation
- [ ] Add minimum unit tests
- [ ] Add integration test with mock LLM Agent

**Files to Modify:**
- `services/plan-service/src/main/java/com/oddiya/plan/service/PlanService.java`
- `services/plan-service/src/main/java/com/oddiya/plan/controller/PlanController.java`
- `services/plan-service/src/test/` (tests)

---

#### 2. Build User Service 🔄 **PARALLEL WORK**

**Priority:** High (P1)  
**Estimated Time:** 1-2 days  
**Blockers:** None

**Tasks:**
- [ ] Initialize Spring Boot project
- [ ] Create User entity
- [ ] Create UserRepository
- [ ] Create UserService
- [ ] Create UserController
- [ ] Implement internal API for Auth Service
- [ ] Add minimum unit tests

**Files to Create:**
- `services/user-service/` (all files)

---

### Short-Term Tasks (Phase 6)

#### 3. Video Service + Worker 🔄 **WEEK 6-7**

**Priority:** Medium (P3)  
**Estimated Time:** 3-4 days  
**Blockers:** None (can start now with local file system)

**Tasks:**
- [ ] Video Service
  - [ ] Create Spring Boot project
  - [ ] Video job CRUD API
  - [ ] Idempotency key handling
  - [ ] Mock SQS (for local dev)
  - [ ] Add tests
- [ ] Video Worker
  - [ ] Create Python project
  - [ ] SQS consumer loop
  - [ ] FFmpeg integration (basic template)
  - [ ] Mock S3 (local file system)
  - [ ] Mock SNS (console logging)
  - [ ] Add tests

---

### Deployment Tasks (Phase 7)

#### 4. Complete Terraform ⏳ **WEEK 8**

**Priority:** Medium (P2)  
**Estimated Time:** 2-3 days

**Tasks:**
- [ ] EKS cluster module
- [ ] Node group configuration
- [ ] EC2 instances for PostgreSQL/Redis
- [ ] Security groups
- [ ] S3 bucket
- [ ] SQS queue + DLQ
- [ ] SNS topic
- [ ] IAM roles and policies
- [ ] Test deployment

#### 5. Kubernetes Manifests ⏳ **WEEK 8**

**Priority:** Medium (P2)  
**Estimated Time:** 1-2 days

**Tasks:**
- [ ] Deployment manifests for all services
- [ ] Service manifests
- [ ] ConfigMaps
- [ ] Secrets management
- [ ] Ingress configuration
- [ ] Test K8s deployment

---

## 🎯 Quick Action Items

### This Week (Priority Order)

1. **Complete Plan Service** (2-3 days)
   - Service layer
   - Controller
   - LLM integration
   - Tests

2. **Build User Service** (1-2 days)
   - Similar pattern to Auth Service
   - Quick implementation

3. **Test End-to-End Flow** (1 day)
   - OAuth → Plan Creation
   - Verify LLM Agent integration

### Next Week

4. **Video Services** (3-4 days)
5. **Testing** (2 days)
6. **Deployment Prep** (2 days)

---

## 📊 Progress Tracking

| Category | Completed | Remaining | Progress |
|----------|-----------|-----------|----------|
| Services | 3 | 4 | 43% |
| Infrastructure | 1 | 1 | 50% |
| Testing | Unit tests | Integration + Load | 40% |
| Deployment | CI/CD | Terraform + K8s | 30% |

**Overall Project Progress:** ~55%

---

## 🚀 How to Proceed

### Option 1: Continue Phase 5
Focus on completing Plan Service with tests.

### Option 2: Build User Service
Parallel work while Plan Service is being finished.

### Option 3: Start Video Services
Begin Video Service implementation.

### Option 4: Infrastructure First
Complete Terraform and Kubernetes setup before finishing services.

---

**Recommendation:** Complete Phase 5 (Plan Service) first, as it's critical for P1 (Core Flow) and follows the CoT strategy in the development plan.

---

See [Development Plan](plan.md) for complete roadmap and strategies.

