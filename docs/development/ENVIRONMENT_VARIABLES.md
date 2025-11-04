# Environment Variables Management

**Last Updated**: October 31, 2025
**Status**: ✅ Best Practices Implemented

---

## Overview

Oddiya uses environment variables for all configuration, following the [12-Factor App methodology](https://12factor.net/config). This document explains how to manage environment variables across different environments.

---

## File Structure

```
oddiya/
├── .env                          # ❌ Actual values for local dev (NOT in git)
├── .env.example                  # ✅ Template with placeholder values (IN git)
├── .env.production.example       # ✅ Production template (IN git)
└── .gitignore                    # Excludes .env but keeps .env.example
```

### File Purposes

| File | Purpose | In Git? | Use When |
|------|---------|---------|----------|
| `.env` | Actual local development values | ❌ No | Running services locally |
| `.env.example` | Template with placeholders | ✅ Yes | Setting up new dev environment |
| `.env.production.example` | Production template | ✅ Yes | Reference for production setup |

---

## Quick Start

### For New Developers

```bash
# 1. Clone repository
git clone https://github.com/your-org/oddiya.git
cd oddiya

# 2. Copy template to .env
cp .env.example .env

# 3. Update .env with your actual values
vim .env  # or nano, code, etc.

# 4. Start services
./scripts/start-local-dev.sh
```

### For Existing Developers

```bash
# Update your .env with new variables from .env.example
diff .env.example .env  # Check what changed
```

---

## Environment Variables Reference

### Database (PostgreSQL)

```bash
DB_HOST=localhost              # PostgreSQL host
DB_PORT=5432                   # PostgreSQL port
DB_NAME=oddiya                 # Database name
DB_USER=admin                  # Database user
DB_PASSWORD=4321               # Database password
```

**Local Development**: Use local PostgreSQL
**Production**: Use AWS RDS with IAM authentication

### Redis (Cache)

```bash
REDIS_HOST=localhost           # Redis host
REDIS_PORT=6379                # Redis port
REDIS_PASSWORD=                # Redis password (empty for local)
REDIS_CACHE_TTL=3600           # Cache TTL in seconds (1 hour)
```

**Local Development**: Use local Redis
**Production**: Use AWS ElastiCache Redis

### AWS Configuration

```bash
AWS_REGION=us-east-1
AWS_ACCESS_KEY_ID=your_access_key_id
AWS_SECRET_ACCESS_KEY=your_secret_access_key

# S3 Storage
S3_BUCKET=oddiya-storage-dev

# SQS & SNS (Video Pipeline)
SQS_QUEUE_URL=https://sqs.us-east-1.amazonaws.com/ACCOUNT/queue-name
SNS_TOPIC_ARN=arn:aws:sns:us-east-1:ACCOUNT:topic-name
```

**Local Development**: Use test AWS account or mock mode
**Production**: Use IAM roles instead of access keys

### LLM Configuration (Google Gemini)

```bash
LLM_PROVIDER=gemini
GOOGLE_API_KEY=your_gemini_api_key_here
GEMINI_MODEL=gemini-2.0-flash-exp
LLM_AGENT_URL=http://localhost:8000

# LangSmith (Optional - for LLM tracing)
LANGSMITH_API_KEY=your_key
LANGSMITH_PROJECT=oddiya-dev
LANGSMITH_API_URL=https://api.smith.langchain.com
```

**Local Development**: Get FREE Gemini API key from https://makersuite.google.com/app/apikey
**Production**: Use same Gemini API key (free tier sufficient for MVP)

### Authentication (OAuth 2.0)

```bash
# Google OAuth
GOOGLE_CLIENT_ID=your_id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your_secret
OAUTH_REDIRECT_URI=http://localhost:8080/login/oauth2/code/google

# Apple Sign In
APPLE_CLIENT_ID=com.oddiya.app
APPLE_TEAM_ID=YOUR_TEAM_ID
APPLE_KEY_ID=YOUR_KEY_ID
APPLE_PRIVATE_KEY=-----BEGIN PRIVATE KEY-----...

# JWT Configuration
JWT_ACCESS_TOKEN_VALIDITY=3600      # 1 hour
JWT_REFRESH_TOKEN_VALIDITY=1209600  # 14 days
```

**Setup**: See `mobile/GOOGLE_OAUTH_ANDROID_SETUP.md` for Google OAuth setup

### Service URLs (Internal Communication)

```bash
# Local Development
USER_SERVICE_URL=http://localhost:8082
AUTH_SERVICE_URL=http://localhost:8081
PLAN_SERVICE_URL=http://localhost:8083
VIDEO_SERVICE_URL=http://localhost:8084

# Production (Kubernetes internal DNS)
USER_SERVICE_URL=http://user-service.oddiya.svc.cluster.local:8082
AUTH_SERVICE_URL=http://auth-service.oddiya.svc.cluster.local:8081
```

### External APIs

```bash
# OpenWeatherMap (Weather data)
OPENWEATHER_API_KEY=your_key

# ExchangeRate API (Currency conversion)
EXCHANGERATE_API_KEY=your_key
```

### Application Settings

```bash
ENVIRONMENT=development        # development | staging | production
LOG_LEVEL=DEBUG                # DEBUG | INFO | WARN | ERROR
```

---

## Best Practices

### ✅ DO

1. **Use .env for local development**
   ```bash
   cp .env.example .env
   # Update with your values
   ```

2. **Keep .env.example updated**
   ```bash
   # When adding new variables, update .env.example with placeholders
   NEW_API_KEY=your_api_key_here  # In .env.example
   ```

3. **Use meaningful placeholder values**
   ```bash
   # ❌ Bad
   API_KEY=xxx

   # ✅ Good
   API_KEY=your_openweather_api_key_here
   ```

4. **Document each variable**
   ```bash
   # OpenWeatherMap API key for weather data
   # Get it from: https://openweathermap.org/api
   OPENWEATHER_API_KEY=your_key
   ```

5. **Use environment-specific values**
   ```bash
   # Development
   S3_BUCKET=oddiya-storage-dev

   # Production
   S3_BUCKET=oddiya-storage-prod
   ```

6. **Load .env files in code**
   ```python
   # Python
   from dotenv import load_dotenv
   load_dotenv()
   ```

   ```java
   // Spring Boot automatically loads application.yml
   // which references ${ENV_VAR:default_value}
   ```

### ❌ DON'T

1. **Never commit .env files**
   ```bash
   # .gitignore already excludes these
   .env
   .env.local
   *.env
   ```

2. **Never hardcode secrets in code**
   ```java
   // ❌ Bad
   private static final String API_KEY = "sk-1234567890";

   // ✅ Good
   @Value("${api.key}")
   private String apiKey;
   ```

3. **Never share .env files**
   - Use password managers (1Password, LastPass)
   - Use secure sharing (AWS Secrets Manager)

4. **Never use production credentials locally**
   - Create separate dev/staging/prod credentials

5. **Never log environment variables**
   ```java
   // ❌ Bad
   logger.info("API Key: {}", apiKey);

   // ✅ Good
   logger.info("API Key configured: {}", apiKey != null);
   ```

---

## Loading Environment Variables

### Spring Boot (Java Services)

**application.yml**:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:oddiya}
    username: ${DB_USER}
    password: ${DB_PASSWORD}
