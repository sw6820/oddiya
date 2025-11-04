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

if [ -z "$GOOGLE_API_KEY" ]; then
    print_error "GOOGLE_API_KEY not set (for Gemini AI)"
    ((MISSING++))
fi

if [ -z "$OPENWEATHER_API_KEY" ]; then
    print_info "OPENWEATHER_API_KEY not set (optional)"
fi

if [ $MISSING -gt 0 ]; then
    echo ""
    print_error "$MISSING required environment variables missing"
    echo ""
    echo "Set Google Gemini API key:"
    echo "  export GOOGLE_API_KEY=your-gemini-key"
    echo ""
    echo "Get your FREE Gemini API key from:"
    echo "  https://makersuite.google.com/app/apikey"
    echo ""
    echo "Optional APIs:"
    echo "  export OPENWEATHER_API_KEY=your-weather-key"
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

# Google Gemini (PRIMARY LLM)
LLM_PROVIDER=gemini
GOOGLE_API_KEY=$GOOGLE_API_KEY
GEMINI_MODEL=gemini-2.0-flash-exp

# External APIs (Optional)
OPENWEATHER_API_KEY=${OPENWEATHER_API_KEY:-}
EXCHANGERATE_API_KEY=${EXCHANGERATE_API_KEY:-}

# LangSmith (Optional - for LLM tracing)
LANGSMITH_API_KEY=${LANGSMITH_API_KEY:-}
LANGSMITH_PROJECT=oddiya-development
LANGCHAIN_TRACING_V2=false

# IMPORTANT: Real APIs enabled
USE_BEDROCK_MOCK=false

# Infrastructure
DB_HOST=postgres
REDIS_HOST=redis
REDIS_PORT=6379
CACHE_TTL=3600
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
echo "  🤖 Google Gemini 2.0 Flash (FREE tier - Korea knowledge)"
echo "  🌤️ OpenWeatherMap (Real weather forecast - optional)"
echo ""
echo "Test at: http://localhost:8080"
echo ""
echo "Create a new plan and see:"
echo "  ✅ Real Korean attractions (Seoul, Busan, Jeju, etc.)"
echo "  ✅ Real weather forecast (if API key provided)"
echo "  ✅ AI-generated detailed itinerary from Gemini"
echo ""
print_success "Ready to create real AI travel plans!"
echo ""

