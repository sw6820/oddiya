# Documentation Structure

All project documentation is organized in the `docs/` directory.

## Quick Links

### For Developers
- [Getting Started](docs/development/getting-started.md) - Set up local environment
- [Development Plan](docs/development/plan.md) - Complete development roadmap
- [Testing Guide](docs/development/testing.md) - Testing standards

### For Operations
- [CI/CD Setup](docs/deployment/ci-cd.md) - GitHub Actions workflow
- [Infrastructure Deployment](docs/deployment/infrastructure.md) - Terraform setup
- [GitHub Actions](docs/deployment/github-actions.md) - CI/CD configuration

### For Architecture
- [Architecture Overview](docs/architecture/overview.md) - System design and specs
- [External APIs](docs/api/external-apis.md) - API integrations

### For AI Assistants
- [CLAUDE.md](CLAUDE.md) - Claude Code context
- [.cursorrules](.cursorrules) - Cursor IDE rules

## Documentation Organization

```
docs/
├── README.md                    # Documentation index
├── architecture/
│   └── overview.md             # System architecture and design
├── development/
│   ├── getting-started.md      # Local development setup
│   ├── plan.md                 # Development roadmap
│   └── testing.md              # Testing standards
├── deployment/
│   ├── ci-cd.md                # CI/CD pipeline
│   ├── github-actions.md       # GitHub Actions setup
│   └── infrastructure.md       # Terraform and AWS
├── api/
│   └── external-apis.md        # External API integrations
└── archive/                     # Historical documents
```

## What Was Consolidated

**Redundant files removed:**
- ❌ FINAL_SUMMARY.md → Content merged into README.md
- ❌ PROGRESS_SUMMARY.md → Archive
- ❌ SESSION_SUMMARY.md → Archive
- ❌ PROJECT_STATUS.md → Content in README.md
- ❌ REPOSITORY_STATUS.md → Archive
- ❌ REPOSITORY_LINKS.md → Archive
- ❌ QUICKSTART.md → Archive
- ❌ START_HERE.md → Archive
- ❌ PHASE2_PROGRESS.md → Archive

**Files reorganized:**
- TechSpecPRD.md → docs/architecture/overview.md
- DEVELOPMENT_PLAN.md → docs/development/plan.md
- TESTING_GUIDE.md → docs/development/testing.md
- API_COMPARISON.md → docs/api/external-apis.md
- README_INFRASTRUCTURE.md → docs/deployment/infrastructure.md

**New structure:**
- ✅ Clear separation by topic
- ✅ No duplication
- ✅ Easy to find
- ✅ Follows best practices

## Starting Point

**New to the project?**
1. Read [README.md](README.md)
2. Follow [Getting Started Guide](docs/development/getting-started.md)
3. Review [Architecture Overview](docs/architecture/overview.md)

**AI Assistant?**
1. Read [CLAUDE.md](CLAUDE.md)
2. Reference [.cursorrules](.cursorrules)
3. Use [Development Plan](docs/development/plan.md) for context

