# Cost-Optimized Deployment Strategy

## 🎯 Goal: Sequential Deployment with EKS Learning

**Strategy**: Deploy cheapest first → Learn with EKS → Migrate to cheapest again

---

## 📊 Chain of Thought (CoT) Analysis

### Current Architecture Analysis

**Services**: 7 microservices
- 6 Spring Boot (Java 21): API Gateway, Auth, User, Plan, Video, Video Worker
- 1 FastAPI (Python): LLM Agent

**Dependencies**:
- PostgreSQL 17.0 (stateful)
- Redis 7.4 (stateful)
- AWS Services: SQS, S3, SNS, Gemini API
- External APIs: OpenWeatherMap

**Resource Requirements**:
- Total RAM: ~3.5GB (6x Spring Boot @ 512MB + Python @ 256MB + Postgres 512MB + Redis 256MB)
- Total CPU: ~2-3 vCPU
- Storage: ~10GB (code + data + logs)

---

## 🏗️ Three-Phase Deployment Plan

---

## **PHASE 1: Cheapest Deployment (Single EC2 + Docker Compose)**

### CoT Reasoning: Why Single EC2 is Cheapest

1. **No orchestration overhead**: No EKS control plane fee ($0.10/hr = $73/month)
2. **Single instance**: No multi-node networking complexity
3. **All-in-one**: Minimizes data transfer costs
4. **Spot instance eligible**: 70% cost savings
5. **Free tier eligible**: t2.micro/t3.micro (first 750 hours/month)

### Architecture

```
┌─────────────────────────────────────────────┐
│   Single EC2 Instance (t3.medium Spot)      │
│   Ubuntu 22.04 LTS                          │
├─────────────────────────────────────────────┤
│                                             │
│   Docker Compose Stack:                    │
│   ├── PostgreSQL (container)                │
│   ├── Redis (container)                     │
│   ├── API Gateway (8080)                    │
│   ├── Auth Service (8081)                   │
│   ├── User Service (8082)                   │
│   ├── Plan Service (8083)                   │
│   ├── Video Service (8084)                  │
│   ├── LLM Agent (8000)                      │
│   └── Video Worker (background)             │
│                                             │
│   + Nginx Reverse Proxy (443)               │
│   + Let's Encrypt SSL                       │
│   + CloudWatch Agent (logs)                 │
│                                             │
└─────────────────────────────────────────────┘
         ↓
    Internet via
   Elastic IP (free with running instance)
```

### Instance Selection

**Primary Choice: t3.medium Spot**
- vCPU: 2
- RAM: 4GB
- Price: ~$0.0104/hr ($7.50/month) vs on-demand $0.0416/hr ($30/month)
- Savings: 75%
- Interruption risk: Low (typically 5% monthly)

**Fallback: t3.small Spot** (if extremely cost-sensitive)
- vCPU: 2
- RAM: 2GB
- Price: ~$0.0052/hr ($3.75/month)
- Risk: May need swap space, slower

### Cost Breakdown (Phase 1)

| Resource | Type | Monthly Cost |
|----------|------|--------------|
| EC2 t3.medium Spot | Compute | $7.50 |
| EBS 30GB gp3 | Storage | $2.40 |
| Elastic IP | Network | $0 (attached) |
| Data Transfer OUT | 5GB/month | $0 (free tier) |
| CloudWatch Logs | 5GB/month | $2.50 |
| **Total** | | **$12.40/month** |

**Plus usage-based costs**:
- Gemini API: ~$0.50-2/month (with caching)
- SQS: $0.40/1M requests (minimal)
- S3: $0.023/GB (video storage)
- SNS: $0.50/1M publishes

**Estimated Total: $15-20/month**

---

### Phase 1 Implementation Steps

#### Step 1: AWS Setup (Sequential)

