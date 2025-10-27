# How to Run Tests - Step by Step

Complete guide to running integration and load tests on your Mac.

## Prerequisites Check

### 1. Start Docker Desktop

```bash
# Check if Docker is running
docker info

# If not running:
# 1. Open Docker Desktop app from Applications
# 2. Wait for "Docker Desktop is running" message
# 3. Verify: docker info
```

### 2. Install Required Tools

```bash
# Install Locust for load testing
pip install locust

# Install pytest for Python tests
pip install pytest requests

# Verify installations
locust --version
pytest --version
```

## Running Tests - Manual Steps

### Step 1: Start Infrastructure

```bash
cd /Users/wjs/cursor/oddiya

# Start PostgreSQL and Redis
docker-compose up -d postgres redis

# Wait 10 seconds
sleep 10

# Verify database
docker exec oddiya-postgres pg_isready -U oddiya_user

# Verify Redis
docker exec oddiya-redis redis-cli ping
# Should output: PONG
```

### Step 2: Run Unit Tests (Quick)

```bash
# Python services (these will work without Docker)
cd services/llm-agent
pip install -r requirements.txt
pytest tests/test_bedrock_service.py -v

cd ../video-worker
pip install -r requirements.txt
pytest tests/test_video_generator.py -v

cd ../..
```

### Step 3: Run Integration Tests

**Note:** Java services need Gradle wrapper setup first.

```bash
# For each Java service, initialize Gradle wrapper
cd services/auth-service
gradle wrapper  # This creates gradlew
chmod +x gradlew

# Then run tests
./gradlew test --tests "*Integration*"

# Repeat for other services:
# user-service, plan-service, video-service
```

### Step 4: Start All Services

```bash
# Use Docker Compose
docker-compose -f docker-compose.local.yml up -d

# Or start individually (for development)
# Terminal 1: User Service
cd services/user-service && ./gradlew bootRun

# Terminal 2: Auth Service  
cd services/auth-service && ./gradlew bootRun

# Terminal 3: LLM Agent
cd services/llm-agent && uvicorn main:app --reload

# Terminal 4: Plan Service
cd services/plan-service && ./gradlew bootRun

# Terminal 5: Video Service
cd services/video-service && ./gradlew bootRun

# Terminal 6: API Gateway
cd services/api-gateway && ./gradlew bootRun
```

### Step 5: Run End-to-End Tests

```bash
# Wait for all services to start (30 seconds)
sleep 30

# Test health endpoints
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
curl http://localhost:8084/actuator/health
curl http://localhost:8000/health

# Run E2E tests
cd tests/integration
pip install -r requirements.txt
pytest test_end_to_end.py -v
```

### Step 6: Run Load Tests

```bash
# Install Locust
pip install locust

# Run baseline test (10 users, 1 minute)
cd tests/load
locust -f locustfile.py \
    --host=http://localhost:8080 \
    --users 10 \
    --spawn-rate 1 \
    --run-time 1m \
    --headless

# Or use automated script
cd /Users/wjs/cursor/oddiya
./scripts/run-load-tests.sh
```

## What Each Test Does

### Integration Tests

**Auth Service:**
- Connects to Redis (Testcontainers)
- Tests refresh token flow
- Validates JWT generation

**User Service:**
- Connects to PostgreSQL (Testcontainers)
- Tests user CRUD operations
- Tests internal API for Auth Service

**Plan Service:**
- Connects to PostgreSQL (Testcontainers)
- Tests plan CRUD operations
- Tests database relationships

**LLM Agent:**
- Tests with mock Bedrock responses
- Tests concurrent requests
- Tests caching functionality

**End-to-End:**
- Tests complete user flow
- Tests service-to-service communication
- Tests API Gateway routing

### Load Tests

**4 Scenarios:**

1. **Baseline (10 users)** - Establish normal performance
2. **Normal Load (25 users)** - Typical daily usage
3. **Peak Load (50 users)** - Weekend/holiday traffic
4. **Stress Test (100 users)** - Find breaking points

**Metrics Collected:**
- Requests per second (RPS)
- Average response time
- 95th/99th percentile
- Failure rate
- Resource usage

## Expected Output

### Integration Tests - Success

```
🧪 RUNNING INTEGRATION TESTS
==============================

☕ JAVA INTEGRATION TESTS
════════════════════════════════════════

✅ auth-service integration tests passed
✅ user-service integration tests passed
✅ plan-service integration tests passed
✅ video-service integration tests passed

🐍 PYTHON INTEGRATION TESTS
════════════════════════════════════════

✅ LLM Agent integration tests passed

🔄 END-TO-END TESTS
════════════════════════════════════════

✅ End-to-end tests passed

📊 INTEGRATION TEST SUMMARY
════════════════════════════════════════

Java Services:
  Passed: 4 / 4
  Failed: 0 / 4

Python Services:
  Passed: 1 / 1
  Failed: 0 / 1

End-to-End:
  ✅ Passed

✅ ALL INTEGRATION TESTS PASSED! (5/5)

✅ Ready for load testing!
   Run: ./scripts/run-load-tests.sh
```

### Load Tests - Expected Results

```
📊 RUNNING LOAD TESTS
======================

🧪 Scenario 1: Baseline Performance
Users: 10, Duration: 1 minute

[2025-01-27 10:00:00] Starting Locust...
[2025-01-27 10:01:00] Stopping Locust...

Type     Name                          # reqs   # fails  Avg   Min   Max  Median  req/s
------------------------------------------------------------------------
GET      /actuator/health               100      0      45ms   20ms  120ms   40ms   1.67
GET      /api/users/me                  200      0      85ms   45ms  250ms   75ms   3.33
GET      /api/plans                     150      1     120ms   60ms  450ms  110ms   2.50
POST     /api/plans                      50      0     350ms  180ms  800ms  320ms   0.83
------------------------------------------------------------------------
Aggregated                              500      1      125ms  20ms  800ms  90ms    8.33

✅ Baseline test complete

🧪 Scenario 2: Normal Load
Users: 25, Duration: 2 minutes
[Results similar but with higher load...]

📈 LOAD TEST RESULTS
════════════════════════════════════════

Baseline Performance:
  Requests: 500, Failures: 1 (0.2%), Avg: 125ms, Max: 800ms, RPS: 8

Normal Load:
  Requests: 1200, Failures: 5 (0.4%), Avg: 280ms, Max: 1500ms, RPS: 10

Peak Load:
  Requests: 1800, Failures: 45 (2.5%), Avg: 650ms, Max: 3000ms, RPS: 15

Stress Test:
  Requests: 1500, Failures: 300 (20%), Avg: 2500ms, Max: 8000ms, RPS: 12
  ⚠️  Connection pool exhausted
  ⚠️  PostgreSQL at 95% CPU
  ⚠️  t2.micro RAM limit reached

✅ LOAD TESTING COMPLETE!
```

## Troubleshooting - Docker Not Running

**Your current issue:** Docker daemon is not running

**Solution:**

```bash
# 1. Open Docker Desktop
open -a Docker

# 2. Wait for it to start (30-60 seconds)

# 3. Verify
docker info

# 4. Then run tests
./scripts/run-integration-tests.sh
```

## Quick Demo (Without Full Tests)

Since Docker isn't running, let me show you what you can test right now:

```bash
# Test Python services (no Docker needed)
cd services/llm-agent
pip install -r requirements.txt
pytest tests/ -v

cd ../video-worker  
pip install -r requirements.txt
pytest tests/ -v
```

Would you like me to:
1. Create a detailed walkthrough for when Docker is running?
2. Show you the test code structure?
3. Create mock test results as examples?

