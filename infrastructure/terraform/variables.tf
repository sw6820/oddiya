variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "ap-northeast-2"
}

variable "environment" {
  description = "Environment name"
  type        = string
  default     = "dev"
}

variable "project_name" {
  description = "Project name"
  type        = string
  default     = "oddiya"
}

variable "eks_instance_type" {
  description = "Instance type for EKS node"
  type        = string
  default     = "t3.medium"
}

variable "db_instance_type" {
  description = "Instance type for database EC2"
  type        = string
  default     = "t2.micro"
}

variable "redis_instance_type" {
  description = "Instance type for Redis EC2"
  type        = string
  default     = "t2.micro"
}

variable "vpc_cidr" {
  description = "CIDR block for VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "allowed_cidr_blocks" {
  description = "CIDR blocks allowed to access the infrastructure"
  type        = list(string)
  default     = ["0.0.0.0/0"]  # Restrict in production
}

