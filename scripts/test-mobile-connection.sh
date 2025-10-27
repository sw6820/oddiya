#!/bin/bash

echo "🧪 TESTING MOBILE CONNECTION"
echo "=============================="
echo ""

# Get local IP
LOCAL_IP=$(ipconfig getifaddr en0 2>/dev/null)

if [ -z "$LOCAL_IP" ]; then
    echo "❌ No WiFi connection found"
    echo "Please connect to WiFi and try again"
    exit 1
fi

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_info() {
    echo -e "${YELLOW}ℹ️  $1${NC}"
}

echo "Your IP: $LOCAL_IP"
echo ""

# Test 1: localhost (for simulator)
echo "Test 1: Localhost (iOS Simulator)"
if curl -s -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
    print_success "localhost:8080 is accessible"
else
    print_error "localhost:8080 is NOT accessible"
    print_info "Run: ./scripts/start-for-mobile-testing.sh"
fi
echo ""

# Test 2: Local IP (for physical device)
echo "Test 2: Local IP (Physical Device)"
if curl -s -f http://$LOCAL_IP:8080/actuator/health > /dev/null 2>&1; then
    print_success "$LOCAL_IP:8080 is accessible"
else
    print_error "$LOCAL_IP:8080 is NOT accessible"
    print_info "Check firewall settings"
fi
echo ""

# Test 3: Check if port is open
echo "Test 3: Port Accessibility"
if nc -z -w 2 $LOCAL_IP 8080 2>/dev/null; then
    print_success "Port 8080 is open and listening"
else
    print_error "Port 8080 is NOT accessible"
    print_info "Services may not be running"
fi
echo ""

# Test 4: Docker containers
echo "Test 4: Docker Services"
RUNNING=$(docker ps --filter "status=running" | grep oddiya | wc -l | xargs)
if [ "$RUNNING" -gt 0 ]; then
    print_success "$RUNNING Oddiya services running"
    docker ps --format "table {{.Names}}\t{{.Status}}" | grep oddiya
else
    print_error "No Oddiya services running"
    print_info "Run: ./scripts/start-for-mobile-testing.sh"
fi
echo ""

# Summary
echo "════════════════════════════════════════"
echo "📱 MOBILE APP CONFIGURATION"
echo "════════════════════════════════════════"
echo ""
echo "iOS Simulator:"
echo "  let baseURL = \"http://localhost:8080\""
echo ""
echo "Android Emulator:"
echo "  const val BASE_URL = \"http://10.0.2.2:8080\""
echo ""
echo "Physical Device:"
echo "  let baseURL = \"http://$LOCAL_IP:8080\""
echo ""
echo "Test in mobile browser:"
echo "  http://$LOCAL_IP:8080/actuator/health"
echo ""

