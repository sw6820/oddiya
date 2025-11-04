# Seoul Region Deployment - Quick Setup

**Region:** ap-northeast-2 (Seoul, South Korea)
**Version:** v1.4 - Phase 1 Cost Optimized
**Last Updated:** 2025-11-04

---

## ✅ Configuration Updated for Seoul

All Phase 1 Terraform files now use **ap-northeast-2** (Seoul region) by default.

---

## 🔐 Secrets Management - Quick Reference

### Step 1: Create Local .env File

```bash
# Copy example
cp .env.example .env

# Edit with your actual secrets
nano .env
```

**Add these values:**
```bash
# Google Gemini API Key (get from https://ai.google.dev/)
GOOGLE_API_KEY=AIzaSyD...your-actual-key
GEMINI_MODEL=gemini-2.0-flash-exp

# Database Password (generate strong password)
DB_PASSWORD=MySecureP@ssw0rd123

# Database connection (local dev)
DB_HOST=localhost
DB_PORT=5432
DB_NAME=oddiya
DB_USER=admin

# Redis (local dev)
REDIS_HOST=localhost
REDIS_PORT=6379
```

### Step 2: Create Terraform Variables

```bash
cd infrastructure/terraform/phase1

# Copy example
cp terraform.tfvars.example terraform.tfvars

# Edit with your values
nano terraform.tfvars
```

**Required values:**
```hcl
# Seoul region (default)
aws_region = "ap-northeast-2"
environment = "prod"
instance_type = "t2.micro"

# SSH key pair name (create in AWS console first!)
key_pair_name = "oddiya-prod"

# Your IP address (find with: curl ifconfig.me)
admin_ip_whitelist = ["123.456.789.0/32"]

# Secrets (NEVER commit this file!)
db_password = "MySecureP@ssw0rd123"  # 16+ chars
gemini_api_key = "AIzaSyD...your-key"
```

### Step 3: Generate Strong Password

```bash
# Option 1: OpenSSL (best)
openssl rand -base64 24
# Example output: xK8mP2vL9nQ3rB5tC6wA7uD8

# Option 2: Python
python3 -c "import secrets; print(secrets.token_urlsafe(24))"

# Save this password in both:
# - .env (DB_PASSWORD)
# - terraform.tfvars (db_password)
```

### Step 4: Verify .gitignore Protection

```bash
# Check that secrets are ignored
git status --ignored | grep -E "\.env|terraform\.tfvars"

# Should show:
# .env
# infrastructure/terraform/phase1/terraform.tfvars

# If not showing, they're properly ignored (not tracked)
```

### Step 5: Create SSH Key in AWS Console

**Important: Must be in ap-northeast-2 (Seoul) region!**

1. Go to: **AWS Console → EC2 → Key Pairs**
2. **Select region:** ap-northeast-2 (top right)
3. Click: **Create Key Pair**
4. Name: `oddiya-prod`
5. Type: RSA
6. Format: .pem
7. Click **Create** → Downloads `oddiya-prod.pem`

**Save securely:**
```bash
# Move to SSH directory
mv ~/Downloads/oddiya-prod.pem ~/.ssh/

# Set correct permissions (MUST be 400)
chmod 400 ~/.ssh/oddiya-prod.pem

# Verify
ls -l ~/.ssh/oddiya-prod.pem
# Should show: -r-------- (read-only for owner)
```

---

## 🚀 Deploy to Seoul

### Prerequisites Checklist

- [ ] Created `.env` file with real secrets
- [ ] Created `terraform.tfvars` with your values
- [ ] Generated strong database password (16+ chars)
- [ ] Obtained Gemini API key from https://ai.google.dev/
- [ ] Created SSH key pair in AWS (ap-northeast-2 region!)
- [ ] Downloaded and secured `oddiya-prod.pem` (chmod 400)
- [ ] Found your public IP (`curl ifconfig.me`)
- [ ] AWS CLI configured for ap-northeast-2

### Deploy Command

```bash
cd infrastructure/terraform/phase1

# Initialize Terraform
terraform init

# Preview changes
terraform plan

# Deploy to Seoul (takes ~5 minutes)
terraform apply
# Type 'yes' when prompted

# Save outputs
terraform output > outputs.txt
```

### Post-Deployment

```bash
# Get EC2 IPs
APP_IP=$(terraform output -raw app_server_public_ip)
DB_IP=$(terraform output -raw db_server_private_ip)

echo "App Server IP: $APP_IP"
echo "DB Server IP: $DB_IP"

# Setup database
cd ../../../scripts
./setup-database-phase1.sh

# Deploy applications
./deploy-phase1.sh

# Update secrets on EC2
ssh -i ~/.ssh/oddiya-prod.pem ec2-user@$APP_IP

# Edit LLM Agent .env
nano /opt/oddiya/llm-agent/.env
# Add real GOOGLE_API_KEY

# Edit Plan Service systemd
sudo nano /etc/systemd/system/plan-service.service
# Add real DB_PASSWORD

# Restart services
sudo systemctl daemon-reload
sudo systemctl restart plan-service llm-agent

# Check status
sudo systemctl status plan-service llm-agent
```

---

## 🔒 Security Best Practices

### What's Protected

