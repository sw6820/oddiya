#!/bin/bash
# deploy-phase1-ec2.sh - Deploy Oddiya on Single EC2 Instance (Cheapest Option)
#
# Cost: ~$15-20/month (t3.medium Spot + EBS + CloudWatch)
# Runtime: ~10-15 minutes
#
# Prerequisites:
# - AWS CLI configured
# - SSH keypair created
# - Domain name (optional, for SSL)

set -e

# ============================================================================
# CONFIGURATION
# ============================================================================

REGION="${AWS_REGION:-ap-northeast-2}"
INSTANCE_TYPE="t3.medium"
SPOT_PRICE="0.0416"  # On-demand price as max
AMI_ID="ami-0c9c942bd7bf113a2"  # Ubuntu 22.04 LTS (ap-northeast-2)
KEY_NAME="oddiya-keypair"
VOLUME_SIZE=30
PROJECT_NAME="oddiya"
ENVIRONMENT="production"

# Network
VPC_CIDR="10.0.0.0/16"
SUBNET_CIDR="10.0.1.0/24"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# ============================================================================
# HELPER FUNCTIONS
# ============================================================================

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

check_prerequisites() {
    log_info "Checking prerequisites..."

    if ! command -v aws &> /dev/null; then
        log_error "AWS CLI not found. Install from https://aws.amazon.com/cli/"
        exit 1
    fi

    if ! aws sts get-caller-identity &> /dev/null; then
        log_error "AWS CLI not configured. Run 'aws configure'"
        exit 1
    fi

    log_info "Prerequisites OK"
}

create_vpc_if_needed() {
    log_info "Checking VPC..."

    VPC_ID=$(aws ec2 describe-vpcs \
        --filters "Name=tag:Name,Values=${PROJECT_NAME}-vpc" \
        --query 'Vpcs[0].VpcId' \
        --output text \
        --region $REGION 2>/dev/null || echo "None")

    if [ "$VPC_ID" == "None" ] || [ -z "$VPC_ID" ]; then
        log_info "Creating VPC..."
        VPC_ID=$(aws ec2 create-vpc \
            --cidr-block $VPC_CIDR \
            --region $REGION \
            --query 'Vpc.VpcId' \
            --output text)

        aws ec2 create-tags \
            --resources $VPC_ID \
            --tags Key=Name,Value=${PROJECT_NAME}-vpc \
            --region $REGION

        # Enable DNS
        aws ec2 modify-vpc-attribute \
            --vpc-id $VPC_ID \
            --enable-dns-hostnames \
            --region $REGION

        log_info "Created VPC: $VPC_ID"
    else
        log_info "Using existing VPC: $VPC_ID"
    fi
}

create_subnet_if_needed() {
    log_info "Checking subnet..."

    SUBNET_ID=$(aws ec2 describe-subnets \
        --filters "Name=vpc-id,Values=$VPC_ID" "Name=tag:Name,Values=${PROJECT_NAME}-public-subnet" \
        --query 'Subnets[0].SubnetId' \
        --output text \
        --region $REGION 2>/dev/null || echo "None")

    if [ "$SUBNET_ID" == "None" ] || [ -z "$SUBNET_ID" ]; then
        log_info "Creating subnet..."
        SUBNET_ID=$(aws ec2 create-subnet \
            --vpc-id $VPC_ID \
            --cidr-block $SUBNET_CIDR \
            --availability-zone ${REGION}a \
            --region $REGION \
            --query 'Subnet.SubnetId' \
            --output text)

        aws ec2 create-tags \
            --resources $SUBNET_ID \
            --tags Key=Name,Value=${PROJECT_NAME}-public-subnet \
            --region $REGION

        # Auto-assign public IP
        aws ec2 modify-subnet-attribute \
            --subnet-id $SUBNET_ID \
            --map-public-ip-on-launch \
            --region $REGION

        log_info "Created subnet: $SUBNET_ID"
    else
        log_info "Using existing subnet: $SUBNET_ID"
    fi
}

