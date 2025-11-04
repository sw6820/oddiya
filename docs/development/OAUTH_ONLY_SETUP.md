# OAuth-Only Authentication Setup

**Date**: October 31, 2025
**Status**: ✅ Implemented - Configuration Required

---

## Overview

Oddiya uses **OAuth-only authentication** with Google and Apple Sign-In. Email/password authentication has been removed to simplify the authentication flow and improve security.

---

## Architecture

```
Mobile App → Google/Apple OAuth → Auth Service → JWT Tokens → Secure Storage
```

**Flow**:
1. User clicks "Continue with Google" in mobile app
2. Google Sign-In SDK opens dialog
3. User selects Google account
4. Google returns ID token
5. Mobile app sends ID token to Auth Service
6. Auth Service verifies ID token with Google
7. Auth Service creates/finds user in User Service
8. Auth Service generates JWT tokens (access + refresh)
9. Mobile app stores tokens in SecureStore
10. User is logged in ✅

---

## Prerequisites

### 1. Google OAuth Credentials

**Get from**: https://console.cloud.google.com/

**Required**:
- ✅ Google Client ID (Web Application)
- ✅ Google Client Secret
- ✅ Authorized redirect URI: `http://localhost:8080/login/oauth2/code/google`

**For Mobile**:
- ✅ Android Client ID (from Google Cloud Console)
- ✅ SHA-1 fingerprint registered
- ✅ google-services.json configured

**Setup Guide**: See `mobile/GOOGLE_OAUTH_ANDROID_SETUP.md`

### 2. Apple Sign-In Credentials (Optional)

**Get from**: https://developer.apple.com/

**Required**:
- ✅ Apple Client ID
- ✅ Apple Team ID
- ✅ Apple Key ID
- ✅ Apple Private Key

---

## Configuration

### Backend (.env)

```bash
# Google OAuth 2.0
GOOGLE_CLIENT_ID=123456789-abcdefg.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=GOCSPX-your_actual_secret_here
OAUTH_REDIRECT_URI=http://localhost:8080/login/oauth2/code/google

# Apple Sign In (Optional)
APPLE_CLIENT_ID=com.oddiya.app
APPLE_TEAM_ID=ABC123DEF4
APPLE_KEY_ID=XYZ789ABC1
APPLE_PRIVATE_KEY=-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----

# JWT Configuration
JWT_ACCESS_TOKEN_VALIDITY=3600        # 1 hour
JWT_REFRESH_TOKEN_VALIDITY=1209600    # 14 days

# Service URLs
USER_SERVICE_URL=http://localhost:8082
```

### Mobile (App.tsx)

```typescript
// Mobile app configuration
const GOOGLE_WEB_CLIENT_ID = '123456789-abcdefg.apps.googleusercontent.com';

useEffect(() => {
  // Initialize Google Sign-In
  googleSignInService.configure(GOOGLE_WEB_CLIENT_ID);
}, []);
```

---

## Endpoints

### Auth Service Endpoints

#### 1. OAuth Login (Mobile - ID Token)

```
POST /api/auth/google
Content-Type: application/json

Request:
{
  "idToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6..."
}

Response (200 OK):
{
  "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "userId": 123
}
```

#### 2. OAuth Web Flow (Browser - Authorization Code)

```
GET /oauth2/authorize/google

Response (302 Redirect):
Location: https://accounts.google.com/o/oauth2/v2/auth?
  client_id=...&
  redirect_uri=http://localhost:8080/login/oauth2/code/google&
  response_type=code&
  scope=openid+profile+email&
  state=random-uuid
```

#### 3. OAuth Callback (Web)

```
POST /api/auth/oauth2/callback/google
Content-Type: application/json

Request:
{
  "code": "4/0AZEOvhX...",
  "state": "random-uuid"
}

Response (200 OK):
{
  "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "userId": 123
}
```

#### 4. Refresh Token

```
POST /api/auth/refresh
Content-Type: application/json

Request:
{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}

Response (200 OK):
{
  "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "new-refresh-token-uuid",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "userId": 123
}
```

#### 5. JWKS (Public Key)

```
GET /.well-known/jwks.json

Response (200 OK):
{
  "keys": [
    {
      "kty": "RSA",
      "kid": "oddiya-2024",
      "use": "sig",
      "alg": "RS256",
      "n": "base64-encoded-modulus",
      "e": "AQAB"
    }
  ]
}
```

---

## Removed Endpoints

The following endpoints have been **removed** as we only use OAuth:

❌ `POST /api/auth/signup` - Email/password signup
❌ `POST /api/auth/login` - Email/password login

**Reason**: OAuth-only authentication for better security and user experience.

---

## Testing

### 1. Test Web OAuth Flow

