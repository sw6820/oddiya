# Mobile API Testing Guide

Complete guide for testing Oddiya APIs from a mobile app perspective.

## Quick Start

```bash
# Start all services for mobile testing
./scripts/start-for-mobile-testing.sh

# Run automated API tests
./scripts/test-mobile-api.sh
```

## Base URL

**Local Development:**
```
http://localhost:8080
```

**Production:**
```
https://api.oddiya.com
```

## Authentication Flow

### 1. OAuth Login (Google)

**Mobile initiates OAuth:**

```bash
# Step 1: Get authorization URL
GET /api/auth/oauth2/authorize/google

Response:
{
  "authorizationUrl": "https://accounts.google.com/o/oauth2/auth?..."
}
```

**Mobile opens browser → User logs in → Mobile receives code**

```bash
# Step 2: Exchange code for tokens
POST /api/auth/oauth2/callback/google
Content-Type: application/json

{
  "code": "4/0AY0e-g7...",
  "redirectUri": "com.oddiya://oauth/callback"
}

Response:
{
  "accessToken": "eyJhbGciOiJSUzI1NiIs...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "expiresIn": 3600,
  "userId": 1
}
```

**Mobile stores tokens securely**

### 2. Using Access Token

All subsequent requests include the token:

```bash
Authorization: Bearer eyJhbGciOiJSUzI1NiIs...
```

Or use the X-User-Id header for testing:

```bash
X-User-Id: 1
```

## API Endpoints for Mobile

### User Profile

#### Get Current User

```bash
GET /api/users/me
X-User-Id: 1

Response 200:
{
  "id": 1,
  "email": "user@example.com",
  "name": "John Doe",
  "provider": "google",
  "createdAt": "2025-01-20T10:00:00",
  "updatedAt": "2025-01-20T10:00:00"
}
```

#### Update Profile

```bash
PATCH /api/users/me
X-User-Id: 1
Content-Type: application/json

{
  "name": "Jane Doe",
  "email": "jane@example.com"
}

Response 200:
{
  "id": 1,
  "email": "jane@example.com",
  "name": "Jane Doe",
  "provider": "google",
  "createdAt": "2025-01-20T10:00:00",
  "updatedAt": "2025-01-27T15:30:00"
}
```

### Travel Plans

#### Create New Plan (AI-Generated)

```bash
POST /api/plans
X-User-Id: 1
Content-Type: application/json

{
  "title": "Seoul Weekend Trip",
  "startDate": "2025-12-01",
  "endDate": "2025-12-03"
}

Response 200:
{
  "id": 1,
  "userId": 1,
  "title": "Seoul Weekend Trip",
  "startDate": "2025-12-01",
  "endDate": "2025-12-03",
  "details": [
    {
      "id": 1,
      "day": 1,
      "location": "Gyeongbokgung Palace",
      "activity": "Visit the main palace and watch the changing of the guard ceremony"
    },
    {
      "id": 2,
      "day": 1,
      "location": "Bukchon Hanok Village",
      "activity": "Walk through traditional Korean houses"
    },
    {
      "id": 3,
      "day": 2,
      "location": "Myeongdong",
      "activity": "Shopping and street food"
    }
  ],
  "createdAt": "2025-01-27T10:00:00",
  "updatedAt": "2025-01-27T10:00:00"
}
```

#### Get All User's Plans

```bash
GET /api/plans
X-User-Id: 1

Response 200:
[
  {
    "id": 1,
    "userId": 1,
    "title": "Seoul Weekend Trip",
    "startDate": "2025-12-01",
    "endDate": "2025-12-03",
    "details": [...],
    "createdAt": "2025-01-27T10:00:00",
    "updatedAt": "2025-01-27T10:00:00"
  },
  {
    "id": 2,
    "userId": 1,
    "title": "Busan Summer Vacation",
    "startDate": "2026-07-15",
    "endDate": "2026-07-20",
    "details": [...],
    "createdAt": "2025-01-27T11:00:00",
    "updatedAt": "2025-01-27T11:00:00"
  }
]
```

#### Get Single Plan

```bash
GET /api/plans/1
X-User-Id: 1

Response 200:
{
  "id": 1,
  "userId": 1,
  "title": "Seoul Weekend Trip",
  "startDate": "2025-12-01",
  "endDate": "2025-12-03",
  "details": [...],
  "createdAt": "2025-01-27T10:00:00",
  "updatedAt": "2025-01-27T10:00:00"
}
```

#### Update Plan

```bash
PATCH /api/plans/1
X-User-Id: 1
Content-Type: application/json

{
  "title": "Seoul Long Weekend",
  "startDate": "2025-12-01",
  "endDate": "2025-12-04"
}

Response 200:
{
  "id": 1,
  "userId": 1,
  "title": "Seoul Long Weekend",
  "startDate": "2025-12-01",
  "endDate": "2025-12-04",
  "details": [...],
  "createdAt": "2025-01-27T10:00:00",
  "updatedAt": "2025-01-27T16:00:00"
}
```

#### Delete Plan

```bash
DELETE /api/plans/1
X-User-Id: 1

Response 200:
{
  "message": "Plan deleted successfully"
}
```

### Videos

#### Create Video Job

**IMPORTANT:** Mobile app must generate a UUID for idempotency

