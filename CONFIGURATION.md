# Configuration Guide

Complete guide to Oddiya configuration files and environment setup.

## 📁 Configuration Files Overview

### Docker Compose Files

| File | Purpose | Use When |
|------|---------|----------|
| `docker-compose.yml` | Production deployment | Deploying to AWS EC2 |
| `docker-compose.local.yml` | Local development | Developing locally with full stack |

**Key Differences:**

| Feature | docker-compose.yml | docker-compose.local.yml |
|---------|-------------------|--------------------------|
| **Database** | None (stateless) | PostgreSQL 17.0 |
| **Redis** | Included | Included |
| **Services** | API Gateway, Plan, LLM Agent | All 7 services |
| **Nginx** | Yes (port 80) | No |
| **Health Checks** | No | Yes |
| **Restart Policy** | unless-stopped | unless-stopped |

### Service Configuration Files

Each service has its own `application.yml`:

```
services/
├── api-gateway/src/main/resources/application.yml
├── auth-service/src/main/resources/application.yml
├── plan-service/src/main/resources/
│   ├── application.yml         # Default profile
│   └── application-local.yml   # Local development profile
├── user-service/src/main/resources/application.yml
└── video-service/src/main/resources/application.yml
```

---

## 🔧 Environment Variables

### Required Variables

All services require these environment variables:

```bash
# LLM Provider (Google Gemini)
GOOGLE_API_KEY=your_gemini_api_key_here
GEMINI_MODEL=gemini-2.0-flash-exp

# OAuth (Optional but recommended)
GOOGLE_CLIENT_ID=your_google_oauth_client_id
GOOGLE_CLIENT_SECRET=your_google_oauth_secret

# Redis
REDIS_HOST=localhost  # or 'redis' in Docker
REDIS_PORT=6379
```

### Local Development Variables

Additional variables for local development with `docker-compose.local.yml`:

```bash
# PostgreSQL Database
DB_HOST=localhost  # or 'postgres' in Docker
DB_PORT=5432
DB_NAME=oddiya
DB_USER=oddiya_user
DB_PASSWORD=oddiya_password_dev
```

### Production Variables

For production deployment with `docker-compose.yml`:

```bash
# Service Configuration
SPRING_PROFILES_ACTIVE=prod
LLM_AGENT_URL=http://llm-agent:8000
```

---

## 📋 Configuration by Environment

### Development (docker-compose.local.yml)

**Start command:**
```bash
docker-compose -f docker-compose.local.yml up -d
```

**What runs:**
- PostgreSQL (5432)
- Redis (6379)
- Auth Service (8081)
- User Service (8082)
- Plan Service (8083)
- Video Service (8084)
- API Gateway (8080)
- LLM Agent (8000)

**Environment setup:**
```bash
# Create .env file
cat > .env << 'EOF'
GOOGLE_API_KEY=your_key_here
GOOGLE_CLIENT_ID=your_client_id
GOOGLE_CLIENT_SECRET=your_secret
REDIS_HOST=redis
DB_HOST=postgres
EOF
```

### Production (docker-compose.yml)

**Start command:**
```bash
docker-compose up -d
```

**What runs:**
- Nginx (80, 443)
- API Gateway (8080)
- Plan Service (8083)
- LLM Agent (8000)
- Redis (6379)

**Environment setup:**
```bash
# Create .env file on EC2
cat > .env << 'EOF'
GOOGLE_API_KEY=your_production_key
GEMINI_MODEL=gemini-2.0-flash-exp
REDIS_HOST=redis
SPRING_PROFILES_ACTIVE=prod
EOF
```

**Why no database?**
Production deployment uses stateless architecture:
- No user data persistence (for MVP simplicity)
- Lower resource requirements
- Fits in t2.micro (1GB RAM)

---

## 🎯 Configuration Profiles

### Spring Boot Profiles

Services support multiple profiles via `SPRING_PROFILES_ACTIVE`:

**Profile: default** (no environment variable set)
- Uses embedded H2 database
- Mock external APIs
- For quick testing

**Profile: local**
- Uses PostgreSQL
- Real Redis
- Local development setup

**Profile: prod**
- Production-optimized settings
- Real external APIs
- Cache enabled

### Activating Profiles

**In Docker Compose:**
```yaml
environment:
  - SPRING_PROFILES_ACTIVE=prod
```

**In Command Line:**
```bash
java -jar app.jar --spring.profiles.active=local
```

**In application.yml:**
```yaml
spring:
  profiles:
    active: local
```

---

## 🔐 Secrets Management

### Development

Use `.env` file (NOT committed to git):

```bash
# .env
GOOGLE_API_KEY=your_key
GOOGLE_CLIENT_ID=your_id
GOOGLE_CLIENT_SECRET=your_secret
```

Load with:
```bash
export $(cat .env | xargs)
```

