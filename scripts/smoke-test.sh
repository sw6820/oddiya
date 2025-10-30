#!/bin/bash
# Quick Smoke Test - Verify services are running
# Run this after starting docker-compose

set -e

GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

echo "🔥 Smoke Test - Quick Health Check"
echo ""

# LLM Agent
echo -n "LLM Agent (8000)... "
if curl -sf http://localhost:8000/health > /dev/null; then
    echo -e "${GREEN}✓${NC}"
else
    echo -e "${RED}✗${NC}"
    exit 1
fi

# Plan Service
echo -n "Plan Service (8083)... "
if curl -sf http://localhost:8083/actuator/health > /dev/null; then
    echo -e "${GREEN}✓${NC}"
else
    echo -e "${RED}✗${NC}"
    exit 1
fi

# API Gateway
echo -n "API Gateway (8080)... "
if curl -sf http://localhost:8080/actuator/health > /dev/null; then
    echo -e "${GREEN}✓${NC}"
else
    echo -e "${RED}✗${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}✓ All services healthy!${NC}"
echo ""
echo "Run full integration test: ./scripts/test-integration.sh"
