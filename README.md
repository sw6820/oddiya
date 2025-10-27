# Oddiya Project

**AI-Powered Mobile Travel Planner with Automated Video Generation**

## Quick Start

This is an 8-week MVP project building 7 microservices on AWS EKS with resource-constrained infrastructure.

### Current Status

✅ **Phase 0 Complete:** Git repository initialized and development plan created  
📋 **Next:** Phase 1 - Local development infrastructure (Docker Compose)

### Project Architecture

- **7 Microservices:** API Gateway Owner/Auth/User/Plan/Video Services + LLM Agent + Video Worker
- **Hybrid Infrastructure:** Stateless on EKS (t3.medium), Stateful on EC2s (2x t2.micro)
- **Local-First Development:** Docker Compose → Local AWS Testing → Kubernetes Deployment

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

### Next Steps

1. Phase 1: Set up Docker Compose with PostgreSQL and Redis
2. Phase 2: Build Auth and User Services
3. Phase 3: Implement API Gateway with JWT validation

See [DEVELOPMENT_PLAN.md](DEVELOPMENT_PLAN.md) for complete roadmap.

---

**Important:** This project uses a t2.micro PostgreSQL (1GB RAM) which will be the **primary performance bottleneck**. This is an accepted trade-off for learning/cost reasons.

