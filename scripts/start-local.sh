#!/bin/bash

set -e

echo "🚀 STARTING ODDIYA LOCALLY"
echo "=========================="
echo ""

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_info() {
    echo -e "${YELLOW}ℹ️  $1${NC}"
}

# Step 1: Stop any running containers
print_info "Stopping existing containers..."
docker-compose down 2>/dev/null || true
docker-compose -f docker-compose.local.yml down 2>/dev/null || true

# Step 2: Start infrastructure only first
print_info "Starting infrastructure (PostgreSQL + Redis)..."
docker-compose up -d postgres redis

# Wait for infrastructure
print_info "Waiting for infrastructure to be ready..."
sleep 5

# Step 3: Start all services
print_info "Starting all 7 microservices..."
docker-compose -f docker-compose.local.yml up -d

# Step 4: Wait for services
print_info "Waiting for services to start..."
sleep 10

# Step 5: Health checks
echo ""
echo "════════════════════════════════════"
echo "🏥 HEALTH CHECK"
echo "════════════════════════════════════"

check_service() {
    local name=$1
    local port=$2
    local path=${3:-"/actuator/health"}
    
    if curl -s -f "http://localhost:${port}${path}" > /dev/null 2>&1; then
        print_success "$name is healthy (port $port)"
        return 0
    else
        echo "⏳ $name is starting... (port $port)"
        return 1
    fi
}

# Give services time to start
sleep 5

check_service "Auth Service" 8081
check_service "User Service" 8082
check_service "Plan Service" 8083
check_service "Video Service" 8084
check_service "LLM Agent" 8000 "/health"
check_service "API Gateway" 8080

echo ""
echo "════════════════════════════════════"
echo "✅ ODDIYA IS RUNNING LOCALLY!"
echo "════════════════════════════════════"
echo ""
echo "Services:"
echo "  📍 API Gateway:   http://localhost:8080"
echo "  🔐 Auth Service:  http://localhost:8081"
echo "  👤 User Service:  http://localhost:8082"
echo "  📝 Plan Service:  http://localhost:8083"
echo "  🎥 Video Service: http://localhost:8084"
echo "  🤖 LLM Agent:     http://localhost:8000"
echo ""
echo "Infrastructure:"
echo "  🐘 PostgreSQL:    localhost:5432"
echo "  🔴 Redis:         localhost:6379"
echo ""
echo "Useful commands:"
echo "  View logs:        docker-compose logs -f"
echo "  Stop services:    docker-compose down"
echo "  Restart service:  docker-compose restart <service-name>"
echo ""

