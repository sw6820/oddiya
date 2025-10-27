#!/bin/bash

set -e

echo "🧪 RUNNING INTEGRATION TESTS"
echo "=============================="
echo ""

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_info() {
    echo -e "${YELLOW}ℹ️  $1${NC}"
}

# Step 1: Check Docker
print_info "Checking Docker..."
if ! docker info > /dev/null 2>&1; then
    print_error "Docker is not running"
    exit 1
fi
print_success "Docker is running"
echo ""

# Step 2: Start infrastructure
print_info "Starting test infrastructure..."
docker-compose up -d postgres redis
sleep 5

# Wait for database
print_info "Waiting for PostgreSQL..."
timeout 30 bash -c 'until docker exec oddiya-postgres pg_isready -U oddiya_user 2>/dev/null; do sleep 1; done'
print_success "PostgreSQL ready"

print_info "Waiting for Redis..."
timeout 30 bash -c 'until docker exec oddiya-redis redis-cli ping 2>/dev/null | grep -q PONG; do sleep 1; done'
print_success "Redis ready"
echo ""

# Step 3: Run Java integration tests
echo "════════════════════════════════════════"
echo "☕ JAVA INTEGRATION TESTS"
echo "════════════════════════════════════════"
echo ""

JAVA_SERVICES=("auth-service" "user-service" "plan-service" "video-service")
JAVA_PASSED=0
JAVA_FAILED=0

for service in "${JAVA_SERVICES[@]}"; do
    print_info "Testing $service..."
    cd "services/$service"
    
    if [ -f "gradlew" ]; then
        chmod +x gradlew
        if ./gradlew integrationTest 2>&1 | tee /tmp/${service}-integration.log; then
            print_success "$service integration tests passed"
            ((JAVA_PASSED++))
        else
            # Check if integration tests exist
            if grep -q "No tests found" /tmp/${service}-integration.log; then
                print_info "$service: No integration tests (using standard tests)"
                if ./gradlew test --tests "*Integration*" 2>/dev/null; then
                    ((JAVA_PASSED++))
                else
                    print_info "$service: Running all tests"
                    ./gradlew test && ((JAVA_PASSED++)) || ((JAVA_FAILED++))
                fi
            else
                print_error "$service integration tests failed"
                ((JAVA_FAILED++))
            fi
        fi
    fi
    
    cd ../..
done

echo ""

# Step 4: Run Python integration tests  
echo "════════════════════════════════════════"
echo "🐍 PYTHON INTEGRATION TESTS"
echo "════════════════════════════════════════"
echo ""

PYTHON_PASSED=0
PYTHON_FAILED=0

# LLM Agent
print_info "Testing llm-agent..."
cd services/llm-agent
pip install -q -r requirements.txt
if pytest tests/test_integration.py -v; then
    print_success "LLM Agent integration tests passed"
    ((PYTHON_PASSED++))
else
    print_error "LLM Agent integration tests failed"
    ((PYTHON_FAILED++))
fi
cd ../..

echo ""

# Step 5: Run end-to-end tests
echo "════════════════════════════════════════"
echo "🔄 END-TO-END TESTS"
echo "════════════════════════════════════════"
echo ""

print_info "Starting all services for E2E tests..."
./scripts/start-for-mobile-testing.sh > /dev/null 2>&1 &
START_PID=$!

sleep 20  # Wait for services to start

print_info "Running E2E tests..."
cd tests/integration
pip install -q -r requirements.txt

if pytest test_end_to_end.py -v; then
    print_success "End-to-end tests passed"
    E2E_PASSED=true
else
    print_error "End-to-end tests failed"
    E2E_PASSED=false
fi

cd ../..
echo ""

# Summary
echo "════════════════════════════════════════"
echo "📊 INTEGRATION TEST SUMMARY"
echo "════════════════════════════════════════"
echo ""
echo "Java Services:"
echo "  Passed: $JAVA_PASSED / ${#JAVA_SERVICES[@]}"
echo "  Failed: $JAVA_FAILED / ${#JAVA_SERVICES[@]}"
echo ""
echo "Python Services:"
echo "  Passed: $PYTHON_PASSED / 1"
echo "  Failed: $PYTHON_FAILED / 1"
echo ""
echo "End-to-End:"
if [ "$E2E_PASSED" = true ]; then
    echo "  ✅ Passed"
else
    echo "  ❌ Failed"
fi
echo ""

TOTAL_PASSED=$((JAVA_PASSED + PYTHON_PASSED))
TOTAL_FAILED=$((JAVA_FAILED + PYTHON_FAILED))
TOTAL_TESTS=$((${#JAVA_SERVICES[@]} + 1))

if [ $TOTAL_FAILED -eq 0 ] && [ "$E2E_PASSED" = true ]; then
    print_success "ALL INTEGRATION TESTS PASSED! ($TOTAL_PASSED/$TOTAL_TESTS)"
    echo ""
    echo "✅ Ready for load testing!"
    echo "   Run: ./scripts/run-load-tests.sh"
    exit 0
else
    print_error "SOME TESTS FAILED ($TOTAL_FAILED failures)"
    echo ""
    echo "Please fix failing tests before proceeding to load testing"
    exit 1
fi

