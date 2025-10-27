#!/bin/bash

set -e

echo "📱 STARTING ODDIYA FOR MOBILE APP TESTING"
echo "=========================================="
echo ""

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_info() {
    echo -e "${YELLOW}ℹ️  $1${NC}"
}

print_mobile() {
    echo -e "${BLUE}📱 $1${NC}"
}

# Step 1: Clean up
print_info "Cleaning up old containers..."
docker-compose down 2>/dev/null || true
docker-compose -f docker-compose.local.yml down 2>/dev/null || true

# Step 2: Start infrastructure
print_info "Starting PostgreSQL and Redis..."
docker-compose up -d postgres redis

print_info "Waiting for database to be ready..."
sleep 10

# Verify database is ready
docker exec oddiya-postgres pg_isready -U oddiya_user || {
    echo "❌ Database failed to start"
    docker-compose logs postgres
    exit 1
}

print_success "Infrastructure ready!"

# Step 3: Start services
print_info "Starting all microservices..."
docker-compose -f docker-compose.local.yml up -d

print_info "Waiting for services to start..."
sleep 15

# Step 4: Health check
echo ""
echo "════════════════════════════════════════"
echo "🏥 CHECKING SERVICE HEALTH"
echo "════════════════════════════════════════"

check_health() {
    local service=$1
    local port=$2
    local path=${3:-"/actuator/health"}
    
    max_retries=10
    retry=0
    
    while [ $retry -lt $max_retries ]; do
        if curl -s -f "http://localhost:${port}${path}" > /dev/null 2>&1; then
            print_success "$service is ready (http://localhost:$port)"
            return 0
        fi
        retry=$((retry + 1))
        sleep 2
    done
    
    echo "⚠️  $service not responding"
    return 1
}

check_health "API Gateway" 8080
check_health "Auth Service" 8081
check_health "User Service" 8082
check_health "Plan Service" 8083
check_health "Video Service" 8084
check_health "LLM Agent" 8000 "/health"

echo ""
echo "════════════════════════════════════════"
echo "📱 MOBILE APP ENDPOINTS READY!"
echo "════════════════════════════════════════"
echo ""
print_mobile "Base URL: http://localhost:8080"
echo ""
print_mobile "Available Endpoints:"
echo "  🔐 OAuth Login:"
echo "     GET  /api/auth/oauth2/authorize/google"
echo "     POST /api/auth/oauth2/callback/google"
echo ""
echo "  👤 User Profile:"
echo "     GET    /api/users/me"
echo "     PATCH  /api/users/me"
echo ""
echo "  📝 Travel Plans:"
echo "     GET    /api/plans"
echo "     POST   /api/plans"
echo "     GET    /api/plans/{id}"
echo "     PATCH  /api/plans/{id}"
echo "     DELETE /api/plans/{id}"
echo ""
echo "  🎥 Videos:"
echo "     GET    /api/videos"
echo "     POST   /api/videos"
echo "     GET    /api/videos/{id}"
echo ""
echo "════════════════════════════════════════"
echo "🧪 TESTING TOOLS"
echo "════════════════════════════════════════"
echo ""
echo "Option 1: Use Postman Collection"
echo "  → Import: docs/api/Oddiya-Mobile-API.postman_collection.json"
echo ""
echo "Option 2: Use curl commands"
echo "  → See: docs/api/MOBILE_API_TESTING.md"
echo ""
echo "Option 3: Use test script"
echo "  → Run: ./scripts/test-mobile-api.sh"
echo ""
echo "════════════════════════════════════════"
echo "📊 USEFUL COMMANDS"
echo "════════════════════════════════════════"
echo ""
echo "View all logs:"
echo "  docker-compose logs -f"
echo ""
echo "View specific service:"
echo "  docker-compose logs -f api-gateway"
echo ""
echo "Restart a service:"
echo "  docker-compose restart plan-service"
echo ""
echo "Stop everything:"
echo "  ./scripts/stop-local.sh"
echo ""
print_success "Ready for mobile app testing! 🚀"
echo ""

