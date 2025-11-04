# Oddiya Scripts

Automation scripts for development, testing, and deployment.

## 📂 Script Categories

### 🚀 Development & Startup

**Primary Scripts:**
```bash
./start-local-dev.sh          # ⭐ Start all services for development (172 lines)
./stop-local-dev.sh           # Stop all development services
./quick-start-ui.sh           # Quick start for UI development only (54 lines)
```

**Service-Specific:**
```bash
./start-services.sh           # Start API Gateway + Plan Service (56 lines)
./start-auth-services.sh      # Start Auth Service only (82 lines)
./restart-auth-service.sh     # Restart Auth Service with new config (42 lines)
./start-auth-with-env.sh      # Start Auth Service with environment variables (17 lines)
./start-plan-service.sh       # Start Plan Service only (16 lines)
```

**Legacy:**
```bash
./start-local.sh              # [DEPRECATED] Use start-local-dev.sh instead (95 lines)
./stop-local.sh               # [DEPRECATED] Use stop-local-dev.sh instead (16 lines)
```

### 🧪 Testing

**Integration & API Tests:**
```bash
./test-integration.sh         # ⭐ Complete integration test suite (243 lines)
./run-integration-tests.sh    # Run integration tests with reporting (179 lines)
./test-local.sh               # Test all local services (156 lines)
./smoke-test.sh               # Quick health check (44 lines)
```

**Mobile Testing:**
```bash
./start-for-mobile-testing.sh # Start backend for mobile app testing (147 lines)
./test-mobile-api.sh          # Test mobile API endpoints (143 lines)
./test-mobile-connection.sh   # Test mobile app connectivity (95 lines)
./test-mobile-features.sh     # Test mobile-specific features (189 lines)
```

**Performance:**
```bash
./run-load-tests.sh           # Run Locust load tests (172 lines)
./analyze-performance.sh      # Analyze performance metrics (149 lines)
```

### 🔧 Environment & Configuration

```bash
./validate-env.sh             # ⭐ Validate environment variables (133 lines)
./load-env.sh                 # Load .env file (129 lines)
./run-with-env.sh             # Run command with environment (76 lines)
./get-local-ip.sh             # Get local IP for mobile testing (78 lines)
```

### 🌐 API Configuration

```bash
./enable-real-apis.sh         # Switch to real external APIs (139 lines)
```

### 🚢 Deployment

```bash
./deploy-phase1-ec2.sh        # Deploy to AWS EC2 (Phase 1) (448 lines)
./aws/setup-ec2.sh            # EC2 instance initial setup
```

### 📱 Mobile Build

```bash
./mobile/build-android.sh     # Build Android APK
./mobile/build-expo.sh        # Interactive Expo build
./mobile/migrate-to-expo.sh   # Migrate from React Native CLI to Expo
```

---

## 📖 Usage Guide

### Quick Start Development

```bash
# 1. Setup (first time only)
./validate-env.sh

# 2. Start all services
./start-local-dev.sh

# 3. Test everything is running
./smoke-test.sh

# 4. Stop services when done
./stop-local-dev.sh
```

### Mobile Development Workflow

```bash
# 1. Start backend for mobile testing
./start-for-mobile-testing.sh

# 2. Get your local IP
./get-local-ip.sh

# 3. Test mobile API
./test-mobile-api.sh

# 4. Test mobile features
./test-mobile-features.sh
```

### Testing Workflow

```bash
# Quick health check
./smoke-test.sh

# Full integration tests
./test-integration.sh

# Load testing
./run-load-tests.sh

# Performance analysis
./analyze-performance.sh
```

### Deployment Workflow

```bash
# 1. Validate environment
./validate-env.sh

# 2. Run tests
./test-integration.sh

# 3. Deploy to EC2
./deploy-phase1-ec2.sh
```

---

## 🔧 Script Details

### start-local-dev.sh

**Purpose:** Start all services for local development

