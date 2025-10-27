# How to Run Tests on Your Mac

## ⚠️ Docker Desktop Required

Tests cannot run without Docker Desktop.

### Step 1: Start Docker Desktop

1. **Open Finder**
2. **Go to Applications folder**
3. **Double-click "Docker"**
4. **Wait for Docker icon in menu bar to show "Docker Desktop is running"**

Or from Terminal:
```bash
open -a Docker
```

### Step 2: Verify Docker is Running

```bash
docker info
```

Should show: "Server Version: 28.x.x" (not "Cannot connect to daemon")

### Step 3: Run Integration Tests

```bash
cd /Users/wjs/cursor/oddiya
./scripts/run-integration-tests.sh
```

### Step 4: Run Load Tests

```bash
./scripts/run-load-tests.sh
```

### Step 5: View Results

```bash
./scripts/analyze-performance.sh
open test-results/load/baseline-report.html
```

---

## Alternative: Manual Testing

If you can't run Docker, test manually:

### Python Tests (No Docker needed)

```bash
# LLM Agent
cd services/llm-agent
pip3 install -r requirements.txt
python3 -m pytest tests/test_bedrock_service.py -v

# Video Worker
cd services/video-worker
pip3 install -r requirements.txt
python3 -m pytest tests/test_video_generator.py -v
```

### Java Tests (Need gradlew setup)

```bash
cd services/auth-service
gradle wrapper
./gradlew test
```

---

## What Tests Will Show

When Docker is running, tests will:

✅ Validate all services work correctly
✅ Test database connectivity
✅ Test service communication
✅ Measure performance (response times, throughput)
✅ Identify bottlenecks
✅ Document t2.micro limitations

Results saved to: `test-results/`

---

**Current Status:** Docker daemon is not running  
**Action Required:** Start Docker Desktop first