```bash
# 1.1. Create VPC (if not exists)
aws ec2 create-vpc --cidr-block 10.0.0.0/16 --tag-specifications 'ResourceType=vpc,Tags=[{Key=Name,Value=oddiya-vpc}]'

# 1.2. Create public subnet
aws ec2 create-subnet --vpc-id <vpc-id> --cidr-block 10.0.1.0/24 --availability-zone ap-northeast-2a

# 1.3. Create Internet Gateway
aws ec2 create-internet-gateway
aws ec2 attach-internet-gateway --vpc-id <vpc-id> --internet-gateway-id <igw-id>

# 1.4. Create Security Group
aws ec2 create-security-group \
  --group-name oddiya-ec2-sg \
  --description "Oddiya single EC2 security group" \
  --vpc-id <vpc-id>

# Allow SSH (22), HTTP (80), HTTPS (443), and direct service ports for testing
aws ec2 authorize-security-group-ingress --group-id <sg-id> --protocol tcp --port 22 --cidr 0.0.0.0/0
aws ec2 authorize-security-group-ingress --group-id <sg-id> --protocol tcp --port 80 --cidr 0.0.0.0/0
aws ec2 authorize-security-group-ingress --group-id <sg-id> --protocol tcp --port 443 --cidr 0.0.0.0/0
aws ec2 authorize-security-group-ingress --group-id <sg-id> --protocol tcp --port 8080 --cidr 0.0.0.0/0 # API Gateway

# 1.5. Allocate Elastic IP
aws ec2 allocate-address --domain vpc --tag-specifications 'ResourceType=elastic-ip,Tags=[{Key=Name,Value=oddiya-eip}]'
```

#### Step 2: Launch Spot Instance

```bash
# Create launch template for Spot
cat > spot-launch-template.json <<EOF
{
  "ImageId": "ami-0c9c942bd7bf113a2",
  "InstanceType": "t3.medium",
  "KeyName": "oddiya-keypair",
  "SecurityGroupIds": ["<sg-id>"],
  "SubnetId": "<subnet-id>",
  "BlockDeviceMappings": [
    {
      "DeviceName": "/dev/xvda",
      "Ebs": {
        "VolumeSize": 30,
        "VolumeType": "gp3",
        "DeleteOnTermination": true
      }
    }
  ],
  "TagSpecifications": [
    {
      "ResourceType": "instance",
      "Tags": [
        {"Key": "Name", "Value": "oddiya-all-in-one"},
        {"Key": "Environment", "Value": "production"},
        {"Key": "Project", "Value": "oddiya"}
      ]
    }
  ],
  "UserData": "$(base64 -w0 user-data.sh)"
}
EOF

# Request Spot instance
aws ec2 request-spot-instances \
  --spot-price "0.0416" \
  --instance-count 1 \
  --type "persistent" \
  --launch-specification file://spot-launch-template.json
```

#### Step 3: User Data Script (Automated Setup)

