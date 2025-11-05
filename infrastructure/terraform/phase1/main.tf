# Oddiya Phase 1 Infrastructure
# 2x t2.micro EC2 instances (App + DB)

terraform {
  required_version = ">= 1.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "Oddiya"
      Environment = var.environment
      ManagedBy   = "Terraform"
      Phase       = "Phase1"
    }
  }
}

# VPC - Use default VPC (simplest, no conflicts)
# Default VPC is clean and ready to use
data "aws_vpc" "main" {
  default = true
}

# Create new VPC only if the existing one doesn't exist (fallback)
# resource "aws_vpc" "main" {
#   cidr_block           = "10.0.0.0/16"
#   enable_dns_hostnames = true
#   enable_dns_support   = true
#
#   tags = {
#     Name = "oddiya-vpc-${var.environment}"
#   }
# }

# Internet Gateway - Use existing default VPC IGW
data "aws_internet_gateway" "main" {
  filter {
    name   = "attachment.vpc-id"
    values = [data.aws_vpc.main.id]
  }
}

# Public Subnet - Use existing default VPC subnet in AZ-a (has public IP)
data "aws_subnet" "public" {
  vpc_id            = data.aws_vpc.main.id
  availability_zone = "${var.aws_region}a"
}

# Private Subnet - Use existing default VPC subnet in AZ-c (t2.micro supported)
data "aws_subnet" "private" {
  vpc_id            = data.aws_vpc.main.id
  availability_zone = "${var.aws_region}c"
}

# NAT Gateway removed for cost optimization ($32/month savings)
# Database server will not have automatic internet access
# Updates must be done via SSH tunnel through app server
# See: docs/deployment/PHASE1_DEPLOYMENT_PLAN.md for instructions

# Route Tables - Use default VPC route tables (already configured)
# Default VPC automatically has:
# - Main route table with IGW route for public subnets
# - All subnets use this main route table by default
# No need to create additional route tables for default VPC

# Security Group: Application Server
resource "aws_security_group" "app_server" {
  name        = "oddiya-app-server-${var.environment}"
  description = "Security group for Oddiya application server"
  vpc_id      = data.aws_vpc.main.id

  # SSH
  ingress {
    description = "SSH from admin"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = var.admin_ip_whitelist
  }

  # LLM Agent
  ingress {
    description = "LLM Agent API"
    from_port   = 8000
    to_port     = 8000
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Plan Service
  ingress {
    description = "Plan Service API"
    from_port   = 8083
    to_port     = 8083
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # All outbound
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "oddiya-app-server-sg-${var.environment}"
  }
}

# Security Group: Database Server
resource "aws_security_group" "db_server" {
  name        = "oddiya-db-server-${var.environment}"
  description = "Security group for Oddiya database server"
  vpc_id      = data.aws_vpc.main.id

  # PostgreSQL from app server only
  ingress {
    description     = "PostgreSQL from app server"
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.app_server.id]
  }

  # SSH from app server (bastion)
  ingress {
    description     = "SSH from app server"
    from_port       = 22
    to_port         = 22
    protocol        = "tcp"
    security_groups = [aws_security_group.app_server.id]
  }

  # All outbound (for yum updates via NAT)
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "oddiya-db-server-sg-${var.environment}"
  }
}

# IAM Role for App Server
resource "aws_iam_role" "app_server" {
  name = "oddiya-app-server-role-${var.environment}"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ec2.amazonaws.com"
        }
      }
    ]
  })

  tags = {
    Name = "oddiya-app-server-role-${var.environment}"
  }
}

# IAM Instance Profile for App Server
resource "aws_iam_instance_profile" "app_server" {
  name = "oddiya-app-server-profile-${var.environment}"
  role = aws_iam_role.app_server.name
}

# Attach CloudWatch Logs policy
resource "aws_iam_role_policy_attachment" "app_server_cloudwatch" {
  role       = aws_iam_role.app_server.name
  policy_arn = "arn:aws:iam::aws:policy/CloudWatchLogsFullAccess"
}

# Attach Secrets Manager policy
resource "aws_iam_role_policy_attachment" "app_server_secrets" {
  role       = aws_iam_role.app_server.name
  policy_arn = "arn:aws:iam::aws:policy/SecretsManagerReadWrite"
}

# Key Pair (must be created manually first)
data "aws_key_pair" "main" {
  key_name = var.key_pair_name
}

# Latest Amazon Linux 2023 AMI
data "aws_ami" "amazon_linux_2023" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-*-x86_64"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

# EC2 Instance: Application Server
resource "aws_instance" "app_server" {
  ami           = data.aws_ami.amazon_linux_2023.id
  instance_type = var.instance_type

  subnet_id                   = data.aws_subnet.public.id
  vpc_security_group_ids      = [aws_security_group.app_server.id]
  associate_public_ip_address = true
  key_name                    = data.aws_key_pair.main.key_name
  iam_instance_profile        = aws_iam_instance_profile.app_server.name

  root_block_device {
    volume_type           = "gp3"
    volume_size           = 30
    delete_on_termination = true
  }

  user_data = file("${path.module}/user-data-app.sh")

  tags = {
    Name = "oddiya-app-server-${var.environment}"
    Type = "Application"
  }
}

# Elastic IP for App Server
resource "aws_eip" "app_server" {
  domain   = "vpc"
  instance = aws_instance.app_server.id

  tags = {
    Name = "oddiya-app-server-eip-${var.environment}"
  }

  depends_on = [data.aws_internet_gateway.main]
}

# EC2 Instance: Database Server
resource "aws_instance" "db_server" {
  ami           = data.aws_ami.amazon_linux_2023.id
  instance_type = var.instance_type

  subnet_id              = data.aws_subnet.private.id
  vpc_security_group_ids = [aws_security_group.db_server.id]
  key_name               = data.aws_key_pair.main.key_name

  root_block_device {
    volume_type           = "gp3"
    volume_size           = 30
    delete_on_termination = true
  }

  user_data = file("${path.module}/user-data-db.sh")

  tags = {
    Name = "oddiya-db-server-${var.environment}"
    Type = "Database"
  }
}

# Outputs
output "app_server_public_ip" {
  description = "Public IP of application server"
  value       = aws_eip.app_server.public_ip
}

output "app_server_private_ip" {
  description = "Private IP of application server"
  value       = aws_instance.app_server.private_ip
}

output "db_server_private_ip" {
  description = "Private IP of database server"
  value       = aws_instance.db_server.private_ip
}

output "vpc_id" {
  description = "VPC ID"
  value       = data.aws_vpc.main.id
}

output "public_subnet_id" {
  description = "Public subnet ID"
  value       = data.aws_subnet.public.id
}

output "private_subnet_id" {
  description = "Private subnet ID"
  value       = data.aws_subnet.private.id
}