```

**How it works**:
1. Spring Boot reads environment variables
2. Syntax: `${VAR_NAME:default_value}`
3. If `DB_HOST` not set, uses `localhost`

### Python (FastAPI Services)

**Using python-dotenv**:
```python
import os
from dotenv import load_dotenv

# Load .env file
load_dotenv()

# Access variables
db_host = os.getenv("DB_HOST", "localhost")
db_port = int(os.getenv("DB_PORT", "5432"))
```

### Docker Compose

**docker-compose.yml**:
```yaml
services:
  api-gateway:
    image: oddiya/api-gateway:latest
    env_file:
      - .env  # Load all variables from .env
    environment:
      - DB_HOST=${DB_HOST}  # Or specify individually
      - DB_PORT=${DB_PORT}
```

### Kubernetes

**ConfigMap** (non-sensitive):
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: oddiya-config
data:
  DB_HOST: "postgres.oddiya.svc.cluster.local"
  DB_PORT: "5432"
  ENVIRONMENT: "production"
```

**Secret** (sensitive):
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: oddiya-secrets
type: Opaque
stringData:
  DB_USER: admin
  DB_PASSWORD: super_secret_password
```

**Deployment**:
```yaml
apiVersion: apps/v1
kind: Deployment
spec:
  template:
    spec:
      containers:
      - name: auth-service
        envFrom:
        - configMapRef:
            name: oddiya-config
        - secretRef:
            name: oddiya-secrets
```

---

## Production Secrets Management

### AWS Secrets Manager (Recommended)

**Store secrets**:
```bash
aws secretsmanager create-secret \
  --name oddiya/prod/db \
  --secret-string '{
    "username":"admin",
    "password":"super_secret_password",
    "host":"oddiya-postgres.xxxxx.rds.amazonaws.com"
  }'
```

**Retrieve in application**:
```java
// Spring Cloud AWS
spring:
  config:
    import:
      - aws-secretsmanager:oddiya/prod/db
```

### Kubernetes Secrets (Alternative)

**Create from .env**:
```bash
kubectl create secret generic oddiya-secrets \
  --from-env-file=.env \
  --namespace=oddiya
```

**Mount as environment variables**:
```yaml
containers:
- name: auth-service
  envFrom:
  - secretRef:
      name: oddiya-secrets
```

---

## Troubleshooting

### Issue: Service can't connect to database

**Check**:
```bash
# Verify .env file exists
ls -la .env

# Check DB variables are set
grep DB_ .env

# Test database connection
PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -U $DB_USER -d $DB_NAME
```

### Issue: "Environment variable not found"

**Solution**:
```bash
# 1. Check .env file has the variable
grep API_KEY .env

# 2. Check .env is loaded
echo $API_KEY  # Should print value

# 3. Restart service
./gradlew bootRun
```

### Issue: Using wrong environment values

**Solution**:
```bash
# 1. Check which .env file is loaded
ls -la .env

# 2. Verify ENVIRONMENT variable
grep ENVIRONMENT .env

# 3. Use different .env file
mv .env .env.old
cp .env.production.example .env
```

---

## Security Checklist

- [ ] `.env` is in `.gitignore`
- [ ] No secrets in code
- [ ] No secrets in logs
- [ ] Different credentials for dev/prod
- [ ] Production uses AWS Secrets Manager
- [ ] Credentials rotated regularly
- [ ] Access keys use least privilege IAM roles
- [ ] `.env` file has restricted permissions (chmod 600)
- [ ] Secrets not shared via email/chat
- [ ] Old secrets revoked after rotation

---

## References

- **12-Factor App Config**: https://12factor.net/config
- **AWS Secrets Manager**: https://aws.amazon.com/secrets-manager/
- **Kubernetes Secrets**: https://kubernetes.io/docs/concepts/configuration/secret/
- **Spring Boot External Config**: https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config
- **python-dotenv**: https://github.com/theskumar/python-dotenv

---

**Questions?** Check troubleshooting section or contact DevOps team.
