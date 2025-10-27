# Quick Reference Guide

One-page reference for common Oddiya operations.

## 🚀 Starting Services

```bash
# Start all services for mobile testing
./scripts/start-for-mobile-testing.sh

# Start with specific environment
./scripts/run-with-env.sh local|staging|production

# Start only infrastructure (PostgreSQL + Redis)
docker-compose up -d postgres redis
```

## 🧪 Testing

```bash
# Run all tests locally
./scripts/test-local.sh

# Test mobile APIs
./scripts/test-mobile-api.sh

# Test connection from mobile
./scripts/test-mobile-connection.sh

# Validate configuration
./scripts/validate-env.sh .env.local
```

## 📱 Mobile Development

```bash
# Get your Mac's IP for mobile
./scripts/get-local-ip.sh

# iOS Simulator: http://localhost:8080
# Android Emulator: http://10.0.2.2:8080
# Physical Device: http://[YOUR_IP]:8080
```

## 🔧 Configuration

```bash
# Create from template
cp env.example .env.local

# Load environment
source ./scripts/load-env.sh local

# Validate before deploy
./scripts/validate-env.sh .env.production
```

## 📊 Monitoring

```bash
# View all logs
docker-compose logs -f

# View specific service
docker-compose logs -f auth-service

# Check service status
docker-compose ps

# Check health
curl http://localhost:8080/actuator/health
```

## 🛑 Stopping Services

```bash
# Stop all services
./scripts/stop-local.sh

# Or manually
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

## 🗄️ Database

```bash
# Access PostgreSQL
docker exec -it oddiya-postgres psql -U oddiya_user -d oddiya

# View schemas
\dn

# View tables
\dt user_service.*
\dt plan_service.*
\dt video_service.*

# Exit
\q
```

## 🔴 Redis

```bash
# Access Redis CLI
docker exec -it oddiya-redis redis-cli

# Check connection
ping

# View all keys
keys *

# Exit
exit
```

## 🐛 Troubleshooting

```bash
# Restart specific service
docker-compose restart auth-service

# Rebuild service
docker-compose build auth-service
docker-compose up -d auth-service

# View errors
docker-compose logs --tail=50 auth-service

# Check ports
lsof -i :8080
lsof -i :5432

# Clean everything
docker-compose down -v
docker system prune -a
```

## 📦 Building Services

```bash
# Build Java service
cd services/auth-service
./gradlew clean build

# Build Python service
cd services/llm-agent
pip install -r requirements.txt

# Build Docker image
docker build -t oddiya/auth-service:latest .
```

## 🔐 Common API Calls

```bash
# Health check
curl http://localhost:8080/actuator/health

# Create user (internal)
curl -X POST http://localhost:8082/api/v1/users/internal/users \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","name":"Test","provider":"google","providerId":"123"}'

# Get user profile
curl http://localhost:8080/api/users/me \
  -H "X-User-Id: 1"

# Create travel plan
curl -X POST http://localhost:8080/api/plans \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -d '{"title":"Seoul Trip","startDate":"2025-12-01","endDate":"2025-12-03"}'

# Get plans
curl http://localhost:8080/api/plans \
  -H "X-User-Id: 1"

# Create video job
curl -X POST http://localhost:8080/api/videos \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -H "Idempotency-Key: $(uuidgen)" \
  -d '{"photoUrls":["photo1.jpg","photo2.jpg"],"template":"default"}'
```

## 📋 Service Ports

| Service | Port | Health Check |
|---------|------|--------------|
| API Gateway | 8080 | http://localhost:8080/actuator/health |
| Auth Service | 8081 | http://localhost:8081/actuator/health |
| User Service | 8082 | http://localhost:8082/actuator/health |
| Plan Service | 8083 | http://localhost:8083/actuator/health |
| Video Service | 8084 | http://localhost:8084/actuator/health |
| LLM Agent | 8000 | http://localhost:8000/health |
| PostgreSQL | 5432 | - |
| Redis | 6379 | - |

## 🔗 Useful Links

- **GitHub:** https://github.com/sw6820/oddiya
- **Actions:** https://github.com/sw6820/oddiya/actions
- **Docs:** [docs/README.md](../README.md)

## 📝 Environment Variables

```bash
# Required for all environments
DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD
REDIS_HOST, REDIS_PORT
AWS_REGION, S3_BUCKET

# Required for production
AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY
GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET
KAKAO_LOCAL_API_KEY

# Optional
MOCK_MODE (true for development)
LOG_LEVEL (DEBUG for development)
```

## 🎯 Next Steps

- [Mobile API Testing](../api/MOBILE_API_TESTING.md)
- [Configuration Management](CONFIGURATION_MANAGEMENT.md)
- [Local Testing Guide](LOCAL_TESTING.md)
- [Development Plan](plan.md)

