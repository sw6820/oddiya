# Phase 1 Quick Start Guide

**Goal:** Deploy Oddiya to 2x t2.micro EC2 instances in ~30 minutes

---

## 📋 Prerequisites (10 minutes)

### 1. AWS Account Setup
```bash
# Create AWS account (if not exists)
# Set up billing alerts

# Create IAM user with admin access
# Download access key CSV
```

### 2. Install Tools
```bash
# Install AWS CLI
curl "https://awscli.amazonaws.com/AWSCLIV2.pkg" -o "AWSCLIV2.pkg"
sudo installer -pkg AWSCLIV2.pkg -target /

# Install Terraform
brew tap hashicorp/tap
brew install hashicorp/tap/terraform

# Configure AWS CLI
aws configure
# Enter: Access Key ID, Secret Access Key, Region (us-east-1), Format (json)
```

### 3. Create SSH Key Pair
```bash
# In AWS Console:
# EC2 → Key Pairs → Create Key Pair
# Name: oddiya-prod
# Type: RSA, Format: .pem

# Save to ~/.ssh/
mv ~/Downloads/oddiya-prod.pem ~/.ssh/
chmod 400 ~/.ssh/oddiya-prod.pem
```

### 4. Get API Keys
```bash
# Gemini API (FREE):
# Visit: https://ai.google.dev/
# Sign in with Google
# Get API Key → Copy key

# Save for later:
export GEMINI_API_KEY="your-key-here"
```

---

## 🚀 Deployment (20 minutes)

### Step 1: Configure Terraform (2 minutes)

```bash
cd infrastructure/terraform/phase1

# Copy template
cp terraform.tfvars.example terraform.tfvars

# Edit configuration
nano terraform.tfvars
```

**Required values:**
```hcl
aws_region = "us-east-1"
environment = "prod"
instance_type = "t2.micro"
key_pair_name = "oddiya-prod"

# YOUR IP (find it: curl ifconfig.me)
admin_ip_whitelist = ["YOUR_IP/32"]

# Strong password (16+ chars)
db_password = "CHANGE_THIS_SECURE_PASSWORD"

# Gemini API key
gemini_api_key = "your-gemini-api-key-here"
```

### Step 2: Deploy Infrastructure (5 minutes)

```bash
# Initialize Terraform
terraform init

# Preview changes
terraform plan

# Deploy (takes ~5 minutes)
terraform apply

# Type 'yes' when prompted
```

**Expected output:**
```
Apply complete! Resources: 15 added, 0 changed, 0 destroyed.

Outputs:
app_server_public_ip = "54.xxx.xxx.xxx"
db_server_private_ip = "10.0.2.xxx"
```

**Save these IPs!** You'll need them.

### Step 3: Setup Database (5 minutes)

```bash
cd ../../../scripts

# Run database setup script
./setup-database-phase1.sh

# When prompted, enter database password (same as in terraform.tfvars)
# Confirm password
```

**Wait for:**
- PostgreSQL configuration
- Database and user creation
- Schema creation
- Migrations (if any)
- Backup setup

### Step 4: Deploy Applications (5 minutes)

```bash
# Run deployment script
./deploy-phase1.sh

# Script will:
# - Build Plan Service (Java)
# - Package LLM Agent (Python)
# - Deploy to EC2
# - Create systemd services
# - Start services
# - Run health checks
```

### Step 5: Update Secrets (3 minutes)

The deployment script creates service files with placeholder secrets. Update them now:

```bash
APP_IP=$(cd ../infrastructure/terraform/phase1 && terraform output -raw app_server_public_ip)

# SSH to app server
ssh -i ~/.ssh/oddiya-prod.pem ec2-user@$APP_IP

# Update LLM Agent API key
nano /opt/oddiya/llm-agent/.env
# Replace: YOUR_GEMINI_API_KEY_HERE with actual key

# Update Plan Service database password
sudo nano /etc/systemd/system/plan-service.service
# Replace: YOUR_DB_PASSWORD_HERE with actual password

# Reload and restart services
sudo systemctl daemon-reload
sudo systemctl restart plan-service llm-agent

# Wait 10 seconds
sleep 10

# Check status
sudo systemctl status plan-service
sudo systemctl status llm-agent

# Exit SSH
exit
```

---

## ✅ Verification (5 minutes)

### 1. Health Checks

```bash
APP_IP=$(cd infrastructure/terraform/phase1 && terraform output -raw app_server_public_ip)

# Test LLM Agent
curl http://$APP_IP:8000/health
# Expected: {"status":"ok"}

# Test Plan Service
curl http://$APP_IP:8083/actuator/health
# Expected: {"status":"UP"}
```

### 2. End-to-End Test

```bash
# Create a plan
curl -X POST http://$APP_IP:8083/api/v1/plans \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -d '{
    "destination": "Seoul",
    "startDate": "2025-11-10",
    "endDate": "2025-11-12",
    "budget": 100000
  }'

# Expected: JSON response with plan details (takes ~6 seconds first time)
```

