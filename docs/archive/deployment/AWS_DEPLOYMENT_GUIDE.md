# AWS Deployment Guide - Minimum Cost Setup

## Overview

Deploy Oddiya on a **single EC2 t2.micro instance** (AWS Free Tier) with all services running via Docker Compose.

**Monthly Cost:** $0 (first 12 months with Free Tier)

## Architecture

```
Internet → EC2 t2.micro (Free Tier)
           ├─ Nginx (Port 80/443)
           ├─ API Gateway (8080)
           ├─ Plan Service (8083)
           ├─ LLM Agent (8000)
           └─ Redis (6379)
```

## Prerequisites

- AWS Account
- Google Gemini API Key (free tier: 15 requests/min)
- Domain name (optional, can use EC2 public IP)

## Step 1: Launch EC2 Instance

### 1.1 Create Instance

1. Go to AWS Console → EC2 → Launch Instance
2. **Name:** `oddiya-server`
3. **AMI:** Amazon Linux 2023 (Free tier eligible)
4. **Instance Type:** `t2.micro` (1 vCPU, 1GB RAM) ✅ Free Tier
5. **Key Pair:** Create new or select existing
6. **Storage:** 8GB gp3 (Free Tier includes 30GB)

### 1.2 Configure Security Group

Create security group with these inbound rules:

| Type  | Port | Source      | Description           |
|-------|------|-------------|-----------------------|
| SSH   | 22   | Your IP     | SSH access            |
| HTTP  | 80   | 0.0.0.0/0   | Web traffic           |
| HTTPS | 443  | 0.0.0.0/0   | Secure web traffic    |

### 1.3 Launch & Connect

```bash
# Download your key pair (e.g., oddiya-key.pem)
chmod 400 oddiya-key.pem

# Connect to instance
ssh -i oddiya-key.pem ec2-user@<EC2_PUBLIC_IP>
```

## Step 2: Setup EC2 Environment

### 2.1 Run Setup Script

```bash
# Download setup script
curl -o setup-ec2.sh https://raw.githubusercontent.com/YOUR_REPO/main/scripts/aws/setup-ec2.sh
chmod +x setup-ec2.sh
./setup-ec2.sh
```

### 2.2 Clone Repository

```bash
cd /opt/oddiya
git clone https://github.com/YOUR_REPO/oddiya.git .
```

### 2.3 Create Environment File

```bash
cat > .env << 'ENVEOF'
GOOGLE_API_KEY=your_gemini_api_key_here
GEMINI_MODEL=gemini-2.0-flash-exp
ENVEOF
```

## Step 3: Deploy Services

### 3.1 Build and Start Services

```bash
# Build all Docker images
docker-compose build

# Start all services in detached mode
docker-compose up -d

# Check status
docker-compose ps
```

Expected output:
```
NAME                   STATUS    PORTS
oddiya-nginx           Up        0.0.0.0:80->80/tcp
oddiya-api-gateway     Up        0.0.0.0:8080->8080/tcp
oddiya-plan-service    Up        0.0.0.0:8083->8083/tcp
oddiya-llm-agent       Up        0.0.0.0:8000->8000/tcp
oddiya-redis           Up        0.0.0.0:6379->6379/tcp
```

### 3.2 Verify Deployment

```bash
# Check Nginx
curl http://localhost

# Check services
docker-compose logs -f
```

## Step 4: Access Your App

### 4.1 Get Public IP

```bash
# On EC2 instance
curl http://169.254.169.254/latest/meta-data/public-ipv4
```

### 4.2 Access Frontend

Open browser: `http://<EC2_PUBLIC_IP>`

## Step 5: (Optional) Setup Custom Domain

### 5.1 Point Domain to EC2

In your DNS provider (e.g., Cloudflare, Namecheap):
```
Type: A
Name: @
Value: <EC2_PUBLIC_IP>
TTL: 1 hour
```

### 5.2 Get Free SSL Certificate

```bash
# Install Certbot
sudo yum install -y certbot python3-certbot-nginx

# Get certificate
sudo certbot --nginx -d yourdomain.com

# Auto-renewal
sudo systemctl enable certbot-renew.timer
```

## Monitoring & Maintenance

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f llm-agent

# Last 100 lines
docker-compose logs --tail=100
```

### Restart Services

```bash
# Restart all
docker-compose restart

# Restart specific service
docker-compose restart llm-agent
```

### Update Deployment

```bash
cd /opt/oddiya
git pull
docker-compose build
docker-compose up -d
```

### Stop Services

```bash
# Stop all services
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

## Cost Breakdown

| Resource         | Spec           | Monthly Cost |
|------------------|----------------|--------------|
| EC2 t2.micro     | 1GB RAM, 1 vCPU| $0 (Free Tier 12 months) |
| EBS Storage      | 8GB gp3        | $0 (Free Tier includes 30GB) |
| Data Transfer    | 15GB/month     | $0 (Free Tier) |
| Gemini API       | 15 req/min     | $0 (Free Tier) |
| **Total**        |                | **$0/month** |

**After Free Tier (12 months):**
- t2.micro: ~$8.50/month
- EBS 8GB: ~$0.80/month
- **Total: ~$10/month**

## Troubleshooting

### Services Not Starting

```bash
# Check Docker status
sudo systemctl status docker

# Check logs
docker-compose logs

# Rebuild images
docker-compose build --no-cache
```

### Out of Memory (t2.micro has only 1GB)

```bash
# Add swap space
sudo dd if=/dev/zero of=/swapfile bs=1M count=1024
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
```

### Port Already in Use

```bash
# Find process using port
sudo lsof -i :80
sudo kill -9 <PID>
```

## Security Best Practices

1. **Change default passwords** in docker-compose.yml
2. **Enable EC2 IMDSv2** (metadata protection)
3. **Restrict SSH access** to your IP only
4. **Enable CloudWatch monitoring** (Free Tier: 10 metrics)
5. **Set up automatic backups** with AWS Backup (Free Tier: 5GB)
6. **Use IAM roles** instead of AWS keys
7. **Enable VPC Flow Logs** for network monitoring

## Backup Strategy

### Automated Backup Script

```bash
#!/bin/bash
# Save to /opt/oddiya/backup.sh
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/opt/oddiya/backups"
mkdir -p $BACKUP_DIR

# Backup Docker volumes
docker-compose down
tar -czf $BACKUP_DIR/volumes_$DATE.tar.gz /var/lib/docker/volumes/oddiya_redis_data
docker-compose up -d

# Keep only last 7 days
find $BACKUP_DIR -name "volumes_*.tar.gz" -mtime +7 -delete
```

Schedule with cron:
```bash
crontab -e
# Add: 0 2 * * * /opt/oddiya/backup.sh
```

## Scaling Options (If Needed Later)

If t2.micro becomes insufficient:

1. **Vertical Scaling:**
   - Upgrade to t3.small (2GB RAM) - $15/month
   - Upgrade to t3.medium (4GB RAM) - $30/month

2. **Horizontal Scaling:**
   - Add Application Load Balancer
   - Run multiple EC2 instances
   - Separate database to RDS

3. **Managed Services:**
   - ECS Fargate for containers
   - AWS App Runner (easiest)
   - Elastic Beanstalk

## Support

For issues, check:
- GitHub Issues: https://github.com/YOUR_REPO/oddiya/issues
- Logs: `docker-compose logs`
- AWS CloudWatch Logs

---

**Last Updated:** 2025-11-03