```bash
POST /api/videos
X-User-Id: 1
Idempotency-Key: 550e8400-e29b-41d4-a716-446655440000
Content-Type: application/json

{
  "photoUrls": [
    "https://oddiya-storage.s3.ap-northeast-2.amazonaws.com/photos/user1/photo1.jpg",
    "https://oddiya-storage.s3.ap-northeast-2.amazonaws.com/photos/user1/photo2.jpg",
    "https://oddiya-storage.s3.ap-northeast-2.amazonaws.com/photos/user1/photo3.jpg"
  ],
  "template": "default"
}

Response 202 Accepted:
{
  "id": 1,
  "userId": 1,
  "status": "PENDING",
  "photoUrls": [...],
  "template": "default",
  "videoUrl": null,
  "idempotencyKey": "550e8400-e29b-41d4-a716-446655440000",
  "createdAt": "2025-01-27T10:00:00",
  "updatedAt": "2025-01-27T10:00:00"
}
```

#### Get All Video Jobs

```bash
GET /api/videos
X-User-Id: 1

Response 200:
[
  {
    "id": 1,
    "userId": 1,
    "status": "COMPLETED",
    "photoUrls": [...],
    "template": "default",
    "videoUrl": "https://oddiya-storage.s3.ap-northeast-2.amazonaws.com/videos/1/output.mp4",
    "idempotencyKey": "550e8400-e29b-41d4-a716-446655440000",
    "createdAt": "2025-01-27T10:00:00",
    "updatedAt": "2025-01-27T10:05:30"
  }
]
```

#### Get Single Video Job

```bash
GET /api/videos/1
X-User-Id: 1

Response 200:
{
  "id": 1,
  "userId": 1,
  "status": "COMPLETED",
  "photoUrls": [...],
  "template": "default",
  "videoUrl": "https://oddiya-storage.s3.ap-northeast-2.amazonaws.com/videos/1/output.mp4",
  "idempotencyKey": "550e8400-e29b-41d4-a716-446655440000",
  "createdAt": "2025-01-27T10:00:00",
  "updatedAt": "2025-01-27T10:05:30"
}
```

## Video Status Flow

```
1. Mobile uploads photos to S3
   ↓
2. Mobile creates video job (status: PENDING)
   ↓
3. Mobile receives SNS push notification
   ↓
4. Mobile fetches video job (status: COMPLETED)
   ↓
5. Mobile displays video URL
```

**DO NOT poll for status - use push notifications**

## Error Responses

### 400 Bad Request

```json
{
  "error": "Bad Request",
  "message": "Title is required",
  "timestamp": "2025-01-27T10:00:00"
}
```

### 401 Unauthorized

```json
{
  "error": "Unauthorized",
  "message": "Invalid or expired token",
  "timestamp": "2025-01-27T10:00:00"
}
```

### 404 Not Found

```json
{
  "error": "Not Found",
  "message": "Plan not found",
  "timestamp": "2025-01-27T10:00:00"
}
```

### 500 Internal Server Error

```json
{
  "error": "Internal Server Error",
  "message": "An unexpected error occurred",
  "timestamp": "2025-01-27T10:00:00"
}
```

## Testing with curl

### Complete User Flow

```bash
# 1. Create user (normally via OAuth)
curl -X POST http://localhost:8082/api/v1/users/internal/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "name": "Test User",
    "provider": "google",
    "providerId": "test-123"
  }'

# 2. Get user profile
curl http://localhost:8080/api/users/me \
  -H "X-User-Id: 1"

# 3. Create travel plan
curl -X POST http://localhost:8080/api/plans \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -d '{
    "title": "Seoul Trip",
    "startDate": "2025-12-01",
    "endDate": "2025-12-03"
  }'

# 4. Get plans
curl http://localhost:8080/api/plans \
  -H "X-User-Id: 1"

# 5. Create video job
curl -X POST http://localhost:8080/api/videos \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -H "Idempotency-Key: $(uuidgen)" \
  -d '{
    "photoUrls": ["photo1.jpg", "photo2.jpg"],
    "template": "default"
  }'

# 6. Check video status
curl http://localhost:8080/api/videos/1 \
  -H "X-User-Id: 1"
```

## Mobile App Integration Checklist

- [ ] OAuth flow implemented
- [ ] Token storage (secure keychain/keystore)
- [ ] Token refresh logic
- [ ] API client with base URL
- [ ] Request interceptor for auth headers
- [ ] Response interceptor for error handling
- [ ] Network error handling
- [ ] Offline mode support
- [ ] Push notification setup (SNS)
- [ ] UUID generation for idempotency
- [ ] Photo upload to S3
- [ ] Video playback from S3 URLs

## Performance Tips

1. **Cache user profile** - Update only when changed
2. **Lazy load plans** - Paginate if many plans
3. **Preload images** - Download S3 photos in background
4. **Retry logic** - Implement exponential backoff
5. **Timeout values** - 30s for normal requests, 60s for video creation

## Security Best Practices

1. **Never log tokens** in production
2. **Use HTTPS only** in production
3. **Validate SSL certificates**
4. **Store tokens in secure storage**
5. **Clear tokens on logout**
6. **Implement biometric auth** for re-authentication

## Troubleshooting

### Issue: 401 Unauthorized

**Check:**
- Token included in request?
- Token expired? (Check expiresIn)
- User ID matches token?

### Issue: Video never completes

**Check:**
- Check video job status via API
- View Video Worker logs: `docker logs oddiya-video-worker`
- Verify SQS queue has messages

### Issue: Plans not AI-generated

**Check:**
- LLM Agent is running
- Check logs: `docker logs oddiya-llm-agent`
- Verify Bedrock API key (or mock mode enabled)

## Next Steps

1. **Use Postman Collection** - Import pre-configured requests
2. **Implement Mobile SDK** - Wrap APIs in native code
3. **Add Analytics** - Track API usage
4. **Monitor Performance** - Response times
5. **Set up Staging** - Test with real devices

## Resources

- [Postman Collection](./Oddiya-Mobile-API.postman_collection.json)
- [API Gateway Routes](../../services/api-gateway/src/main/resources/application.yml)
- [Local Testing Guide](../development/LOCAL_TESTING.md)

