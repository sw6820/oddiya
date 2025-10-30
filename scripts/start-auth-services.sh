#!/bin/bash

# Start all services for Oddiya authentication testing
echo "🚀 Starting Oddiya Services..."
echo ""

# Database credentials
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=oddiya
export DB_USER=oddiya_user
export DB_PASSWORD=4321

# Check PostgreSQL
echo "1️⃣ Checking PostgreSQL..."
if lsof -ti:5432 >/dev/null 2>&1; then
    echo "✅ PostgreSQL is running"
else
    echo "❌ PostgreSQL is not running. Please start it first:"
    echo "   brew services start postgresql@17"
    echo "   OR: docker-compose up -d postgres"
    exit 1
fi

# Check Redis
echo ""
echo "2️⃣ Checking Redis..."
if lsof -ti:6379 >/dev/null 2>&1; then
    echo "✅ Redis is running"
else
    echo "❌ Redis is not running. Please start it first:"
    echo "   brew services start redis"
    echo "   OR: docker-compose up -d redis"
    exit 1
fi

# Start User Service
echo ""
echo "3️⃣ Starting User Service (port 8082)..."
cd /Users/wjs/cursor/oddiya/services/user-service
DB_HOST=localhost DB_PORT=5432 DB_NAME=oddiya DB_USER=oddiya_user DB_PASSWORD=4321 nohup ./gradlew bootRun > /tmp/user-service.log 2>&1 &
echo "Started User Service"

# Start Auth Service
echo ""
echo "4️⃣ Starting Auth Service (port 8081)..."
cd /Users/wjs/cursor/oddiya/services/auth-service
nohup ./gradlew bootRun > /tmp/auth-service.log 2>&1 &
echo "Started Auth Service"

# Start API Gateway
echo ""
echo "5️⃣ Starting API Gateway (port 8080)..."
cd /Users/wjs/cursor/oddiya/services/api-gateway
nohup ./gradlew bootRun > /tmp/api-gateway.log 2>&1 &
echo "Started API Gateway"

echo ""
echo "⏳ Waiting for services to start (30 seconds)..."
sleep 30

echo ""
echo "📊 Service Status:"
echo "===================="

lsof -ti:8082 >/dev/null 2>&1 && echo "✅ User Service (8082): RUNNING" || echo "❌ User Service (8082): FAILED - check /tmp/user-service.log"
lsof -ti:8081 >/dev/null 2>&1 && echo "✅ Auth Service (8081): RUNNING" || echo "❌ Auth Service (8081): FAILED - check /tmp/auth-service.log"
lsof -ti:8080 >/dev/null 2>&1 && echo "✅ API Gateway (8080): RUNNING" || echo "❌ API Gateway (8080): FAILED - check /tmp/api-gateway.log"

echo ""
echo "🌐 Open your browser to:"
echo "   http://localhost:8080/mobile"
echo ""
echo "📝 Test the authentication:"
echo "   1. Click 'Sign Up' and create an account"
echo "   2. Login with your credentials"
echo "   3. Create a travel plan!"
echo ""
echo "📋 View logs:"
echo "   tail -f /tmp/user-service.log"
echo "   tail -f /tmp/auth-service.log"
echo "   tail -f /tmp/api-gateway.log"
