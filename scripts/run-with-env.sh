#!/bin/bash

# Run services with specific environment configuration

set -e

# Default to local
ENV=${1:-local}

echo "🚀 STARTING ODDIYA WITH $ENV ENVIRONMENT"
echo "=========================================="
echo ""

# Load environment
source ./scripts/load-env.sh $ENV

# Determine which docker-compose file to use
if [ "$ENV" = "local" ] || [ "$ENV" = "development" ]; then
    COMPOSE_FILE="docker-compose.local.yml"
else
    COMPOSE_FILE="docker-compose.yml"
fi

echo "Using compose file: $COMPOSE_FILE"
echo ""

# Stop existing containers
echo "Stopping existing containers..."
docker-compose -f $COMPOSE_FILE down 2>/dev/null || true

# Start services
echo "Starting services..."
docker-compose -f $COMPOSE_FILE --env-file $ENV_FILE up -d

echo ""
echo "Waiting for services to start..."
sleep 10

# Health check
echo ""
echo "════════════════════════════════════════"
echo "🏥 HEALTH CHECK"
echo "════════════════════════════════════════"
echo ""

check_health() {
    local name=$1
    local url=$2
    
    if curl -s -f "$url" > /dev/null 2>&1; then
        echo "✅ $name"
        return 0
    else
        echo "⏳ $name (starting...)"
        return 1
    fi
}

check_health "API Gateway" "http://localhost:8080/actuator/health"
check_health "Auth Service" "http://localhost:8081/actuator/health"
check_health "User Service" "http://localhost:8082/actuator/health"
check_health "Plan Service" "http://localhost:8083/actuator/health"
check_health "Video Service" "http://localhost:8084/actuator/health"
check_health "LLM Agent" "http://localhost:8000/health"

echo ""
echo "════════════════════════════════════════"
echo "✅ SERVICES RUNNING ($ENV)"
echo "════════════════════════════════════════"
echo ""
echo "API Gateway:  http://localhost:8080"
echo ""
echo "View logs:    docker-compose -f $COMPOSE_FILE logs -f"
echo "Stop:         docker-compose -f $COMPOSE_FILE down"
echo ""

