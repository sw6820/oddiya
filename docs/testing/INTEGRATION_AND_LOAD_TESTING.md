# Integration and Load Testing Guide

Complete guide for validating Oddiya before AWS deployment.

## Overview

**Option 3: Test First** strategy ensures:
- All services work together correctly
- Performance baselines are established
- Bottlenecks are identified before cloud costs
- Confidence in deployment

## Test Suite Overview

### Integration Tests
- **Java Services:** Testcontainers with PostgreSQL/Redis
- **Python Services:** pytest with mocked dependencies
- **End-to-End:** Full user flows across all services

### Load Tests
- **Tool:** Locust
- **Scenarios:** 4 levels (10, 25, 50, 100 users)
- **Metrics:** Response time, RPS, failure rate

## Running Tests

### Complete Test Flow

```bash
# 1. Run integration tests (validates correctness)
./scripts/run-integration-tests.sh

# 2. Run load tests (validates performance)
./scripts/run-load-tests.sh

# 3. Analyze results (identifies bottlenecks)
./scripts/analyze-performance.sh
```

## Expected Results

### Integration Tests

**All services should:**
- ✅ Connect to PostgreSQL successfully
- ✅ Connect to Redis successfully
- ✅ Handle CRUD operations correctly
- ✅ Communicate with other services
- ✅ Pass validation checks
- ✅ Handle errors gracefully

### Load Test Targets

#### With t2.micro Constraints

| Scenario | Users | Target RPS | Avg Response | Failure Rate |
|----------|-------|------------|--------------|--------------|
| Baseline | 10 | 5-10 | < 500ms | < 1% |
| Normal | 25 | 10-20 | < 1000ms | < 1% |
| Peak | 50 | 15-30 | < 1500ms | < 5% |
| Stress | 100 | 20-40 | < 2000ms | < 10% |

**Note:** These are realistic targets given t2.micro limitations (1GB RAM, 1 vCPU)

## Known Bottlenecks

### 1. PostgreSQL (t2.micro) - EXPECTED BOTTLENECK ⚠️

**Limitations:**
- 1GB RAM
- 1 vCPU
- ~100 max connections (default)
- Slow disk I/O

**Expected Issues:**
- Connection pool exhaustion at >30-40 concurrent users
- Slow query performance under load
- Memory pressure with complex joins

**Mitigation:**
- Keep connection pools small (max 10 per service)
- Add Redis caching for frequent queries
- Use simple queries, avoid complex joins
- Consider connection pooling proxy (PgBouncer)

### 2. Redis (t2.micro) - MINOR BOTTLENECK ⚠️

**Limitations:**
- 1GB RAM
- No persistence overhead for cache-only usage

**Expected Issues:**
- Memory eviction when cache grows >800MB
- Slower performance with large datasets

**Mitigation:**
- Set maxmemory-policy to allkeys-lru
- Monitor memory usage
- TTL all cache entries

### 3. LLM Agent - BEDROCK API LIMITS ⚠️

**Limitations:**
- AWS Bedrock rate limits
- Network latency (50-200ms)
- API costs per request

**Expected Issues:**
- Throttling errors at high request rates
- Increased costs during load tests

**Mitigation:**
- Use MOCK_MODE=true for load testing
- Implement Redis caching (1hr TTL)
- Retry logic with exponential backoff

## Test Scenarios Explained

### Scenario 1: Baseline (10 users)

**Purpose:** Establish performance baseline without stress

**User Behavior:**
- 10 concurrent users
- Mixed operations (read-heavy)
- 1-3 second wait between requests

**Success Criteria:**
- All requests succeed (>99%)
- Response times < 500ms average
- No error logs

### Scenario 2: Normal Load (25 users)

**Purpose:** Simulate typical daily usage

**User Behavior:**
- 25 concurrent users
- Realistic operation mix
- Normal think time

**Success Criteria:**
- Failure rate < 1%
- Response times < 1000ms average
- Stable resource usage

### Scenario 3: Peak Load (50 users)

**Purpose:** Simulate peak hours (weekends, holidays)

**User Behavior:**
- 50 concurrent users
- High activity
- Shorter think time

**Success Criteria:**
- Failure rate < 5%
- Response times < 1500ms average
- Services remain stable

