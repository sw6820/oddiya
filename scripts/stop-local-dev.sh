#!/bin/bash

# Oddiya Local Development Stop Script
# Stops all running services

set -e

echo "🛑 Stopping Oddiya Local Development Environment"
echo "================================================"

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

# Color codes
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Stop Java services and LLM Agent
if [ -f "logs/pids.txt" ]; then
    echo -e "${YELLOW}Stopping Java services and LLM Agent...${NC}"
    while read pid; do
        if ps -p $pid > /dev/null 2>&1; then
            kill $pid 2>/dev/null || true
            echo -e "${GREEN}✓ Stopped process $pid${NC}"
        fi
    done < logs/pids.txt
    rm logs/pids.txt
else
    echo -e "${YELLOW}No running services found (logs/pids.txt not found)${NC}"
fi

# Stop Docker services
echo -e "\n${YELLOW}Stopping Docker services (PostgreSQL + Redis)...${NC}"
docker-compose down
echo -e "${GREEN}✓ Docker services stopped${NC}"

echo -e "\n${GREEN}================================================${NC}"
echo -e "${GREEN}✅ All services stopped${NC}"
echo -e "${GREEN}================================================${NC}"
