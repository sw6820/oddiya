# Oddiya Project

**AI-Powered Mobile Travel Planner with Automated Video Generation**

## Quick Start

This is an 8-week MVP project building 7 microservices on AWS EKS with resource-constrained infrastructure.

### Current Status

✅ **Phase 0-4 Complete:** Git, Infrastructure, Auth, Gateway, LLM Agent with tests  
✅ **CI/CD Ready:** GitHub Actions configured  
✅ **IaC Ready:** Terraform infrastructure setup  
📋 **Next:** Phase 5-7 - Complete remaining services and deploy

### Project Architecture

- **7 Microservices:** API Gateway + Auth/User/Plan/Video Services + LLM Agent + Video Worker
- **Hybrid Infrastructure:** Stateless on EKS (t3.medium), Stateful on EC2s (2x t2.micro)
- **CI/CD:** GitHub Actions for automated testing and Docker builds
- **IaC:** Terraform for AWS infrastructure provisioning
- **Local-First Development:** Docker Compose → Terraform Deploy → Kubernetes

### Development Priorities

1. **P1: Core Flow** - OAuth Login → AI-Powered Travel Planning (Week 1-5)
2. **P2: K8s Operations** - EKS deployment + operations (Week 1-2, 8)
3. **P3: Video Generation** - Async video processing pipeline (Week 6-7)

### Getting Started

See [DEVELOPMENT_PLAN.md](DEVELOPMENT_PLAN.md) for detailed Chain of Thought planning and ultrathink strategy.

### Key Documents

- **[TechSpecPRD.md](TechSpecPRD.md)** - Complete technical specification
- **[DEVELOPMENT_PLAN.md](DEVELOPMENT_PLAN.md)** - Phased development plan with CoT reasoning
- **[CLAUDE.md](CLAUDE.md)** - Claude Code assistant guidelines

### Git Strategy

- Commit by feature/module for easy rollback
- Each commit is atomic and testable
- Branch strategy: `main` (stable) → `develop` (integration)

### Current Commit History

```
1d9cde4 docs: add development plan with CoT ultrathink strategy
a772754 chore: add .gitignore for Java, Python, Docker, Kubernetes
b665109 docs: initial project documentation (TechSpecPRD.md, CLAUDE.md)
```

### Completed ✅

- **Phase 0:** Git repository and development plan
- **Phase 1:** Local infrastructure (Docker Compose)
- **Phase 2:** Auth Service (OAuth + JWT + tests)
- **Phase 3:** API Gateway (routing configured)
- **Phase 4:** LLM Agent (FastAPI + Bedrock + tests)
- **CI/CD:** GitHub Actions pipeline
- **IaC:** Terraform for AWS

### Next Steps

1. Phase 5: Complete Plan Service with LLM integration
2. Phase 6: Build Video Services (Video Service + Worker)
3. Phase 7: Testing, documentation, and deployment

See [FINAL_SUMMARY.md](FINAL_SUMMARY.md) for complete status.

---

**Important:** This project uses a t2.micro PostgreSQL (1GB RAM) which will be the **primary performance bottleneck**. This is an accepted trade-off for learning/cost reasons.

