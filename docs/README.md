# Oddiya Documentation

Welcome to the Oddiya project documentation. This directory contains all technical documentation, guides, and references organized by topic.

## 📚 Documentation Organization

### 🚀 Quick Access (Start Here!)

| For... | Read This |
|--------|-----------|
| **New to project** | [Getting Started](development/getting-started.md) |
| **Need commands** | [Quick Reference](development/QUICK_REFERENCE.md) |
| **Mobile developer** | [Mobile API Testing](api/MOBILE_API_TESTING.md) |
| **DevOps engineer** | [GitHub Actions](deployment/GITHUB_ACTIONS.md) |

### 📂 By Category

#### Architecture (1 file)
- **[System Overview](architecture/overview.md)** - Complete technical specification

#### Development (6 files)
- **[Getting Started](development/getting-started.md)** - 5-minute setup guide
- **[Quick Reference](development/QUICK_REFERENCE.md)** - One-page command reference
- **[Configuration Management](development/CONFIGURATION_MANAGEMENT.md)** - Environment & secrets
- **[Local Testing](development/LOCAL_TESTING.md)** - Test before deployment
- **[Mobile Local Access](development/MOBILE_LOCAL_ACCESS.md)** - Connect mobile devices
- **[Development Plan](development/plan.md)** - Complete roadmap with CoT
- **[Testing Standards](development/testing.md)** - Unit testing guidelines

#### Deployment (3 files)
- **[GitHub Actions](deployment/GITHUB_ACTIONS.md)** - Automatic testing on push
- **[CI/CD Pipeline](deployment/ci-cd.md)** - Build and deploy automation
- **[Infrastructure](deployment/infrastructure.md)** - Terraform & AWS setup

#### APIs (2 files)
- **[Mobile API Testing](api/MOBILE_API_TESTING.md)** - Complete API reference
- **[External APIs](api/external-apis.md)** - Kakao, Weather, Exchange APIs

#### Archive (8 files)
- Historical documentation preserved for reference

### 🔍 Find Documentation

- **[Alphabetical Index](INDEX.md)** - All docs A-Z
- **[GitHub Repository](https://github.com/sw6820/oddiya)** - Source code

## 🏗️ Project Status

**Full-Stack MVP Complete:** 95+ commits ✅

### Services
- ✅ 7 Microservices (Auth, Gateway, User, Plan, Video×2, LLM Agent)
- ✅ Complete User Journey (plan → confirm → photos → video → profile)
- ✅ Mobile Web App (interactive, Korean UI)
- ✅ React Native foundation

### Key Achievements
- ✅ **No Hardcoding** - All data externalized to YAML
- ✅ **Prompt Management** - Separated from code
- ✅ **LangChain + LangGraph** - Iterative AI refinement
- ✅ **Free Text Location** - Any Korean city/region
- ✅ **Real Places** - Specific named locations
- ✅ **Plan Status** - DRAFT → CONFIRMED → COMPLETED
- ✅ **Photo Upload** - S3 presigned URLs
- ✅ **Video Linking** - Videos connected to plans
- ✅ **Profile Stats** - Travel collection with statistics

**Ready for:**
- ✅ Local testing (fully functional)
- ✅ Real AI (with Bedrock API keys)
- ⏳ AWS deployment (Terraform + K8s)

---

**Last Updated:** 2025-10-29 (95 commits, 280+ files)

