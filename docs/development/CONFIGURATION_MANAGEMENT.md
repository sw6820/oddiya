# Configuration Management Guide

Best practices for managing Oddiya configuration across environments.

## Overview

Oddiya uses environment variables for configuration management with support for:
- Multiple environments (local, staging, production)
- Automatic validation
- Secrets management
- Docker Compose integration

## Quick Start

### 1. Create Your Environment File

```bash
# Copy template
cp .env.example .env.local

# Edit with your values
nano .env.local
```

### 2. Validate Configuration

```bash
./scripts/validate-env.sh .env.local
```

### 3. Run Services

```bash
./scripts/run-with-env.sh local
```

## Environment Files

### File Structure

```
.env.example        # Template with all variables (commit to git)
.env.local          # Local development (commit to git, no secrets)
.env.staging        # Staging environment (DO NOT commit)
.env.production     # Production environment (DO NOT commit)
.env                # Custom environment (DO NOT commit)
```

### .env.local (Safe to Commit)

```bash
ENVIRONMENT=development
DB_PASSWORD=oddiya_password_dev  # Dev password, not real
GOOGLE_CLIENT_ID=test-client-id  # Test credentials
MOCK_MODE=true                   # Use mocks, not real APIs
```

✅ **Safe** - No real credentials
✅ **Commit** - Team can use it immediately
✅ **Default** - Works out of the box

### .env.staging (DO NOT Commit)

```bash
ENVIRONMENT=staging
DB_PASSWORD=<real-staging-password>
GOOGLE_CLIENT_ID=<real-staging-client-id>
AWS_ACCESS_KEY_ID=<real-aws-key>
MOCK_MODE=false
```

❌ **DO NOT COMMIT** - Contains real credentials
🔒 **Store in:** AWS Secrets Manager, 1Password, etc.

### .env.production (DO NOT Commit)

```bash
ENVIRONMENT=production
DB_PASSWORD=<strong-production-password>
GOOGLE_CLIENT_ID=<production-client-id>
AWS_ACCESS_KEY_ID=<production-aws-key>
MOCK_MODE=false
```

❌ **DO NOT COMMIT** - Contains production secrets
🔒 **Store in:** AWS Secrets Manager, Vault, etc.

## Configuration Categories

### 1. Database

```bash
DB_HOST=localhost
DB_PORT=5432
DB_NAME=oddiya
DB_USER=oddiya_user
DB_PASSWORD=strong-password-here
```

**Production Tips:**
- Use strong passwords (20+ characters)
- Rotate passwords regularly
- Use connection pooling

### 2. Redis

```bash
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=  # Optional for local, required for production
```

**Production Tips:**
- Enable password authentication
- Use Redis Cluster for HA
- Configure maxmemory policy

### 3. AWS Services

```bash
AWS_REGION=ap-northeast-2
AWS_ACCESS_KEY_ID=AKIAIOSFODNN7EXAMPLE
AWS_SECRET_ACCESS_KEY=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
S3_BUCKET=oddiya-storage
SQS_QUEUE_URL=https://sqs.ap-northeast-2.amazonaws.com/123/oddiya-jobs
SNS_TOPIC_ARN=arn:aws:sns:ap-northeast-2:123:oddiya-notifications
```

**Production Tips:**
- Use IAM roles (EC2/EKS) instead of keys when possible
- Restrict IAM permissions to minimum required
- Enable CloudWatch logging

### 4. OAuth Credentials

```bash
GOOGLE_CLIENT_ID=123456789.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=GOCSPX-abc123def456
OAUTH_REDIRECT_URI=https://app.oddiya.com/oauth/callback
```

**Production Tips:**
- Use separate OAuth apps per environment
- Restrict redirect URIs
- Enable OAuth consent screen

### 5. JWT Configuration

```bash
JWT_ACCESS_TOKEN_VALIDITY=3600      # 1 hour
JWT_REFRESH_TOKEN_VALIDITY=1209600  # 14 days
```

**Production Tips:**
- Short access token lifetime (1 hour)
- Longer refresh token (7-14 days)
- Rotate signing keys regularly

## Using Configuration

### Method 1: Direct Load

```bash
# Load and start services
./scripts/run-with-env.sh local
```

### Method 2: Docker Compose

```bash
# Load environment
source ./scripts/load-env.sh local

# Start with loaded environment
docker-compose --env-file .env.local up -d
```

### Method 3: Manual

```bash
# Load in current shell
set -a
source .env.local
set +a

# Run service
cd services/auth-service
./gradlew bootRun
```

## Validation

### Before Deployment

```bash
# Validate configuration
./scripts/validate-env.sh .env.production

# Output:
# ✅ DB_HOST
# ✅ DB_PASSWORD
# ⚠️  WARNING: Using default GOOGLE_CLIENT_ID
# ❌ MISSING: AWS_ACCESS_KEY_ID
```

