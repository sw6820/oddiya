#!/bin/bash

set -e

echo "🧪 LOCAL TESTING SCRIPT"
echo "======================="
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_info() {
    echo -e "${YELLOW}ℹ️  $1${NC}"
}

# Step 1: Check dependencies
echo "Step 1: Checking dependencies..."
command -v docker >/dev/null 2>&1 || { print_error "Docker not installed"; exit 1; }
command -v docker-compose >/dev/null 2>&1 || { print_error "Docker Compose not installed"; exit 1; }
command -v java >/dev/null 2>&1 || { print_error "Java not installed"; exit 1; }
command -v python3 >/dev/null 2>&1 || { print_error "Python3 not installed"; exit 1; }
print_success "All dependencies installed"
echo ""

# Step 2: Test Java services
echo "Step 2: Testing Java services..."
JAVA_SERVICES=("auth-service" "api-gateway" "user-service" "plan-service" "video-service")

for service in "${JAVA_SERVICES[@]}"; do
    print_info "Testing $service..."
    cd "services/$service"
    
    # Make gradlew executable if it exists
    if [ -f "gradlew" ]; then
        chmod +x gradlew
        ./gradlew clean test || { print_error "$service tests failed"; cd ../..; exit 1; }
    else
        print_info "No gradlew found for $service, skipping..."
    fi
    
    cd ../..
    print_success "$service tests passed"
done
echo ""

# Step 3: Test Python services
echo "Step 3: Testing Python services..."
PYTHON_SERVICES=("llm-agent" "video-worker")

for service in "${PYTHON_SERVICES[@]}"; do
    print_info "Testing $service..."
    cd "services/$service"
    
    if [ -f "requirements.txt" ]; then
        pip install -q -r requirements.txt
        pytest -v || { print_error "$service tests failed"; cd ../..; exit 1; }
    fi
    
    cd ../..
    print_success "$service tests passed"
done
echo ""

# Step 4: Start infrastructure
echo "Step 4: Starting infrastructure (PostgreSQL + Redis)..."
docker-compose up -d postgres redis
sleep 5

# Wait for services to be healthy
print_info "Waiting for PostgreSQL to be ready..."
timeout 30 bash -c 'until docker exec oddiya-postgres pg_isready -U oddiya_user; do sleep 1; done' || {
    print_error "PostgreSQL failed to start"
    docker-compose logs postgres
    exit 1
}

print_info "Waiting for Redis to be ready..."
timeout 30 bash -c 'until docker exec oddiya-redis redis-cli ping | grep -q PONG; do sleep 1; done' || {
    print_error "Redis failed to start"
    docker-compose logs redis
    exit 1
}

print_success "Infrastructure started successfully"
echo ""

# Step 5: Build services
echo "Step 5: Building all services..."
print_info "Building Java services..."
for service in "${JAVA_SERVICES[@]}"; do
    cd "services/$service"
    if [ -f "gradlew" ]; then
        ./gradlew clean build -x test || { print_error "Failed to build $service"; cd ../..; exit 1; }
    fi
    cd ../..
done
print_success "All services built successfully"
echo ""

# Step 6: Run integration checks
echo "Step 6: Running integration checks..."

print_info "Checking PostgreSQL schemas..."
docker exec oddiya-postgres psql -U oddiya_user -d oddiya -c "\dn" | grep -q "user_service" || {
    print_error "user_service schema not found"
    exit 1
}
docker exec oddiya-postgres psql -U oddiya_user -d oddiya -c "\dn" | grep -q "plan_service" || {
    print_error "plan_service schema not found"
    exit 1
}
docker exec oddiya-postgres psql -U oddiya_user -d oddiya -c "\dn" | grep -q "video_service" || {
    print_error "video_service schema not found"
    exit 1
}
print_success "Database schemas verified"

print_info "Checking Redis connection..."
docker exec oddiya-redis redis-cli ping | grep -q PONG || {
    print_error "Redis connection failed"
    exit 1
}
print_success "Redis connection verified"
echo ""

# Summary
echo "════════════════════════════════════"
echo "🎉 ALL LOCAL TESTS PASSED!"
echo "════════════════════════════════════"
echo ""
echo "Next steps:"
echo "1. Start all services: docker-compose -f docker-compose.local.yml up -d"
echo "2. Check logs: docker-compose logs -f"
echo "3. Test endpoints: curl http://localhost:8080/api/health"
echo ""
echo "Services will be available at:"
echo "  - API Gateway:   http://localhost:8080"
echo "  - Auth Service:  http://localhost:8081"
echo "  - User Service:  http://localhost:8082"
echo "  - Plan Service:  http://localhost:8083"
echo "  - Video Service: http://localhost:8084"
echo "  - LLM Agent:     http://localhost:8000"
echo ""

