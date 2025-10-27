# Local Testing Guide

Complete guide for testing Oddiya services locally before deployment.

## Prerequisites

- Docker & Docker Compose
- Java 21
- Python 3.11+
- Git

## Quick Start

```bash
# 1. Run all tests
./scripts/test-local.sh

# 2. Start all services
./scripts/start-local.sh

# 3. Access services
curl http://localhost:8080/actuator/health
```

## Detailed Testing Process

### Phase 1: Unit Tests

Test each service independently:

```bash
# Java services
cd services/auth-service && ./gradlew test
cd services/api-gateway && ./gradlew test
cd services/user-service && ./gradlew test
cd services/plan-service && ./gradlew test
cd services/video-service && ./gradlew test

# Python services
cd services/llm-agent && pytest
cd services/video-worker && pytest
```

### Phase 2: Infrastructure

Start PostgreSQL and Redis:

```bash
docker-compose up -d postgres redis

# Verify PostgreSQL
docker exec oddiya-postgres psql -U oddiya_user -d oddiya -c "\dn"

# Verify Redis
docker exec oddiya-redis redis-cli ping
```

### Phase 3: Service Integration

Start services one by one:

```bash
# 1. Infrastructure (already running)
# 2. User Service (no dependencies)
cd services/user-service && ./gradlew bootRun

# 3. Auth Service (depends on User Service + Redis)
cd services/auth-service && ./gradlew bootRun

# 4. LLM Agent (depends on Redis)
cd services/llm-agent && uvicorn main:app --reload

# 5. Plan Service (depends on LLM Agent)
cd services/plan-service && ./gradlew bootRun

# 6. Video Service (depends on PostgreSQL)
cd services/video-service && ./gradlew bootRun

# 7. API Gateway (depends on all services)
cd services/api-gateway && ./gradlew bootRun
```

### Phase 4: End-to-End Testing

Test complete user flows:

#### 1. Health Checks

```bash
# All services should return 200 OK
curl http://localhost:8081/actuator/health  # Auth
curl http://localhost:8082/actuator/health  # User
curl http://localhost:8083/actuator/health  # Plan
curl http://localhost:8084/actuator/health  # Video
curl http://localhost:8000/health          # LLM Agent
curl http://localhost:8080/actuator/health  # Gateway
```

#### 2. Database Connectivity

```bash
# Check tables exist
docker exec oddiya-postgres psql -U oddiya_user -d oddiya -c "
  SELECT schemaname, tablename 
  FROM pg_tables 
  WHERE schemaname IN ('user_service', 'plan_service', 'video_service');
"
```

#### 3. Redis Connectivity

```bash
# Test Redis from Auth Service
docker exec oddiya-redis redis-cli KEYS "*"
```

#### 4. Service Communication

```bash
# Test User Service internal API (used by Auth)
curl -X POST http://localhost:8082/api/v1/users/internal/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "name": "Test User",
    "provider": "google",
    "providerId": "123456"
  }'

# Test LLM Agent (used by Plan Service)
curl -X POST http://localhost:8000/api/v1/plans/generate \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Seoul Trip",
    "startDate": "2025-12-01",
    "endDate": "2025-12-03"
  }'
```

## Docker Compose Testing

### Build and run all services:

```bash
# Build all images
docker-compose -f docker-compose.local.yml build

# Start all services
docker-compose -f docker-compose.local.yml up -d

# View logs
docker-compose logs -f

# Check service status
docker-compose ps
```

### Test service networking:

```bash
# All services should be on the same network
docker network inspect oddiya_oddiya-network

# Test internal DNS resolution
docker exec oddiya-api-gateway ping postgres
docker exec oddiya-api-gateway ping redis
docker exec oddiya-api-gateway ping auth-service
```

## Common Issues & Solutions

### Issue: Port already in use

```bash
# Find process using port
lsof -ti:8080
lsof -ti:5432

# Kill process
kill -9 $(lsof -ti:8080)
```

### Issue: PostgreSQL connection refused

```bash
# Check if PostgreSQL is running
docker ps | grep postgres

# Check PostgreSQL logs
docker logs oddiya-postgres

# Restart PostgreSQL
docker-compose restart postgres
```

### Issue: Gradle build fails

```bash
# Clean and rebuild
cd services/auth-service
./gradlew clean build --refresh-dependencies
```

### Issue: Docker build fails

```bash
# Remove old images
docker system prune -a

# Rebuild from scratch
docker-compose -f docker-compose.local.yml build --no-cache
```

## Performance Testing

### Load test with Apache Bench:

```bash
# Install Apache Bench
sudo apt-get install apache2-utils  # Ubuntu
brew install httpd  # macOS

# Test API Gateway
ab -n 1000 -c 10 http://localhost:8080/actuator/health

# Test Plan Service
ab -n 100 -c 5 -p plan.json -T application/json \
  -H "X-User-Id: 1" \
  http://localhost:8083/api/v1/plans
```

### Monitor resource usage:

```bash
# Docker stats
docker stats

# Service-specific logs
docker logs -f oddiya-auth-service
docker logs -f oddiya-plan-service
```

## Pre-Deployment Checklist

- [ ] All unit tests pass
- [ ] All integration tests pass
- [ ] Services start without errors
- [ ] Health checks return 200 OK
- [ ] Database schemas created correctly
- [ ] Redis connection working
- [ ] Service-to-service communication works
- [ ] API Gateway routes all requests correctly
- [ ] Docker images build successfully
- [ ] No port conflicts
- [ ] Environment variables set correctly
- [ ] Logs show no errors

## Automated Testing Script

Use the comprehensive test script:

```bash
chmod +x scripts/test-local.sh
./scripts/test-local.sh
```

This script will:
1. Check dependencies
2. Run all unit tests
3. Start infrastructure
4. Verify database schemas
5. Check service health
6. Report results

## Next Steps

After successful local testing:

1. Push to GitHub
2. CI/CD pipeline will run
3. Review GitHub Actions results
4. Deploy to staging environment
5. Run production smoke tests

## Troubleshooting

### View all container logs:

```bash
docker-compose logs --tail=100 -f
```

### Restart a specific service:

```bash
docker-compose restart auth-service
```

### Rebuild a specific service:

```bash
docker-compose build auth-service
docker-compose up -d auth-service
```

### Access service shell:

```bash
docker exec -it oddiya-auth-service sh
```

### Database debugging:

```bash
# Access PostgreSQL
docker exec -it oddiya-postgres psql -U oddiya_user -d oddiya

# Run SQL queries
SELECT * FROM user_service.users LIMIT 5;
SELECT * FROM plan_service.travel_plans LIMIT 5;
SELECT * FROM video_service.video_jobs LIMIT 5;
```

## Resources

- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot Testing](https://spring.io/guides/gs/testing-web/)
- [pytest Documentation](https://docs.pytest.org/)
- [Project README](../../README.md)

