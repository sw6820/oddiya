#!/bin/bash
# Oddiya Phase 1 Deployment Script
# Deploys Plan Service + LLM Agent to EC2 Application Server

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}===== Oddiya Phase 1 Deployment =====${NC}"

# Check prerequisites
check_prerequisites() {
    echo -e "${YELLOW}Checking prerequisites...${NC}"

    if ! command -v terraform &> /dev/null; then
        echo -e "${RED}Error: Terraform not installed${NC}"
        exit 1
    fi

    if ! command -v aws &> /dev/null; then
        echo -e "${RED}Error: AWS CLI not installed${NC}"
        exit 1
    fi

    if [ ! -f "infrastructure/terraform/phase1/terraform.tfvars" ]; then
        echo -e "${RED}Error: terraform.tfvars not found. Copy from terraform.tfvars.example${NC}"
        exit 1
    fi

    echo -e "${GREEN}✓ Prerequisites OK${NC}"
}

# Get EC2 IPs from Terraform
get_ec2_ips() {
    echo -e "${YELLOW}Getting EC2 instance IPs...${NC}"

    cd infrastructure/terraform/phase1

    APP_SERVER_IP=$(terraform output -raw app_server_public_ip 2>/dev/null)
    DB_SERVER_IP=$(terraform output -raw db_server_private_ip 2>/dev/null)

    if [ -z "$APP_SERVER_IP" ]; then
        echo -e "${RED}Error: Could not get App Server IP. Run 'terraform apply' first${NC}"
        exit 1
    fi

    echo -e "${GREEN}App Server IP: $APP_SERVER_IP${NC}"
    echo -e "${GREEN}DB Server IP: $DB_SERVER_IP${NC}"

    cd ../../..
}

# Build Java service
build_plan_service() {
    echo -e "${YELLOW}Building Plan Service...${NC}"

    cd services/plan-service
    ./gradlew clean build -x test

    if [ ! -f "build/libs/plan-service-0.0.1-SNAPSHOT.jar" ]; then
        echo -e "${RED}Error: Plan Service JAR not found${NC}"
        exit 1
    fi

    echo -e "${GREEN}✓ Plan Service built${NC}"
    cd ../..
}

# Package Python service
package_llm_agent() {
    echo -e "${YELLOW}Packaging LLM Agent...${NC}"

    cd services/llm-agent

    tar czf llm-agent.tar.gz \
        src/ \
        requirements.txt \
        main.py

    if [ ! -f "llm-agent.tar.gz" ]; then
        echo -e "${RED}Error: LLM Agent package not found${NC}"
        exit 1
    fi

    echo -e "${GREEN}✓ LLM Agent packaged${NC}"
    cd ../..
}

# Deploy Plan Service
deploy_plan_service() {
    echo -e "${YELLOW}Deploying Plan Service to EC2...${NC}"

    KEY_PATH="$HOME/.ssh/oddiya-prod.pem"

    if [ ! -f "$KEY_PATH" ]; then
        echo -e "${RED}Error: SSH key not found at $KEY_PATH${NC}"
        exit 1
    fi

    # Copy JAR
    echo "Copying JAR file..."
    scp -i "$KEY_PATH" \
        -o StrictHostKeyChecking=no \
        services/plan-service/build/libs/plan-service-0.0.1-SNAPSHOT.jar \
        ec2-user@$APP_SERVER_IP:/opt/oddiya/plan-service/app.jar

    # Create application.yml
    echo "Creating application.yml..."
    ssh -i "$KEY_PATH" -o StrictHostKeyChecking=no ec2-user@$APP_SERVER_IP << 'EOF_APP_YML'
cat > /opt/oddiya/plan-service/application.yml <<EOF
server:
  port: 8083

spring:
  datasource:
    url: jdbc:postgresql://DB_SERVER_IP:5432/oddiya?currentSchema=plan_service
    username: admin
    password: \${DB_PASSWORD}
    hikari:
      maximum-pool-size: 5
      connection-timeout: 30000
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
    show-sql: false

llm-agent:
  base-url: http://localhost:8000

logging:
  level:
    com.oddiya: INFO
  file:
    name: /opt/oddiya/logs/plan-service.log
EOF
EOF_APP_YML

    # Replace DB_SERVER_IP in application.yml
    ssh -i "$KEY_PATH" -o StrictHostKeyChecking=no ec2-user@$APP_SERVER_IP \
        "sed -i 's/DB_SERVER_IP/$DB_SERVER_IP/g' /opt/oddiya/plan-service/application.yml"

    # Create systemd service
    echo "Creating systemd service..."
    ssh -i "$KEY_PATH" -o StrictHostKeyChecking=no ec2-user@$APP_SERVER_IP << 'EOF_SYSTEMD'
sudo tee /etc/systemd/system/plan-service.service > /dev/null <<EOF
[Unit]
Description=Oddiya Plan Service
After=network.target

[Service]
Type=simple
User=ec2-user
WorkingDirectory=/opt/oddiya/plan-service
Environment="DB_PASSWORD=YOUR_DB_PASSWORD_HERE"
ExecStart=/usr/bin/java -Xmx512m -jar /opt/oddiya/plan-service/app.jar --spring.config.location=/opt/oddiya/plan-service/application.yml
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
EOF
EOF_SYSTEMD

    echo -e "${GREEN}✓ Plan Service deployed${NC}"
}