```bash
#!/bin/bash
# user-data.sh - Runs on first boot

set -e

# Update system
apt-get update
apt-get upgrade -y

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh
usermod -aG docker ubuntu

# Install Docker Compose
curl -L "https://github.com/docker/compose/releases/download/v2.24.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

# Install AWS CLI
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
./aws/install

# Install CloudWatch agent
wget https://s3.amazonaws.com/amazoncloudwatch-agent/ubuntu/amd64/latest/amazon-cloudwatch-agent.deb
dpkg -i amazon-cloudwatch-agent.deb

# Install Nginx
apt-get install -y nginx certbot python3-certbot-nginx

# Create application directory
mkdir -p /opt/oddiya
cd /opt/oddiya

# Clone repository (or download release)
# For production, use CI/CD to push built artifacts
git clone https://github.com/<your-org>/oddiya.git .

# Create .env file
cat > .env <<EOF
# Google Gemini
GOOGLE_API_KEY=your_key_here
GEMINI_MODEL=gemini-2.5-flash-lite
LLM_PROVIDER=gemini
MOCK_MODE=false

# Database
DB_HOST=postgres
DB_PORT=5432
DB_NAME=oddiya
DB_USER=oddiya_user
DB_PASSWORD=$(openssl rand -base64 32)

# Redis
REDIS_HOST=redis
REDIS_PORT=6379

# AWS
AWS_REGION=ap-northeast-2
S3_BUCKET=oddiya-storage
SQS_QUEUE_URL=https://sqs.ap-northeast-2.amazonaws.com/<account>/oddiya-video-jobs
SNS_TOPIC_ARN=arn:aws:sns:ap-northeast-2:<account>:oddiya-notifications

# External APIs
OPENWEATHER_API_KEY=your_key_here
EOF

# Start services
docker-compose -f docker-compose.local.yml up -d

# Configure Nginx
cat > /etc/nginx/sites-available/oddiya <<'NGINX'
server {
    listen 80;
    server_name api.oddiya.com;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
NGINX

ln -sf /etc/nginx/sites-available/oddiya /etc/nginx/sites-enabled/
nginx -t && systemctl restart nginx

# Setup SSL (Let's Encrypt)
# certbot --nginx -d api.oddiya.com --non-interactive --agree-tos -m admin@oddiya.com

# Setup CloudWatch Logs
cat > /opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json <<'CW'
{
  "logs": {
    "logs_collected": {
      "files": {
        "collect_list": [
          {
            "file_path": "/opt/oddiya/logs/*.log",
            "log_group_name": "/oddiya/application",
            "log_stream_name": "{instance_id}"
          }
        ]
      }
    }
  }
}
CW

/opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl \
  -a fetch-config \
  -m ec2 \
  -s \
  -c file:/opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json

echo "✅ Oddiya deployment complete!"
```

#### Step 4: Health Checks & Monitoring

```bash
# After SSH to instance
ssh -i oddiya-keypair.pem ubuntu@<elastic-ip>

# Check Docker containers
docker ps

# Check logs
docker-compose -f docker-compose.local.yml logs -f

# Test API Gateway
curl http://localhost:8080/actuator/health

# Test LLM Agent
curl http://localhost:8000/health
```

---

## **PHASE 2: EKS Deployment for Learning**

### CoT Reasoning: Why EKS for Learning

1. **Industry standard**: Kubernetes knowledge is valuable
2. **Autoscaling**: Learn HPA, cluster autoscaling
3. **Service mesh**: Experiment with Istio/Linkerd
4. **GitOps**: Implement ArgoCD/Flux
5. **Observability**: Prometheus + Grafana stack
6. **Multi-environment**: Dev/Staging/Prod patterns

### Architecture

```
┌────────────────────────────────────────────────────┐
│              AWS EKS Cluster                       │
├────────────────────────────────────────────────────┤
│                                                    │
│   Control Plane (Managed by AWS) - $73/month      │
│                                                    │
│   Worker Nodes:                                   │
│   └── 1x t3.medium Spot ($7.50/month)             │
│       ├── API Gateway (Pod)                        │
│       ├── Auth Service (Pod)                       │
│       ├── User Service (Pod)                       │
│       ├── Plan Service (Pod)                       │
│       ├── Video Service (Pod)                      │
│       ├── LLM Agent (Pod)                          │
│       └── Video Worker (Pod)                       │
│                                                    │
│   External Datastore (EC2):                       │
│   ├── PostgreSQL (t2.micro) - $8.50/month          │
│   └── Redis (t2.micro) - $8.50/month               │
│                                                    │
│   Ingress:                                        │
│   └── Nginx Ingress Controller → ALB              │
│                                                    │
└────────────────────────────────────────────────────┘
```

### Cost Breakdown (Phase 2)

| Resource | Type | Monthly Cost |
|----------|------|--------------|
| EKS Control Plane | Managed K8s | $73.00 |
| t3.medium Spot Worker | Compute | $7.50 |
| t2.micro x2 (DB + Redis) | Compute | $17.00 |
| EBS 30GB x3 | Storage | $7.20 |
| ALB | Load Balancer | $16.20 |
| Data Transfer | Network | $5-10 |
| CloudWatch Logs | Monitoring | $5 |
| **Total** | | **~$131/month** |

**10x more expensive than Phase 1** - justifiable only for learning!

### Phase 2 Implementation

#### Step 1: Create EKS Cluster