### Scenario 4: Stress Test (100 users)

**Purpose:** Find breaking points and limits

**User Behavior:**
- 100 concurrent users
- Aggressive load
- Minimal think time

**Expected:**
- Some failures (< 10%)
- Degraded performance
- Identifies limits

**NOT A FAILURE IF:**
- t2.micro CPU/memory maxes out
- Connection pool exhausts
- Response times increase significantly

**This is documentation, not a bug!**

## Interpreting Results

### Good Results ✅

```
Baseline (10 users):
  Requests: 1000
  Failures: 2 (0.2%)
  Avg Response: 250ms
  RPS: 16

Normal Load (25 users):
  Requests: 2500
  Failures: 15 (0.6%)
  Avg Response: 650ms
  RPS: 20
```

### Concerning Results ⚠️

```
Peak Load (50 users):
  Requests: 3000
  Failures: 300 (10%)
  Avg Response: 2500ms
  RPS: 10
```

**Action:** Investigate logs, check database connections

### Expected Results with t2.micro

```
Stress Test (100 users):
  Requests: 2000
  Failures: 400 (20%)
  Avg Response: 5000ms
  RPS: 6

Database Connections: 95/100 (maxed out)
PostgreSQL CPU: 90%+
Redis Memory: 600MB
```

**This is EXPECTED with t2.micro constraints!**

## Bottleneck Documentation

### Create Performance Report

After running tests:

```bash
# Generate comprehensive report
./scripts/analyze-performance.sh > test-results/PERFORMANCE_REPORT.md

# Add to git
git add test-results/
git commit -m "docs: add performance test results"
```

### Document Bottlenecks

Expected findings:

**1. Database Connection Limit**
- Breaking point: ~40-50 concurrent users
- Symptom: "Too many connections" errors
- Solution: PgBouncer or larger instance (t3.medium)

**2. Query Performance**
- Breaking point: Complex queries >1000ms
- Symptom: Slow response times
- Solution: Add indexes, simplify queries

**3. Memory Pressure**
- Breaking point: Heavy load sustained >5 minutes
- Symptom: OOM errors, container restarts
- Solution: Larger instances or optimize memory usage

## Pre-Deployment Checklist

- [ ] Integration tests pass (100%)
- [ ] Load baseline test passes
- [ ] Normal load test acceptable
- [ ] Peak load limits documented
- [ ] Stress test results documented
- [ ] Bottlenecks identified and recorded
- [ ] Performance report created
- [ ] Monitoring plan established
- [ ] Scaling strategy defined

## After Testing

### If Tests Pass

```bash
# Document results
./scripts/analyze-performance.sh > docs/testing/PERFORMANCE_BASELINE.md

# Proceed to deployment
# See: docs/deployment/infrastructure.md
```

### If Tests Fail

1. Review failure logs
2. Fix issues in code
3. Re-run tests
4. Don't deploy until passing

### If Performance is Poor

1. Identify specific bottlenecks
2. Implement optimizations:
   - Add database indexes
   - Increase connection pools (carefully)
   - Add Redis caching
   - Optimize queries
3. Re-run load tests
4. Compare before/after

## Continuous Testing

### In CI/CD

Integration tests run automatically on every push.

### Before Each Deploy

```bash
# Always run full test suite
./scripts/test-local.sh            # Unit tests
./scripts/run-integration-tests.sh # Integration tests
./scripts/run-load-tests.sh        # Load tests (optional)
```

## Resources

- [Testcontainers Documentation](https://www.testcontainers.org/)
- [Locust Documentation](https://docs.locust.io/)
- [Load Testing Best Practices](https://docs.locust.io/en/stable/writing-a-locustfile.html)
- [PostgreSQL Performance Tuning](https://wiki.postgresql.org/wiki/Performance_Optimization)

## Summary

**Test First Approach:**

1. ✅ Integration tests validate correctness
2. ✅ Load tests validate performance
3. ✅ Analysis identifies bottlenecks
4. ✅ Document limitations (t2.micro)
5. ✅ Deploy with confidence

**Expected outcome:**
- Know your limits before AWS costs
- Document performance characteristics
- Plan scaling strategy
- Deploy successfully!

---

**Next:** Run the tests and document results before deploying to AWS.

