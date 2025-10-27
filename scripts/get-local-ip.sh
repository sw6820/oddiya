#!/bin/bash

echo "🌐 FINDING YOUR MAC'S LOCAL IP ADDRESS"
echo "========================================"
echo ""

# Get WiFi IP
WIFI_IP=$(ipconfig getifaddr en0 2>/dev/null)

# Get Ethernet IP
ETHERNET_IP=$(ipconfig getifaddr en1 2>/dev/null)

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

if [ -n "$WIFI_IP" ]; then
    echo -e "${GREEN}✅ WiFi IP Address:${NC}"
    echo -e "${BLUE}   http://$WIFI_IP:8080${NC}"
    echo ""
    echo "Use this in your mobile app when connected to WiFi"
    echo ""
    FOUND_IP=$WIFI_IP
fi

if [ -n "$ETHERNET_IP" ]; then
    echo -e "${GREEN}✅ Ethernet IP Address:${NC}"
    echo -e "${BLUE}   http://$ETHERNET_IP:8080${NC}"
    echo ""
    echo "Use this in your mobile app when connected via Ethernet"
    echo ""
    FOUND_IP=$ETHERNET_IP
fi

if [ -z "$WIFI_IP" ] && [ -z "$ETHERNET_IP" ]; then
    echo -e "${YELLOW}⚠️  No network connection found${NC}"
    echo ""
    echo "Please connect to WiFi or Ethernet and try again"
    exit 1
fi

echo "════════════════════════════════════════"
echo "📱 DEVICE-SPECIFIC URLS"
echo "════════════════════════════════════════"
echo ""
echo "iOS Simulator:"
echo "  http://localhost:8080"
echo ""
echo "Android Emulator:"
echo "  http://10.0.2.2:8080"
echo ""
echo "Physical Device (iPhone/Android):"
echo "  http://$FOUND_IP:8080"
echo ""
echo "════════════════════════════════════════"
echo "🧪 TEST CONNECTION"
echo "════════════════════════════════════════"
echo ""
echo "From your mobile browser, visit:"
echo "  http://$FOUND_IP:8080/actuator/health"
echo ""
echo "You should see: {\"status\":\"UP\"}"
echo ""
echo "════════════════════════════════════════"
echo "⚠️  IMPORTANT"
echo "════════════════════════════════════════"
echo ""
echo "1. Make sure services are running:"
echo "   ./scripts/start-for-mobile-testing.sh"
echo ""
echo "2. Mobile device must be on SAME WiFi network"
echo ""
echo "3. Check firewall allows connections:"
echo "   System Settings > Network > Firewall"
echo ""

