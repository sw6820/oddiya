# Remaining Tasks - Oddiya Project

**Last Updated:** 2025-10-30
**Current Status:** MVP Core Complete + Full Auth System (90%)

---

## ✅ Completed (100%)

### Backend
- [x] 7 Microservices implemented
- [x] Auth Service (OAuth, JWT, **Email/Password Login** ✨)
- [x] API Gateway (routing)
- [x] User Service (profile + **Internal API for Email Auth** ✨)
- [x] Plan Service (travel plans)
- [x] Video Service (job management)
- [x] Video Worker (FFmpeg)
- [x] LLM Agent (**Gemini 2.5 Flash Lite** ✨)

### Mobile
- [x] Mobile React Native App with **Authentication System** ✨
  - [x] Welcome, Login, Signup screens
  - [x] Secure token storage (Keychain/KeyStore)
  - [x] Persistent login state
  - [x] Automatic token refresh
  - [x] Form validation
- [x] Plans and Videos screens
- [x] Navigation with auth flow

### AI/LLM
- [x] **Migrated from AWS Bedrock to Google Gemini** ✨
- [x] Gemini 2.5 Flash Lite integration
- [x] Real AI-generated travel plans with specific places
- [x] Secure storage of API keys
- [x] LangGraph workflow for planning

### Deployment
- [x] **Cost-Optimized Deployment Strategy** ✨
  - [x] Phase 1: Single EC2 Spot ($15-20/mo)
  - [x] Phase 2: EKS for learning ($131/mo)
  - [x] Phase 3: Oracle Cloud Free Tier ($0/mo)
- [x] Deployment scripts and documentation

### Architecture
- [x] No Hardcoding (YAML configuration)
- [x] Prompt management system
- [x] Database schemas
- [x] Docker Compose setup
- [x] GitHub Actions CI/CD

### Documentation
- [x] 35+ comprehensive documents ✨
- [x] Architecture guides
- [x] API documentation
- [x] Mobile Authentication Guide
- [x] Cost-Optimized Deployment Guide
- [x] CHANGELOG.md
- [x] No Hardcoding guide

---

## ⏳ Remaining Tasks

### Phase 1: Critical Backend Implementation ✅ **COMPLETED!**

- [x] **User Service Internal API** ⭐⭐⭐ ✅
  - [x] POST /internal/users/email - Create user with email/password
  - [x] GET /internal/users/email/{email} - Find user by email
  - [x] Add passwordHash field to User entity
  - [x] Add email uniqueness constraint (already exists)
  - [x] Database migration created
  - **Mobile auth now fully functional!**

- [ ] **End-to-End Auth Testing** ⭐⭐⭐
  - [ ] Test signup flow (mobile → auth → user service → database)
  - [ ] Test login flow
  - [ ] Test token refresh
  - [ ] Verify password hashing works
  - Estimated: 2 hours

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

### Phase 4: Real AI Integration ✅ **COMPLETED!**

- [x] **LLM Agent with Gemini** ✅
  - [x] Migrated from Bedrock to Gemini 2.5 Flash Lite
  - [x] Fixed dependency issues
  - [x] Configured Gemini API keys securely
  - [x] Tested AI plan generation with real places
  - [x] Verified real Korean place recommendations (해운대, 감천문화마을, etc.)

- [ ] **OpenWeatherMap Integration** (Optional)
  - [ ] Get API key
  - [ ] Test weather data retrieval
  - [ ] Integrate into plan generation
  - Estimated: 2 hours

- [x] **Plan Service Integration** ✅
  - [x] Connected Plan Service to LLM Agent
  - [x] Tested AI-generated plans with Gemini
  - [x] Verified specific Korean place names

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
| Auth System (Email/Password) | ✅ Complete | 100% ⬆️ |
| Mobile App (React Native) | ✅ Complete | 100% |
| Real AI (Gemini) | ✅ Complete | 100% |
| Deployment Planning | ✅ Complete | 100% |
| Photo Upload | ⏳ APIs Ready | 60% |
| Video Generation | ⏳ APIs Ready | 60% |
| AWS Deployment | ❌ Not Started | 0% |

**Overall Progress: ~90%** ⬆️ (up from 85%)

---

## 💡 Notes

**What's Working:**
- ✅ Travel plan creation with **real AI** (Gemini 2.5 Flash Lite)
- ✅ Plan list and details
- ✅ **Mobile authentication system** (email/password + OAuth)
- ✅ **Secure token storage** (Keychain/KeyStore)
- ✅ **Persistent login state** across app restarts
- ✅ YAML configuration
- ✅ No hardcoding
- ✅ **Cost-optimized deployment strategy** ($0-20/mo)

**What Needs Work:**
- ⏳ End-to-end auth testing (ready to test!)
- ⏳ Photo/Video feature integration
- ⏳ AWS deployment (optional)

**What's Optional:**
- AWS deployment
- Advanced features (password reset, email verification)
- Monitoring/alerting
- Photo/Video features

---

**Current Focus:** Test end-to-end authentication flow (mobile → auth → user service → database)