create_internet_gateway_if_needed() {
    log_info "Checking Internet Gateway..."

    IGW_ID=$(aws ec2 describe-internet-gateways \
        --filters "Name=attachment.vpc-id,Values=$VPC_ID" \
        --query 'InternetGateways[0].InternetGatewayId' \
        --output text \
        --region $REGION 2>/dev/null || echo "None")

    if [ "$IGW_ID" == "None" ] || [ -z "$IGW_ID" ]; then
        log_info "Creating Internet Gateway..."
        IGW_ID=$(aws ec2 create-internet-gateway \
            --region $REGION \
            --query 'InternetGateway.InternetGatewayId' \
            --output text)

        aws ec2 create-tags \
            --resources $IGW_ID \
            --tags Key=Name,Value=${PROJECT_NAME}-igw \
            --region $REGION

        aws ec2 attach-internet-gateway \
            --internet-gateway-id $IGW_ID \
            --vpc-id $VPC_ID \
            --region $REGION

        log_info "Created Internet Gateway: $IGW_ID"
    else
        log_info "Using existing Internet Gateway: $IGW_ID"
    fi

    # Create/update route table
    ROUTE_TABLE_ID=$(aws ec2 describe-route-tables \
        --filters "Name=vpc-id,Values=$VPC_ID" "Name=association.main,Values=true" \
        --query 'RouteTables[0].RouteTableId' \
        --output text \
        --region $REGION)

    aws ec2 create-route \
        --route-table-id $ROUTE_TABLE_ID \
        --destination-cidr-block 0.0.0.0/0 \
        --gateway-id $IGW_ID \
        --region $REGION 2>/dev/null || log_warn "Route already exists"
}

create_security_group_if_needed() {
    log_info "Checking Security Group..."

    SG_ID=$(aws ec2 describe-security-groups \
        --filters "Name=group-name,Values=${PROJECT_NAME}-ec2-sg" "Name=vpc-id,Values=$VPC_ID" \
        --query 'SecurityGroups[0].GroupId' \
        --output text \
        --region $REGION 2>/dev/null || echo "None")

    if [ "$SG_ID" == "None" ] || [ -z "$SG_ID" ]; then
        log_info "Creating Security Group..."
        SG_ID=$(aws ec2 create-security-group \
            --group-name ${PROJECT_NAME}-ec2-sg \
            --description "Oddiya single EC2 security group" \
            --vpc-id $VPC_ID \
            --region $REGION \
            --query 'GroupId' \
            --output text)

        # Allow SSH
        aws ec2 authorize-security-group-ingress \
            --group-id $SG_ID \
            --protocol tcp \
            --port 22 \
            --cidr 0.0.0.0/0 \
            --region $REGION

        # Allow HTTP
        aws ec2 authorize-security-group-ingress \
            --group-id $SG_ID \
            --protocol tcp \
            --port 80 \
            --cidr 0.0.0.0/0 \
            --region $REGION

        # Allow HTTPS
        aws ec2 authorize-security-group-ingress \
            --group-id $SG_ID \
            --protocol tcp \
            --port 443 \
            --cidr 0.0.0.0/0 \
            --region $REGION

        # Allow API Gateway (for testing)
        aws ec2 authorize-security-group-ingress \
            --group-id $SG_ID \
            --protocol tcp \
            --port 8080 \
            --cidr 0.0.0.0/0 \
            --region $REGION

        log_info "Created Security Group: $SG_ID"
    else
        log_info "Using existing Security Group: $SG_ID"
    fi
}

generate_user_data() {
    cat > /tmp/user-data.sh <<'USERDATA'
#!/bin/bash
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
apt-get install -y nginx

# Create application directory
mkdir -p /opt/oddiya
cd /opt/oddiya

# Note: Application code should be deployed separately via CI/CD
# This script only sets up the infrastructure

echo "✅ Instance setup complete. Deploy application using: ssh ubuntu@<ip> 'cd /opt/oddiya && docker-compose up -d'"
USERDATA

    log_info "User data script generated"
}

