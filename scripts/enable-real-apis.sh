#!/bin/bash

echo "🔧 ENABLING REAL APIS FOR ODDIYA"
echo "=================================="
echo ""

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_info() {
    echo -e "${YELLOW}ℹ️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Check if running in correct directory
if [ ! -f "docker-compose.local.yml" ]; then
    print_error "Must run from project root (/Users/wjs/cursor/oddiya)"
    exit 1
fi

print_info "This script will configure real API integration"
echo ""

# Check environment variables
MISSING=0

if [ -z "$AWS_ACCESS_KEY_ID" ]; then
    print_error "AWS_ACCESS_KEY_ID not set"
    ((MISSING++))
fi

if [ -z "$AWS_SECRET_ACCESS_KEY" ]; then
    print_error "AWS_SECRET_ACCESS_KEY not set"
    ((MISSING++))
fi

if [ -z "$KAKAO_LOCAL_API_KEY" ]; then
    print_error "KAKAO_LOCAL_API_KEY not set"
    ((MISSING++))
fi

if [ -z "$OPENWEATHER_API_KEY" ]; then
    print_error "OPENWEATHER_API_KEY not set"
    ((MISSING++))
fi

if [ $MISSING -gt 0 ]; then
    echo ""
    print_error "$MISSING required environment variables missing"
    echo ""
    echo "Set them first:"
    echo "  export AWS_ACCESS_KEY_ID=your-key"
    echo "  export AWS_SECRET_ACCESS_KEY=your-secret"
    echo "  export KAKAO_LOCAL_API_KEY=your-key"
    echo "  export OPENWEATHER_API_KEY=your-key"
    echo ""
    echo "Then run this script again"
    exit 1
fi

print_success "All required API keys found"
echo ""

# Create .env.local with real APIs
print_info "Creating .env.local with real API configuration..."

cat > .env.local << EOF
# Real API Configuration
ENVIRONMENT=development

# AWS Bedrock (REAL)
AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY
AWS_REGION=ap-northeast-2
BEDROCK_MODEL_ID=anthropic.claude-3-sonnet-20240229-v1:0

# External APIs (REAL)
KAKAO_LOCAL_API_KEY=$KAKAO_LOCAL_API_KEY
OPENWEATHER_API_KEY=$OPENWEATHER_API_KEY

# LangSmith (Optional)
LANGSMITH_API_KEY=${LANGSMITH_API_KEY:-}
LANGSMITH_PROJECT=oddiya-development

# IMPORTANT: Real APIs enabled
MOCK_MODE=false

# Infrastructure
DB_HOST=postgres
REDIS_HOST=redis
EOF

print_success ".env.local created"
echo ""

# Restart LLM Agent
print_info "Restarting LLM Agent with real APIs..."

docker-compose -f docker-compose.local.yml --env-file .env.local up -d --build llm-agent

echo ""
print_success "LLM Agent restarted with REAL APIs!"
echo ""

# Wait for service
print_info "Waiting for LLM Agent to start..."
sleep 15

# Test
print_info "Testing LLM Agent..."
HEALTH=$(curl -s http://localhost:8000/health)

if echo "$HEALTH" | grep -q "healthy"; then
    print_success "LLM Agent is healthy"
else
    print_error "LLM Agent may not be ready"
fi

echo ""
echo "════════════════════════════════════════"
echo "✅ REAL APIS ENABLED!"
echo "════════════════════════════════════════"
echo ""
echo "Now when you create a plan, it will use:"
echo "  🤖 AWS Bedrock (Claude Sonnet)"
echo "  🌤️ OpenWeatherMap (Real weather)"
echo "  📍 Kakao Local API (Real places)"
echo ""
echo "Test at: http://172.16.102.149:8080/app"
echo ""
echo "Create a new plan and see:"
echo "  ✅ Real Seoul attractions"
echo "  ✅ Real weather forecast"
echo "  ✅ AI-generated detailed itinerary"
echo ""
print_success "Ready to create real AI plans!"
echo ""

