#!/bin/bash
# EC2 Instance Setup Script for Oddiya
# Run this on a fresh Amazon Linux 2023 / Ubuntu t2.micro instance

set -e

echo "=== Oddiya EC2 Setup Script ==="
echo "Setting up Docker, Docker Compose, and required tools..."

# Update system
sudo yum update -y || sudo apt-get update -y

# Install Docker
if ! command -v docker &> /dev/null; then
    echo "Installing Docker..."
    sudo yum install -y docker || sudo apt-get install -y docker.io
    sudo systemctl start docker
    sudo systemctl enable docker
    sudo usermod -aG docker $USER
    echo "Docker installed successfully"
else
    echo "Docker already installed"
fi

# Install Docker Compose
if ! command -v docker-compose &> /dev/null; then
    echo "Installing Docker Compose..."
    sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
    echo "Docker Compose installed successfully"
else
    echo "Docker Compose already installed"
fi

# Install Git
if ! command -v git &> /dev/null; then
    echo "Installing Git..."
    sudo yum install -y git || sudo apt-get install -y git
    echo "Git installed successfully"
else
    echo "Git already installed"
fi

# Create app directory
sudo mkdir -p /opt/oddiya
sudo chown $USER:$USER /opt/oddiya
cd /opt/oddiya

echo ""
echo "=== Setup Complete ==="
echo "Next steps:"
echo "1. Clone your repository: git clone <your-repo-url> ."
echo "2. Create .env file with GOOGLE_API_KEY"
echo "3. Run: docker-compose up -d"
echo ""
echo "IMPORTANT: Configure Security Group to allow:"
echo "  - Port 80 (HTTP) from 0.0.0.0/0"
echo "  - Port 443 (HTTPS) from 0.0.0.0/0"
echo "  - Port 22 (SSH) from your IP only"
echo ""
