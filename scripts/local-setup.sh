#!/bin/bash

# Oddiya Local Setup Script
# This script sets up the local development environment

set -e  # Exit on error

echo "🚀 Setting up Oddiya local development environment..."

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}❌ Docker is not running. Please start Docker Desktop and try again.${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Docker is running${NC}"

# Check if docker-compose is installed
if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    echo -e "${RED}❌ docker-compose is not installed${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Docker Compose is installed${NC}"

# Check if .env.local exists
if [ ! -f .env.local ]; then
    echo -e "${BLUE}📝 Creating .env.local from env.local.example...${NC}"
    cp env.local.example .env.local
    echo -e "${GREEN}✓ .env.local created${NC}"
    echo -e "${BLUE}⚠️  Please edit .env.local with your actual API keys and configuration${NC}"
else
    echo -e "${GREEN}✓ .env.local already exists${NC}"
fi

# Start Docker Compose services
echo -e "${BLUE}🐳 Starting PostgreSQL and Redis containers...${NC}"
docker-compose up -d

# Wait for services to be healthy
echo -e "${BLUE}⏳ Waiting for services to be ready...${NC}"
sleep 5

# Check PostgreSQL health
if docker-compose exec -T postgres pg_isready -U oddiya_user -d oddiya > /dev/null 2>&1; then
    echo -e "${GREEN}✓ PostgreSQL is ready${NC}"
else
    echo -e "${RED}❌ PostgreSQL failed to start${NC}"
    docker-compose logs postgres
    exit 1
fi

# Check Redis health
if docker-compose exec -T redis redis-cli ping > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Redis is ready${NC}"
else
    echo -e "${RED}❌ Redis failed to start${NC}"
    docker-compose logs redis
    exit 1
fi

# Verify database schemas were created
echo -e "${BLUE}🔍 Verifying database schemas...${NC}"
SCHEMAS=$(docker-compose exec -T postgres psql -U oddiya_user -d oddiya -t -c "SELECT schema_name FROM information_schema.schemata WHERE schema_name IN ('user_service', 'plan_service', 'video_service');" | tr -d ' ' | grep -v '^$' | wc -l)

if [ "$SCHEMAS" -eq 3 ]; then
    echo -e "${GREEN}✓ All schemas created: user_service, plan_service, video_service${NC}"
else
    echo -e "${YELLOW}⚠️  Some schemas may not be created yet${NC}"
fi

echo ""
echo -e "${GREEN}✅ Local development environment is ready!${NC}"
echo ""
echo "📊 Container Status:"
docker-compose ps
echo ""
echo "🔗 Connection Information:"
echo "  PostgreSQL: localhost:5432"
echo "  Redis: localhost:6379"
echo ""
echo "📝 Useful Commands:"
echo "  docker-compose logs -f       # View logs"
echo "  docker-compose stop          # Stop containers"
echo "  docker-compose down          # Stop and remove containers"
echo "  docker-compose restart       # Restart containers"
echo ""