**What it does:**
- Starts PostgreSQL and Redis (Docker)
- Starts all Java services (Auth, User, Plan, API Gateway)
- Starts Python LLM Agent
- Configures proper environment variables
- Health checks for all services

**Prerequisites:**
- Docker running
- Java 21 installed
- Python 3.11+ installed
- `.env` file configured

**Usage:**
```bash
./start-local-dev.sh
```

### test-integration.sh

**Purpose:** Run complete integration test suite

**What it tests:**
- Auth Service (OAuth, JWT)
- User Service (CRUD operations)
- Plan Service (AI planning)
- API Gateway (routing, CORS)
- LLM Agent (Gemini API)

**Usage:**
```bash
./test-integration.sh
```

**Output:** Test results and coverage report

### validate-env.sh

**Purpose:** Validate all environment variables are set correctly

**Checks:**
- Required variables exist
- Database connectivity
- Redis connectivity
- API keys valid
- Service URLs reachable

**Usage:**
```bash
./validate-env.sh
```

**Exit codes:**
- `0`: All validations passed
- `1`: Validation failed (check output)

### start-for-mobile-testing.sh

**Purpose:** Start backend optimized for mobile app testing

**What it does:**
- Starts only services needed by mobile app
- Configures CORS for mobile device IPs
- Enables debug logging
- Displays local IP address

**Usage:**
```bash
./start-for-mobile-testing.sh
```

**Then in mobile app:** Update `API_BASE_URL` to displayed IP

### deploy-phase1-ec2.sh

**Purpose:** Deploy to AWS EC2 (Phase 1: Simple Docker setup)

**What it does:**
- Validates EC2 instance
- Installs Docker
- Clones repository
- Configures environment
- Starts services with docker-compose
- Runs health checks

**Prerequisites:**
- EC2 instance running
- SSH access configured
- `.env` file prepared

**Usage:**
```bash
./deploy-phase1-ec2.sh <EC2_IP_ADDRESS>
```

---

## 🚨 Deprecation Notice

### Deprecated Scripts

The following scripts are deprecated and will be removed in a future version:

- `start-local.sh` → Use `start-local-dev.sh` instead
- `stop-local.sh` → Use `stop-local-dev.sh` instead

These scripts are kept for backward compatibility but are no longer maintained.

---

## 🛠️ Script Development Guidelines

When creating new scripts:

1. **Add shebang:** `#!/bin/bash`
2. **Set error handling:** `set -e` (exit on error)
3. **Add description comment** at the top
4. **Use consistent naming:** `verb-noun.sh` (e.g., `start-service.sh`)
5. **Add to this README** with description and usage
6. **Make executable:** `chmod +x script-name.sh`
7. **Test thoroughly** before committing

### Script Template

```bash
#!/bin/bash
# Description of what this script does
# Usage: ./script-name.sh [arguments]

set -e  # Exit on error

# Color output
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${GREEN}Starting...${NC}"

# Your script logic here

echo -e "${GREEN}Complete!${NC}"
```

---

## 📝 Common Issues

### "Permission denied"

```bash
chmod +x scripts/*.sh
```

### "Docker not running"

```bash
# macOS/Windows
# Start Docker Desktop

# Linux
sudo systemctl start docker
```

### "Port already in use"

```bash
# Find process using port
lsof -i :8080

# Kill process
kill -9 <PID>
```

### "Environment variables not set"

```bash
# Check .env file exists
ls -la .env

# Validate all variables
./validate-env.sh
```

---

## 📚 Related Documentation

- [Getting Started](../docs/GETTING_STARTED.md) - Setup guide
- [Local Testing](../docs/development/LOCAL_TESTING.md) - Testing guide
- [Environment Variables](../docs/development/ENVIRONMENT_VARIABLES.md) - Configuration
- [Deployment Guide](../docs/deployment/DEPLOYMENT_GUIDE.md) - Deployment instructions

---

**Last Updated:** 2025-11-03
