#!/bin/bash

echo "🛑 STOPPING ODDIYA LOCAL ENVIRONMENT"
echo "====================================="
echo ""

# Stop all services
docker-compose down
docker-compose -f docker-compose.local.yml down

echo ""
echo "✅ All services stopped"
echo ""
echo "To start again: ./scripts/start-local.sh"
echo ""

