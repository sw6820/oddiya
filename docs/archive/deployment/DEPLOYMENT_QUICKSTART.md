# 🚀 Deployment Quick Start Guide

This guide helps you deploy Oddiya using the cost-optimized three-phase strategy.

---

## 📋 Prerequisites

1. **AWS Account** with credentials configured
2. **AWS CLI** installed and configured (`aws configure`)
3. **SSH Keypair** created in your target region
4. **Domain name** (optional, for SSL/TLS)
5. **API Keys**:
   - Google Gemini API key
   - OpenWeatherMap API key (optional)

---

## 🎯 Three-Phase Strategy Overview

```
Phase 1: Single EC2 Spot ($15-20/mo)  ← START HERE
    ↓
Phase 2: EKS Learning ($131/mo)        ← Study Kubernetes
    ↓
Phase 3: Back to Cheapest ($0-20/mo)  ← Oracle Cloud Free Tier
```

---

## 🚀 Phase 1: Deploy to Single EC2 (CHEAPEST)

**Cost**: ~$15-20/month
**Time**: 10-15 minutes
**Best for**: Initial deployment, low traffic

### Step 1: Prepare AWS Environment

```bash
# Navigate to project root
cd /Users/wjs/cursor/oddiya

# Set your region (optional)
export AWS_REGION=ap-northeast-2

# Create SSH keypair if you don't have one
aws ec2 create-key-pair \
  --key-name oddiya-keypair \
  --query 'KeyMaterial' \
  --output text > ~/oddiya-keypair.pem

chmod 400 ~/oddiya-keypair.pem
```

### Step 2: Run Deployment Script

```bash
# Deploy infrastructure
./scripts/deploy-phase1-ec2.sh
```

The script will:
- ✅ Create VPC, subnet, internet gateway
- ✅ Create security groups
- ✅ Launch t3.medium Spot instance
- ✅ Allocate Elastic IP
- ✅ Install Docker, Docker Compose, Nginx
- ✅ Configure CloudWatch logging

### Step 3: Deploy Application

```bash
# Get the public IP from script output
PUBLIC_IP=<your-elastic-ip>

# SSH to instance
ssh -i ~/oddiya-keypair.pem ubuntu@$PUBLIC_IP

# Clone repository
cd /opt/oddiya
git clone https://github.com/<your-org>/oddiya.git .

# Configure environment
cp .env.example .env
nano .env  # Edit with your credentials

# Required variables:
# GOOGLE_API_KEY=your_gemini_api_key
# GEMINI_MODEL=gemini-2.5-flash-lite
# LLM_PROVIDER=gemini
# MOCK_MODE=false
```

### Step 4: Start Services

```bash
# Build images (first time only)
docker-compose -f docker-compose.local.yml build

# Start all services
docker-compose -f docker-compose.local.yml up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f
```

### Step 5: Test

```bash
# From your local machine
curl http://$PUBLIC_IP:8080/actuator/health

# Test LLM Agent
curl -X POST http://$PUBLIC_IP:8080/api/v1/plans/generate \
  -H 'Content-Type: application/json' \
  -d '{
    "title": "서울 1일 여행",
    "location": "서울",
    "startDate": "2025-11-15",
    "endDate": "2025-11-15",
    "budget": "medium",
    "interests": ["culture", "food"]
  }'
```

### Step 6: Setup SSL (Optional)

```bash
# On EC2 instance
sudo apt-get install -y certbot python3-certbot-nginx

# Configure your domain A record to point to $PUBLIC_IP
# Then run:
sudo certbot --nginx -d api.oddiya.com
```

✅ **Phase 1 Complete!** Your application is now running on a single EC2 instance.

---

## 📚 Phase 2: Deploy to EKS for Learning

**Cost**: ~$131/month
**Time**: 30-45 minutes
**Best for**: Learning Kubernetes, production-grade setup

**⚠️ Only proceed if you want to learn EKS/Kubernetes!**

### Why Learn with EKS?

- Real production Kubernetes experience
- Autoscaling (HPA, Cluster Autoscaler)
- Service mesh (Istio)
- GitOps (ArgoCD)
- Monitoring (Prometheus + Grafana)
- **Resume/interview value!**

### Step 1: Install eksctl

```bash
# macOS
brew tap weaveworks/tap
brew install weaveworks/tap/eksctl

# Linux
curl --silent --location "https://github.com/weaveworks/eksctl/releases/latest/download/eksctl_$(uname -s)_amd64.tar.gz" | tar xz -C /tmp
sudo mv /tmp/eksctl /usr/local/bin
```

### Step 2: Create EKS Cluster

```bash
# This takes 15-20 minutes
eksctl create cluster \
  --name oddiya-eks \
  --region ap-northeast-2 \
  --version 1.28 \
  --nodegroup-name oddiya-workers \
  --node-type t3.medium \
  --nodes 1 \
  --nodes-min 1 \
  --nodes-max 3 \
  --spot \
  --managed \
  --with-oidc
```

