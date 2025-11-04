# Getting Started with Oddiya

Quick guide to start developing locally.

## Prerequisites

- **Docker Desktop** installed and running
- **Java 21** (for Spring Boot services)
- **Python 3.11** (for FastAPI services)
- **Git** configured

## Quick Start (5 Minutes)

### 1. Clone Repository

```bash
git clone https://github.com/sw6820/oddiya.git
cd oddiya
```

### 2. Set Up Local Environment

```bash
# Copy environment template
cp env.local.example .env.local

# Edit with your API keys (optional for local dev)
vim .env.local
```

### 3. Start Local Infrastructure

```bash
# Start PostgreSQL and Redis
docker-compose up -d

# Verify services are running
docker-compose ps

# Check PostgreSQL
docker-compose exec postgres psql -U oddiya_user -d oddiya -c "\dn"

# Check Redis
docker-compose exec redis redis-cli ping
```

### 4. Run Services

#### Auth Service
```bash
cd services/auth-service
./gradlew bootRun
# Runs on http://localhost:8081
```

#### API Gateway
```bash
cd services/api-gateway
./gradlew bootRun
# Runs on http://localhost:8080
```

#### LLM Agent
```bash
cd services/llm-agent
pip install -r requirements.txt
uvicorn main:app --reload
# Runs on http://localhost:8000
```

### 5. Test Endpoints

```bash
# Health check
curl http://localhost:8080/health

# Auth service
curl http://localhost:8081/health

# LLM Agent
curl http://localhost:8000/health
```

## Development Workflow

### Running Tests

```bash
# Auth Service
cd services/auth-service && ./gradlew test

# LLM Agent
cd services/llm-agent && pytest
```

### Database Migrations

Migrations run automatically on first start. Manual:
```bash
# Connect to database
docker-compose exec postgres psql -U oddiya_user -d oddiya

# Run SQL manually if needed
```

### Stopping Services

```bash
# Stop containers
docker-compose stop

# Remove containers and data
docker-compose down -v
```

## Next Steps

- Read [Development Plan](plan.md) for detailed roadmap
- Review [Architecture Overview](../architecture/overview.md)
- Set up [CI/CD](../deployment/github-actions.md)

## Troubleshooting

### Port Already in Use
```bash
# Find process using port
lsof -i :8080

# Kill process or change port in application.yml
```

### Docker Issues
```bash
# Check Docker is running
docker info

# Restart Docker Desktop if needed
```

## Resources

- [Architecture Documentation](../architecture/overview.md)
- [Testing Guide](testing.md)
- [Deployment Guide](../deployment/infrastructure.md)

