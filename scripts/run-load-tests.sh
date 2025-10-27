#!/bin/bash

set -e

echo "📊 RUNNING LOAD TESTS"
echo "======================"
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

print_test() {
    echo -e "${BLUE}🧪 $1${NC}"
}

# Check Locust is installed
if ! command -v locust &> /dev/null; then
    print_info "Installing Locust..."
    pip install locust
fi

# Check services are running
print_info "Checking services..."
if ! curl -s -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
    print_error "Services not running!"
    echo ""
    echo "Start services first:"
    echo "  ./scripts/start-for-mobile-testing.sh"
    exit 1
fi
print_success "All services running"
echo ""

# Create results directory
mkdir -p test-results/load

echo "════════════════════════════════════════"
echo "📊 LOAD TEST SCENARIOS"
echo "════════════════════════════════════════"
echo ""

# Scenario 1: Baseline (10 users, 1 minute)
print_test "Scenario 1: Baseline Performance"
echo "Users: 10, Duration: 1 minute"
echo ""

locust -f tests/load/locustfile.py \
    --host=http://localhost:8080 \
    --users 10 \
    --spawn-rate 1 \
    --run-time 1m \
    --headless \
    --only-summary \
    --html=test-results/load/baseline-report.html \
    --csv=test-results/load/baseline

print_success "Baseline test complete"
echo ""

# Scenario 2: Normal Load (25 users, 2 minutes)
print_test "Scenario 2: Normal Load"
echo "Users: 25, Duration: 2 minutes"
echo ""

locust -f tests/load/locustfile.py \
    --host=http://localhost:8080 \
    --users 25 \
    --spawn-rate 2 \
    --run-time 2m \
    --headless \
    --only-summary \
    --html=test-results/load/normal-load-report.html \
    --csv=test-results/load/normal-load

print_success "Normal load test complete"
echo ""

# Scenario 3: Peak Load (50 users, 2 minutes)
print_test "Scenario 3: Peak Load"
echo "Users: 50, Duration: 2 minutes"
echo ""

locust -f tests/load/locustfile.py \
    --host=http://localhost:8080 \
    --users 50 \
    --spawn-rate 5 \
    --run-time 2m \
    --headless \
    --only-summary \
    --html=test-results/load/peak-load-report.html \
    --csv=test-results/load/peak-load

print_success "Peak load test complete"
echo ""

# Scenario 4: Stress Test (100 users, 1 minute)
print_test "Scenario 4: Stress Test (Finding Limits)"
echo "Users: 100, Duration: 1 minute"
echo ""

locust -f tests/load/locustfile.py \
    --host=http://localhost:8080 \
    --users 100 \
    --spawn-rate 10 \
    --run-time 1m \
    --headless \
    --only-summary \
    --html=test-results/load/stress-test-report.html \
    --csv=test-results/load/stress-test

print_success "Stress test complete"
echo ""

# Generate summary
echo "════════════════════════════════════════"
echo "📈 LOAD TEST RESULTS"
echo "════════════════════════════════════════"
echo ""

print_info "Analyzing results..."

# Check if results exist
if [ -f "test-results/load/baseline_stats.csv" ]; then
    echo "Baseline Performance:"
    tail -n 1 test-results/load/baseline_stats.csv | awk -F, '{print "  Requests: "$3", Failures: "$4", Avg: "$6"ms, Max: "$9"ms"}'
    echo ""
fi

if [ -f "test-results/load/normal-load_stats.csv" ]; then
    echo "Normal Load:"
    tail -n 1 test-results/load/normal-load_stats.csv | awk -F, '{print "  Requests: "$3", Failures: "$4", Avg: "$6"ms, Max: "$9"ms"}'
    echo ""
fi

if [ -f "test-results/load/peak-load_stats.csv" ]; then
    echo "Peak Load:"
    tail -n 1 test-results/load/peak-load_stats.csv | awk -F, '{print "  Requests: "$3", Failures: "$4", Avg: "$6"ms, Max: "$9"ms"}'
    echo ""
fi

if [ -f "test-results/load/stress-test_stats.csv" ]; then
    echo "Stress Test:"
    tail -n 1 test-results/load/stress-test_stats.csv | awk -F, '{print "  Requests: "$3", Failures: "$4", Avg: "$6"ms, Max: "$9"ms"}'
    echo ""
fi

echo "════════════════════════════════════════"
print_success "LOAD TESTING COMPLETE!"
echo "════════════════════════════════════════"
echo ""
echo "📁 Results saved to:"
echo "  test-results/load/"
echo ""
echo "📊 HTML Reports:"
ls -1 test-results/load/*.html 2>/dev/null || echo "  No HTML reports generated"
echo ""
echo "📋 Next Steps:"
echo "  1. Review HTML reports"
echo "  2. Check for bottlenecks"
echo "  3. Run: ./scripts/analyze-performance.sh"
echo ""

