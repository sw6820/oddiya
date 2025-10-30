#!/bin/bash

# Quick Start - Just API Gateway and databases
# For testing the mobile UI without full LLM integration

set -e

echo "🚀 Quick Start - Mobile UI Only"
echo "================================"

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

# Start Docker services if not running
if ! docker ps | grep -q oddiya-postgres; then
    echo "Starting Docker services..."
    docker-compose up -d
    sleep 5
fi

# Kill existing API Gateway
pkill -f "api-gateway" || true

# Start API Gateway with localhost config
echo "Starting API Gateway on port 8080..."
cd services/api-gateway

REDIS_HOST=localhost \
REDIS_PORT=6379 \
DB_HOST=localhost \
DB_PORT=5432 \
java -jar build/libs/api-gateway-0.1.0.jar \
    --spring.profiles.active=local \
    --spring.data.redis.host=localhost \
    --spring.cloud.gateway.routes[0].id=plan-service \
    --spring.cloud.gateway.routes[0].uri=http://localhost:8083 \
    --spring.cloud.gateway.routes[0].predicates[0]="Path=/api/plans,/api/plans/**" \
    > "$PROJECT_ROOT/logs/api-gateway-ui.log" 2>&1 &

API_GATEWAY_PID=$!
echo "✓ API Gateway started (PID: $API_GATEWAY_PID)"

sleep 5

echo ""
echo "✅ Mobile UI is ready!"
echo "================================"
echo "📱 Open: http://localhost:8080/mobile"
echo ""
echo "⚠️  Note: Plan creation won't work (LLM Agent not started)"
echo "          To start full stack, use: ./scripts/start-local-dev.sh"
echo ""
echo "📂 Logs: tail -f logs/api-gateway-ui.log"
echo "🛑 Stop: pkill -f api-gateway"
