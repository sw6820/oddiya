# Oddiya Documentation

> Complete documentation for the Oddiya AI-powered travel planner

## 📖 Quick Navigation

**📋 현재 구현 상태 (최신):** [Current Implementation Status](CURRENT_IMPLEMENTATION_STATUS.md) - 2025-11-04 업데이트

**New to Oddiya?** Start here: [Getting Started Guide](GETTING_STARTED.md)

**Ready to deploy?** Go to: [Deployment Guide](deployment/DEPLOYMENT_GUIDE.md)

**Need API reference?** Check: [Mobile API Testing](api/MOBILE_API_TESTING.md)

---

## 📁 Documentation Structure

### 🚀 Getting Started

| Document | Description | Time |
|----------|-------------|------|
| **[Getting Started](GETTING_STARTED.md)** | Complete setup from scratch to deployment | 40 min |
| [Quick Reference](development/QUICK_REFERENCE.md) | Common commands and quick tips | 5 min |

### 🏗️ Architecture

| Document | Description |
|----------|-------------|
| [System Overview](architecture/overview.md) | Complete system architecture and design |
| [Token Management](architecture/TOKEN_AND_SESSION_MANAGEMENT.md) | JWT authentication flow |

### 💻 Development

| Document | Description |
|----------|-------------|
| [Environment Variables](development/ENVIRONMENT_VARIABLES.md) | All configuration settings |
| [OAuth Setup](development/OAUTH_ONLY_SETUP.md) | Google/Apple OAuth configuration |
| [No Hardcoding Guide](development/NO_HARDCODING_GUIDE.md) | LLM-first development principles |
| [Local Testing](development/LOCAL_TESTING.md) | Test backend services locally |
| [Mobile Local Testing](development/MOBILE_LOCAL_TESTING.md) | Test mobile app with local backend |
| [Configuration Management](development/CONFIGURATION_MANAGEMENT.md) | Manage configs across environments |
| [Development Plan](development/plan.md) | Phased development strategy |
| [Testing Guide](development/testing.md) | Testing standards and practices |

### 🚢 Deployment

| Document | Description | Cost |
|----------|-------------|------|
| **[Deployment Guide](deployment/DEPLOYMENT_GUIDE.md)** | Complete deployment walkthrough | $0-10/mo |
| [GitHub Actions](deployment/GITHUB_ACTIONS.md) | CI/CD pipeline setup | Free |
| [API Setup](deployment/API_SETUP_GUIDE.md) | External API integrations | Free tier |
| [Infrastructure](deployment/infrastructure.md) | Infrastructure as code | AWS free tier |
| [CI/CD](deployment/ci-cd.md) | Continuous integration/deployment | Free |

### 🔌 API & Integrations

| Document | Description |
|----------|-------------|
| [Mobile API Testing](api/MOBILE_API_TESTING.md) | REST API reference and testing |
| [External APIs](api/external-apis.md) | Third-party API integrations |

### 📱 Mobile

| Document | Description |
|----------|-------------|
| [Mobile README](../mobile/README.md) | Mobile app overview |
| [Quick Start](../mobile/QUICK_START.md) | Build and deploy mobile app (15 min) |
| [Architecture](../mobile/ARCHITECTURE.md) | Mobile app architecture |
| [Planning](../mobile/PLANNING.md) | Mobile feature planning |
| [Google OAuth Android](../mobile/GOOGLE_OAUTH_ANDROID_SETUP.md) | Android OAuth setup |
| [Google OAuth Summary](../mobile/GOOGLE_OAUTH_IMPLEMENTATION_SUMMARY.md) | OAuth implementation details |
| [Authentication](../mobile/AUTHENTICATION_GUIDE.md) | Mobile auth flow |

### 🧪 Testing

| Document | Description |
|----------|-------------|
| [Integration & Load Testing](testing/INTEGRATION_AND_LOAD_TESTING.md) | Performance testing guide |
| [How to Run Tests](testing/HOW_TO_RUN_TESTS.md) | Test execution instructions |
| [Load Tests](../tests/load/README.md) | Locust load testing setup |

---

## 🎯 Common Use Cases

### I want to...

**Set up the project for the first time**
→ [Getting Started Guide](GETTING_STARTED.md)

**Deploy to AWS**
→ [Deployment Guide](deployment/DEPLOYMENT_GUIDE.md)

**Build Android/iOS app**
→ [Mobile Quick Start](../mobile/QUICK_START.md)

**Configure Google OAuth**
→ [OAuth Setup](development/OAUTH_ONLY_SETUP.md)

**Test the API locally**
→ [Local Testing](development/LOCAL_TESTING.md)

**Understand the architecture**
→ [System Overview](architecture/overview.md)

**Set up CI/CD**
→ [GitHub Actions](deployment/GITHUB_ACTIONS.md)

**Integrate external APIs**
→ [API Setup Guide](deployment/API_SETUP_GUIDE.md)

---

## 📊 Project Overview

### Technology Stack

**Backend:**
- Spring Boot 3.2 (Java 21)
- Python FastAPI (LLM Agent)
- Redis 7.4 (caching)

**Frontend:**
- React Native 0.75 + Expo
- Redux Toolkit

**AI:**
- Google Gemini 2.0 Flash
- LangChain + LangGraph

**Infrastructure:**
- Docker Compose (dev/prod)
- AWS EC2 (free tier)
- Expo EAS Build (cloud builds)

### Project Structure

```
oddiya/
├── docs/                      # This documentation
├── services/                  # Backend microservices
│   ├── api-gateway/          # API Gateway + Web UI
│   ├── auth-service/         # OAuth authentication
│   ├── plan-service/         # Travel planning
│   └── llm-agent/            # AI planning engine
├── mobile/                    # React Native mobile app
├── scripts/                   # Automation scripts
└── infrastructure/            # Docker & deployment configs
```

---

## 🔧 Quick Commands

### Local Development
```bash
# Start all services
docker-compose up -d

# Stop services
docker-compose down

# View logs
docker-compose logs -f
```

### Mobile Development
```bash
cd mobile

# Login to Expo
eas login

# Build Android + iOS
eas build --platform all

# Run on device
npm start
```

### Testing
```bash
# Run integration tests
./scripts/test-integration.sh

# Run load tests
cd tests/load && locust -f locustfile.py
```

---

## 💰 Cost Summary

| Item | Free Tier | After 12 Months |
|------|-----------|-----------------|
| AWS EC2 t2.micro | $0 | ~$8.50/mo |
| Gemini API | $0 | $0 |
| Expo EAS Build | $0 (30/mo) | $0 or $29/mo |
| **Total** | **$0/mo** | **~$10/mo** |

**Store Deployment:**
- Google Play: $25 (one-time)
- Apple App Store: $99/year

---

## 📝 Contributing

Before making changes:

1. Read [CLAUDE.md](../CLAUDE.md) for project guidelines
2. Follow [No Hardcoding Guide](development/NO_HARDCODING_GUIDE.md)
3. Check [Development Plan](development/plan.md) for priorities
4. Run tests before committing

---

## 🆘 Need Help?

1. Check relevant documentation above
2. Search [GitHub Issues](https://github.com/YOUR_REPO/oddiya/issues)
3. Create a new issue with details

---

**Last Updated:** 2025-11-03
**Version:** 1.0.0

