# Secrets Management Guide

**Version:** v1.4
**Last Updated:** 2025-11-04

Complete guide for managing sensitive data (API keys, passwords, tokens) in Oddiya deployment.

---

## 🔐 Overview

Oddiya requires several secrets for deployment:

| Secret | Where Used | Example |
|--------|------------|---------|
| Database Password | PostgreSQL, Plan Service | `MySecureP@ssw0rd123` |
| Gemini API Key | LLM Agent | `AIzaSyD...` |
| AWS Access Keys | Terraform, AWS CLI | `AKIAIOSFODNN7EXAMPLE` |
| SSH Private Key | EC2 access | `~/.ssh/oddiya-prod.pem` |

**Critical:** These MUST NEVER be committed to git!

---

## 🎯 Secrets Management Strategy

### Phase 1: Manual (Current - Simple)

**Best for:** Initial deployment, development, small teams

**Approach:**
- Store secrets locally in `.env` files
- Use `.gitignore` to prevent commits
- Manually configure on EC2 instances
- Use AWS Secrets Manager for production

### Phase 2: AWS Secrets Manager (Production)

**Best for:** Production deployments, team collaboration

**Approach:**
- Store all secrets in AWS Secrets Manager
- Application retrieves secrets at runtime
- Automatic rotation support
- Audit logging enabled

---

## 📁 Local Secrets Management

### 1. Directory Structure

```
oddiya/
├── .env                           # ❌ NEVER COMMIT
├── .env.example                   # ✅ Template (safe to commit)
├── .gitignore                     # ✅ Must include .env
│
├── infrastructure/terraform/phase1/
│   ├── terraform.tfvars           # ❌ NEVER COMMIT
│   └── terraform.tfvars.example   # ✅ Template (safe to commit)
│
├── services/llm-agent/
│   ├── .env                       # ❌ NEVER COMMIT
│   └── .env.example               # ✅ Template (safe to commit)
│
└── mobile/
    ├── .env                       # ❌ NEVER COMMIT (if using)
    └── src/constants/config.ts    # ⚠️ OK for non-sensitive config
```

### 2. Create .gitignore (Already Done)

Verify `.gitignore` includes:

```bash
# Check gitignore
cat .gitignore | grep -E "\.env|terraform\.tfvars|\.pem"
```

**Should include:**
```gitignore
# Environment files
.env
.env.local
.env.*.local

# Terraform
terraform.tfvars
*.tfstate
*.tfstate.backup

# SSH keys
*.pem
*.key
```

### 3. Create .env Files (Don't Commit!)

**Root .env:**
```bash
# Copy example
cp .env.example .env

# Edit with real values
nano .env
```

**Example .env:**
```bash
# Google Gemini
GOOGLE_API_KEY=AIzaSyD...your-actual-key
GEMINI_MODEL=gemini-2.0-flash-exp

# Database
DB_PASSWORD=MySecureP@ssw0rd123
DB_HOST=localhost
DB_PORT=5432
DB_NAME=oddiya
DB_USER=admin

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
```

**LLM Agent .env:**
```bash
cd services/llm-agent
cp .env.example .env
nano .env

# Add:
GOOGLE_API_KEY=AIzaSyD...
GEMINI_MODEL=gemini-2.0-flash-exp
```

### 4. Verify .env Not Tracked

```bash
# Check if .env is ignored
git status --ignored | grep .env

# Should show:
# .env
# services/llm-agent/.env

# If .env is NOT ignored, add to .gitignore:
echo ".env" >> .gitignore
echo "services/llm-agent/.env" >> .gitignore
```

### 5. Terraform Variables

```bash
cd infrastructure/terraform/phase1

# Copy example
cp terraform.tfvars.example terraform.tfvars

# Edit with secrets
nano terraform.tfvars
```

**terraform.tfvars (NEVER COMMIT):**
```hcl
aws_region = "ap-northeast-2"
environment = "prod"
key_pair_name = "oddiya-prod"

# Your IP
admin_ip_whitelist = ["123.456.789.0/32"]

# Secrets
db_password = "MySecureP@ssw0rd123"
gemini_api_key = "AIzaSyD...your-actual-key"
```

**Verify not tracked:**
```bash
git status | grep terraform.tfvars
# Should show nothing (file is ignored)
```

---

## 🔒 SSH Key Management

### 1. Create SSH Key Pair

