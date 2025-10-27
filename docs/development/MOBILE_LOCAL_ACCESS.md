# Accessing Local Backend from Mobile Devices

Guide for connecting your mobile app to the backend running on your Mac.

## Quick Start

### 1. Find Your Mac's IP Address

```bash
# Run this script to get your local IP
./scripts/get-local-ip.sh

# Or manually:
ifconfig | grep "inet " | grep -v 127.0.0.1
```

Your IP will look like: `192.168.1.100` or `10.0.0.50`

### 2. Start Services

```bash
./scripts/start-for-mobile-testing.sh
```

### 3. Use IP Address in Mobile App

**Instead of:**
```
http://localhost:8080
```

**Use:**
```
http://192.168.1.100:8080
```

## Option 1: Physical Mobile Device (Same WiFi)

### Requirements:
- Mac and mobile device on **same WiFi network**
- Firewall allows incoming connections

### Setup:

**Step 1: Get your Mac's local IP**
```bash
# macOS
ipconfig getifaddr en0  # WiFi
ipconfig getifaddr en1  # Ethernet
```

**Step 2: Allow firewall access**
```bash
# Open System Settings > Network > Firewall
# Allow incoming connections for:
# - Docker
# - Java
# - Python
```

**Step 3: Test connection from mobile**
```bash
# Open Safari on iPhone/iPad
http://192.168.1.100:8080/actuator/health

# Should see: {"status":"UP"}
```

**Step 4: Configure mobile app**
```swift
// iOS (Swift)
let baseURL = "http://192.168.1.100:8080"
```

```kotlin
// Android (Kotlin)
const val BASE_URL = "http://192.168.1.100:8080"
```

## Option 2: iOS Simulator

### Requirements:
- Xcode installed
- iOS Simulator running

### Setup:

**Simulator can use `localhost`:**
```swift
// iOS Simulator
let baseURL = "http://localhost:8080"
// OR
let baseURL = "http://127.0.0.1:8080"
```

**Why it works:**
- iOS Simulator shares network with host Mac
- `localhost` = your Mac

**Test in Safari (Simulator):**
```
http://localhost:8080/actuator/health
```

## Option 3: Android Emulator

### Requirements:
- Android Studio installed
- Android Emulator running

### Setup:

**Android Emulator uses special IP:**
```kotlin
// Android Emulator
const val BASE_URL = "http://10.0.2.2:8080"
```

**Why `10.0.2.2`?**
- Android Emulator's special alias for host machine
- `10.0.2.2` = your Mac's localhost

**Test in Chrome (Emulator):**
```
http://10.0.2.2:8080/actuator/health
```

## Testing from Mobile Browser

### iPhone/iPad (Safari)

1. Connect to same WiFi as Mac
2. Open Safari
3. Go to: `http://[YOUR_MAC_IP]:8080/actuator/health`
4. Should see JSON response

### Android (Chrome)

**Physical Device:**
```
http://[YOUR_MAC_IP]:8080/actuator/health
```

**Emulator:**
```
http://10.0.2.2:8080/actuator/health
```

## Complete Example Flow

### 1. On Mac - Start Services

```bash
# Terminal 1: Get IP address
ipconfig getifaddr en0
# Output: 192.168.1.100

# Terminal 2: Start services
./scripts/start-for-mobile-testing.sh
```

### 2. Test from Mobile Browser

```
# iPhone Safari
http://192.168.1.100:8080/actuator/health

# Should return:
{"status":"UP"}
```

### 3. Test API Endpoints

```
# Get user profile
http://192.168.1.100:8080/api/users/me

# Create travel plan
POST http://192.168.1.100:8080/api/plans
Headers:
  Content-Type: application/json
  X-User-Id: 1
Body:
{
  "title": "Seoul Trip",
  "startDate": "2025-12-01",
  "endDate": "2025-12-03"
}
```

## Mobile App Configuration

### Environment-Based URLs

**iOS (Swift):**
```swift
enum Environment {
    case development
    case staging
    case production
    
    var baseURL: String {
        switch self {
        case .development:
            #if targetEnvironment(simulator)
                return "http://localhost:8080"
            #else
                return "http://192.168.1.100:8080"  // Your Mac IP
            #endif
        case .staging:
            return "https://staging.oddiya.com"
        case .production:
            return "https://api.oddiya.com"
        }
    }
}

// Usage
let apiClient = APIClient(baseURL: Environment.development.baseURL)
```

**Android (Kotlin):**
```kotlin
object ApiConfig {
    private const val LOCAL_EMULATOR = "http://10.0.2.2:8080"
    private const val LOCAL_DEVICE = "http://192.168.1.100:8080"
    private const val STAGING = "https://staging.oddiya.com"
    private const val PRODUCTION = "https://api.oddiya.com"
    
    val baseUrl: String
        get() = when (BuildConfig.BUILD_TYPE) {
            "debug" -> if (isEmulator()) LOCAL_EMULATOR else LOCAL_DEVICE
            "staging" -> STAGING
            else -> PRODUCTION
        }
}
```

