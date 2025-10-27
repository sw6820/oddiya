# Infrastructure as Code - Terraform + GitHub Actions

## Overview

This project uses **Terraform** for AWS infrastructure provisioning and **GitHub Actions** for CI/CD automation.

## What's Included

### 🚀 GitHub Actions (`.github/workflows/`)

#### 1. CI Pipeline (`ci.yml`)
**Runs on:** Every push and pull request

**Jobs:**
- ✅ **Test Java Services** - Auth, API Gateway, Plan Service
- ✅ **Test Python Services** - LLM Agent, Video Worker
- ✅ **Lint & Format** - Code quality checks
- ✅ **Build Docker Images** - Only on `main` branch
- ✅ **Push to Docker Hub** - Automated image builds

**Features:**
- Matrix strategy for parallel testing
- Artifact uploads for test results
- Docker image tagging with commit SHA

### 🏗️ Terraform (`infrastructure/terraform/`)

#### Infrastructure Provisioned:
- ✅ **VPC** - Virtual Private Cloud
- ✅ **Subnets** - Public and Private subnets
- ✅ **Internet Gateway** - Internet connectivity
- ✅ **Route Tables** - Network routing
- ⏳ **EKS Cluster** - Kubernetes cluster
- ⏳ **EC2 Instances** - PostgreSQL and Redis
- ⏳ **Security Groups** - Network security
- ⏳ **S3 Buckets** - Object storage
- ⏳ **SQS Queues** - Message queues
- ⏳ **SNS Topics** - Notifications
- ⏳ **IAM Roles** - Access control

## 🚀 Getting Started

### Prerequisites

1. **Terraform** (>= 1.5.0)
   ```bash
   brew install terraform
   # or download from https://www.terraform.io/downloads
   ```

2. **AWS CLI** configured
   ```bash
   aws configure
   # AWS Access Key ID
   # AWS Secret Access Key
   # Default region: ap-northeast-2
   ```

3. **S3 Bucket** for Terraform state
   ```bash
   aws s3 mb s3://oddiya-terraform-state --region ap-northeast-2
   ```

4. **Docker Hub** credentials (for CI)
   - Add secrets in GitHub Settings → Secrets:
     - `DOCKER_USERNAME`
     - `DOCKER_PASSWORD`

### Terraform Commands

```bash
# Navigate to terraform directory
cd infrastructure/terraform

# Initialize Terraform
terraform init

# Plan infrastructure changes
terraform plan

# Apply infrastructure
terraform apply

# Destroy infrastructure
terraform destroy

# View outputs
terraform output
```

### GitHub Actions Setup

The CI pipeline runs automatically on every push. To manually trigger:

1. Go to GitHub → Actions
2. Select "CI Pipeline"
3. Click "Run workflow"

## 📊 Current Infrastructure Status

### Completed ✅
- VPC and networking
- GitHub Actions CI pipeline
- Docker image builds

### To Be Added ⏳
- EKS cluster configuration
- EC2 instances (PostgreSQL, Redis)
- Security groups
- AWS resources (S3, SQS, SNS)
- ALB (Application Load Balancer)
- Auto Scaling configuration

## 🔧 Configuration

### Environment Variables

Create `terraform.tfvars`:
```hcl
aws_region        = "ap-northeast-2"
environment       = "dev"
project_name      = "oddiya"
eks_instance_type = "t3.medium"
db_instance_type  = "t2.micro"
```

### Secrets Management

**AWS:**
- Store in AWS Secrets Manager
- Access via IAM roles

**GitHub:**
- Repository Settings → Secrets and variables → Actions
- Required secrets:
  - `DOCKER_USERNAME`
  - `DOCKER_PASSWORD`
  - `AWS_ACCESS_KEY_ID` (for deployment)
  - `AWS_SECRET_ACCESS_KEY` (for deployment)

## 🎯 Deployment Strategy

### Local Development
- Use `docker-compose up` for local stack
- No Terraform needed

### Staging/Production
1. GitHub Actions runs tests
2. Builds Docker images
3. Pushes to Docker Hub
4. Terraform deploys to AWS
5. Kubernetes applies manifests

## 📝 Next Steps

1. Complete Terraform modules for:
   - EKS cluster
   - EC2 instances
   - Security groups
   - AWS services (S3, SQS, SNS)

2. Add deployment workflow to GitHub Actions

3. Configure monitoring and alerting

4. Add rollback capabilities

## 🔗 Resources

- [Terraform AWS Provider Docs](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)
- [GitHub Actions Docs](https://docs.github.com/en/actions)
- [AWS EKS Docs](https://docs.aws.amazon.com/eks/)

---

**Note:** Start with local development. Deploy to AWS only when ready for staging/production testing.

