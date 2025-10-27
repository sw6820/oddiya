# CI/CD with GitHub Actions

GitHub Actions workflow for automated testing and deployment.

## Overview

The CI pipeline automatically:
- ✅ Tests all Java and Python services
- ✅ Checks code quality (linting, formatting)
- ✅ Builds Docker images on main branch
- ✅ Pushes images to Docker Hub

## Workflow File

Location: `.github/workflows/ci.yml`

## Setup

### 1. GitHub Secrets

Add these in GitHub Settings → Secrets and variables → Actions:

**Required Secrets:**
- `DOCKER_USERNAME` - Your Docker Hub username
- `DOCKER_PASSWORD` - Your Docker Hub password/token

**Optional Secrets (for AWS deployment):**
- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`

### 2. Workflow Triggers

The workflow runs on:
- Push to `main` or `develop` branches
- Pull requests to `main` or `develop`

## Jobs

### test-java-services
- Tests Auth Service, API Gateway, Plan Service
- Uses Gradle test runner
- Uploads test results as artifacts

### test-python-services
- Tests LLM Agent and Video Worker
- Uses pytest
- Uploads coverage reports

### lint
- Python: black (formatting) + flake8 (linting)
- Java: Gradle check

### build-docker
- Only runs on `main` branch
- Builds and pushes Docker images
- Tags with both `latest` and commit SHA

## Manual Execution

Trigger workflow manually:
1. Go to GitHub → Actions
2. Select "CI Pipeline"
3. Click "Run workflow"

## Viewing Results

### Test Results
- GitHub Actions page shows all job results
- Download artifacts to see detailed test outputs

### Docker Images
```bash
# Images are pushed to Docker Hub
docker pull sw6820/oddiya-auth-service:latest
docker pull sw6820/oddiya-llm-agent:latest
```

## Next Steps

Add deployment workflow:
- Deploy to staging on merge to develop
- Deploy to production on merge to main
- Rollback capability

## Troubleshooting

### CI Failing
- Check GitHub Actions page for error messages
- Verify all secrets are configured
- Test locally: `./gradlew test`

### Docker Build Failing
- Check Docker credentials in secrets
- Verify Dockerfile syntax
- Test locally: `docker build .`

## References

- [GitHub Actions Docs](https://docs.github.com/en/actions)
- [Docker Hub](https://hub.docker.com/)