### Step 3: Deploy PostgreSQL + Redis (Separate EC2)

```bash
# Launch 2x t2.micro instances for databases
# (Not recommended in Kubernetes due to state management complexity)

./scripts/deploy-databases-ec2.sh
```

### Step 4: Deploy Services to EKS

```bash
# Build and push images to ECR
./scripts/build-and-push-ecr.sh

# Create namespace
kubectl create namespace oddiya

# Apply ConfigMaps
kubectl apply -f infrastructure/kubernetes/config/

# Deploy services
kubectl apply -f infrastructure/kubernetes/

# Check status
kubectl get pods -n oddiya -w
```

### Step 5: Learning Exercises

**Exercise 1: Horizontal Pod Autoscaling**
```bash
kubectl autoscale deployment api-gateway \
  --cpu-percent=70 \
  --min=1 \
  --max=3 \
  -n oddiya
```

**Exercise 2: Rolling Updates**
```bash
kubectl set image deployment/api-gateway \
  api-gateway=oddiya/api-gateway:v1.1 \
  -n oddiya

kubectl rollout status deployment/api-gateway -n oddiya
```

**Exercise 3: Service Mesh (Istio)**
```bash
istioctl install --set profile=demo
kubectl label namespace oddiya istio-injection=enabled
```

**Exercise 4: GitOps (ArgoCD)**
```bash
kubectl create namespace argocd
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
```

✅ **Phase 2 Complete!** You now have production Kubernetes experience.

---

## 💰 Phase 3: Migrate to Cheapest Option

After learning with EKS for 2-3 months, migrate back to save money.

### Option A: Back to Single EC2 (Same as Phase 1)

```bash
# Save $110/month
./scripts/migrate-eks-to-ec2.sh
```

### Option B: Oracle Cloud Free Tier (RECOMMENDED)

**Cost**: $0/month forever!
**Resources**:
- 4x ARM Ampere A1 instances (24GB RAM total!)
- 200GB block storage
- 10TB/month egress

```bash
# Build ARM-compatible images
docker buildx create --use
docker buildx build --platform linux/arm64 \
  -t oddiya/api-gateway:arm64 \
  --push .

# Deploy to Oracle Cloud
./scripts/deploy-phase3-oracle.sh
```

### Option C: AWS Lightsail

**Cost**: $10-30/month
**Pros**: Predictable pricing, managed

```bash
./scripts/deploy-phase3-lightsail.sh
```

### Decommission EKS

```bash
# Delete EKS cluster (save $131/month!)
eksctl delete cluster --name oddiya-eks --region ap-northeast-2

# Terminate database EC2 instances
aws ec2 terminate-instances --instance-ids <instance-ids>
```

---

## 📊 Cost Comparison

| Phase | Monthly Cost | Use Case |
|-------|--------------|----------|
| **Phase 1** | **$15-20** | ✅ Initial deployment |
| **Phase 2** | **$131** | 📚 Learning Kubernetes |
| **Phase 3A** | **$15-20** | ✅ Back to cheap |
| **Phase 3B** | **$0** | ✅ Oracle Free Tier (BEST!) |

**Recommended Learning Path**:
1. Start with Phase 1 ($15/mo) for 1 month
2. Upgrade to Phase 2 ($131/mo) for 2-3 months to learn K8s
3. Migrate to Phase 3B ($0/mo) using Oracle Cloud free tier

**Total Learning Investment**: 3 months × $131 = **$393 for production Kubernetes experience!**

---

## 🔧 Troubleshooting

### Issue: Spot Instance Interrupted

```bash
# Check Spot interruption notices
aws ec2 describe-spot-instance-requests \
  --spot-instance-request-ids <spot-request-id>

# Re-run deployment script (it will create a new Spot request)
./scripts/deploy-phase1-ec2.sh
```

### Issue: Out of Memory

```bash
# On EC2 instance, check memory
free -h

# Add swap space
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
```

### Issue: Docker Container Crashes

```bash
# Check logs
docker-compose logs <service-name>

# Restart specific service
docker-compose restart <service-name>

# Restart all
docker-compose down && docker-compose up -d
```

### Issue: Database Connection Errors

```bash
# Check PostgreSQL is running
docker ps | grep postgres

# Check database logs
docker logs oddiya-postgres

# Recreate database container
docker-compose down postgres
docker-compose up -d postgres
```

---

## 📚 Additional Resources

- [Full Deployment Plan](docs/deployment/COST_OPTIMIZED_DEPLOYMENT.md)
- [AWS Cost Optimization](https://aws.amazon.com/pricing/)
- [EKS Best Practices](https://aws.github.io/aws-eks-best-practices/)
- [Oracle Cloud Free Tier](https://www.oracle.com/cloud/free/)

---

## 🆘 Getting Help

1. Check logs: `docker-compose logs -f`
2. Review [troubleshooting guide](docs/deployment/TROUBLESHOOTING.md)
3. Open an issue on GitHub
4. Contact: admin@oddiya.com

---

**Happy Deploying! 🚀**