launch_spot_instance() {
    log_info "Launching Spot instance..."

    generate_user_data

    # Create launch specification
    cat > /tmp/spot-launch-spec.json <<EOF
{
  "ImageId": "$AMI_ID",
  "InstanceType": "$INSTANCE_TYPE",
  "KeyName": "$KEY_NAME",
  "SecurityGroupIds": ["$SG_ID"],
  "SubnetId": "$SUBNET_ID",
  "BlockDeviceMappings": [
    {
      "DeviceName": "/dev/sda1",
      "Ebs": {
        "VolumeSize": $VOLUME_SIZE,
        "VolumeType": "gp3",
        "DeleteOnTermination": true
      }
    }
  ],
  "IamInstanceProfile": {
    "Name": "ec2-cloudwatch-role"
  },
  "UserData": "$(base64 -w0 /tmp/user-data.sh)"
}
EOF

    # Request Spot instance
    SPOT_REQUEST_ID=$(aws ec2 request-spot-instances \
        --spot-price "$SPOT_PRICE" \
        --instance-count 1 \
        --type "persistent" \
        --launch-specification file:///tmp/spot-launch-spec.json \
        --region $REGION \
        --query 'SpotInstanceRequests[0].SpotInstanceRequestId' \
        --output text)

    log_info "Spot request created: $SPOT_REQUEST_ID"
    log_info "Waiting for Spot instance to be fulfilled..."

    # Wait for fulfillment
    for i in {1..60}; do
        STATUS=$(aws ec2 describe-spot-instance-requests \
            --spot-instance-request-ids $SPOT_REQUEST_ID \
            --region $REGION \
            --query 'SpotInstanceRequests[0].Status.Code' \
            --output text)

        if [ "$STATUS" == "fulfilled" ]; then
            INSTANCE_ID=$(aws ec2 describe-spot-instance-requests \
                --spot-instance-request-ids $SPOT_REQUEST_ID \
                --region $REGION \
                --query 'SpotInstanceRequests[0].InstanceId' \
                --output text)

            log_info "Spot instance fulfilled: $INSTANCE_ID"
            break
        elif [ "$STATUS" == "price-too-low" ] || [ "$STATUS" == "capacity-not-available" ]; then
            log_error "Spot request failed: $STATUS"
            exit 1
        fi

        echo -n "."
        sleep 5
    done

    if [ -z "$INSTANCE_ID" ]; then
        log_error "Timeout waiting for Spot instance"
        exit 1
    fi

    # Tag instance
    aws ec2 create-tags \
        --resources $INSTANCE_ID \
        --tags \
            Key=Name,Value=${PROJECT_NAME}-all-in-one \
            Key=Environment,Value=$ENVIRONMENT \
            Key=Project,Value=$PROJECT_NAME \
        --region $REGION

    # Wait for instance to be running
    log_info "Waiting for instance to be running..."
    aws ec2 wait instance-running \
        --instance-ids $INSTANCE_ID \
        --region $REGION
}

allocate_elastic_ip() {
    log_info "Allocating Elastic IP..."

    EIP_ALLOC_ID=$(aws ec2 allocate-address \
        --domain vpc \
        --region $REGION \
        --query 'AllocationId' \
        --output text)

    aws ec2 create-tags \
        --resources $EIP_ALLOC_ID \
        --tags Key=Name,Value=${PROJECT_NAME}-eip \
        --region $REGION

    # Associate with instance
    aws ec2 associate-address \
        --instance-id $INSTANCE_ID \
        --allocation-id $EIP_ALLOC_ID \
        --region $REGION

    PUBLIC_IP=$(aws ec2 describe-addresses \
        --allocation-ids $EIP_ALLOC_ID \
        --region $REGION \
        --query 'Addresses[0].PublicIp' \
        --output text)

    log_info "Elastic IP allocated: $PUBLIC_IP"
}

print_summary() {
    echo ""
    echo "========================================="
    echo "🎉 Deployment Complete!"
    echo "========================================="
    echo ""
    echo "Instance ID: $INSTANCE_ID"
    echo "Public IP: $PUBLIC_IP"
    echo "Region: $REGION"
    echo "Instance Type: $INSTANCE_TYPE (Spot)"
    echo ""
    echo "Next steps:"
    echo "1. SSH to instance: ssh -i $KEY_NAME.pem ubuntu@$PUBLIC_IP"
    echo "2. Deploy application:"
    echo "   cd /opt/oddiya"
    echo "   git clone https://github.com/<your-org>/oddiya.git ."
    echo "   cp .env.example .env"
    echo "   # Edit .env with your credentials"
    echo "   docker-compose -f docker-compose.local.yml up -d"
    echo "3. Check logs: docker-compose logs -f"
    echo "4. Test API: curl http://$PUBLIC_IP:8080/actuator/health"
    echo ""
    echo "Cost estimate: ~\$15-20/month"
    echo "========================================="
}

# ============================================================================
# MAIN
# ============================================================================

main() {
    log_info "Starting Phase 1 deployment (Single EC2 + Docker Compose)"

    check_prerequisites
    create_vpc_if_needed
    create_subnet_if_needed
    create_internet_gateway_if_needed
    create_security_group_if_needed
    launch_spot_instance
    allocate_elastic_ip
    print_summary

    log_info "✅ Phase 1 deployment complete!"
}

main "$@"