### 3. Verify Database Persistence

```bash
# List plans
curl http://$APP_IP:8083/api/v1/plans -H "X-User-Id: 1"

# Expected: JSON array with your created plan
# If empty, check Plan Service logs:
# ssh -i ~/.ssh/oddiya-prod.pem ec2-user@$APP_IP
# sudo journalctl -u plan-service -n 50
```

---

## 📱 Update Mobile App

```typescript
// mobile/src/constants/config.ts
export const API_CONFIG = {
  LLM_AGENT_BASE_URL: 'http://54.xxx.xxx.xxx:8000', // Your APP_IP
  PLAN_SERVICE_BASE_URL: 'http://54.xxx.xxx.xxx:8083',
};
```

```bash
# Rebuild mobile app
cd mobile
npm run ios    # or npm run android
```

---

## 🐛 Troubleshooting

### Issue: Health checks fail

```bash
# SSH to app server
ssh -i ~/.ssh/oddiya-prod.pem ec2-user@$APP_IP

# Check service status
sudo systemctl status plan-service
sudo systemctl status llm-agent

# Check logs
sudo journalctl -u plan-service -f
sudo journalctl -u llm-agent -f

# Common issues:
# - LLM Agent: Invalid Gemini API key
# - Plan Service: Wrong database password or cannot reach DB
```

### Issue: Cannot SSH to EC2

```bash
# Verify security group allows your IP
MY_IP=$(curl -s ifconfig.me)
echo $MY_IP

# Update terraform.tfvars if IP changed
cd infrastructure/terraform/phase1
nano terraform.tfvars
# Update admin_ip_whitelist = ["NEW_IP/32"]

terraform apply
```

### Issue: Database connection failed

```bash
# Test from app server
ssh -i ~/.ssh/oddiya-prod.pem ec2-user@$APP_IP

DB_IP=$(cd /tmp && terraform output -raw db_server_private_ip)
telnet $DB_IP 5432

# If connection refused:
# 1. Check DB server security group
# 2. Check PostgreSQL is running
# 3. Check pg_hba.conf allows app server IP
```

### Issue: Out of memory

```bash
# Check memory usage
ssh -i ~/.ssh/oddiya-prod.pem ec2-user@$APP_IP
free -h

# If low memory:
# - Reduce JVM heap: Edit /etc/systemd/system/plan-service.service
#   Change -Xmx512m to -Xmx384m
# - Restart: sudo systemctl daemon-reload && sudo systemctl restart plan-service
```

---

## 💰 Cost Monitor

```bash
# Check AWS billing dashboard daily
# Set up billing alert for $10/month

# To stop instances (saves $17/mo):
cd infrastructure/terraform/phase1
terraform destroy
# Type 'yes' to confirm

# WARNING: This deletes ALL data!
```

---

## 📊 Monitoring

### View Logs

```bash
# SSH to app server
ssh -i ~/.ssh/oddiya-prod.pem ec2-user@$APP_IP

# Follow Plan Service logs
sudo journalctl -u plan-service -f

# Follow LLM Agent logs
sudo journalctl -u llm-agent -f

# View last 50 lines
sudo journalctl -u plan-service -n 50
```

### Check Resource Usage

```bash
# CPU and Memory
top

# Disk space
df -h

# Network connections
sudo netstat -tlnp | grep -E '8000|8083'
```

---

## 🎯 Success Checklist

- [ ] Terraform apply successful
- [ ] Both EC2 instances running
- [ ] Database created and accessible
- [ ] Plan Service health check passes
- [ ] LLM Agent health check passes
- [ ] Can create a plan via API
- [ ] Plan persists in database
- [ ] Mobile app configured with EC2 IP
- [ ] Mobile app can create plans
- [ ] Billing alert configured

---

## 📚 Next Steps

After successful deployment:

1. **Set up domain (optional):**
   - Register domain (e.g., oddiya.com)
   - Point to Elastic IP
   - Set up SSL/TLS with Let's Encrypt

2. **Add Redis caching:**
   - Reduces LLM costs by 99%
   - See Phase 2 documentation

3. **Enable monitoring:**
   - CloudWatch alarms
   - Log aggregation
   - Performance metrics

4. **Implement OAuth:**
   - Google Sign-In
   - Apple Sign-In
   - See authentication guide

---

## 🆘 Support

- **Terraform errors:** Check `terraform plan` output
- **Service errors:** Check logs with `journalctl`
- **Network errors:** Verify security groups and IPs
- **API errors:** Check application logs

**Documentation:**
- [Full Deployment Plan](PHASE1_DEPLOYMENT_PLAN.md)
- [Terraform README](../../infrastructure/terraform/phase1/README.md)

---

**Estimated Total Time:** 30-40 minutes
**Monthly Cost (after free tier):** ~$57/month
**Monthly Cost (with optimizations):** ~$25/month

**Status:** Ready for Production ✅