**In AWS Console:**
```
1. Go to: EC2 → Key Pairs (Seoul/ap-northeast-2 region!)
2. Click: Create Key Pair
3. Name: oddiya-prod
4. Type: RSA
5. Format: .pem
6. Download: oddiya-prod.pem
```

**Save securely:**
```bash
# Move to SSH directory
mv ~/Downloads/oddiya-prod.pem ~/.ssh/

# Set correct permissions (MUST be 400)
chmod 400 ~/.ssh/oddiya-prod.pem

# Verify
ls -la ~/.ssh/oddiya-prod.pem
# Should show: -r-------- (read-only for owner)
```

**NEVER commit to git!**

### 2. Use SSH Key

```bash
# Connect to EC2
ssh -i ~/.ssh/oddiya-prod.pem ec2-user@<EC2-IP>

# Or set SSH config for convenience
nano ~/.ssh/config

# Add:
Host oddiya-app
  HostName <EC2-PUBLIC-IP>
  User ec2-user
  IdentityFile ~/.ssh/oddiya-prod.pem

# Now use: ssh oddiya-app
```

---

## ☁️ AWS Secrets Manager (Production)

### Why Use AWS Secrets Manager?

- ✅ Centralized secret storage
- ✅ Automatic rotation
- ✅ Access control via IAM
- ✅ Audit logging (who accessed what, when)
- ✅ No secrets in code or EC2

**Cost:** $0.40/secret/month + $0.05 per 10,000 API calls

### 1. Store Secrets

```bash
# Store database password
aws secretsmanager create-secret \
  --name oddiya/prod/db-password \
  --description "PostgreSQL admin password" \
  --secret-string "MySecureP@ssw0rd123" \
  --region ap-northeast-2

# Store Gemini API key
aws secretsmanager create-secret \
  --name oddiya/prod/gemini-api-key \
  --description "Google Gemini API key" \
  --secret-string "AIzaSyD...your-key" \
  --region ap-northeast-2
```

### 2. Retrieve Secrets (Python - LLM Agent)

**Update `services/llm-agent/src/config/settings.py`:**

```python
import boto3
import os
from functools import lru_cache

class Settings:
    def __init__(self):
        # Try AWS Secrets Manager first (production)
        if os.getenv("USE_SECRETS_MANAGER") == "true":
            self.google_api_key = self._get_secret("oddiya/prod/gemini-api-key")
        else:
            # Fallback to .env (development)
            self.google_api_key = os.getenv("GOOGLE_API_KEY")

        self.gemini_model = os.getenv("GEMINI_MODEL", "gemini-2.0-flash-exp")

    def _get_secret(self, secret_name: str) -> str:
        """Retrieve secret from AWS Secrets Manager"""
        session = boto3.session.Session()
        client = session.client(
            service_name='secretsmanager',
            region_name='ap-northeast-2'
        )

        try:
            response = client.get_secret_value(SecretId=secret_name)
            return response['SecretString']
        except Exception as e:
            print(f"Error retrieving secret {secret_name}: {e}")
            raise

@lru_cache()
def get_settings():
    return Settings()
```

**Update systemd service:**
```bash
sudo nano /etc/systemd/system/llm-agent.service

# Add:
Environment="USE_SECRETS_MANAGER=true"
```

### 3. Retrieve Secrets (Java - Plan Service)

**Add dependency to `build.gradle`:**
```gradle
dependencies {
    implementation 'software.amazon.awssdk:secretsmanager:2.20.0'
}
```

**Create `SecretsManagerConfig.java`:**
```java
package com.oddiya.plan.config;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import org.springframework.stereotype.Component;

@Component
public class SecretsManagerConfig {

    private final SecretsManagerClient client;

    public SecretsManagerConfig() {
        this.client = SecretsManagerClient.builder()
            .region(Region.AP_NORTHEAST_2)
            .build();
    }

    public String getSecret(String secretName) {
        GetSecretValueRequest request = GetSecretValueRequest.builder()
            .secretId(secretName)
            .build();

        GetSecretValueResponse response = client.getSecretValue(request);
        return response.secretString();
    }
}
```

**Update `application.yml`:**
```yaml
spring:
  datasource:
    password: ${DB_PASSWORD:#{@secretsManagerConfig.getSecret('oddiya/prod/db-password')}}
```

### 4. IAM Permissions

**Add to EC2 IAM role:**
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "secretsmanager:GetSecretValue",
        "secretsmanager:DescribeSecret"
      ],
      "Resource": [
        "arn:aws:secretsmanager:ap-northeast-2:*:secret:oddiya/prod/*"
      ]
    }
  ]
}
```

---

## 🔄 Secrets Rotation

### Manual Rotation (Current)

```bash
# 1. Generate new password
NEW_PASSWORD=$(openssl rand -base64 32)

