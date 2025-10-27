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

**MVP Complete:** All 7 services implemented ✅

- ✅ **Auth Service** - OAuth 2.0, RS256 JWT, refresh tokens
- ✅ **API Gateway** - Spring Cloud Gateway with JWT validation
- ✅ **User Service** - Profile management, internal API
- ✅ **Plan Service** - Travel plans CRUD + LLM integration
- ✅ **LLM Agent** - Bedrock integration, Kakao API, Redis caching
- ✅ **Video Service** - SQS producer, idempotency handling
- ✅ **Video Worker** - FFmpeg pipeline, S3 integration, SNS notifications
- ✅ **Infrastructure** - Docker Compose, Dockerfiles, CI/CD, Terraform VPC

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

✅ **OAuth Authentication** - Google (Apple ready)  
✅ **AI Travel Planning** - AWS Bedrock + Kakao Local API  
✅ **Video Generation** - Automated short-form videos with FFmpeg  
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
