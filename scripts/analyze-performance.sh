#!/bin/bash

echo "📊 PERFORMANCE ANALYSIS"
echo "======================="
echo ""

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Check if results exist
if [ ! -d "test-results/load" ]; then
    echo "❌ No load test results found"
    echo "Run: ./scripts/run-load-tests.sh first"
    exit 1
fi

echo "Analyzing load test results..."
echo ""

# Function to analyze CSV
analyze_csv() {
    local file=$1
    local scenario=$2
    
    if [ ! -f "$file" ]; then
        return
    fi
    
    echo "════════════════════════════════════════"
    echo "$scenario"
    echo "════════════════════════════════════════"
    
    # Get aggregated stats (last line)
    stats=$(tail -n 1 "$file")
    
    requests=$(echo "$stats" | awk -F, '{print $3}')
    failures=$(echo "$stats" | awk -F, '{print $4}')
    avg_response=$(echo "$stats" | awk -F, '{print $6}')
    min_response=$(echo "$stats" | awk -F, '{print $5}')
    max_response=$(echo "$stats" | awk -F, '{print $9}')
    rps=$(echo "$stats" | awk -F, '{print $10}')
    
    echo "Total Requests:    $requests"
    echo "Total Failures:    $failures"
    echo "Failure Rate:      $(awk "BEGIN {printf \"%.2f%%\", ($failures/$requests)*100}")"
    echo "Requests/Second:   $rps"
    echo "Avg Response:      ${avg_response}ms"
    echo "Min Response:      ${min_response}ms"
    echo "Max Response:      ${max_response}ms"
    
    # Performance assessment
    echo ""
    echo "Assessment:"
    
    # Check avg response time
    if (( $(echo "$avg_response < 500" | bc -l) )); then
        echo -e "${GREEN}✅ Average response time: EXCELLENT (<500ms)${NC}"
    elif (( $(echo "$avg_response < 1000" | bc -l) )); then
        echo -e "${YELLOW}⚠️  Average response time: ACCEPTABLE (500-1000ms)${NC}"
    else
        echo -e "${RED}❌ Average response time: POOR (>1000ms)${NC}"
    fi
    
    # Check failure rate
    failure_pct=$(awk "BEGIN {print ($failures/$requests)*100}")
    if (( $(echo "$failure_pct < 1" | bc -l) )); then
        echo -e "${GREEN}✅ Failure rate: EXCELLENT (<1%)${NC}"
    elif (( $(echo "$failure_pct < 5" | bc -l) )); then
        echo -e "${YELLOW}⚠️  Failure rate: ACCEPTABLE (1-5%)${NC}"
    else
        echo -e "${RED}❌ Failure rate: POOR (>5%)${NC}"
    fi
    
    # Check throughput
    if (( $(echo "$rps > 50" | bc -l) )); then
        echo -e "${GREEN}✅ Throughput: GOOD (>50 RPS)${NC}"
    elif (( $(echo "$rps > 20" | bc -l) )); then
        echo -e "${YELLOW}⚠️  Throughput: ACCEPTABLE (20-50 RPS)${NC}"
    else
        echo -e "${RED}❌ Throughput: LOW (<20 RPS)${NC}"
    fi
    
    echo ""
}

# Analyze each scenario
analyze_csv "test-results/load/baseline_stats.csv" "📈 Baseline (10 users)"
analyze_csv "test-results/load/normal-load_stats.csv" "📈 Normal Load (25 users)"
analyze_csv "test-results/load/peak-load_stats.csv" "📈 Peak Load (50 users)"
analyze_csv "test-results/load/stress-test_stats.csv" "📈 Stress Test (100 users)"

# Check for bottlenecks
echo "════════════════════════════════════════"
echo "🔍 BOTTLENECK ANALYSIS"
echo "════════════════════════════════════════"
echo ""

print_info "Checking Docker resource usage..."
docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}" | grep oddiya

echo ""
print_info "Checking database connections..."
DB_CONNECTIONS=$(docker exec oddiya-postgres psql -U oddiya_user -d oddiya -t -c \
    "SELECT count(*) FROM pg_stat_activity WHERE datname='oddiya';" 2>/dev/null | xargs)
echo "Active PostgreSQL connections: $DB_CONNECTIONS"

if [ "$DB_CONNECTIONS" -gt 50 ]; then
    echo -e "${RED}⚠️  High connection count! May hit t2.micro limits${NC}"
fi

echo ""
print_info "Checking Redis memory..."
REDIS_MEMORY=$(docker exec oddiya-redis redis-cli info memory | grep used_memory_human | cut -d: -f2 | tr -d '\r')
echo "Redis memory usage: $REDIS_MEMORY"

echo ""
echo "════════════════════════════════════════"
echo "📝 RECOMMENDATIONS"
echo "════════════════════════════════════════"
echo ""

# Generate recommendations
if [ -f "test-results/load/stress-test_stats.csv" ]; then
    stress_failures=$(tail -n 1 test-results/load/stress-test_stats.csv | awk -F, '{print $4}')
    stress_total=$(tail -n 1 test-results/load/stress-test_stats.csv | awk -F, '{print $3}')
    
    if [ "$stress_failures" -gt "$(($stress_total / 10))" ]; then
        echo "⚠️  High failure rate under stress:"
        echo "   - Consider increasing database connection pool"
        echo "   - Monitor t2.micro resource limits"
        echo "   - Add Redis caching for frequent queries"
    fi
fi

echo "💡 General Recommendations:"
echo "   1. Review HTML reports in test-results/load/"
echo "   2. Identify slowest endpoints"
echo "   3. Monitor database query performance"
echo "   4. Consider caching strategies"
echo "   5. Document t2.micro limitations"
echo ""

print_success "Performance analysis complete!"
echo ""
echo "📁 Full reports: test-results/load/*.html"
echo ""