# 2. Update in Secrets Manager
aws secretsmanager put-secret-value \
  --secret-id oddiya/prod/db-password \
  --secret-string "$NEW_PASSWORD" \
  --region ap-northeast-2

# 3. Update database
ssh -i ~/.ssh/oddiya-prod.pem ec2-user@<DB-IP>
sudo -u postgres psql -d oddiya -c "ALTER USER admin PASSWORD '$NEW_PASSWORD';"

# 4. Restart services (will fetch new secret)
sudo systemctl restart plan-service
```

### Automatic Rotation (Future)

**Lambda function for rotation:**
```python
import boto3
import psycopg2
import secrets

def lambda_handler(event, context):
    # Generate new password
    new_password = secrets.token_urlsafe(32)

    # Update in database
    # ... (update PostgreSQL password)

    # Update in Secrets Manager
    # ... (rotate secret)

    return {"statusCode": 200}
```

---

## 🚨 Security Best Practices

### 1. Password Requirements

**Database Password:**
- ✅ Minimum 16 characters
- ✅ Mix of uppercase, lowercase, numbers, symbols
- ✅ No dictionary words
- ✅ Unique (not reused)

**Generate strong password:**
```bash
# Option 1: OpenSSL
openssl rand -base64 24

# Option 2: pwgen
pwgen -s 24 1

# Option 3: Python
python3 -c "import secrets; print(secrets.token_urlsafe(24))"
```

### 2. API Key Security

**Gemini API Key:**
- ✅ Keep in `.env` (development)
- ✅ Use Secrets Manager (production)
- ✅ Restrict by IP (if possible)
- ✅ Monitor usage in Google Console

**Never:**
- ❌ Commit to git
- ❌ Share via email/Slack
- ❌ Hardcode in source files
- ❌ Log in application logs

### 3. SSH Key Security

**Best practices:**
- ✅ Use 400 permissions (`chmod 400`)
- ✅ Store in `~/.ssh/`
- ✅ Use passphrase (optional but recommended)
- ✅ Rotate periodically (every 90 days)

**If compromised:**
```bash
# 1. Delete old key pair in AWS console
# 2. Create new key pair
# 3. Update EC2 instances with new public key
```

### 4. Terraform State Security

**terraform.tfstate may contain secrets!**

**Solution: Use S3 backend with encryption:**

```hcl
# infrastructure/terraform/phase1/main.tf
terraform {
  backend "s3" {
    bucket         = "oddiya-terraform-state-prod"
    key            = "phase1/terraform.tfstate"
    region         = "ap-northeast-2"
    encrypt        = true
    dynamodb_table = "oddiya-terraform-locks"
  }
}
```

**Setup:**
```bash
# 1. Create S3 bucket
aws s3api create-bucket \
  --bucket oddiya-terraform-state-prod \
  --region ap-northeast-2 \
  --create-bucket-configuration LocationConstraint=ap-northeast-2

# 2. Enable versioning
aws s3api put-bucket-versioning \
  --bucket oddiya-terraform-state-prod \
  --versioning-configuration Status=Enabled

# 3. Enable encryption
aws s3api put-bucket-encryption \
  --bucket oddiya-terraform-state-prod \
  --server-side-encryption-configuration '{
    "Rules": [{
      "ApplyServerSideEncryptionByDefault": {
        "SSEAlgorithm": "AES256"
      }
    }]
  }'

# 4. Create DynamoDB table for state locking
aws dynamodb create-table \
  --table-name oddiya-terraform-locks \
  --attribute-definitions AttributeName=LockID,AttributeType=S \
  --key-schema AttributeName=LockID,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --region ap-northeast-2
```

---

## 📋 Secrets Checklist

### Before Deployment

- [ ] Created `.env` files with real values
- [ ] Verified `.env` is in `.gitignore`
- [ ] Created `terraform.tfvars` with secrets
- [ ] Verified `terraform.tfvars` is ignored
- [ ] Generated strong database password (16+ chars)
- [ ] Obtained Gemini API key
- [ ] Created SSH key pair in AWS (ap-northeast-2)
- [ ] Downloaded and secured SSH key (chmod 400)
- [ ] Verified no secrets in git: `git log --all -S "GOOGLE_API_KEY"`

### After Deployment

- [ ] Secrets stored in AWS Secrets Manager (optional)
- [ ] IAM policies configured for secrets access
- [ ] Terraform state in encrypted S3 bucket
- [ ] Password rotation schedule set (90 days)
- [ ] API key usage monitoring enabled
- [ ] SSH keys rotated (optional)

---

## 🔍 Audit & Monitoring

### 1. Check for Leaked Secrets

**Before commit:**
```bash
# Search for potential secrets
git diff | grep -E "API_KEY|PASSWORD|SECRET"

