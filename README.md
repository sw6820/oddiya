# Oddiya

**AI-Powered Mobile Travel Planner with Automated Video Generation**

8-week MVP building 7 microservices on AWS EKS with resource-constrained infrastructure.

## 🚀 Quick Start

```bash
# 1. Clone and setup
git clone https://github.com/sw6820/oddiya.git
cd oddiya
cp env.example .env.local

# 2. Start all services
./scripts/start-for-mobile-testing.sh

# 3. Test APIs
./scripts/test-mobile-api.sh

# 4. Access API Gateway
curl http://localhost:8080/actuator/health
```

**[Full Setup Guide →](docs/development/getting-started.md)**

## Current Status

**Full-Stack MVP Complete:** 95+ commits ✅

### Backend (7 Microservices)
- ✅ **Auth Service** - OAuth 2.0, RS256 JWT, refresh tokens
- ✅ **API Gateway** - Routing, JWT validation, mobile web app
- ✅ **User Service** - Profile management, internal API
- ✅ **Plan Service** - AI travel plans with user journey (confirm, photos, videos)
- ✅ **LLM Agent** - LangChain + LangGraph + LangSmith ready
- ✅ **Video Service** - SQS producer, plan linking
- ✅ **Video Worker** - FFmpeg pipeline, S3, SNS

### Mobile
- ✅ **Mobile Web App** - Complete user journey (plan → confirm → upload → video → profile)
- ✅ **React Native** - Foundation with Redux, API client, components

### AI & Configuration
- ✅ **No Hardcoding** - All data in YAML files
- ✅ **Prompt Management** - Separated from code
- ✅ **Real Places** - Specific restaurants and attractions
- ✅ **Free Text Input** - Any Korean city/region

### Infrastructure
- ✅ Docker Compose, Dockerfiles, CI/CD, Terraform VPC

## Architecture

7 microservices on AWS EKS + stateful components on EC2:

| Service | Tech | Port | Purpose |
|---------|------|------|---------|
| **API Gateway** | Spring Cloud Gateway | 8080 | Routing, JWT validation |
| **Auth Service** | Spring Boot | 8081 | OAuth 2.0, RS256 JWT |
| **User Service** | Spring Boot | 8082 | User profiles |
| **Plan Service** | Spring Boot | 8083 | Travel plans + AI |
| **LLM Agent** | FastAPI | 8000 | Bedrock, Kakao API |
| **Video Service** | Spring Boot | 8084 | Video jobs, SQS |
| **Video Worker** | Python | - | FFmpeg, S3, SNS |

**Infrastructure:** EKS + 2x t2.micro EC2 (PostgreSQL, Redis) + S3/SQS/SNS

**[Full Architecture →](docs/architecture/overview.md)**

## Core Features

✅ **Complete User Journey** - Plan → Confirm → Upload Photos → Generate Video → Profile Collection  
✅ **AI Travel Planning** - LangChain + LangGraph with iterative refinement  
✅ **No Hardcoding** - All data in YAML configuration files  
✅ **Prompt Management** - Separated from code, easy to update  
✅ **Free Text Location** - Any Korean city (Seoul, Busan, Jeju, Gyeongju, Jeonju, etc.)  
✅ **Real Places** - Specific restaurants and attractions (명동교자, 올레국수, 해녀의 집)  
✅ **Mobile Web App** - Fully functional with beautiful UI  
✅ **Video Generation** - FFmpeg with plan-photo linking  
✅ **OAuth Authentication** - Google (Apple ready)  
✅ **Microservices** - 7 independently deployable services  
✅ **Cloud-Ready** - Docker + Kubernetes + Terraform  
✅ **CI/CD** - Automatic testing on every push

## 📚 Documentation

**[Complete Documentation Index](docs/README.md)**

### Quick Links
- **[Quick Start](docs/development/getting-started.md)** - Set up in 5 minutes
- **[Quick Reference](docs/development/QUICK_REFERENCE.md)** - Common commands
- **[Configuration](docs/development/CONFIGURATION_MANAGEMENT.md)** - Environment setup
- **[Mobile Testing](docs/api/MOBILE_API_TESTING.md)** - Connect your mobile app
- **[Architecture](docs/architecture/overview.md)** - System design

## Deployment Ready

**MVP Complete:** All core services implemented and tested

### What's Working
- 7 microservices with unit tests
- Docker containers for all services
- CI/CD pipeline (GitHub Actions)
- Local development environment
- Database schemas and migrations

### Next Steps (Production)
1. Deploy to AWS EKS
2. Configure production secrets
3. Set up monitoring (Prometheus/Grafana)
4. Load testing with Locust
5. Production documentation

## Git Strategy

- Commit by feature/module for easy rollback
- Each commit is atomic and testable
- Branch strategy: `main` (stable) → `develop` (integration)

## Repository

- **GitHub:** https://github.com/sw6820/oddiya
- **Actions:** https://github.com/sw6820/oddiya/actions
- **Commits:** 56+ commits, 150+ files

---

**For AI Assistants:** See [CLAUDE.md](CLAUDE.md) or [.cursorrules](.cursorrules)

**Important:** This project uses t2.micro PostgreSQL (1GB RAM) which will be the **primary performance bottleneck**. This is an accepted trade-off for learning/cost reasons.
