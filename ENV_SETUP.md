# Environment Configuration Guide

## 📋 Quick Start

### 1. **Local Development (Mock Mode)**

```bash
# Copy template
cp .env.example .env.local

# Edit .env.local and set:
MOCK_MODE=true  # Uses mock LLM responses
DB_PASSWORD=oddiya_password_dev
```

### 2. **Local Development (Real Bedrock)**

```bash
# Use the existing .env.bedrock file (already configured)
# OR create .env.local with your AWS credentials:

cp .env.example .env.local

# Edit .env.local and set:
AWS_ACCESS_KEY_ID=your_bedrock_key
AWS_SECRET_ACCESS_KEY=your_bedrock_secret
BEDROCK_MODEL_ID=anthropic.claude-sonnet-4-5-20250929-v1:0
BEDROCK_REGION=us-east-1
MOCK_MODE=false
```

### 3. **Production Deployment**

Use AWS Secrets Manager for all sensitive values:
- Database credentials
- AWS keys
- OAuth secrets
- API keys

---

## 📁 File Structure

| File | Purpose | Committed to Git? |
|------|---------|-------------------|
| `.env.example` | Template with all variables | ✅ Yes (safe) |
| `.env.local` | Your local development config | ❌ No (.gitignore) |
| `.env.bedrock` | Real Bedrock credentials | ❌ No (.gitignore) |
| `.env` | Alternative local config | ❌ No (.gitignore) |

---

## 🔑 Required Variables

### Minimum for Local Development

```bash
# Database (use docker-compose defaults)
DB_HOST=localhost
DB_PORT=5432
DB_NAME=oddiya
DB_USER=oddiya_user
DB_PASSWORD=oddiya_password_dev

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# LLM (Mock mode)
MOCK_MODE=true
BEDROCK_MODEL_ID=anthropic.claude-sonnet-4-5-20250929-v1:0
```

### Required for Real LLM

```bash
# AWS Bedrock
AWS_ACCESS_KEY_ID=BedrockAPIKey-xxxxx
AWS_SECRET_ACCESS_KEY=base64encodedkey
BEDROCK_REGION=us-east-1
MOCK_MODE=false
```

### Required for OAuth (Auth Service)

```bash
GOOGLE_CLIENT_ID=your_id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=GOCSPX-xxxxx
```

---

## 🚀 Start Services

### Using Docker Compose (Recommended)

```bash
# Start infrastructure only
docker-compose -f docker-compose.local.yml up -d postgres redis

# Start all services
docker-compose -f docker-compose.local.yml up -d

# Check logs
docker-compose -f docker-compose.local.yml logs -f llm-agent plan-service
```

### Manual Start (Development)

```bash
# Terminal 1: PostgreSQL & Redis
docker-compose -f docker-compose.local.yml up postgres redis

# Terminal 2: LLM Agent
cd services/llm-agent
pip install -r requirements.txt
uvicorn main:app --reload --port 8000

# Terminal 3: Plan Service
cd services/plan-service
./gradlew bootRun

# Terminal 4: API Gateway
cd services/api-gateway
./gradlew bootRun
```

---

## 🔍 Environment Variables Reference

### Database & Cache

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_HOST` | `localhost` | PostgreSQL host |
| `DB_PORT` | `5432` | PostgreSQL port |
| `DB_NAME` | `oddiya` | Database name |
| `DB_USER` | `oddiya_user` | Database user |
| `DB_PASSWORD` | `oddiya_password_dev` | Database password (CHANGE IN PROD!) |
| `REDIS_HOST` | `localhost` | Redis host |
| `REDIS_PORT` | `6379` | Redis port |
| `REDIS_CACHE_TTL` | `3600` | Cache TTL (seconds) |

### LLM Configuration

| Variable | Value | Description |
|----------|-------|-------------|
| `BEDROCK_MODEL_ID` | `anthropic.claude-sonnet-4-5-20250929-v1:0` | Claude Sonnet 4.5 (recommended) |
|  | `anthropic.claude-3-5-sonnet-20241022-v2:0` | Claude 3.5 Sonnet (alternative) |
| `BEDROCK_REGION` | `us-east-1` | **Must be us-east-1** for Bedrock |
| `MOCK_MODE` | `true`/`false` | Use mock responses or real LLM |
| `LLM_AGENT_URL` | `http://llm-agent:8000` | LLM Agent service URL |

### AWS Infrastructure