**Automatically ignored by .gitignore:**
- ✅ `.env` files
- ✅ `terraform.tfvars`
- ✅ `*.pem` SSH keys
- ✅ `*.tfstate` (Terraform state)
- ✅ `.terraform/` directory

**Never commit these files!**

### Verify No Secrets in Git

```bash
# Check git status (should show nothing sensitive)
git status

# Search for potential leaks
git diff | grep -E "API_KEY|PASSWORD|SECRET"

# Search git history
git log --all -S "AIzaSy" --oneline  # No results = good!
```

### If You Accidentally Commit Secrets

**1. Immediately revoke the secret:**
- Gemini API: Delete in Google Console
- Database: Change password
- SSH key: Delete in AWS Console

**2. Remove from git history:**
```bash
# Option 1: BFG Repo-Cleaner (easiest)
# Download from: https://rtyley.github.io/bfg-repo-cleaner/
java -jar bfg.jar --delete-files .env
java -jar bfg.jar --replace-text passwords.txt

# Option 2: git-filter-repo
pip install git-filter-repo
git filter-repo --path .env --invert-paths
```

**3. Force push (if not shared with others):**
```bash
git push --force
```

---

## 💰 Seoul Region Costs

| Resource | Monthly Cost (Free Tier) | After Free Tier |
|----------|--------------------------|-----------------|
| EC2 t2.micro x2 | $0 | $17.00 |
| EBS gp3 (50 GB) | $1.60 | $4.00 |
| NAT Gateway | ~~$32.00~~ **$0** (removed) | **$0** |
| Elastic IPs | $0 | $3.60 |
| Data Transfer | $3.00 | $5.00 |
| **Total** | **~$5/mo** | **~$26/mo** |

**Note:** Prices in Seoul region are similar to US regions.

---

## 🌍 Why Seoul Region?

### Advantages

- ✅ Lower latency for Korean users
- ✅ Compliance with Korean data laws (if needed)
- ✅ Similar pricing to US regions
- ✅ Good availability of services

### Considerations

- ⚠️ Some AWS services may not be available
- ⚠️ Support is in English/Korean
- ⚠️ Region code: `ap-northeast-2` (remember this!)

---

## 📋 Quick Commands Cheat Sheet

### Deploy
```bash
cd infrastructure/terraform/phase1
terraform apply
```

### Get IPs
```bash
terraform output app_server_public_ip
terraform output db_server_private_ip
```

### SSH to App Server
```bash
ssh -i ~/.ssh/oddiya-prod.pem ec2-user@<APP-IP>
```

### SSH to Database (via App Server)
```bash
ssh -i ~/.ssh/oddiya-prod.pem \
  -J ec2-user@<APP-IP> \
  ec2-user@<DB-IP>
```

### View Logs
```bash
# On app server
sudo journalctl -u plan-service -f
sudo journalctl -u llm-agent -f
```

### Health Checks
```bash
curl http://<APP-IP>:8000/health
curl http://<APP-IP>:8083/actuator/health
```

### Destroy Everything
```bash
cd infrastructure/terraform/phase1
terraform destroy
# Type 'yes' - WARNING: Deletes all data!
```

---

## 🆘 Troubleshooting

### Issue: Can't connect to EC2

**Check your IP:**
```bash
curl ifconfig.me
# Update terraform.tfvars if IP changed
```

**Verify security group:**
```bash
aws ec2 describe-security-groups \
  --region ap-northeast-2 \
  --filters Name=group-name,Values=oddiya-app-server-prod
```

### Issue: Secrets not working

**Verify .env exists:**
```bash
ls -la .env
ls -la services/llm-agent/.env
```

**Check SSH key permissions:**
```bash
ls -l ~/.ssh/oddiya-prod.pem
# Must show: -r--------
```

### Issue: Wrong region

**Check AWS CLI region:**
```bash
aws configure get region
# Should show: ap-northeast-2
```

**Update if wrong:**
```bash
aws configure set region ap-northeast-2
```

---

## 📚 Documentation

- **Secrets Management:** [docs/deployment/SECRETS_MANAGEMENT.md](docs/deployment/SECRETS_MANAGEMENT.md)
- **Quick Start:** [docs/deployment/PHASE1_QUICK_START.md](docs/deployment/PHASE1_QUICK_START.md)
- **Deployment Plan:** [docs/deployment/PHASE1_DEPLOYMENT_PLAN.md](docs/deployment/PHASE1_DEPLOYMENT_PLAN.md)

---

## ✅ Deployment Checklist

- [ ] Region set to ap-northeast-2 (Seoul)
- [ ] Created `.env` with real secrets
- [ ] Created `terraform.tfvars` with real values
- [ ] Generated strong database password
- [ ] Obtained Gemini API key
- [ ] Created SSH key pair in Seoul region
- [ ] Downloaded and secured oddiya-prod.pem
- [ ] Found your public IP address
- [ ] Verified secrets not in git
- [ ] AWS CLI configured for Seoul
- [ ] Ready to deploy!

**Next:** `cd infrastructure/terraform/phase1 && terraform apply`

---

**Status:** Ready for Seoul Deployment ✅
**Cost:** ~$5/month (free tier), ~$26/month (after)
**Time:** 30-40 minutes total
