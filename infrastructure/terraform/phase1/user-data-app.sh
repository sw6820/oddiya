#!/bin/bash
# User data script for Oddiya Application Server (EC2 #1)

set -e

# Log everything
exec > >(tee /var/log/user-data.log)
exec 2>&1

echo "===== Starting Oddiya App Server Setup ====="
echo "Date: $(date)"
echo "Hostname: $(hostname)"

# Update system
echo "Updating system packages..."
dnf update -y

# Install Java 21 (Amazon Corretto)
echo "Installing Java 21..."
dnf install -y java-21-amazon-corretto java-21-amazon-corretto-devel

# Verify Java installation
java -version

# Install Python 3.11
echo "Installing Python 3.11..."
dnf install -y python3.11 python3.11-pip python3.11-devel

# Verify Python installation
python3.11 --version

# Install PostgreSQL client (for testing DB connectivity)
echo "Installing PostgreSQL client..."
dnf install -y postgresql15

# Install AWS CLI v2
echo "Installing AWS CLI..."
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
dnf install -y unzip
unzip awscliv2.zip
./aws/install
rm -rf aws awscliv2.zip

# Create application directories
echo "Creating application directories..."
mkdir -p /opt/oddiya/plan-service
mkdir -p /opt/oddiya/llm-agent
mkdir -p /opt/oddiya/scripts
mkdir -p /opt/oddiya/logs

# Set ownership
chown -R ec2-user:ec2-user /opt/oddiya

# Create deployment marker file
echo "Deployment initialized at $(date)" > /opt/oddiya/DEPLOYMENT_STATUS
chown ec2-user:ec2-user /opt/oddiya/DEPLOYMENT_STATUS

# Configure CloudWatch Logs Agent (optional)
echo "Installing CloudWatch Logs agent..."
dnf install -y amazon-cloudwatch-agent

# Enable and start SSM agent (for AWS Systems Manager)
echo "Enabling SSM agent..."
systemctl enable amazon-ssm-agent
systemctl start amazon-ssm-agent

echo "===== App Server Setup Complete ====="
echo "Next steps:"
echo "1. Deploy Plan Service JAR to /opt/oddiya/plan-service/"
echo "2. Deploy LLM Agent code to /opt/oddiya/llm-agent/"
echo "3. Create systemd service files"
echo "4. Configure environment variables"
