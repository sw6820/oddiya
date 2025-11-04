# Token and Session Management for User Identity

**Last Updated**: October 30, 2025
**Status**: ✅ Implemented and Operational

---

## Overview

Oddiya uses a **dual-token authentication system** with JWT access tokens and UUID refresh tokens, following OAuth 2.0 best practices. This document explains how tokens are generated, stored, validated, and refreshed throughout the user's journey.

---

## Table of Contents

1. [Token Architecture](#token-architecture)
2. [Security Model](#security-model)
3. [Token Lifecycle](#token-lifecycle)
4. [Implementation Details](#implementation-details)
5. [Session Management](#session-management)
6. [Best Practices](#best-practices)
7. [Troubleshooting](#troubleshooting)

---

## Token Architecture

### 1. Access Tokens (JWT with RS256)

**Purpose**: Authenticate API requests

**Format**: JSON Web Token (JWT)
```
eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoxMjMsImVtYWlsIjoiam9obkBleGFtcGxlLmNvbSIsInJvbGVzIjpbIlVTRVIiXSwiaWF0IjoxNjk4MDE2MDAwLCJleHAiOjE2OTgwMTk2MDB9.signature
```

**Structure**:
```json
{
  "header": {
    "alg": "RS256",
    "typ": "JWT"
  },
  "payload": {
    "user_id": 123,
    "email": "john@example.com",
    "roles": ["USER"],
    "iat": 1698016000,  // Issued at
    "exp": 1698019600   // Expires (1 hour later)
  },
  "signature": "..."
}
```

**Properties**:
- **Signing Algorithm**: RS256 (RSA + SHA-256)
- **Expiry**: 1 hour (3600 seconds)
- **Storage**: Mobile app SecureStore (KeyStore/Keychain)
- **Validation**: API Gateway verifies signature using public key from Auth Service JWKS endpoint
- **Stateless**: Self-contained, no server-side session lookup required

**Why RS256?**
- Asymmetric encryption: Auth Service signs with private key, API Gateway validates with public key
- API Gateway doesn't need private key (security separation)
- Supports public key rotation via JWKS

### 2. Refresh Tokens (UUID)

**Purpose**: Obtain new access tokens without re-authentication

**Format**: UUID v4
```
550e8400-e29b-41d4-a716-446655440000
```

**Properties**:
- **Format**: Random UUID (128-bit)
- **Expiry**: 14 days (1,209,600 seconds)
- **Storage**:
  - Backend: Redis with key `refresh_token:{uuid}` → value `{user_id}`
  - Mobile: SecureStore (KeyStore/Keychain)
- **Stateful**: Requires Redis lookup for validation
- **Single-use (optional)**: Can implement token rotation on refresh

**Why UUID in Redis?**
- Can be revoked immediately (logout, security breach)
- Track active sessions per user
- Detect suspicious activity (multiple devices, geolocation)
- Lower risk if leaked (can be deleted from Redis)

---

## Security Model

### Storage Security

**Mobile App - SecureStore (expo-secure-store)**

```typescript
// src/utils/secureStorage.ts
import * as SecureStore from 'expo-secure-store';

await SecureStore.setItemAsync('access_token', token);
```

**Android**: KeyStore (Hardware-backed)
- Keys stored in Trusted Execution Environment (TEE) or Secure Element (SE)
- Cannot be extracted even with root access
- Encrypted at rest with device-specific keys
- Biometric protection available (fingerprint/face unlock)

**iOS**: Keychain
- Stored in secure enclave (dedicated crypto chip)
- Protected by device passcode + biometric (Touch ID/Face ID)
- Access requires app signature matching
- Persists across app reinstall (unless device reset)

**Backend - Redis**

```bash
# Refresh token storage
SET refresh_token:550e8400-e29b-41d4-a716-446655440000 "123"
EXPIRE refresh_token:550e8400-e29b-41d4-a716-446655440000 1209600  # 14 days
```

**Properties**:
- In-memory storage (fast lookup)
- Automatic expiry (TTL)
- Master password protection
- TLS encryption for network transport
- Running on private subnet (t2.micro EC2)

### Validation Security

**API Gateway JWT Validation**

```java
// 1. Fetch public key from Auth Service JWKS endpoint
GET https://auth-service:8081/.well-known/jwks.json

// 2. Cache public key in Redis (1 hour TTL)
// 3. Verify JWT signature
DecodedJWT jwt = JWT.require(Algorithm.RSA256(publicKey))
    .build()
    .verify(token);

// 4. Check expiry
if (jwt.getExpiresAt().before(new Date())) {
    throw new TokenExpiredException();
}

// 5. Extract user_id and forward to backend
request.addHeader("X-User-ID", jwt.getClaim("user_id").asString());
```

**Refresh Token Validation**

```java
// 1. Check if token exists in Redis
String userId = redis.get("refresh_token:" + refreshToken);
if (userId == null) {
    throw new InvalidTokenException("Token not found or expired");
}

// 2. Check if token has been revoked (blacklist)
if (redis.exists("revoked_token:" + refreshToken)) {
    throw new InvalidTokenException("Token has been revoked");
}

// 3. Generate new access token
String newAccessToken = jwtService.generateAccessToken(userId);
return new TokenResponse(newAccessToken, refreshToken);
```

---

## Token Lifecycle

### 1. Token Creation (OAuth Login)

**User Journey**: User clicks "Continue with Google"

```
┌──────────┐
│  Mobile  │
│   App    │
└────┬─────┘
     │
     │ 1. Click "Continue with Google"
     │
     ▼
┌──────────────────────────────┐
│ Google Sign-In Dialog        │
│ (Native Android SDK)         │
└────┬─────────────────────────┘
     │
     │ 2. User selects account
     │
     ▼
┌──────────────────────────────┐
│ Google returns ID token      │
│ (JWT signed by Google)       │
└────┬─────────────────────────┘
     │
     │ 3. POST /api/auth/google
     │    { "idToken": "..." }
     │
     ▼
┌──────────────────────────────┐
│ Auth Service                 │
│ -------------------------    │
│ 1. Verify ID token with      │
│    Google (public key)       │
│ 2. Extract user info:        │
│    - email                   │
│    - name                    │
│    - google_id               │
│ 3. Find/create user:         │
│    POST /internal/users      │
│ 4. Generate tokens:          │
│    - Access (JWT, RS256)     │
│    - Refresh (UUID)          │
│ 5. Store refresh in Redis    │
└────┬─────────────────────────┘
     │
     │ 4. Return tokens
     │    { accessToken, refreshToken, userId }
     │
     ▼
┌──────────────────────────────┐
│ Mobile App                   │
│ -------------------------    │
│ 1. Store in SecureStore:     │
│    - accessToken             │
│    - refreshToken            │
│    - userId                  │
│    - email                   │
│ 2. Update Redux state:       │
│    - isAuthenticated: true   │
│    - user: { ... }           │
│ 3. Navigate to main app      │
└──────────────────────────────┘
```

**Code Implementation**:

```typescript
// mobile/src/store/slices/authSlice.ts
export const loginWithGoogle = createAsyncThunk(
  'auth/loginWithGoogle',
  async () => {
    // 1. Sign in with Google and get ID token
    const googleUser = await googleSignInService.signIn();

    // 2. Send ID token to backend for verification
    const tokenResponse = await authService.googleLogin(googleUser.idToken);

    // 3. Store tokens securely
    await secureStorage.setAuthData({
      accessToken: tokenResponse.accessToken,
      refreshToken: tokenResponse.refreshToken,
      userId: String(tokenResponse.userId),
      email: googleUser.email,
    });

    // 4. Fetch user profile
    const user = await userService.getProfile();

    return { tokenResponse, user };
  }
);
```

### 2. Token Usage (API Requests)

**Every API call includes access token in Authorization header**

```typescript
// mobile/src/api/client.ts
import axios from 'axios';
import { secureStorage } from '@/utils/secureStorage';

const apiClient = axios.create({
  baseURL: 'http://api.oddiya.com',
});

// Add access token to every request
apiClient.interceptors.request.use(async config => {
  const accessToken = await secureStorage.getAccessToken();
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`;
  }
  return config;
});
```

**API Gateway validates token**:

```
┌──────────┐
│  Mobile  │
│   App    │
└────┬─────┘
     │
     │ GET /api/plans/123
     │ Authorization: Bearer eyJhbGc...
     │
     ▼
┌──────────────────────────────┐
│ API Gateway                  │
│ -------------------------    │
│ 1. Extract JWT from header   │
│ 2. Fetch public key (cached) │
│ 3. Verify signature          │
│ 4. Check expiry              │
│ 5. Extract user_id           │
│ 6. Forward to backend:       │
│    X-User-ID: 123            │
└────┬─────────────────────────┘
     │
     ▼
┌──────────────────────────────┐
│ Plan Service                 │
│ -------------------------    │
│ 1. Read X-User-ID header     │
│ 2. Query database            │
│ 3. Return user's plans       │
└──────────────────────────────┘
```

### 3. Token Refresh (Access Token Expired)

**Automatic refresh when access token expires (1 hour)**

```typescript
// mobile/src/api/client.ts
apiClient.interceptors.response.use(
  response => response,
  async error => {
    const originalRequest = error.config;

    // Token expired (401) and not a retry
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        // 1. Get refresh token from SecureStore
        const refreshToken = await secureStorage.getRefreshToken();

        if (!refreshToken) {
          // No refresh token - logout
          throw new Error('No refresh token');
        }

        // 2. Request new access token
        const response = await axios.post(
          'http://api.oddiya.com/api/auth/refresh',
          { refreshToken }
        );

        const { accessToken, refreshToken: newRefreshToken } = response.data;

        // 3. Store new tokens
        await secureStorage.setAuthData({
          accessToken,
          refreshToken: newRefreshToken,
          userId: await secureStorage.getUserId(),
        });

        // 4. Retry original request with new token
        originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        return apiClient(originalRequest);
      } catch (refreshError) {
        // Refresh failed - logout user
        await secureStorage.clearAll();
        // Navigate to login screen
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);
```

**Backend Refresh Flow**:

```
┌──────────┐
│  Mobile  │
│   App    │
└────┬─────┘
     │
     │ POST /api/auth/refresh
     │ { "refreshToken": "550e8400..." }
     │
     ▼
┌──────────────────────────────┐
│ Auth Service                 │
│ -------------------------    │
│ 1. Lookup in Redis:          │
│    GET refresh_token:550e... │
│    → user_id: 123            │
│ 2. Check if revoked:         │
│    EXISTS revoked_token:...  │
│ 3. Generate new access token │
│ 4. (Optional) Rotate refresh │
│    - Delete old refresh      │
│    - Generate new UUID       │
│    - Store in Redis          │
│ 5. Return tokens             │
└────┬─────────────────────────┘
     │
     │ { accessToken, refreshToken }
     │
     ▼
┌──────────────────────────────┐
│ Mobile App                   │
│ -------------------------    │
│ 1. Store new tokens          │
│ 2. Retry failed request      │
└──────────────────────────────┘
```

### 4. Token Revocation (Logout)

**User clicks logout**

```typescript
// mobile/src/store/slices/authSlice.ts
export const logout = createAsyncThunk('auth/logout', async () => {
  // 1. Call backend to revoke refresh token
  const refreshToken = await secureStorage.getRefreshToken();
  if (refreshToken) {
    await authService.logout(refreshToken);
  }

  // 2. Clear local storage
  await secureStorage.clearAll();

  // 3. Sign out from Google (if OAuth)
  if (await googleSignInService.isSignedIn()) {
    await googleSignInService.signOut();
  }
});
```

**Backend Revocation**:

```java
// Auth Service - POST /api/auth/logout
public void logout(String refreshToken) {
    // 1. Delete refresh token from Redis
    redis.delete("refresh_token:" + refreshToken);

    // 2. Add to revocation list (optional, for extra security)
    // Access tokens valid until expiry (max 1 hour)
    redis.setex(
        "revoked_token:" + refreshToken,
        3600,  // 1 hour
        "revoked"
    );
}
```

**Security Note**:
- Access tokens remain valid until expiry (max 1 hour)
- To invalidate immediately: maintain token blacklist in Redis
- Trade-off: performance vs. immediate revocation

---

## Implementation Details

### Mobile App (React Native + Redux)

**File Structure**:
```
mobile/
├── src/
│   ├── store/
│   │   └── slices/
│   │       └── authSlice.ts          # Redux auth logic
│   ├── api/
│   │   ├── client.ts                 # Axios with interceptors
│   │   └── services.ts               # API methods
│   ├── utils/
│   │   └── secureStorage.ts          # SecureStore wrapper
│   └── services/
│       └── googleSignInService.ts    # Google OAuth wrapper
```

**Key Files**:

**1. authSlice.ts** - Redux authentication state
```typescript
const initialState: AuthState = {
  user: null,
  accessToken: null,
  refreshToken: null,
  isAuthenticated: false,
  isLoading: false,
  error: null,
};

// Thunks:
// - loginWithGoogle()
// - loginWithEmail()
// - signupWithEmail()
// - loginWithOAuth()
// - refreshAuthToken()
// - loadStoredAuth()
// - logout()
```

**2. secureStorage.ts** - Token storage
```typescript
export const secureStorage = {
  async setAuthData(data: {
    accessToken: string;
    refreshToken: string;
    userId: string;
    email?: string;
  }): Promise<void> {
    await Promise.all([
      SecureStore.setItemAsync('access_token', data.accessToken),
      SecureStore.setItemAsync('refresh_token', data.refreshToken),
      SecureStore.setItemAsync('user_id', data.userId),
      data.email ? SecureStore.setItemAsync('user_email', data.email) : Promise.resolve(),
    ]);
  },

  async getAuthData(): Promise<{
    accessToken: string | null;
    refreshToken: string | null;
    userId: string | null;
  }> {
    const [accessToken, refreshToken, userId] = await Promise.all([
      SecureStore.getItemAsync('access_token'),
      SecureStore.getItemAsync('refresh_token'),
      SecureStore.getItemAsync('user_id'),
    ]);
    return { accessToken, refreshToken, userId };
  },

  async clearAll(): Promise<void> {
    await Promise.all([
      SecureStore.deleteItemAsync('access_token'),
      SecureStore.deleteItemAsync('refresh_token'),
      SecureStore.deleteItemAsync('user_id'),
      SecureStore.deleteItemAsync('user_email'),
    ]);
  },
};
```

**3. client.ts** - API client with auto-refresh
```typescript
// Request interceptor - add token
apiClient.interceptors.request.use(async config => {
  const accessToken = await secureStorage.getAccessToken();
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`;
  }
  return config;
});

// Response interceptor - handle 401 and refresh
apiClient.interceptors.response.use(
  response => response,
  async error => {
    if (error.response?.status === 401 && !error.config._retry) {
      // Refresh token and retry
      // (see Token Refresh section above)
    }
    return Promise.reject(error);
  }
);
```

### Backend (Spring Boot)

**File Structure**:
```
services/
├── auth-service/
│   └── src/main/java/com/oddiya/auth/
│       ├── controller/
│       │   └── AuthController.java      # POST /api/auth/google, /refresh, /logout
│       ├── service/
│       │   ├── AuthService.java         # Token generation
│       │   ├── JwtService.java          # JWT signing/validation
│       │   └── GoogleAuthService.java   # ID token verification
│       ├── config/
│       │   └── JwksController.java      # GET /.well-known/jwks.json
│       └── repository/
│           └── RedisRefreshTokenRepository.java
│
└── api-gateway/
    └── src/main/java/com/oddiya/gateway/
        ├── filter/
        │   └── JwtAuthenticationFilter.java   # Validate JWT, add X-User-ID
        └── config/
            └── SecurityConfig.java
```

**Key Components**:

**1. AuthController.java** - Token endpoints
```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/google")
    public ResponseEntity<TokenResponse> googleLogin(@RequestBody GoogleLoginRequest request) {
        // Verify Google ID token
        GoogleIdToken.Payload payload = googleAuthService.verifyIdToken(request.getIdToken());

        // Find or create user
        User user = userService.findOrCreateOAuthUser(
            payload.getEmail(),
            (String) payload.get("name"),
            "google",
            payload.getSubject()
        );

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = UUID.randomUUID().toString();

        // Store refresh token in Redis (14 days)
        refreshTokenRepository.save(refreshToken, user.getId(), Duration.ofDays(14));

        return ResponseEntity.ok(new TokenResponse(
            accessToken,
            refreshToken,
            "Bearer",
            3600,  // 1 hour
            user.getId()
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody RefreshRequest request) {
        // Validate refresh token
        Long userId = refreshTokenRepository.findByToken(request.getRefreshToken())
            .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        // Generate new access token
        User user = userService.findById(userId);
        String newAccessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());

        // (Optional) Rotate refresh token
        String newRefreshToken = UUID.randomUUID().toString();
        refreshTokenRepository.delete(request.getRefreshToken());
        refreshTokenRepository.save(newRefreshToken, userId, Duration.ofDays(14));

        return ResponseEntity.ok(new TokenResponse(
            newAccessToken,
            newRefreshToken,
            "Bearer",
            3600,
            userId
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequest request) {
        // Delete refresh token from Redis
        refreshTokenRepository.delete(request.getRefreshToken());

        // (Optional) Add to revocation list
        revokedTokenRepository.save(request.getRefreshToken(), Duration.ofHours(1));

        return ResponseEntity.ok().build();
    }
}
```

**2. JwtService.java** - JWT generation and validation
```java
@Service
public class JwtService {

    @Value("${jwt.private-key}")
    private String privateKeyPath;

    @Value("${jwt.public-key}")
    private String publicKeyPath;

    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;

    @PostConstruct
    public void init() throws Exception {
        // Load RSA keys
        this.privateKey = loadPrivateKey(privateKeyPath);
        this.publicKey = loadPublicKey(publicKeyPath);
    }

    public String generateAccessToken(Long userId, String email) {
        return JWT.create()
            .withIssuer("oddiya-auth-service")
            .withSubject(String.valueOf(userId))
            .withClaim("email", email)
            .withClaim("roles", List.of("USER"))
            .withIssuedAt(new Date())
            .withExpiresAt(new Date(System.currentTimeMillis() + 3600 * 1000))  // 1 hour
            .sign(Algorithm.RSA256(publicKey, privateKey));
    }

    public DecodedJWT verifyAccessToken(String token) {
        return JWT.require(Algorithm.RSA256(publicKey, null))
            .withIssuer("oddiya-auth-service")
            .build()
            .verify(token);
    }
}
```

**3. JwtAuthenticationFilter.java** - API Gateway validation
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwksService jwksService;  // Fetches public key from Auth Service

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) {
        String token = extractToken(request);

        if (token != null) {
            try {
                // Fetch public key (cached)
                RSAPublicKey publicKey = jwksService.getPublicKey();

                // Verify JWT
                DecodedJWT jwt = JWT.require(Algorithm.RSA256(publicKey, null))
                    .build()
                    .verify(token);

                // Add user ID to request
                request.setAttribute("X-User-ID", jwt.getSubject());

            } catch (JWTVerificationException e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
```

**4. RedisRefreshTokenRepository.java** - Redis storage
```java
@Repository
public class RedisRefreshTokenRepository {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String PREFIX = "refresh_token:";

    public void save(String token, Long userId, Duration ttl) {
        redisTemplate.opsForValue().set(
            PREFIX + token,
            String.valueOf(userId),
            ttl
        );
    }

    public Optional<Long> findByToken(String token) {
        String userId = redisTemplate.opsForValue().get(PREFIX + token);
        return userId != null ? Optional.of(Long.parseLong(userId)) : Optional.empty();
    }

    public void delete(String token) {
        redisTemplate.delete(PREFIX + token);
    }
}
```

---

## Session Management

### Session Persistence

**On App Startup**:

```typescript
// App.tsx
useEffect(() => {
  const initializeAuth = async () => {
    try {
      // Load stored tokens from SecureStore
      await dispatch(loadStoredAuth()).unwrap();

      // User stays logged in!

    } catch (error) {
      // No stored auth - show welcome screen
      console.log('No stored authentication found');
    }
  };

  initializeAuth();
}, [dispatch]);
```

**Benefits**:
- User stays logged in after app restart
- No need to sign in every time
- Seamless UX

### Session Monitoring

**Track Active Sessions** (Optional Feature):

```java
// Store session metadata in Redis
public void createSession(Long userId, String refreshToken, String deviceInfo) {
    String sessionKey = "user_sessions:" + userId;

    SessionInfo session = SessionInfo.builder()
        .refreshToken(refreshToken)
        .deviceName(deviceInfo.getDeviceName())
        .deviceType(deviceInfo.getDeviceType())
        .ipAddress(deviceInfo.getIpAddress())
        .createdAt(LocalDateTime.now())
        .lastAccessedAt(LocalDateTime.now())
        .build();

    // Store in Redis Set
    redisTemplate.opsForSet().add(sessionKey, session.toJson());
}

// List user's active sessions
public List<SessionInfo> getActiveSessions(Long userId) {
    String sessionKey = "user_sessions:" + userId;
    return redisTemplate.opsForSet().members(sessionKey)
        .stream()
        .map(SessionInfo::fromJson)
        .collect(Collectors.toList());
}

// Revoke specific session
public void revokeSession(Long userId, String refreshToken) {
    // Delete refresh token
    refreshTokenRepository.delete(refreshToken);

    // Remove from user's sessions
    String sessionKey = "user_sessions:" + userId;
    redisTemplate.opsForSet().members(sessionKey)
        .stream()
        .filter(s -> SessionInfo.fromJson(s).getRefreshToken().equals(refreshToken))
        .forEach(s -> redisTemplate.opsForSet().remove(sessionKey, s));
}
```

**Use Cases**:
- Show user their active devices
- "Sign out all devices" feature
- Detect suspicious activity (login from new location)
- Limit concurrent sessions

### Session Security

**Best Practices Implemented**:

1. **Short-lived access tokens** (1 hour)
   - Limits damage if leaked
   - Forces periodic re-validation

2. **Long-lived refresh tokens** (14 days)
   - Good UX (persistent login)
   - Can be revoked on logout

3. **Hardware-backed storage**
   - Tokens protected by device security
   - Requires biometric/PIN to extract

4. **HTTPS only**
   - Tokens encrypted in transit
   - Prevents man-in-the-middle attacks

5. **Automatic refresh**
   - Transparent to user
   - No "session expired" interruptions

**Additional Security Measures (Optional)**:

1. **Token Rotation**
   - Generate new refresh token on each use
   - Invalidate old refresh token
   - Detect token replay attacks

2. **Device Fingerprinting**
   - Bind refresh token to device
   - Detect token theft across devices

3. **Geolocation Validation**
   - Alert on login from new location
   - Require email verification

4. **Rate Limiting**
   - Limit refresh requests per minute
   - Prevent brute force attacks

---

## Best Practices

### ✅ DO

1. **Always use HTTPS in production**
   ```typescript
   const API_BASE_URL = __DEV__
     ? 'http://localhost:8080'  // Dev only
     : 'https://api.oddiya.com'; // Prod
   ```

2. **Store tokens in SecureStore (NOT AsyncStorage)**
   ```typescript
   // ✅ GOOD - Hardware-backed
   await SecureStore.setItemAsync('access_token', token);

   // ❌ BAD - Plaintext storage
   await AsyncStorage.setItem('access_token', token);
   ```

3. **Validate JWT on every request**
   ```java
   // API Gateway
   DecodedJWT jwt = JWT.require(Algorithm.RSA256(publicKey))
       .build()
       .verify(token);
   ```

4. **Use short expiry for access tokens**
   ```java
   // 1 hour maximum
   .withExpiresAt(new Date(System.currentTimeMillis() + 3600 * 1000))
   ```

5. **Implement automatic token refresh**
   ```typescript
   // Intercept 401 and refresh transparently
   if (error.response?.status === 401) {
     await refreshToken();
     return retryRequest();
   }
   ```

6. **Clear tokens on logout**
   ```typescript
   await secureStorage.clearAll();
   await googleSignInService.signOut();
   ```

7. **Log security events**
   ```java
   // Audit log
   logger.info("User {} logged in from IP {}", userId, ipAddress);
   logger.warn("Failed login attempt for email {}", email);
   ```

### ❌ DON'T

1. **Never store tokens in AsyncStorage**
   - AsyncStorage is plaintext
   - Accessible to other apps with root access

2. **Never hardcode secrets**
   ```java
   // ❌ BAD
   private static final String JWT_SECRET = "my-secret-key";

   // ✅ GOOD
   @Value("${jwt.private-key}")
   private String privateKeyPath;
   ```

3. **Never use long-lived access tokens**
   - Keep access tokens short (1 hour max)
   - Use refresh tokens for persistence

4. **Never trust client-side token validation**
   - Always validate on backend
   - Client validation is convenience only

5. **Never expose private keys**
   - Keep JWT signing key on Auth Service only
   - API Gateway uses public key only

6. **Never log tokens**
   ```java
   // ❌ BAD
   logger.debug("Token: {}", token);

   // ✅ GOOD
   logger.debug("Token received for user {}", userId);
   ```

---

## Troubleshooting

### Issue: "Token expired" immediately after login

**Symptoms**:
- User signs in successfully
- Immediately gets 401 Unauthorized
- Token appears valid

**Possible Causes**:
1. **Clock skew** - Server and client clocks out of sync
2. **Timezone mismatch** - Server using different timezone

**Solutions**:
```java
// Add clock skew tolerance (5 minutes)
DecodedJWT jwt = JWT.require(Algorithm.RSA256(publicKey))
    .acceptLeeway(300)  // 5 minutes tolerance
    .build()
    .verify(token);
```

### Issue: "Invalid signature" error

**Symptoms**:
- JWT validation fails
- Error: "The Token's Signature resulted invalid"

**Possible Causes**:
1. **Wrong public key** - API Gateway using old public key
2. **Key rotation** - Auth Service rotated keys, Gateway cached old key
3. **Corrupted key** - Key file damaged

**Solutions**:
```java
// Implement JWKS with cache refresh
public RSAPublicKey getPublicKey() {
    String cachedKey = redis.get("jwks:public_key");

    if (cachedKey == null) {
        // Fetch from Auth Service
        JWKSet jwks = authServiceClient.fetchJwks();
        RSAPublicKey publicKey = (RSAPublicKey) jwks.getKeyByKeyId("oddiya-2024").toRSAKey().toPublicKey();

        // Cache for 1 hour
        redis.setex("jwks:public_key", 3600, serializeKey(publicKey));

        return publicKey;
    }

    return deserializeKey(cachedKey);
}
```

### Issue: Refresh token not working

**Symptoms**:
- User logged out after 1 hour
- Refresh endpoint returns 401

**Possible Causes**:
1. **Token not in Redis** - Never stored or expired early
2. **Redis connection issue** - Can't reach Redis
3. **Token revoked** - User logged out from another device

**Debug Steps**:
```bash
# Check if token exists in Redis
redis-cli
> GET refresh_token:550e8400-e29b-41d4-a716-446655440000
"123"  # User ID

> TTL refresh_token:550e8400-e29b-41d4-a716-446655440000
1209000  # Remaining seconds

# If returns (nil) - token doesn't exist
# If returns -2 - token expired
```

### Issue: User logged out unexpectedly

**Symptoms**:
- User randomly logged out
- No user action triggered logout

**Possible Causes**:
1. **App reinstall** - SecureStore cleared (iOS)
2. **Device settings change** - Biometric disabled, passcode changed
3. **Token revoked** - Backend invalidated session
4. **Memory pressure** - OS cleared app data

**Solutions**:
```typescript
// Add error handling for token retrieval
try {
  const authData = await secureStorage.getAuthData();
  if (!authData.accessToken) {
    throw new Error('No token found');
  }
} catch (error) {
  // Token retrieval failed - graceful logout
  await dispatch(logout());
  Alert.alert(
    'Session Expired',
    'Please sign in again',
    [{ text: 'OK', onPress: () => navigation.navigate('Welcome') }]
  );
}
```

### Issue: High Redis memory usage

**Symptoms**:
- Redis memory growing continuously
- t2.micro (1GB RAM) running out of memory

**Possible Causes**:
1. **Tokens not expiring** - TTL not set correctly
2. **Too many sessions** - User creating many refresh tokens
3. **Memory leak** - Old tokens not cleaned up

**Debug**:
```bash
# Check Redis memory usage
redis-cli INFO memory

# Count refresh tokens
redis-cli KEYS "refresh_token:*" | wc -l

# Check for tokens without TTL
redis-cli
> SCAN 0 MATCH refresh_token:* COUNT 100
> TTL refresh_token:{uuid}
-1  # No expiry set! (BUG)

# Find tokens without TTL
for key in $(redis-cli KEYS "refresh_token:*"); do
  ttl=$(redis-cli TTL $key)
  if [ "$ttl" == "-1" ]; then
    echo "No TTL: $key"
  fi
done
```

**Solutions**:
```java
// Always set TTL when storing refresh token
public void save(String token, Long userId, Duration ttl) {
    redisTemplate.opsForValue().set(
        PREFIX + token,
        String.valueOf(userId),
        ttl  // ✅ MUST SET TTL
    );
}

// Implement token cleanup job
@Scheduled(cron = "0 0 * * * *")  // Every hour
public void cleanupExpiredTokens() {
    Set<String> keys = redisTemplate.keys("refresh_token:*");

    for (String key : keys) {
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);

        // Delete tokens expiring in next 5 minutes
        if (ttl != null && ttl < 300) {
            redisTemplate.delete(key);
            logger.info("Cleaned up expiring token: {}", key);
        }
    }
}
```

---

## Summary

### Current Implementation

✅ **Implemented**:
- JWT access tokens (RS256, 1 hour)
- UUID refresh tokens (14 days)
- SecureStore for mobile (KeyStore/Keychain)
- Redis storage for refresh tokens
- Automatic token refresh on 401
- Persistent login on app restart
- Google OAuth integration
- Secure logout with token revocation

✅ **Security Features**:
- Hardware-backed storage
- Asymmetric encryption (RS256)
- Public key distribution via JWKS
- Short-lived access tokens
- Token expiry validation
- HTTPS in production

✅ **User Experience**:
- Seamless login (OAuth)
- Stay logged in after app restart
- No "session expired" interruptions
- Automatic token refresh
- Fast validation (stateless JWT)

### Optional Enhancements

⏳ **Future Improvements** (if needed):
- Token rotation (new refresh token on each use)
- Session monitoring (active devices list)
- Device fingerprinting (bind token to device)
- Geolocation validation (alert on new location)
- Rate limiting (prevent brute force)
- Token blacklist (immediate revocation)
- Biometric authentication (re-auth for sensitive actions)
- Remember device (skip 2FA on trusted devices)

---

## References

- **OAuth 2.0 RFC**: https://tools.ietf.org/html/rfc6749
- **JWT RFC**: https://tools.ietf.org/html/rfc7519
- **RS256 vs HS256**: https://auth0.com/blog/rs256-vs-hs256/
- **JWKS Specification**: https://tools.ietf.org/html/rfc7517
- **React Native SecureStore**: https://docs.expo.dev/versions/latest/sdk/securestore/
- **Android KeyStore**: https://developer.android.com/training/articles/keystore
- **iOS Keychain**: https://developer.apple.com/documentation/security/keychain_services

---

**Last Updated**: October 30, 2025
**Maintained By**: Oddiya Platform Team
**Questions?** Check troubleshooting section or contact backend team.
