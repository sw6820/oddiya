# Load Testing with Locust

Performance and load testing for Oddiya services.

## Installation

```bash
pip install locust
```

## Running Tests

### Basic Load Test

```bash
# Start services first
cd ../..
./scripts/start-for-mobile-testing.sh

# Run Locust
cd tests/load
locust -f locustfile.py --host=http://localhost:8080
```

Open browser: http://localhost:8089

### Test Scenarios

#### Scenario 1: Normal Usage
- **Users:** 10
- **Spawn rate:** 1/second
- **Duration:** 5 minutes
- **Purpose:** Baseline performance

#### Scenario 2: Peak Load
- **Users:** 50
- **Spawn rate:** 5/second
- **Duration:** 10 minutes
- **Purpose:** Test under load

#### Scenario 3: Stress Test
- **Users:** 100
- **Spawn rate:** 10/second
- **Duration:** 5 minutes
- **Purpose:** Find breaking point

### Command Line (No UI)

```bash
# Run with 10 users for 1 minute
locust -f locustfile.py \
    --host=http://localhost:8080 \
    --users 10 \
    --spawn-rate 1 \
    --run-time 1m \
    --headless

# Run specific user class
locust -f locustfile.py \
    --host=http://localhost:8080 \
    --users 20 \
    --spawn-rate 2 \
    --run-time 5m \
    --headless \
    OddiyaUser
```

### Test Results

Locust will show:
- **RPS** (Requests per second)
- **Response times** (min, max, avg, median)
- **Failure rate** (%)
- **Requests per endpoint**

### Expected Performance

#### Target Metrics (t2.micro constraints)

| Metric | Target | Notes |
|--------|--------|-------|
| Avg Response Time | < 500ms | User-facing endpoints |
| 95th Percentile | < 1000ms | Most requests |
| 99th Percentile | < 2000ms | Edge cases |
| RPS (total) | 50-100 | Limited by t2.micro |
| Failure Rate | < 1% | Error tolerance |

#### Expected Bottlenecks

1. **PostgreSQL (t2.micro)** - 1GB RAM
   - Connection pool exhaustion
   - Slow query performance
   - Limited concurrent connections

2. **Redis (t2.micro)** - 1GB RAM
   - Memory limits
   - Eviction policies triggered

3. **LLM Agent**
   - Bedrock API rate limits
   - Network latency to AWS

### Monitoring During Tests

```bash
# Terminal 1: Locust
locust -f locustfile.py --host=http://localhost:8080

# Terminal 2: Docker stats
docker stats

# Terminal 3: Service logs
docker-compose logs -f

# Terminal 4: Database connections
docker exec oddiya-postgres psql -U oddiya_user -d oddiya -c \
    "SELECT count(*) FROM pg_stat_activity WHERE datname='oddiya';"
```

### Test Checklist

- [ ] Health endpoints respond quickly
- [ ] User profile operations work under load
- [ ] Plan creation handles concurrency
- [ ] Database doesn't run out of connections
- [ ] Redis cache reduces load
- [ ] No memory leaks in services
- [ ] Error rates stay below 1%
- [ ] Response times within targets
- [ ] Services recover from errors
- [ ] Idempotency works correctly

### Analyzing Results

#### Good Signs
- ✅ Consistent response times
- ✅ Low failure rate
- ✅ Stable under load
- ✅ Quick recovery

#### Red Flags
- ❌ Response times increasing
- ❌ High failure rates
- ❌ Memory continuously growing
- ❌ Database connection errors

### Next Steps

1. Run baseline test (10 users)
2. Gradually increase load
3. Document breaking points
4. Identify bottlenecks
5. Optimize if needed
6. Repeat tests

### Common Issues

**Issue: Connection refused**
```bash
# Check services are running
docker-compose ps
```

**Issue: High failure rate**
```bash
# Check service logs
docker-compose logs auth-service
```

**Issue: Slow responses**
```bash
# Monitor database
docker exec oddiya-postgres psql -U oddiya_user -d oddiya -c \
    "SELECT pid, query, state FROM pg_stat_activity WHERE state='active';"
```

## Resources

- [Locust Documentation](https://docs.locust.io/)
- [Load Testing Best Practices](https://locust.io/docs/latest/writing-a-locustfile.html)