# Use git-secrets (install first)
git secrets --scan
```

**Check entire repository:**
```bash
# Search for API keys
git log --all -S "AIzaSy" --oneline

# Search for passwords
git log --all -S "password" --oneline
```

### 2. Monitor API Usage

**Gemini API:**
- Check usage: https://console.cloud.google.com/apis/dashboard
- Set budget alerts
- Monitor for unusual patterns

**AWS Secrets Manager:**
```bash
# View access logs (requires CloudTrail)
aws cloudtrail lookup-events \
  --lookup-attributes AttributeKey=ResourceType,AttributeValue=AWS::SecretsManager::Secret \
  --region ap-northeast-2
```

---

## 🆘 What If Secrets Are Compromised?

### If API Key Leaked

1. **Immediately revoke key:**
   - Google Console → API Credentials → Delete
   - Generate new key

2. **Update everywhere:**
   ```bash
   # Update .env
   nano services/llm-agent/.env

   # Update on EC2
   ssh oddiya-app
   nano /opt/oddiya/llm-agent/.env
   sudo systemctl restart llm-agent

   # Update Secrets Manager (if using)
   aws secretsmanager put-secret-value \
     --secret-id oddiya/prod/gemini-api-key \
     --secret-string "NEW_KEY"
   ```

3. **Search git history:**
   ```bash
   # Remove from git history (if committed)
   # Use BFG Repo-Cleaner or git-filter-repo
   ```

### If Database Password Leaked

1. **Change password immediately:**
   ```bash
   ssh oddiya-db
   sudo -u postgres psql -d oddiya
   ALTER USER admin PASSWORD 'NEW_SECURE_PASSWORD';
   \q
   ```

2. **Update services:**
   ```bash
   # Update Plan Service
   sudo nano /etc/systemd/system/plan-service.service
   # Change Environment="DB_PASSWORD=..."
   sudo systemctl daemon-reload
   sudo systemctl restart plan-service
   ```

### If SSH Key Leaked

1. **Delete key pair in AWS console**
2. **Create new key pair**
3. **Update authorized_keys on EC2:**
   ```bash
   # From AWS console: Instance Connect
   # Or use Session Manager (if configured)

   # Remove old key, add new key
   nano ~/.ssh/authorized_keys
   ```

---

## 📚 Tools & Resources

### Password Managers (Recommended)

- **1Password:** Team password sharing
- **LastPass:** Free for personal use
- **Bitwarden:** Open source, self-hostable

### Secret Scanning Tools

- **git-secrets:** Prevent committing secrets
  ```bash
  brew install git-secrets
  git secrets --install
  git secrets --register-aws
  ```

- **truffleHog:** Find secrets in git history
  ```bash
  pip install truffleHog
  truffleHog --regex --entropy=False .
  ```

- **detect-secrets:** Baseline secret detection
  ```bash
  pip install detect-secrets
  detect-secrets scan > .secrets.baseline
  ```

### AWS Tools

- **AWS Secrets Manager:** Centralized secrets
- **AWS Systems Manager Parameter Store:** Simple key-value store (cheaper)
- **AWS KMS:** Encryption keys management

---

## 📝 Summary

### Development (Phase 1)

**Use:**
- `.env` files locally (gitignored)
- `terraform.tfvars` for infrastructure secrets
- SSH key in `~/.ssh/` (chmod 400)
- Manual secrets management

**Cost:** $0

### Production (Phase 2+)

**Use:**
- AWS Secrets Manager for all secrets
- S3 backend for Terraform state (encrypted)
- Automatic rotation enabled
- IAM policies for access control

**Cost:** ~$1-2/month

---

**Next Steps:**
1. Create `.env` files with real values
2. Create `terraform.tfvars` with secrets
3. Generate SSH key pair in AWS (ap-northeast-2)
4. Deploy Phase 1
5. Later: Migrate to AWS Secrets Manager

**Status:** Ready for Secure Deployment ✅
