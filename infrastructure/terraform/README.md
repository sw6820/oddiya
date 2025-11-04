# Oddiya Terraform Infrastructure

This directory contains Terraform configurations for Oddiya's infrastructure deployments.

---

## 📁 Directory Structure

```
infrastructure/terraform/
├── README.md              # This file
├── main.tf                # Root config for EKS deployment (Phase 7)
├── vpc.tf                 # VPC for EKS (2 public + 2 private subnets)
├── variables.tf           # Variables for EKS deployment
├── outputs.tf             # Outputs for EKS deployment
└── phase1/                # Simple 2 EC2 deployment (START HERE!)
    ├── main.tf            # Complete Phase 1 infrastructure
    ├── variables.tf       # Phase 1 variables
    ├── user-data-app.sh   # App server initialization
    ├── user-data-db.sh    # Database server initialization
    ├── terraform.tfvars.example
    └── README.md          # Phase 1 detailed guide
```

---

## 🎯 Which Configuration to Use?

### 👉 Phase 1 (Recommended - START HERE!)

**Use:** `phase1/` directory
**Cost:** ~$5/month (with free tier)
**Deploys:**
- 2x EC2 t2.micro instances
- VPC with 1 public + 1 private subnet
- No NAT Gateway (cost optimized)
- PostgreSQL + Plan Service + LLM Agent

**Best for:**
- ✅ Initial MVP deployment
- ✅ Testing and development
- ✅ Low cost ($5-26/month)
- ✅ Simple architecture
- ✅ Mobile app backend (Android/iOS)

**Deploy with:**
```bash
cd phase1
terraform init
terraform apply
```

### Phase 7 (Future - Full Production)

**Use:** Root directory (main.tf, vpc.tf, etc.)
**Cost:** ~$120/month
**Deploys:**
- EKS cluster with t3.medium nodes
- 2x t2.micro EC2 for DB + Redis
- NAT Gateway
- Load balancer
- Auto-scaling

**Best for:**
- Later production scaling
- High availability requirements
- After Phase 1-6 are complete

**Not ready yet** - Use Phase 1 first!

---

## 🚀 Quick Start (Phase 1)

### Prerequisites

1. **AWS Account** with admin access
2. **AWS CLI** configured (`aws configure`)
3. **Terraform** installed (>= 1.0)
4. **SSH Key** created in AWS EC2 console (name: `oddiya-prod`)

### Deployment

```bash
# Navigate to Phase 1
cd infrastructure/terraform/phase1

# Configure variables
cp terraform.tfvars.example terraform.tfvars
nano terraform.tfvars  # Edit with your values

# Deploy
terraform init
terraform plan
terraform apply  # Type 'yes'

# Get outputs
terraform output
```

### Post-Deployment

```bash
# Setup database
cd ../../../scripts
./setup-database-phase1.sh

# Deploy applications
./deploy-phase1.sh
```

**Full guide:** See `phase1/README.md`

---

## 💰 Cost Comparison

| Configuration | Monthly Cost (Free Tier) | After Free Tier |
|---------------|--------------------------|-----------------|
| **Phase 1 (Optimized)** | **~$5** | **~$26** |
| Root EKS (Full) | ~$70 | ~$120 |

### Phase 1 Cost Breakdown

| Resource | Cost (Free Tier) | After Free Tier |
|----------|------------------|-----------------|
| EC2 t2.micro x2 | $0 | $17.00 |
| EBS gp3 (50 GB) | $1.60 | $4.00 |
| NAT Gateway | ~~$32.00~~ **$0** | **$0** |
| Elastic IPs | $0 | $3.60 |
| Data Transfer | $3.00 | $5.00 |
| **Total** | **~$5** | **~$26** |

**Cost Optimization:** Phase 1 removes NAT Gateway, saving **$32/month**!

---

## 🔄 Migration Path

```
Phase 1 (2 EC2s) → Phase 2 (+ Redis) → Phase 3 (+ Domain/SSL)
                                            ↓
                                       Phase 4 (+ OAuth)
                                            ↓
                                       Phase 5 (+ Video)
                                            ↓
                                       Phase 6 (+ Load Balancer)
                                            ↓
                                       Phase 7 (EKS/K8s)
```

**Current Status:** Phase 1 Ready ✅

---

## 📊 Architecture Comparison

### Phase 1 (Simple)

```
Mobile App
    ↓ HTTP
EC2 #1: App Server (Public IP)
    ├── Plan Service (8083)
    └── LLM Agent (8000)
    ↓ Private network
EC2 #2: Database (Private IP)
    └── PostgreSQL (5432)
```

### Phase 7 (Full - Future)

```
Mobile App
    ↓ HTTPS
ALB (Load Balancer)
    ↓
EKS Cluster
    ├── API Gateway (K8s)
    ├── Auth Service (K8s)
    ├── Plan Service (K8s)
    └── LLM Agent (K8s)
    ↓
EC2 #1: Database
EC2 #2: Redis
```

---

## 🔐 Security Differences

### Phase 1

- ✅ Database in private subnet (no public access)
- ✅ App server public (ports 22, 8000, 8083)
- ✅ Security groups restrict access
- ⚠️ No load balancer
- ⚠️ HTTP only (add SSL in Phase 3)

### Phase 7

- ✅ All services behind load balancer
- ✅ HTTPS with ACM certificates
- ✅ Private subnet for all K8s workloads
- ✅ Network policies
- ✅ WAF protection

---

## 📚 Documentation

- **Phase 1 Full Guide:** [phase1/README.md](phase1/README.md)
- **Phase 1 Quick Start:** [../../docs/deployment/PHASE1_QUICK_START.md](../../docs/deployment/PHASE1_QUICK_START.md)
- **Phase 1 Detailed Plan:** [../../docs/deployment/PHASE1_DEPLOYMENT_PLAN.md](../../docs/deployment/PHASE1_DEPLOYMENT_PLAN.md)

---

## 🐛 Troubleshooting

### Phase 1 Issues

**Issue:** Terraform fails with "key pair not found"
```bash
# Create key pair in AWS console first
# Name: oddiya-prod
# Download and save to ~/.ssh/
chmod 400 ~/.ssh/oddiya-prod.pem
```

**Issue:** Database server can't install packages
```bash
# Expected - no NAT Gateway for cost savings
# Install manually via SSH tunnel:
ssh -J ec2-user@<APP-IP> ec2-user@<DB-IP>
sudo dnf install postgresql15 -y
```

**Issue:** Mobile apps can't connect
```bash
# Verify security group allows ports 8000, 8083
# Check your IP hasn't changed
curl ifconfig.me
# Update terraform.tfvars if needed
```

---

## 🆘 Support

1. **Check documentation** in respective directories
2. **Review Terraform plan** before applying
3. **Check AWS console** for resource status
4. **Verify outputs** with `terraform output`

---

## 🎯 Recommendation

**Start with Phase 1:**
1. ✅ Low cost (~$5/month)
2. ✅ Simple to deploy (30 minutes)
3. ✅ Fully functional backend
4. ✅ Supports Android + iOS mobile apps
5. ✅ Database persistence
6. ✅ Real-time streaming

**Migrate to Phase 7 later** when you need:
- High availability (99.9%+ uptime)
- Auto-scaling (handle traffic spikes)
- Multiple services (video generation, etc.)
- Team collaboration (microservices)

---

**Next Action:** `cd phase1 && terraform apply`

**Status:** Phase 1 Ready for Deployment ✅
**Last Updated:** 2025-11-04
