# GitHub Actions - Automatic Testing

Complete guide to automatic testing on every GitHub push.

## Overview

Oddiya uses GitHub Actions to automatically test all services whenever you push code to the repository.

## Workflows

### 1. Test on Every Push (`test-on-push.yml`)

**Triggers:** Every push to any branch

**What it does:**
- ✅ Quick validation checks (commit messages, debug code)
- ✅ Tests all 5 Java services
- ✅ Tests all 2 Python services
- ✅ Uploads test results as artifacts
- ✅ Notifies if tests pass or fail

**Duration:** ~5-10 minutes

### 2. CI Pipeline (`ci.yml`)

**Triggers:** Push to `main` or `develop` branches

**What it does:**
- ✅ Runs all unit tests (Java + Python)
- ✅ Runs linting checks
- ✅ Builds Docker images (main branch only)
- ✅ Pushes images to Docker Hub

**Duration:** ~15-20 minutes

### 3. Pull Request Checks (`pr-checks.yml`)

**Triggers:** Pull requests to `main` or `develop`

**What it does:**
- ✅ Validates PR (no merge conflicts, file sizes)
- ✅ Code quality checks (Black, Flake8, Gradle)
- ✅ Security scanning with Trivy
- ✅ Dependency vulnerability checks

**Duration:** ~5 minutes

## How It Works

### On Every Push:

```
1. You push code to GitHub
   ↓
2. GitHub Actions automatically starts
   ↓
3. Runs "Test on Every Push" workflow
   ↓
4. Tests all Java services in parallel
   ↓
5. Tests all Python services in parallel
   ↓
6. Reports results (✅ or ❌)
```

### On Main Branch Push:

```
1. You push to main branch
   ↓
2. Runs full CI Pipeline
   ↓
3. Tests + Lint checks
   ↓
4. If all pass → Build Docker images
   ↓
5. Push images to Docker Hub
   ↓
6. Ready for deployment
```

## Viewing Test Results

### On GitHub:

1. Go to your repository
2. Click "Actions" tab
3. See all workflow runs
4. Click on any run to see details
5. View test results for each service

### In Pull Requests:

- Tests automatically run on every PR
- See status checks at bottom of PR
- ✅ All checks must pass before merging

## Test Results Artifacts

Test results are saved for 7 days:

- **Location:** Actions → Workflow run → Artifacts
- **Contents:** JUnit XML test reports
- **Download:** Click artifact name to download

## Status Badges

Add to your README:

```markdown
![Tests](https://github.com/sw6820/oddiya/workflows/Test%20on%20Every%20Push/badge.svg)
![CI](https://github.com/sw6820/oddiya/workflows/CI%20Pipeline/badge.svg)
```

## Email Notifications

GitHub sends emails when:
- ❌ Tests fail on your branch
- ✅ Tests pass after previous failure
- 🔴 Build fails on main branch

Configure in: GitHub → Settings → Notifications

## Troubleshooting

### Tests fail only in GitHub Actions

**Common causes:**
1. Missing environment variables
2. Different Java/Python versions
3. Timeout issues

**Solution:**
```bash
# Test locally with same setup
docker run -it eclipse-temurin:21-jdk bash
# Run your gradle commands
```

### Docker build fails

**Check:**
- Dockerfile syntax
- Build context
- Image size limits

**Debug:**
```bash
# Build locally
docker build -t test ./services/auth-service
```

### Tests timeout

**Increase timeout:**
```yaml
- name: Test Service
  timeout-minutes: 10  # Default is 6 hours
```

## Advanced Configuration

### Run tests only on specific paths:

```yaml
on:
  push:
    paths:
      - 'services/**'
      - '.github/workflows/**'
```

### Skip CI on specific commits:

```bash
git commit -m "docs: update README [skip ci]"
```

### Matrix testing (multiple versions):

```yaml
strategy:
  matrix:
    java-version: [21, 17]
    python-version: ['3.11', '3.10']
```

## Best Practices

### 1. Always Test Locally First

```bash
# Before pushing
./scripts/test-local.sh
```

### 2. Use Feature Branches

```bash
git checkout -b feature/new-feature
# Make changes
git push origin feature/new-feature
# Tests run automatically
# Create PR when tests pass
```

### 3. Fix Failing Tests Immediately

- Don't ignore failing tests
- Don't disable tests to make CI pass
- Fix the root cause

### 4. Keep Tests Fast

- Unit tests should be < 5 seconds each
- Integration tests < 30 seconds each
- Total test suite < 10 minutes

### 5. Monitor Test Performance

- Check test duration trends
- Optimize slow tests
- Use parallel execution

## GitHub Actions Costs

**Free tier includes:**
- 2,000 minutes/month for private repos
- Unlimited for public repos

**Current usage:**
- ~10 minutes per push
- ~200 pushes/month = ~2,000 minutes

**Optimization tips:**
- Cache dependencies
- Use matrix builds
- Skip redundant tests

## Secrets Configuration

Required secrets in GitHub repository:

```
Settings → Secrets → Actions

DOCKER_USERNAME=sw6820
DOCKER_PASSWORD=***
```

## Example Workflow Run

```
✅ Quick Check (30s)
   ├─ Check commit message
   ├─ Check for debug code
   └─ Validate files

✅ Test All (8m 45s)
   ├─ Test Java Services (6m 30s)
   │  ├─ Auth Service ✅
   │  ├─ API Gateway ✅
   │  ├─ User Service ✅
   │  ├─ Plan Service ✅
   │  └─ Video Service ✅
   └─ Test Python Services (2m 15s)
      ├─ LLM Agent ✅
      └─ Video Worker ✅

✅ Notify (5s)
   └─ All tests passed!
```

## Integration with Local Development

### Before pushing:

```bash
# 1. Test locally
./scripts/test-local.sh

# 2. If pass, commit
git add .
git commit -m "feat: add new feature"

# 3. Push (triggers automatic tests)
git push origin feature-branch

# 4. Monitor GitHub Actions
# Go to: https://github.com/sw6820/oddiya/actions

# 5. If tests pass, create PR
```

## Continuous Deployment (CD)

Future enhancement:
```yaml
# After tests pass on main
deploy-staging:
  needs: build-docker
  runs-on: ubuntu-latest
  steps:
    - name: Deploy to EKS staging
      run: kubectl apply -f k8s/staging/
```

## Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Workflow Syntax](https://docs.github.com/en/actions/reference/workflow-syntax-for-github-actions)
- [GitHub Actions Marketplace](https://github.com/marketplace?type=actions)

## Summary

**Every time you push code:**
1. ✅ Automatic tests run on GitHub
2. ✅ You get immediate feedback
3. ✅ No manual testing needed
4. ✅ Confidence before deployment

**You can now code and push freely - GitHub Actions has your back!** 🚀

