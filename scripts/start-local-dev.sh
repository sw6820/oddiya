#!/bin/bash

# Oddiya Local Development Startup Script
# Starts all services needed for the mobile web app

set -e

echo "🚀 Starting Oddiya Local Development Environment"
echo "================================================"

# Color codes
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

# Step 1: Start Docker services (PostgreSQL + Redis)
echo -e "\n${YELLOW}Step 1: Starting Docker services (PostgreSQL + Redis)...${NC}"
docker-compose up -d
echo -e "${GREEN}✓ Docker services started${NC}"

# Wait for databases to be healthy
echo -e "\n${YELLOW}Waiting for databases to be ready...${NC}"
sleep 5

# Check PostgreSQL
until docker exec oddiya-postgres pg_isready -U oddiya_user -d oddiya > /dev/null 2>&1; do
    echo "  Waiting for PostgreSQL..."
    sleep 2
done
echo -e "${GREEN}✓ PostgreSQL is ready${NC}"

# Check Redis
until docker exec oddiya-redis redis-cli ping > /dev/null 2>&1; do
    echo "  Waiting for Redis..."
    sleep 2
done
echo -e "${GREEN}✓ Redis is ready${NC}"

# Step 2: Start LLM Agent (Python FastAPI)
echo -e "\n${YELLOW}Step 2: Starting LLM Agent (Python FastAPI on port 8000)...${NC}"

# Check if .env exists for LLM agent
if [ ! -f "services/llm-agent/.env" ]; then
    echo -e "${RED}⚠ Warning: services/llm-agent/.env not found${NC}"
    echo "  Creating .env from template..."
    cp services/llm-agent/.env.example services/llm-agent/.env 2>/dev/null || cp .env.example services/llm-agent/.env
    echo "  ⚠️  Please configure GOOGLE_API_KEY in services/llm-agent/.env"
    echo "  Get your FREE Gemini API key: https://makersuite.google.com/app/apikey"
fi

cd services/llm-agent

# Check if virtual environment exists
if [ ! -d "venv" ]; then
    echo "  Creating Python virtual environment..."
    python3 -m venv venv
fi

# Activate virtual environment and install dependencies
source venv/bin/activate
pip install -q -r requirements.txt

# Start LLM Agent in background
echo "  Starting LLM Agent with MOCK_MODE=true..."
export MOCK_MODE=true  # Use mock mode for development
nohup uvicorn main:app --host 0.0.0.0 --port 8000 --reload > ../../logs/llm-agent.log 2>&1 &
LLM_AGENT_PID=$!
echo -e "${GREEN}✓ LLM Agent started (PID: $LLM_AGENT_PID)${NC}"

cd "$PROJECT_ROOT"

# Step 3: Start Plan Service (Spring Boot on port 8083)
echo -e "\n${YELLOW}Step 3: Starting Plan Service (Spring Boot on port 8083)...${NC}"
cd services/plan-service

# Find the JAR file
JAR_FILE=$(ls build/libs/plan-service-*.jar 2>/dev/null | grep -v plain | head -n 1)

if [ -z "$JAR_FILE" ]; then
    echo "  Building Plan Service..."
    ./gradlew clean build -x test
    JAR_FILE=$(ls build/libs/plan-service-*.jar | grep -v plain | head -n 1)
fi

# Start Plan Service in background
nohup java -jar "$JAR_FILE" \
    --spring.profiles.active=local \
    > ../../logs/plan-service.log 2>&1 &
PLAN_SERVICE_PID=$!
echo -e "${GREEN}✓ Plan Service started (PID: $PLAN_SERVICE_PID)${NC}"

cd "$PROJECT_ROOT"

# Step 4: Start API Gateway (Spring Cloud Gateway on port 8080)
echo -e "\n${YELLOW}Step 4: Starting API Gateway (Spring Cloud Gateway on port 8080)...${NC}"
cd services/api-gateway

# Find the JAR file
JAR_FILE=$(ls build/libs/api-gateway-*.jar 2>/dev/null | grep -v plain | head -n 1)

if [ -z "$JAR_FILE" ]; then
    echo "  Building API Gateway..."
    ./gradlew clean build -x test
    JAR_FILE=$(ls build/libs/api-gateway-*.jar | grep -v plain | head -n 1)
fi

# Start API Gateway in background
nohup java -jar "$JAR_FILE" \
    --spring.profiles.active=local \
    > ../../logs/api-gateway.log 2>&1 &
API_GATEWAY_PID=$!
echo -e "${GREEN}✓ API Gateway started (PID: $API_GATEWAY_PID)${NC}"

cd "$PROJECT_ROOT"

# Create logs directory if it doesn't exist
mkdir -p logs

# Save PIDs to file for easy cleanup
echo "$LLM_AGENT_PID" > logs/pids.txt
echo "$PLAN_SERVICE_PID" >> logs/pids.txt
echo "$API_GATEWAY_PID" >> logs/pids.txt

# Wait for services to be ready
echo -e "\n${YELLOW}Waiting for services to be ready...${NC}"
sleep 10

# Health checks
echo -e "\n${YELLOW}Performing health checks...${NC}"

# Check LLM Agent
if curl -s http://localhost:8000/health > /dev/null 2>&1; then
    echo -e "${GREEN}✓ LLM Agent (port 8000) is healthy${NC}"
else
    echo -e "${RED}✗ LLM Agent (port 8000) is not responding${NC}"
fi

# Check Plan Service
if curl -s http://localhost:8083/actuator/health > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Plan Service (port 8083) is healthy${NC}"
else
    echo -e "${RED}✗ Plan Service (port 8083) is not responding${NC}"
fi

# Check API Gateway
if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo -e "${GREEN}✓ API Gateway (port 8080) is healthy${NC}"
else
    echo -e "${RED}✗ API Gateway (port 8080) is not responding${NC}"
fi

echo -e "\n${GREEN}================================================${NC}"
echo -e "${GREEN}✅ Oddiya Development Environment is Ready!${NC}"
echo -e "${GREEN}================================================${NC}"
echo ""
echo "📱 Mobile Web App:     http://localhost:8080/mobile"
echo "🌐 Full Web App:       http://localhost:8080/app"
echo "📊 API Gateway:        http://localhost:8080/actuator/health"
echo "🤖 LLM Agent:          http://localhost:8000/health"
echo "📝 Plan Service:       http://localhost:8083/actuator/health"
echo ""
echo "📂 Logs:"
echo "   API Gateway:        tail -f logs/api-gateway.log"
echo "   Plan Service:       tail -f logs/plan-service.log"
echo "   LLM Agent:          tail -f logs/llm-agent.log"
echo ""
echo "🛑 To stop all services:"
echo "   ./scripts/stop-local-dev.sh"
echo ""
