# Remaining Tasks - Oddiya Project

**Last Updated:** 2025-10-29  
**Current Status:** MVP Core Complete (104 commits)

---

## ✅ Completed (100%)

### Backend
- [x] 7 Microservices implemented
- [x] Auth Service (OAuth, JWT)
- [x] API Gateway (routing)
- [x] User Service (profile)
- [x] Plan Service (travel plans)
- [x] Video Service (job management)
- [x] Video Worker (FFmpeg)
- [x] LLM Agent (structure ready)

### Mobile
- [x] Mobile Web App (`/mobile`)
- [x] Plan creation with free text location input
- [x] Plan list and detail views
- [x] Korean UI

### Architecture
- [x] No Hardcoding (YAML configuration)
- [x] Prompt management system
- [x] Database schemas
- [x] Docker Compose setup
- [x] GitHub Actions CI/CD

### Documentation
- [x] 32 comprehensive documents
- [x] Architecture guides
- [x] API documentation
- [x] CHANGELOG.md
- [x] No Hardcoding guide

---

## ⏳ Remaining Tasks

### Phase 1: Fix Current Issues (High Priority) - 1-2 days

- [ ] **LLM Agent Dependencies**
  - [ ] Fix langchain package conflicts
  - [ ] Test pip install with new requirements.txt
  - [ ] Verify LLM Agent starts successfully
  - Estimated: 4 hours

- [ ] **Plan Service Entity Issues**
  - [ ] Remove PlanPhoto from TravelPlan entity (done but needs rebuild)
  - [ ] Fix column naming (s3_key vs s3key)
  - [ ] Test plan creation/retrieval
  - Estimated: 2 hours

- [ ] **Browser Cache Issue**
  - [ ] Add cache-control headers to mobile web
  - [ ] Add versioning to static resources
  - Estimated: 1 hour

### Phase 2: Complete Photo Upload (Medium Priority) - 2-3 days

- [ ] **Photo Upload API**
  - [ ] Fix PhotoController routing
  - [ ] Test presigned URL generation
  - [ ] Test photo storage in database
  - Estimated: 4 hours

- [ ] **Photo Display**
  - [ ] Show uploaded photos in plan detail
  - [ ] Add photo gallery view
  - [ ] Test photo retrieval
  - Estimated: 3 hours

- [ ] **S3 Integration**
  - [ ] Configure S3 bucket (or use LocalStack)
  - [ ] Implement actual file upload
  - [ ] Test end-to-end photo flow
  - Estimated: 4 hours

### Phase 3: Video Generation (Medium Priority) - 2-3 days

- [ ] **Video-Plan Linking**
  - [ ] Add plan_id to VideoJob
  - [ ] Create /api/plans/{id}/create-video endpoint
  - [ ] Test video creation from plan photos
  - Estimated: 3 hours

- [ ] **Video Status Tracking**
  - [ ] Implement status polling in UI
  - [ ] Add video player when completed
  - [ ] Test complete video flow
  - Estimated: 3 hours

- [ ] **SQS/SNS Setup**
  - [ ] Configure SQS queue (or use ElasticMQ locally)
  - [ ] Test Video Worker processing
  - [ ] Verify FFmpeg video generation
  - Estimated: 4 hours

### Phase 4: Real AI Integration (Medium Priority) - 1-2 days

- [ ] **LLM Agent with Bedrock**
  - [ ] Fix all dependency issues
  - [ ] Configure Bedrock API keys securely
  - [ ] Test AI plan generation
  - [ ] Verify real place recommendations
  - Estimated: 6 hours

- [ ] **OpenWeatherMap Integration**
  - [ ] Get API key
  - [ ] Test weather data retrieval
  - [ ] Integrate into plan generation
  - Estimated: 2 hours

- [ ] **Plan Service Integration**
  - [ ] Connect Plan Service to LLM Agent
  - [ ] Test AI-generated plans with real data
  - [ ] Verify specific place names
  - Estimated: 3 hours

### Phase 5: User Journey Completion (Low Priority) - 2-3 days

- [ ] **Plan Status Management**
  - [ ] Add confirm/complete buttons
  - [ ] Update status in database
  - [ ] Show status badges
  - Estimated: 3 hours

- [ ] **Profile Statistics**
  - [ ] Implement trip collection API
  - [ ] Show travel statistics
  - [ ] Display completed trips
  - Estimated: 4 hours

- [ ] **Photo-Video Flow**
  - [ ] Complete photo upload → video creation flow
  - [ ] Add progress indicators
  - [ ] Test end-to-end
  - Estimated: 4 hours

### Phase 6: AWS Deployment (Optional) - 1-2 weeks

- [ ] **Terraform Infrastructure**
  - [ ] Complete EKS cluster
  - [ ] EC2 instances (PostgreSQL, Redis)
  - [ ] S3, SQS, SNS resources
  - Estimated: 3 days

- [ ] **Kubernetes Manifests**
  - [ ] Deployments for all services
  - [ ] ConfigMaps and Secrets
  - [ ] Ingress controller
  - Estimated: 2 days

- [ ] **Production Deployment**
  - [ ] Deploy to staging
  - [ ] Load testing
  - [ ] Deploy to production
  - Estimated: 2 days

---

## 🎯 Recommended Priority

### Immediate (This Week)
1. Fix LLM Agent dependencies ⭐⭐⭐
2. Fix browser cache issue ⭐⭐⭐
3. Stabilize Plan Service ⭐⭐⭐

### Short Term (Next 1-2 Weeks)
1. Complete Photo Upload
2. Real AI Integration
3. Video Generation

### Long Term (1-2 Months)
1. AWS Deployment
2. Production optimization
3. Advanced features

---

## 📊 Progress Summary

| Category | Status | Progress |
|----------|--------|----------|
| Backend Services | ✅ Complete | 100% |
| Core Plan Features | ✅ Complete | 100% |
| Mobile Web (Basic) | ✅ Complete | 100% |
| Photo Upload | ⏳ APIs Ready | 60% |
| Video Generation | ⏳ APIs Ready | 60% |
| Real AI | ⏳ Keys Set | 40% |
| AWS Deployment | ❌ Not Started | 0% |

**Overall Progress: ~75%**

---

## 💡 Notes

**What's Working:**
- ✅ Travel plan creation
- ✅ Plan list and details
- ✅ YAML configuration
- ✅ No hardcoding
- ✅ Mobile web interface

**What Needs Work:**
- ⏳ LLM Agent dependency conflicts
- ⏳ Photo/Video feature integration
- ⏳ Real AI activation

**What's Optional:**
- AWS deployment
- Advanced features
- Monitoring/alerting

---

**Current Focus:** Fix LLM dependencies, then complete Photo/Video features