**React Native:**
```javascript
const getBaseURL = () => {
  if (__DEV__) {
    // Development mode
    if (Platform.OS === 'ios') {
      return 'http://localhost:8080';
    } else {
      // Android
      return 'http://10.0.2.2:8080';
    }
  }
  return 'https://api.oddiya.com';
};

export const API_BASE_URL = getBaseURL();
```

## Troubleshooting

### Issue: Cannot connect from physical device

**Check:**
1. Same WiFi network?
```bash
# On Mac
ipconfig getifaddr en0

# On iPhone
Settings > WiFi > [Network Name] > IP Address
# Should start with same numbers (e.g., both 192.168.1.x)
```

2. Firewall blocking?
```bash
# macOS - Check Firewall
System Settings > Network > Firewall

# Allow Docker, Java, Python
```

3. Services running?
```bash
docker ps
# Should see all 7 services
```

### Issue: Connection refused

**Check services are bound to 0.0.0.0, not 127.0.0.1:**

```bash
# Check what ports are listening
lsof -i :8080
lsof -i :8081
lsof -i :8082

# Should show 0.0.0.0:8080 not 127.0.0.1:8080
```

**Fix in docker-compose.local.yml:**
```yaml
services:
  api-gateway:
    ports:
      - "0.0.0.0:8080:8080"  # Bind to all interfaces
```

### Issue: iOS App Transport Security (ATS)

**Error:** "App Transport Security has blocked a cleartext HTTP connection"

**Fix - Add to Info.plist:**
```xml
<key>NSAppTransportSecurity</key>
<dict>
    <key>NSAllowsLocalNetworking</key>
    <true/>
    <key>NSAllowsArbitraryLoads</key>
    <true/>
</dict>
```

**Production - Use HTTPS only:**
```xml
<key>NSAppTransportSecurity</key>
<dict>
    <key>NSAllowsArbitraryLoads</key>
    <false/>
</dict>
```

### Issue: Android Network Security

**Error:** "Cleartext HTTP traffic not permitted"

**Fix - Create `network_security_config.xml`:**
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="true" />
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">10.0.2.2</domain>
        <domain includeSubdomains="true">192.168.1.100</domain>
    </domain-config>
</network-security-config>
```

**Add to AndroidManifest.xml:**
```xml
<application
    android:networkSecurityConfig="@xml/network_security_config"
    ...>
```

## Testing Tools

### 1. Postman (Desktop)

```
# Set environment variable
LOCAL_IP = 192.168.1.100
BASE_URL = http://{{LOCAL_IP}}:8080
```

### 2. curl (Mac Terminal)

```bash
# Test as if you're the mobile app
curl http://192.168.1.100:8080/api/users/me \
  -H "X-User-Id: 1"
```

### 3. Browser DevTools (Mobile)

**iOS Safari:**
1. Settings > Safari > Advanced > Web Inspector (ON)
2. Connect iPhone to Mac via USB
3. Safari (Mac) > Develop > [Your iPhone] > [Page]

**Android Chrome:**
1. Settings > Developer Options > USB Debugging (ON)
2. Connect Android to Mac via USB
3. Chrome (Mac) > chrome://inspect
4. Inspect your WebView

## HTTPS for Local Development (Optional)

### Using ngrok (easiest)

```bash
# Install ngrok
brew install ngrok

# Tunnel to local port
ngrok http 8080

# Use the HTTPS URL in mobile app
https://abc123.ngrok.io
```

### Using mkcert (local certificates)

```bash
# Install mkcert
brew install mkcert

# Generate certificates
mkcert -install
mkcert localhost 192.168.1.100

# Configure API Gateway to use HTTPS
# (requires SSL configuration in application.yml)
```

## Performance Tips

### 1. Use WiFi, not cellular
- Faster
- No data charges
- More reliable

### 2. Keep Mac awake
```bash
caffeinate -d
```

### 3. Monitor network traffic
```bash
# Mac - Monitor connections
lsof -i :8080
```

### 4. Check latency
```bash
# From mobile, measure response time
time curl http://192.168.1.100:8080/actuator/health
```

## Summary

| Device Type | Base URL | Notes |
|-------------|----------|-------|
| iOS Simulator | `http://localhost:8080` | Same network as Mac |
| Android Emulator | `http://10.0.2.2:8080` | Special alias |
| Physical Device | `http://[MAC_IP]:8080` | Same WiFi required |
| Production | `https://api.oddiya.com` | HTTPS only |

## Quick Reference Commands

```bash
# Get Mac IP
ipconfig getifaddr en0

# Start services
./scripts/start-for-mobile-testing.sh

# Test from Mac (as mobile would see it)
curl http://$(ipconfig getifaddr en0):8080/actuator/health

# Check if port is accessible
nc -zv 192.168.1.100 8080

# View all listening ports
netstat -an | grep LISTEN

# Allow port through firewall (if needed)
# System Settings > Network > Firewall > Options
# Add port 8080
```

## Next Steps

1. Start services: `./scripts/start-for-mobile-testing.sh`
2. Get your IP: `ipconfig getifaddr en0`
3. Test in browser: `http://[YOUR_IP]:8080/actuator/health`
4. Configure mobile app with IP address
5. Start coding! 🚀

