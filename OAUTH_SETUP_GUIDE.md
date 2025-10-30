# 🔐 OAuth Setup Guide - Google & Apple Sign-In

**Implementation Date:** October 30, 2025
**Status:** OAuth UI Complete | Credentials Needed

---

## ✅ What's Implemented

### Web UI (Complete!)
- ✅ OAuth-only Welcome Screen
- ✅ "Google로 시작하기" button
- ✅ "Apple로 시작하기" button
- ✅ OAuth callback handling
- ✅ Token storage in localStorage
- ✅ Persistent login across page refreshes

### Backend (Ready!)
- ✅ Auth Service OAuth endpoints
- ✅ User Service integration
- ✅ JWT token generation (RS256)
- ✅ Refresh token rotation

---

## 🚀 Quick Start (Test Without OAuth)

You can test the UI immediately at:

**http://localhost:8080/mobile**

### Current Behavior:
1. ✅ Welcome screen with OAuth buttons displays
2. ⏳ Google button redirects to Auth Service (needs credentials)
3. ⏳ Apple button shows "coming soon" message

---

## 📋 Google OAuth Setup (Production Ready)

### Step 1: Create Google Cloud Project

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Click "Select a project" → "New Project"
3. Enter project name: "Oddiya"
4. Click "Create"

### Step 2: Enable Google+ API

1. In left sidebar: "APIs & Services" → "Library"
2. Search for "Google+ API"
3. Click "Google+ API"
4. Click "Enable"

### Step 3: Create OAuth 2.0 Credentials

1. Go to "APIs & Services" → "Credentials"
2. Click "+ CREATE CREDENTIALS" → "OAuth client ID"
3. If prompted, configure consent screen:
   - User Type: **External**
   - App name: **Oddiya**
   - User support email: **your-email@example.com**
   - Developer contact: **your-email@example.com**
   - Scopes: Add `email` and `profile`
   - Test users: Add your email

4. Create OAuth client ID:
   - Application type: **Web application**
   - Name: **Oddiya Web**
   - Authorized JavaScript origins:
     ```
     http://localhost:8080
     http://localhost:8081
     ```
   - Authorized redirect URIs:
     ```
     http://localhost:8080/oauth2/callback/google
     http://localhost:8081/oauth2/callback/google
     ```

5. Click "Create"
6. **Copy** the Client ID and Client Secret

### Step 4: Configure Auth Service

Create or update `.env` file:

```bash
# In /Users/wjs/cursor/oddiya/services/auth-service/.env
GOOGLE_CLIENT_ID=your-client-id-here.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your-client-secret-here
OAUTH_REDIRECT_URI=http://localhost:8080/oauth2/callback/google
```

### Step 5: Restart Auth Service

```bash
cd /Users/wjs/cursor/oddiya/services/auth-service
export GOOGLE_CLIENT_ID="your-client-id"
export GOOGLE_CLIENT_SECRET="your-client-secret"
./gradlew bootRun
```

Or use the helper script:
```bash
GOOGLE_CLIENT_ID="xxx" GOOGLE_CLIENT_SECRET="yyy" /Users/wjs/cursor/oddiya/scripts/start-auth-services.sh
```

### Step 6: Test OAuth Flow

1. Open: http://localhost:8080/mobile
2. Click "Google로 시작하기"
3. ✅ Should redirect to Google login
4. ✅ Login with Google account
5. ✅ Should redirect back and show planning screen

---

## 🍎 Apple Sign-In Setup (Optional)

### Requirements:
- Apple Developer Account ($99/year)
- Registered App ID with Sign In with Apple capability
- Service ID for web authentication

### Step 1: Create App ID