| Variable | Example | Description |
|----------|---------|-------------|
| `AWS_REGION` | `ap-northeast-2` | Main AWS region (Seoul) |
| `AWS_ACCESS_KEY_ID` | `BedrockAPIKey-xxxxx` | AWS access key |
| `AWS_SECRET_ACCESS_KEY` | `base64string` | AWS secret key |
| `S3_BUCKET` | `oddiya-storage-dev` | S3 bucket name |
| `SQS_QUEUE_URL` | `https://sqs...` | SQS queue URL |
| `SNS_TOPIC_ARN` | `arn:aws:sns:...` | SNS topic ARN |

### Authentication

| Variable | Example | Description |
|----------|---------|-------------|
| `GOOGLE_CLIENT_ID` | `xxx.apps.googleusercontent.com` | Google OAuth client ID |
| `GOOGLE_CLIENT_SECRET` | `GOCSPX-xxxxx` | Google OAuth secret |
| `JWT_ACCESS_TOKEN_VALIDITY` | `3600` | Access token expiry (seconds) |
| `JWT_REFRESH_TOKEN_VALIDITY` | `1209600` | Refresh token expiry (14 days) |

### External APIs

| Variable | Required | Description |
|----------|----------|-------------|
| `OPENWEATHER_API_KEY` | Yes | Weather data for travel planning |
| `KAKAO_REST_API_KEY` | No | Kakao API (not used in LLM-only mode) |
| `EXCHANGERATE_API_KEY` | No | Currency exchange (future feature) |

---

## 🧪 Testing Configuration

### Test Environment Variables Work

```bash
# Check environment is loaded
docker-compose -f docker-compose.local.yml exec llm-agent env | grep BEDROCK

# Test LLM Agent directly
curl http://localhost:8000/health

# Test Plan Service with mock
curl -X POST http://localhost:8083/api/v1/plans \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -d '{"title":"서울 여행","startDate":"2025-11-01","endDate":"2025-11-03"}'
```

---

## 🔐 Security Best Practices

1. **Never commit real credentials**
   - ✅ Use `.env.example` (template only)
   - ❌ Never commit `.env`, `.env.local`, `.env.bedrock`

2. **Use AWS Secrets Manager in production**
   ```yaml
   # application.yml
   spring:
     config:
       import:
         - optional:aws-secretsmanager:oddiya/plan-service/db
   ```

3. **Rotate credentials regularly**
   - AWS keys: Every 90 days
   - OAuth secrets: On security updates
   - Database passwords: During maintenance windows

4. **Principle of least privilege**
   - Bedrock key: Only `bedrock:InvokeModel` permission
   - S3 key: Only specific bucket access
   - Database user: Schema-specific permissions

---

## ⚠️ Common Issues

### Issue: "LLM Agent connection refused"

```bash
# Check if LLM Agent is running
docker-compose -f docker-compose.local.yml ps llm-agent

# Check logs
docker-compose logs llm-agent

# Restart
docker-compose restart llm-agent
```

### Issue: "Invalid AWS credentials"

```bash
# Verify credentials are loaded
docker-compose exec llm-agent env | grep AWS

# Check .env.bedrock is not committed (it shouldn't be)
git status

# Test AWS credentials
aws bedrock-runtime invoke-model \
  --model-id anthropic.claude-sonnet-4-5-20250929-v1:0 \
  --region us-east-1 \
  --body '{"prompt":"Hello"}' \
  output.txt
```

### Issue: "Database connection failed"

```bash
# Check PostgreSQL is running
docker-compose ps postgres

# Test connection
docker-compose exec postgres psql -U oddiya_user -d oddiya

# Check credentials in .env.local match docker-compose.yml
```

---

## 📚 Additional Resources

- **AWS Bedrock Setup**: See `docs/deployment/API_SETUP_GUIDE.md`
- **OAuth Configuration**: See `docs/api/GOOGLE_OAUTH_SETUP.md`
- **Docker Compose**: See `docker-compose.local.yml`
- **Architecture**: See `docs/architecture/overview.md`

---

## 🎯 Environment Checklist

Before starting development:

- [ ] Copy `.env.example` to `.env.local`
- [ ] Set `DB_PASSWORD`
- [ ] Set `MOCK_MODE=true` (or configure AWS Bedrock)
- [ ] Start infrastructure: `docker-compose up -d postgres redis`
- [ ] Verify services start: `docker-compose ps`
- [ ] Test API: `curl http://localhost:8000/health`
- [ ] Check logs: `docker-compose logs -f`

---

**Last Updated**: 2025-10-30
**Architecture**: LLM-Only (No hardcoded travel data)
