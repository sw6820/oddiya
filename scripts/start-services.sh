#!/bin/bash

# Start API Gateway and Plan Service with proper environment variables

cd "$(dirname "$0")/.."

echo "Starting services..."

# Start Plan Service
echo "Starting Plan Service on port 8083..."
(
  export DB_HOST=localhost
  export DB_PORT=5432
  export DB_NAME=oddiya
  export DB_USER=oddiya_user
  export DB_PASSWORD=oddiya_password_dev
  export REDIS_HOST=localhost
  export REDIS_PORT=6379
  export LLM_AGENT_URL=http://localhost:8000

  cd services/plan-service
  nohup java \
    -Dspring.datasource.url=jdbc:postgresql://localhost:5432/oddiya \
    -Dspring.datasource.username=oddiya_user \
    -Dspring.datasource.password=oddiya_password_dev \
    -jar build/libs/plan-service-0.1.0.jar > ../../logs/plan-service.log 2>&1 &
  echo $! > ../../logs/plan-service.pid
  echo "Plan Service PID: $(cat ../../logs/plan-service.pid)"
)

sleep 8

# Start API Gateway
echo "Starting API Gateway on port 8080..."
(
  export REDIS_HOST=localhost
  export REDIS_PORT=6379

  cd services/api-gateway
  nohup java -jar build/libs/api-gateway-0.1.0.jar \
    --spring.data.redis.host=localhost \
    --spring.cloud.gateway.routes[0].id=plan-service \
    --spring.cloud.gateway.routes[0].uri=http://localhost:8083 \
    --spring.cloud.gateway.routes[0].predicates[0]="Path=/api/plans,/api/plans/**" \
    > ../../logs/api-gateway.log 2>&1 &
  echo $! > ../../logs/api-gateway.pid
  echo "API Gateway PID: $(cat ../../logs/api-gateway.pid)"
)

sleep 5

echo ""
echo "Services started!"
echo "API Gateway: http://localhost:8080"
echo "Plan Service: http://localhost:8083"
echo "Mobile Web: http://localhost:8080/mobile"