1. Go to [Apple Developer Portal](https://developer.apple.com/account/)
2. Certificates, IDs & Profiles → Identifiers → App IDs
3. Click "+" to register new App ID
4. Select "App IDs" → Continue
5. Select type: **App**
6. Description: **Oddiya**
7. Bundle ID: **com.oddiya.app** (explicit)
8. Capabilities: Enable **Sign In with Apple**
9. Click "Continue" → "Register"

### Step 2: Create Service ID

1. Identifiers → "+" → Select "Services IDs"
2. Description: **Oddiya Web**
3. Identifier: **com.oddiya.web**
4. Enable **Sign In with Apple**
5. Click "Configure"
6. Primary App ID: Select **Oddiya** (from Step 1)
7. Web Domain: **localhost** (for testing) or **oddiya.com**
8. Return URLs:
   ```
   http://localhost:8080/oauth2/callback/apple
   ```
9. Click "Save" → "Continue" → "Register"

### Step 3: Create Private Key

1. Keys → "+" to create new key
2. Key Name: **Oddiya Apple Sign In Key**
3. Enable **Sign In with Apple**
4. Click "Configure" → Select **Oddiya** App ID
5. Click "Save" → "Continue" → "Register"
6. **Download** the `.p8` file (can only download once!)
7. Note the **Key ID** (e.g., `ABC123DEF4`)

### Step 4: Configure Auth Service

```bash
# In /Users/wjs/cursor/oddiya/services/auth-service/.env
APPLE_CLIENT_ID=com.oddiya.web
APPLE_TEAM_ID=YOUR_TEAM_ID  # Found in Apple Developer account
APPLE_KEY_ID=ABC123DEF4
APPLE_PRIVATE_KEY_PATH=/path/to/AuthKey_ABC123DEF4.p8
APPLE_REDIRECT_URI=http://localhost:8080/oauth2/callback/apple
```

### Step 5: Update AuthController

Uncomment Apple endpoints in `AuthController.java` (currently commented out)

### Step 6: Test Apple Sign-In

1. Open: http://localhost:8080/mobile
2. Click "Apple로 시작하기"
3. ✅ Should redirect to Apple login

---

## 🔧 Configuration Files

### Auth Service (`application.yml`)

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope:
              - email
              - profile
            redirect-uri: ${OAUTH_REDIRECT_URI:http://localhost:8080/oauth2/callback/google}
            authorization-grant-type: authorization_code
```

### API Gateway (No changes needed)
OAuth logic is in Auth Service. API Gateway just routes requests.

---

## 🧪 Testing Without OAuth Credentials

You can still test the UI without setting up OAuth:

1. **Open**: http://localhost:8080/mobile
2. **See**: Welcome screen with OAuth buttons
3. **Click**: "Google로 시작하기"
4. **Result**: Will redirect to Auth Service (shows error without credentials)

To bypass OAuth for testing:
1. Manually set localStorage values in browser console:
```javascript
localStorage.setItem('accessToken', 'test-token');
localStorage.setItem('userId', '1');
localStorage.setItem('userName', 'Test User');
localStorage.setItem('userEmail', 'test@example.com');
location.reload();
```

---

## 📊 OAuth Flow Diagram

```
User clicks "Google로 시작하기"
  ↓
Redirect to Auth Service: /oauth2/authorize/google
  ↓
Auth Service redirects to: https://accounts.google.com/o/oauth2/auth
  ↓
User logs in with Google
  ↓
Google redirects to: http://localhost:8080/oauth2/callback/google?code=xxx
  ↓
Frontend sends code to: POST /api/auth/oauth2/callback/google
  ↓
Auth Service:
  - Exchanges code for Google tokens
  - Gets user info from Google
  - Creates/finds user in User Service
  - Generates JWT tokens (access + refresh)
  ↓
Frontend receives:
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "uuid...",
  "userId": 123
}
  ↓
Frontend stores tokens in localStorage
  ↓
User is logged in! → Navigate to planning screen
```

---

## 🔐 Security Considerations

### State Parameter
- ✅ Generated UUID for CSRF protection
- ⏳ Store in Redis for production (currently basic)

### Token Security
- ✅ RS256 JWT (public/private key)
- ✅ 1-hour access token expiry
- ✅ 14-day refresh token expiry
- ✅ Refresh tokens stored in Redis with TTL

### OAuth Scopes
- Minimal: `email`, `profile` only
- No access to user's Google data beyond authentication

### HTTPS Required
- ⚠️ localhost uses HTTP (OK for development)
- ❗ Production MUST use HTTPS for OAuth redirect URIs

---

## 🐛 Troubleshooting

### "redirect_uri_mismatch" Error
**Problem**: Google shows "Error 400: redirect_uri_mismatch"

**Solution**:
1. Check redirect URI in Google Console matches exactly:
   ```
   http://localhost:8080/oauth2/callback/google
   ```
2. No trailing slash
3. Correct port number
4. HTTP vs HTTPS match

### "invalid_client" Error
**Problem**: Auth Service shows invalid client

**Solution**:
1. Check `GOOGLE_CLIENT_ID` and `GOOGLE_CLIENT_SECRET` are correct
2. Restart Auth Service after setting environment variables
3. Check Auth Service logs: `tail -f /tmp/auth-service.log`

### OAuth Button Does Nothing
**Problem**: Clicking button doesn't redirect

**Solution**:
1. Check browser console for JavaScript errors (F12)
2. Verify Auth Service is running: `lsof -ti:8081`
3. Check Auth Service URL in JavaScript: `AUTH_API = 'http://localhost:8081'`

### User Not Created
**Problem**: OAuth works but user not in database

**Solution**:
1. Check User Service is running: `lsof -ti:8082`
2. Check User Service has database credentials
3. Check logs: `tail -f /tmp/user-service.log`

---

## 📁 Files Modified

### Frontend (API Gateway)
- ✅ `SimpleMobileController.java` - OAuth-only UI

### Backend (Auth Service)
- ✅ `AuthController.java` - OAuth endpoints
- ✅ `AuthService.java` - OAuth logic
- ✅ `application.yml` - OAuth configuration

### Backend (User Service)
- ✅ `UserController.java` - Internal user API
- ✅ `UserService.java` - User creation

---

## 🎯 Next Steps

### Immediate (To Test OAuth)
1. **Set up Google OAuth** (follow Step 1-6 above)
2. **Restart Auth Service** with credentials
3. **Test complete flow** at http://localhost:8080/mobile

### Short-term (Optional)
1. **Add Apple Sign-In** (requires Apple Developer account)
2. **Add social profile pictures** from OAuth providers
3. **Add "Remember me"** option
4. **Add account linking** (link Google + Apple accounts)

### Long-term (Production)
1. **HTTPS setup** with Let's Encrypt
2. **Production domain** (oddiya.com)
3. **Update OAuth redirect URIs** to production domain
4. **Add rate limiting** on OAuth endpoints
5. **Add OAuth token refresh** when access token expires

---

## 📝 Environment Variables Summary

```bash
# Required for Google OAuth
export GOOGLE_CLIENT_ID="xxx.apps.googleusercontent.com"
export GOOGLE_CLIENT_SECRET="xxx"
export OAUTH_REDIRECT_URI="http://localhost:8080/oauth2/callback/google"

# Optional for Apple OAuth
export APPLE_CLIENT_ID="com.oddiya.web"
export APPLE_TEAM_ID="YOUR_TEAM_ID"
export APPLE_KEY_ID="ABC123DEF4"
export APPLE_PRIVATE_KEY_PATH="/path/to/key.p8"

# Database (required)
export DB_HOST="localhost"
export DB_PORT="5432"
export DB_NAME="oddiya"
export DB_USER="oddiya_user"
export DB_PASSWORD="4321"

# Redis (required)
export REDIS_HOST="localhost"
export REDIS_PORT="6379"
```

---

## ✅ Completion Checklist

### UI Implementation
- [x] OAuth-only welcome screen
- [x] Google OAuth button
- [x] Apple OAuth button (placeholder)
- [x] OAuth callback handling
- [x] Token storage
- [x] Persistent login

### Backend Implementation
- [x] OAuth endpoints (Auth Service)
- [x] User creation (User Service)
- [x] JWT token generation
- [x] Refresh token rotation

### Configuration
- [ ] Google OAuth credentials (needs setup)
- [ ] Apple OAuth credentials (optional)
- [ ] Production redirect URIs (future)

### Testing
- [x] UI displays correctly
- [ ] Google OAuth flow (needs credentials)
- [ ] Apple OAuth flow (needs credentials)
- [ ] Token persistence
- [ ] Logout flow

---

**Current Status**: ✅ OAuth UI Complete | ⏳ Needs Google Credentials

**Test Now**: http://localhost:8080/mobile

**Next Action**: Set up Google OAuth (Steps 1-6 above) to enable full authentication!