# Deploy LLM Agent
deploy_llm_agent() {
    echo -e "${YELLOW}Deploying LLM Agent to EC2...${NC}"

    KEY_PATH="$HOME/.ssh/oddiya-prod.pem"

    # Copy package
    echo "Copying LLM Agent package..."
    scp -i "$KEY_PATH" \
        -o StrictHostKeyChecking=no \
        services/llm-agent/llm-agent.tar.gz \
        ec2-user@$APP_SERVER_IP:/opt/oddiya/llm-agent/

    # Extract and install
    echo "Installing LLM Agent..."
    ssh -i "$KEY_PATH" -o StrictHostKeyChecking=no ec2-user@$APP_SERVER_IP << 'EOF_INSTALL'
cd /opt/oddiya/llm-agent
tar xzf llm-agent.tar.gz
python3.11 -m venv venv
source venv/bin/activate
pip install --upgrade pip
pip install -r requirements.txt
deactivate
EOF_INSTALL

    # Create .env file (user must update API key)
    echo "Creating .env file..."
    ssh -i "$KEY_PATH" -o StrictHostKeyChecking=no ec2-user@$APP_SERVER_IP << 'EOF_ENV'
cat > /opt/oddiya/llm-agent/.env <<EOF
GOOGLE_API_KEY=YOUR_GEMINI_API_KEY_HERE
GEMINI_MODEL=gemini-2.0-flash-exp
REDIS_HOST=localhost
REDIS_PORT=6379
EOF
EOF_ENV

    # Create systemd service
    echo "Creating systemd service..."
    ssh -i "$KEY_PATH" -o StrictHostKeyChecking=no ec2-user@$APP_SERVER_IP << 'EOF_SYSTEMD'
sudo tee /etc/systemd/system/llm-agent.service > /dev/null <<EOF
[Unit]
Description=Oddiya LLM Agent
After=network.target

[Service]
Type=simple
User=ec2-user
WorkingDirectory=/opt/oddiya/llm-agent
Environment="PATH=/opt/oddiya/llm-agent/venv/bin:/usr/local/bin:/usr/bin"
ExecStart=/opt/oddiya/llm-agent/venv/bin/python main.py
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
EOF
EOF_SYSTEMD

    echo -e "${GREEN}✓ LLM Agent deployed${NC}"
}

# Start services
start_services() {
    echo -e "${YELLOW}Starting services...${NC}"

    KEY_PATH="$HOME/.ssh/oddiya-prod.pem"

    ssh -i "$KEY_PATH" -o StrictHostKeyChecking=no ec2-user@$APP_SERVER_IP << 'EOF_START'
# Reload systemd
sudo systemctl daemon-reload

# Enable services
sudo systemctl enable plan-service
sudo systemctl enable llm-agent

# Start services
sudo systemctl start plan-service
sudo systemctl start llm-agent

# Wait a bit
sleep 5

# Check status
echo "=== Plan Service Status ==="
sudo systemctl status plan-service --no-pager

echo ""
echo "=== LLM Agent Status ==="
sudo systemctl status llm-agent --no-pager
EOF_START

    echo -e "${GREEN}✓ Services started${NC}"
}

# Health check
health_check() {
    echo -e "${YELLOW}Running health checks...${NC}"

    echo "Waiting for services to be ready (30 seconds)..."
    sleep 30

    echo "Testing LLM Agent..."
    if curl -s "http://$APP_SERVER_IP:8000/health" | grep -q "ok"; then
        echo -e "${GREEN}✓ LLM Agent is healthy${NC}"
    else
        echo -e "${RED}✗ LLM Agent health check failed${NC}"
    fi

    echo "Testing Plan Service..."
    if curl -s "http://$APP_SERVER_IP:8083/actuator/health" | grep -q "UP"; then
        echo -e "${GREEN}✓ Plan Service is healthy${NC}"
    else
        echo -e "${RED}✗ Plan Service health check failed${NC}"
    fi
}

# Print next steps
print_next_steps() {
    echo ""
    echo -e "${GREEN}===== Deployment Complete! =====${NC}"
    echo ""
    echo "📝 Next Steps:"
    echo ""
    echo "1. Update secrets on EC2:"
    echo "   ssh -i ~/.ssh/oddiya-prod.pem ec2-user@$APP_SERVER_IP"
    echo "   - Edit /opt/oddiya/llm-agent/.env (add real GOOGLE_API_KEY)"
    echo "   - Edit /etc/systemd/system/plan-service.service (add real DB_PASSWORD)"
    echo "   - sudo systemctl daemon-reload"
    echo "   - sudo systemctl restart plan-service llm-agent"
    echo ""
    echo "2. Test endpoints:"
    echo "   curl http://$APP_SERVER_IP:8000/health"
    echo "   curl http://$APP_SERVER_IP:8083/actuator/health"
    echo ""
    echo "3. Update mobile app config:"
    echo "   mobile/src/constants/config.ts"
    echo "   LLM_AGENT_BASE_URL: 'http://$APP_SERVER_IP:8000'"
    echo "   PLAN_SERVICE_BASE_URL: 'http://$APP_SERVER_IP:8083'"
    echo ""
    echo "4. View logs:"
    echo "   ssh -i ~/.ssh/oddiya-prod.pem ec2-user@$APP_SERVER_IP"
    echo "   sudo journalctl -u plan-service -f"
    echo "   sudo journalctl -u llm-agent -f"
    echo ""
}

# Main execution
main() {
    check_prerequisites
    get_ec2_ips
    build_plan_service
    package_llm_agent
    deploy_plan_service
    deploy_llm_agent
    start_services
    health_check
    print_next_steps
}

main
