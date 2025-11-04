# Phase 1 Terraform Deployment

This directory contains Terraform configuration for deploying Oddiya Phase 1 infrastructure on AWS.

## 🏗️ Infrastructure

- **EC2 #1:** Application Server (t2.micro, public subnet)
  - Java Plan Service (Port 8083)
  - Python LLM Agent (Port 8000)

- **EC2 #2:** Database Server (t2.micro, private subnet)
  - PostgreSQL 15 (Port 5432)

- **Networking:** VPC, Public/Private Subnets, Internet Gateway, NAT Gateway
- **Security:** Security Groups with minimal necessary access

## 📋 Prerequisites

1. **AWS Account** with admin access
2. **AWS CLI** installed and configured (`aws configure`)
3. **Terraform** >= 1.0 installed
4. **SSH Key Pair** created in AWS EC2 console
5. **Gemini API Key** from https://ai.google.dev/

## 🚀 Quick Start

### Step 1: Create SSH Key Pair

```bash
# In AWS Console: EC2 → Key Pairs → Create Key Pair
# Name: oddiya-prod
# Type: RSA
# Format: .pem
# Download and save to ~/.ssh/oddiya-prod.pem

chmod 400 ~/.ssh/oddiya-prod.pem
```

### Step 2: Configure Variables

```bash
# Copy example file
cp terraform.tfvars.example terraform.tfvars

# Edit with your values
nano terraform.tfvars

# IMPORTANT: Set your IP address for SSH access
# Find your IP: curl ifconfig.me
```

### Step 3: Initialize Terraform

```bash
cd infrastructure/terraform/phase1
terraform init
```

### Step 4: Review Plan

```bash
terraform plan
```

**Expected resources:**
- 1 VPC
- 2 Subnets (public, private)
- 1 Internet Gateway
- 1 NAT Gateway
- 2 Elastic IPs
- 2 Route Tables
- 2 Security Groups
- 1 IAM Role + Instance Profile
- 2 EC2 instances (t2.micro)

### Step 5: Deploy

```bash
terraform apply

# Type 'yes' when prompted
# Deployment takes ~5 minutes
```

### Step 6: Get Outputs

```bash
terraform output

# Expected output:
# app_server_public_ip = "54.x.x.x"
# app_server_private_ip = "10.0.1.x"
# db_server_private_ip = "10.0.2.x"
```

## 📝 Post-Deployment Steps

### 1. Verify EC2 Instances

```bash
# SSH to app server
ssh -i ~/.ssh/oddiya-prod.pem ec2-user@$(terraform output -raw app_server_public_ip)

# Check user data log
tail -f /var/log/user-data.log

# Verify Java
java -version

# Verify Python
python3.11 --version
```

### 2. Configure Database

```bash
# SSH tunnel to DB server through app server
APP_IP=$(terraform output -raw app_server_public_ip)
DB_IP=$(terraform output -raw db_server_private_ip)

ssh -i ~/.ssh/oddiya-prod.pem -L 2222:$DB_IP:22 ec2-user@$APP_IP

# In another terminal, connect to DB server
ssh -i ~/.ssh/oddiya-prod.pem -p 2222 ec2-user@localhost

# Follow database setup from PHASE1_DEPLOYMENT_PLAN.md
```

### 3. Deploy Applications

See `/scripts/deploy-phase1.sh` for automated deployment script.

## 🧹 Cleanup

To destroy all resources:

```bash
terraform destroy

# Type 'yes' when prompted
# This will DELETE all resources and data!
```

## 💰 Cost Estimate

| Resource | Monthly Cost |
|----------|--------------|
| EC2 t2.micro x2 | $17.00 |
| EBS gp3 (50 GB) | $5.00 |
| NAT Gateway | $32.00 |
| Elastic IPs | $3.60 |
| **Total** | **~$57.60** |

**Free Tier (first 12 months):**
- EC2: Free
- EBS: 30 GB free
- Total: ~$35.60/mo

**Cost Optimization:**
- Replace NAT Gateway with NAT Instance: Save $32/mo
- Use Spot Instances: Save 70% on EC2

## 🔒 Security Notes

1. **Change default passwords** in terraform.tfvars
2. **Restrict SSH access** to your IP only in `admin_ip_whitelist`
3. **DO NOT commit** terraform.tfvars to git
4. **Enable MFA** on AWS root account
5. **Use AWS Secrets Manager** for production secrets

## 📊 Monitoring

After deployment, set up CloudWatch alarms:

```bash
# CPU utilization > 80%
# Memory utilization > 90%
# Disk space < 20%
```

## 🐛 Troubleshooting

### Issue: Terraform fails with "Key pair not found"

**Solution:**
```bash
# Verify key pair exists
aws ec2 describe-key-pairs --key-names oddiya-prod

# Create if missing
aws ec2 create-key-pair --key-name oddiya-prod --query 'KeyMaterial' --output text > oddiya-prod.pem
```

### Issue: Cannot SSH to instances

**Solution:**
```bash
# Check security group
aws ec2 describe-security-groups --group-ids $(terraform output -raw app_server_security_group_id)

# Verify your IP is whitelisted
curl ifconfig.me

# Update terraform.tfvars with correct IP
```

### Issue: NAT Gateway too expensive

**Solution:**
```bash
# Option 1: Use NAT Instance (not included, manual setup)
# Option 2: Temporarily disable NAT for cost savings (DB server won't have internet)
```

## 📚 References

- [Phase 1 Deployment Plan](../../../docs/deployment/PHASE1_DEPLOYMENT_PLAN.md)
- [AWS Free Tier](https://aws.amazon.com/free/)
- [Terraform AWS Provider](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)

## 🆘 Support

- Terraform Issues: Check logs with `terraform plan -debug`
- AWS Issues: Check CloudWatch Logs
- Application Issues: SSH to EC2 and check `/var/log/user-data.log`