Or use with Docker Compose (automatic):
```bash
docker-compose up -d  # Reads .env automatically
```

### Production

**Option 1: Environment variables**
```bash
export GOOGLE_API_KEY=your_production_key
```

**Option 2: AWS Secrets Manager**
```bash
# Store secret
aws secretsmanager create-secret \
  --name oddiya/google-api-key \
  --secret-string "your_key"

# Retrieve in application
# (Requires AWS SDK integration)
```

**Option 3: .env file on EC2**
```bash
# Create on EC2 instance
sudo nano /opt/oddiya/.env

# Restrict permissions
sudo chmod 600 /opt/oddiya/.env
```

---

## 📝 Configuration Templates

### .env.example

Template for environment variables:

```bash
# Copy this to .env and fill in your values
# DO NOT commit .env to git

# ============================================
# Required - Google Gemini AI
# ============================================
GOOGLE_API_KEY=your_gemini_api_key_here
GEMINI_MODEL=gemini-2.0-flash-exp

# ============================================
# Optional - OAuth
# ============================================
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

# ============================================
# Infrastructure (Local Development)
# ============================================
DB_HOST=postgres
DB_PORT=5432
DB_NAME=oddiya
DB_USER=oddiya_user
DB_PASSWORD=oddiya_password_dev

REDIS_HOST=redis
REDIS_PORT=6379

# ============================================
# Infrastructure (Production)
# ============================================
# Use 'redis' when running in Docker Compose
# Use 'localhost' when running services directly
REDIS_HOST=redis
SPRING_PROFILES_ACTIVE=prod
```

### eas.json (Mobile)

Expo build configuration:

```json
{
  "build": {
    "production": {
      "android": {
        "buildType": "apk"
      },
      "ios": {
        "buildConfiguration": "Release"
      }
    },
    "development": {
      "android": {
        "buildType": "apk"
      },
      "developmentClient": true,
      "distribution": "internal"
    }
  }
}
```

### app.json (Mobile)

Expo app configuration:

```json
{
  "expo": {
    "name": "Oddiya",
    "slug": "oddiya",
    "version": "1.0.0",
    "ios": {
      "bundleIdentifier": "com.oddiya.app",
      "buildNumber": "1.0.0"
    },
    "android": {
      "package": "com.oddiya.app",
      "versionCode": 1
    }
  }
}
```

---

## 🛠️ Configuration Validation

### Validate Environment

Use the validation script:

```bash
./scripts/validate-env.sh
```

**Checks:**
- ✅ Required variables exist
- ✅ Database connectivity
- ✅ Redis connectivity
- ✅ API keys valid
- ✅ Service URLs reachable

### Manual Validation

```bash
# Check variables are set
echo $GOOGLE_API_KEY

# Test Redis
redis-cli ping

# Test PostgreSQL (local dev)
psql -h localhost -U oddiya_user -d oddiya -c "SELECT 1"

# Test service health
curl http://localhost:8080/health
```

---

## 🚨 Common Configuration Issues

### "GOOGLE_API_KEY not set"

```bash
# Check if set
echo $GOOGLE_API_KEY

# Set it
export GOOGLE_API_KEY=your_key

# Or add to .env file
echo "GOOGLE_API_KEY=your_key" >> .env
```

### "Cannot connect to database"

```bash
# Check PostgreSQL is running
docker ps | grep postgres

# Check connection
psql -h localhost -U oddiya_user -d oddiya

# Restart database
docker-compose -f docker-compose.local.yml restart postgres
```

### "Redis connection failed"

```bash
# Check Redis is running
docker ps | grep redis

# Test connection
redis-cli ping

# Restart Redis
docker-compose restart redis
```

### "Service won't start"

```bash
# Check logs
docker-compose logs [service-name]

# Common issues:
# - Port already in use
# - Environment variables missing
# - Dependency services not running
```

---

## 📚 Related Documentation

- [Environment Variables Guide](docs/development/ENVIRONMENT_VARIABLES.md)
- [Local Development](docs/development/LOCAL_TESTING.md)
- [Deployment Guide](docs/deployment/DEPLOYMENT_GUIDE.md)
- [Getting Started](docs/GETTING_STARTED.md)

---

## 🔗 External Configuration

### Google Gemini API

Get your API key:
1. Visit https://makersuite.google.com/app/apikey
2. Create new API key
3. Copy to `.env` file

**Free tier:** 15 requests/minute

### Google OAuth

Setup OAuth credentials:
1. Visit https://console.cloud.google.com
2. Create new project
3. Enable Google+ API
4. Create OAuth 2.0 credentials
5. Add authorized redirect URIs
6. Copy Client ID and Secret to `.env`

See: [OAuth Setup Guide](docs/development/OAUTH_ONLY_SETUP.md)

---

**Last Updated:** 2025-11-03
