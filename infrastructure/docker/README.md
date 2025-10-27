# Docker Infrastructure Setup

This directory contains Docker Compose configuration and database initialization scripts for local development.

## Quick Start

### Prerequisites
- Docker Desktop installed and running
- Docker Compose (included with Docker Desktop)

### One-Command Setup

```bash
# From project root
./scripts/local-setup.sh
```

This script will:
1. Check Docker is running
2. Create `.env.local` from template if it doesn't exist
3. Start PostgreSQL and Redis containers
4. Wait for services to be ready
5. Verify database schemas were created

### Manual Setup

If you prefer manual setup:

```bash
# 1. Copy environment template
cp env.local.example .env.local

# 2. Edit .env.local with your API keys
vim .env.local

# 3. Start containers
docker-compose up -d

# 4. Check logs
docker-compose logs -f

# 5. Verify services
docker-compose ps
```

## Database Configuration

### PostgreSQL 17.0
- **Host:** localhost
- **Port:** 5432
- **Database:** oddiya
- **User:** oddiya_user
- **Password:** oddiya_password_dev (change in .env.local for security)

### Redis 7.4
- **Host:** localhost
- **Port:** 6379

### Schema-per-Service Model

The database uses a schema-per-service pattern:

- `user_service` - User profile data
- `plan_service` - Travel plans and plan details
- `video_service` - Video job management

### Database Initialization

SQL scripts in `postgres-init/` run automatically on first container start:

1. `01-init-database.sql` - Creates schemas and grants permissions
2. `02-user-service-schema.sql` - User service tables
3. `03-plan-service-schema.sql` - Plan service tables
4. `04-video-service-schema.sql` - Video service tables

**Note:** These scripts only run if PostgreSQL data volume is empty (first time).

## Common Commands

```bash
# View container status
docker-compose ps

# View logs
docker-compose logs -f
docker-compose logs -f postgres
docker-compose logs -f redis

# Stop containers
docker-compose stop

# Start containers
docker-compose start

# Restart containers
docker-compose restart

# Remove containers and volumes (CAUTION: deletes all data)
docker-compose down -v

# Execute SQL commands
docker-compose exec postgres psql -U oddiya_user -d oddiya

# Connect to Redis CLI
docker-compose exec redis redis-cli
```

## Testing Database Connection

### PostgreSQL

```bash
# Test connection
docker-compose exec postgres pg_isready -U oddiya_user

# List schemas
docker-compose exec postgres psql -U oddiya_user -d oddiya -c "\dn"

# List tables in a schema
docker-compose exec postgres psql -U oddiya_user -d oddiya -c "\dt user_service.*"

# Sample query
docker-compose exec postgres psql -U oddiya_user -d oddiya -c "SELECT * FROM user_service.users;"
```

### Redis

```bash
# Test connection
docker-compose exec redis redis-cli ping

# List all keys
docker-compose exec redis redis-cli KEYS '*'

# Get a value
docker-compose exec redis redis-cli GET refresh_token:some-key

# Clear all data
docker-compose exec redis redis-cli FLUSHALL
```

## Troubleshooting

### Port Already in Use

If PostgreSQL port 5432 is already in use:

```bash
# Find process using port 5432
lsof -i :5432

# Kill the process or change port in docker-compose.yml
```

### Containers Won't Start

```bash
# View detailed logs
docker-compose logs postgres
docker-compose logs redis

# Check disk space
df -h

# Restart Docker Desktop
```

### Database Schema Not Created

```bash
# Manually run init scripts
docker-compose exec postgres psql -U oddiya_user -d oddiya -f /docker-entrypoint-initdb.d/01-init-database.sql
```

### Reset Everything

```bash
# Stop and remove all containers and volumes
docker-compose down -v

# Start fresh
docker-compose up -d
```

## Production Note

This setup is for **local development only**. In production:
- PostgreSQL runs on dedicated `t2.micro` EC2 instance
- Redis runs on dedicated `t2.micro` EC2 instance
- Use environment variables to connect to production hosts
- Use proper secrets management (AWS Secrets Manager, etc.)