```bash
# Using eksctl (easiest)
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
  --with-oidc \
  --ssh-access \
  --ssh-public-key oddiya-keypair

# Or using Terraform
cd infrastructure/terraform/eks
terraform init
terraform plan
terraform apply
```

#### Step 2: Deploy PostgreSQL + Redis on EC2

```bash
# Launch 2x t2.micro instances
# - oddiya-postgres-ec2
# - oddiya-redis-ec2

# Configure Security Groups to allow EKS worker nodes
# Port 5432 from EKS SG
# Port 6379 from EKS SG
```

#### Step 3: Deploy Services to EKS

```bash
# Create namespace
kubectl create namespace oddiya

# Apply ConfigMaps and Secrets
kubectl apply -f infrastructure/kubernetes/config/

# Deploy services in order
kubectl apply -f infrastructure/kubernetes/auth-service/
kubectl apply -f infrastructure/kubernetes/user-service/
kubectl apply -f infrastructure/kubernetes/plan-service/
kubectl apply -f infrastructure/kubernetes/llm-agent/
kubectl apply -f infrastructure/kubernetes/video-service/
kubectl apply -f infrastructure/kubernetes/api-gateway/

# Deploy Ingress
kubectl apply -f infrastructure/kubernetes/ingress/

# Check rollout
kubectl get pods -n oddiya -w
```

#### Step 4: Learning Exercises

1. **Autoscaling**
   ```bash
   # Deploy metrics-server
   kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml

   # Configure HPA for API Gateway
   kubectl autoscale deployment api-gateway --cpu-percent=70 --min=1 --max=3 -n oddiya
   ```

2. **Rolling Updates**
   ```bash
   # Update image version
   kubectl set image deployment/api-gateway api-gateway=oddiya/api-gateway:v1.1 -n oddiya
   kubectl rollout status deployment/api-gateway -n oddiya
   kubectl rollout undo deployment/api-gateway -n oddiya
   ```

3. **Service Mesh (Istio)**
   ```bash
   istioctl install --set profile=demo
   kubectl label namespace oddiya istio-injection=enabled
   ```

4. **GitOps (ArgoCD)**
   ```bash
   kubectl create namespace argocd
   kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
   ```

---

## **PHASE 3: Migrate Back to Cheapest Option**

### CoT Reasoning: Best Cheap Options Post-Learning

After EKS learning, evaluate:

**Option A: Back to Single EC2 (Same as Phase 1)**
- Cost: $15-20/month
- Best if: Traffic is low, no HA requirements

**Option B: AWS Lightsail**
- $10/month for 2GB RAM, 1 vCPU
- Includes: 2TB transfer, static IP, DNS management
- Best if: Predictable pricing preferred

**Option C: DigitalOcean App Platform**
- $5/month per service (stateless)
- Managed databases: $15/month (1GB PostgreSQL)
- Total: ~$45-60/month
- Best if: PaaS experience wanted

**Option D: Railway / Render**
- Pay-as-you-go
- ~$30-50/month for similar setup
- Best if: Zero devops maintenance

**Option E: Oracle Cloud Free Tier (Forever Free!)**
- 4x ARM Ampere A1 instances (24GB RAM total!)
- 200GB block storage
- 10TB/month egress
- **Cost: $0/month forever**
- Best if: Can migrate to ARM architecture

### Recommended: Option E (Oracle Cloud Free Tier)

This is **THE CHEAPEST** option that's actually sustainable.

#### Oracle Cloud ARM Migration

**Challenges:**
1. Java 21 works natively on ARM
2. Python works natively on ARM
3. Docker images need multi-arch builds

**Solution:**
```bash
# Build multi-arch images
docker buildx create --use
docker buildx build --platform linux/amd64,linux/arm64 -t oddiya/api-gateway:latest --push .
```

#### Phase 3 Implementation

```bash
# 1. Create Oracle Cloud account
# 2. Provision 1x Ampere A1.Flex instance (4 OCPUs, 24GB RAM)
# 3. Deploy using Docker Compose (same as Phase 1)
# 4. Configure OCI Load Balancer (free tier includes 1)

# Total cost: $0/month + Gemini API costs (~$2/month)
```