### Required Variables

**Always Required:**
- DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD
- REDIS_HOST, REDIS_PORT
- AWS_REGION, S3_BUCKET

**Production Only:**
- AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY
- GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET
- KAKAO_LOCAL_API_KEY

## Secrets Management

### Local Development

✅ Use `.env.local` with test credentials
✅ Commit `.env.local` to git
✅ Enable `MOCK_MODE=true`

### Staging/Production

❌ Never commit `.env.staging` or `.env.production`
✅ Use secrets management service
✅ Inject secrets at runtime

### Option 1: AWS Secrets Manager

```bash
# Store secrets
aws secretsmanager create-secret \
    --name oddiya/production/db-password \
    --secret-string "your-strong-password"

# Retrieve in deployment script
DB_PASSWORD=$(aws secretsmanager get-secret-value \
    --secret-id oddiya/production/db-password \
    --query SecretString \
    --output text)
```

### Option 2: Kubernetes Secrets

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: oddiya-secrets
type: Opaque
stringData:
  db-password: "your-strong-password"
  google-client-secret: "your-oauth-secret"
```

### Option 3: GitHub Secrets

```yaml
# .github/workflows/deploy.yml
env:
  DB_PASSWORD: ${{ secrets.DB_PASSWORD }}
  AWS_ACCESS_KEY_ID: ${{ secrets.AWS_ACCESS_KEY_ID }}
```

## Environment-Specific Configuration

### Development (.env.local)

```bash
ENVIRONMENT=development
LOG_LEVEL=DEBUG
MOCK_MODE=true
ENABLE_SWAGGER=true
DB_PASSWORD=dev-password
```

### Staging (.env.staging)

```bash
ENVIRONMENT=staging
LOG_LEVEL=INFO
MOCK_MODE=false
ENABLE_SWAGGER=true
DB_PASSWORD=<real-staging-password>
```

### Production (.env.production)

```bash
ENVIRONMENT=production
LOG_LEVEL=WARN
MOCK_MODE=false
ENABLE_SWAGGER=false
DB_PASSWORD=<real-production-password>
```

## Best Practices

### 1. Never Commit Secrets

```bash
# .gitignore already includes:
.env
.env.staging
.env.production
.env.*.local

# Safe to commit:
.env.example
.env.local
```

### 2. Use Strong Passwords

```bash
# Generate strong password
openssl rand -base64 32

# Use in .env
DB_PASSWORD=generated-strong-password-here
```

### 3. Validate Before Deploy

```bash
# Always validate
./scripts/validate-env.sh .env.production

# Check for common issues
grep -i "test\|example\|changeme" .env.production
```

### 4. Rotate Credentials Regularly

- Database passwords: Every 90 days
- OAuth secrets: Yearly
- AWS keys: Every 90 days
- JWT signing keys: Every 180 days

### 5. Use Different Credentials Per Environment

```bash
# Development
GOOGLE_CLIENT_ID=dev-client-id

# Staging
GOOGLE_CLIENT_ID=staging-client-id

# Production
GOOGLE_CLIENT_ID=prod-client-id
```

## Troubleshooting

### Issue: Variables Not Loading

```bash
# Check file exists
ls -la .env.local

# Verify format (no spaces around =)
cat .env.local | grep "="

# Load manually
source .env.local
echo $DB_HOST
```

### Issue: Docker Not Using .env

```bash
# Explicitly specify env file
docker-compose --env-file .env.local up

# Verify variables in container
docker exec oddiya-auth-service env | grep DB_
```

### Issue: Missing Variables

```bash
# Run validation
./scripts/validate-env.sh .env.local

# Compare with example
diff .env.local .env.example
```

## Migration Guide

### From .env to Environment-Specific

```bash
# Backup existing .env
cp .env .env.backup

# Create local version
cp .env .env.local

# Remove secrets from .env.local
# Replace with test values

# Create staging (don't commit)
cp .env .env.staging
# Update with staging credentials

# Create production (don't commit)  
cp .env .env.production
# Update with production credentials
```

## Security Checklist

- [ ] `.env.staging` and `.env.production` in `.gitignore`
- [ ] Strong passwords (20+ characters)
- [ ] Different credentials per environment
- [ ] Secrets stored in secrets manager
- [ ] OAuth apps configured per environment
- [ ] AWS IAM roles used instead of keys (when possible)
- [ ] Configuration validated before deployment
- [ ] Credentials rotation schedule established
- [ ] Team trained on secrets management
- [ ] Audit logs enabled for secrets access

## Resources

- [12-Factor App Config](https://12factor.net/config)
- [AWS Secrets Manager](https://aws.amazon.com/secrets-manager/)
- [Docker Secrets](https://docs.docker.com/engine/swarm/secrets/)
- [Kubernetes Secrets](https://kubernetes.io/docs/concepts/configuration/secret/)