```bash
# Open browser and navigate to:
http://localhost:8081/oauth2/authorize/google

# Should redirect to Google OAuth consent screen
# After login, redirects back to callback URL
```

### 2. Test Mobile OAuth Flow

```bash
# From mobile app, click "Continue with Google"
# Google Sign-In dialog opens
# User selects account
# App receives ID token

# Send ID token to backend
curl -X POST http://localhost:8081/api/auth/google \
  -H "Content-Type: application/json" \
  -d '{
    "idToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6..."
  }'
```

### 3. Test Token Refresh

```bash
curl -X POST http://localhost:8081/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
  }'
```

### 4. Verify JWKS Endpoint

```bash
curl http://localhost:8081/.well-known/jwks.json
```

---

## Troubleshooting

### Issue: "400 Bad Request" on OAuth

**Cause**: Invalid or missing Google OAuth credentials

**Solution**:
1. Check `.env` file has real credentials (not placeholders)
2. Verify Client ID matches Google Cloud Console
3. Restart Auth Service: `./scripts/restart-auth-service.sh`

### Issue: "Invalid ID Token"

**Cause**: Mobile app using wrong Web Client ID

**Solution**:
1. Check `App.tsx` has correct `GOOGLE_WEB_CLIENT_ID`
2. Verify Web Client ID (not Android Client ID)
3. Check `google-services.json` has same Web Client ID

### Issue: "Redirect URI mismatch"

**Cause**: Authorized redirect URI not configured in Google Cloud Console

**Solution**:
1. Go to Google Cloud Console → Credentials
2. Edit OAuth Client ID
3. Add: `http://localhost:8080/login/oauth2/code/google`
4. For production: `https://api.oddiya.com/login/oauth2/code/google`

### Issue: Auth Service won't start

**Cause**: Missing environment variables

**Solution**:
```bash
# Check .env file
cat .env | grep GOOGLE

# Should show:
# GOOGLE_CLIENT_ID=123456789-abc.apps.googleusercontent.com
# GOOGLE_CLIENT_SECRET=GOCSPX-xxxxx

# Restart with correct env vars
./scripts/restart-auth-service.sh
```

---

## Security Considerations

### ✅ Implemented

1. **RS256 JWT signing** - Asymmetric encryption
2. **Short-lived access tokens** - 1 hour expiry
3. **Long-lived refresh tokens** - 14 days, revocable
4. **HTTPS only in production**
5. **ID token verification** - Backend verifies with Google/Apple
6. **Secure storage** - Mobile uses KeyStore/Keychain
7. **JWKS public key distribution** - No shared secrets

### ⏳ To Implement

1. **CSRF protection** - State parameter validation
2. **Rate limiting** - Prevent brute force
3. **Token rotation** - New refresh token on each use
4. **Session tracking** - Monitor active sessions per user
5. **Suspicious activity detection** - Login from new location

---

## Production Checklist

- [ ] Google OAuth credentials configured
- [ ] Apple Sign-In credentials configured (optional)
- [ ] Redirect URIs updated for production domain
- [ ] HTTPS enabled (ACM certificate on ALB)
- [ ] Environment variables in AWS Secrets Manager
- [ ] JWKS endpoint accessible from API Gateway
- [ ] Token refresh working
- [ ] Mobile app google-services.json configured
- [ ] SHA-1 fingerprint registered (production keystore)
- [ ] CORS configured for mobile app domains

---

## Quick Start Commands

```bash
# 1. Update .env with Google OAuth credentials
vim .env

# 2. Restart Auth Service
./scripts/restart-auth-service.sh

# 3. Test OAuth flow
open http://localhost:8081/oauth2/authorize/google

# 4. Check logs
tail -f /tmp/auth-service.log

# 5. Verify services
curl http://localhost:8081/actuator/health  # Auth Service
curl http://localhost:8082/actuator/health  # User Service
```

---

## Next Steps

1. **Get Google OAuth credentials** from Google Cloud Console
2. **Update `.env` file** with real credentials
3. **Restart Auth Service**: `./scripts/restart-auth-service.sh`
4. **Test OAuth flow**: Open `http://localhost:8081/oauth2/authorize/google`
5. **Configure mobile app** (see `mobile/GOOGLE_OAUTH_ANDROID_SETUP.md`)

---

## References

- **Google OAuth Setup**: `mobile/GOOGLE_OAUTH_ANDROID_SETUP.md`
- **Token Management**: `docs/architecture/TOKEN_AND_SESSION_MANAGEMENT.md`
- **Environment Variables**: `docs/development/ENVIRONMENT_VARIABLES.md`
- **Google OAuth Docs**: https://developers.google.com/identity/protocols/oauth2
- **Apple Sign In Docs**: https://developer.apple.com/sign-in-with-apple/

---

**Questions?** Check troubleshooting section or contact auth team.