---

## 📋 Migration Checklist

### Phase 1 → Phase 2 (EC2 to EKS)

- [ ] Build and push Docker images to ECR
- [ ] Create EKS cluster
- [ ] Deploy PostgreSQL + Redis on separate EC2s
- [ ] Configure Security Groups for EKS → EC2 communication
- [ ] Deploy services to EKS
- [ ] Update DNS to point to ALB
- [ ] Test all endpoints
- [ ] Monitor for 24 hours
- [ ] Decommission Phase 1 EC2

### Phase 2 → Phase 3 (EKS to Cheapest)

- [ ] Choose target platform (Oracle Cloud recommended)
- [ ] Build ARM-compatible images (if Oracle)
- [ ] Deploy to new platform
- [ ] Test thoroughly in parallel
- [ ] Update DNS to point to new platform
- [ ] Monitor for 48 hours
- [ ] Delete EKS cluster
- [ ] Terminate EC2 instances
- [ ] **Save ~$110/month!**

---

## 🎓 Learning Outcomes from Phase 2

By the end of Phase 2 (EKS), you'll have learned:

✅ Kubernetes fundamentals (Pods, Deployments, Services, Ingress)
✅ AWS EKS setup and management
✅ Horizontal Pod Autoscaling (HPA)
✅ Rolling updates and rollbacks
✅ ConfigMaps and Secrets management
✅ Service mesh basics (if you try Istio)
✅ GitOps workflows (if you try ArgoCD)
✅ Prometheus + Grafana monitoring
✅ Cost optimization (by comparing to Phase 1/3!)

**Value of learning:** Priceless for resume + interviews
**Cost of learning:** ~$110/month (vs $0 for online tutorials, but no real experience!)

---

## 🚀 Quick Start Commands

### Deploy Phase 1 (Cheapest)

```bash
cd /Users/wjs/cursor/oddiya
./scripts/deploy-phase1-ec2.sh
```

### Deploy Phase 2 (EKS Learning)

```bash
cd /Users/wjs/cursor/oddiya
./scripts/deploy-phase2-eks.sh
```

### Migrate to Phase 3 (Cheapest Again)

```bash
cd /Users/wjs/cursor/oddiya
./scripts/deploy-phase3-oracle.sh  # Or other platform
./scripts/decommission-eks.sh
```

---

## 📊 Cost Comparison Summary

| Phase | Platform | Monthly Cost | Use Case |
|-------|----------|--------------|----------|
| **Phase 1** | Single EC2 Spot | **$15-20** | Initial deployment |
| **Phase 2** | EKS + EC2s | **$131** | Learning K8s |
| **Phase 3A** | Single EC2 Spot | **$15-20** | Back to cheap |
| **Phase 3B** | Oracle Cloud | **$0 (free tier)** | **CHEAPEST!** |
| **Phase 3C** | Lightsail | **$30-40** | Predictable cost |
| **Phase 3D** | Railway/Render | **$30-50** | Zero devops |

**Recommended Path**: 1 (EC2) → 2 (EKS for 2-3 months learning) → 3B (Oracle free tier forever)

**Total Learning Cost**: 3 months × $131 = $393 for production Kubernetes experience!

---

## ⚠️ Important Notes

1. **Gemini API costs** are separate and apply to all phases (~$2-5/month with caching)
2. **Always use Spot instances** where possible (75% savings)
3. **Enable CloudWatch alarms** for budget protection
4. **Backup PostgreSQL daily** to S3 (use lifecycle policies for old backups)
5. **Test disaster recovery** at least once before relying on production

---

## 📝 Next Steps

See individual deployment scripts in `/scripts/` directory:
- `deploy-phase1-ec2.sh`
- `deploy-phase2-eks.sh`
- `deploy-phase3-oracle.sh`
- `migrate-phase1-to-phase2.sh`
- `migrate-phase2-to-phase3.sh`
