# Backend Google Login Endpoint Implementation

**Purpose**: Handle Google ID Token from mobile app and return JWT tokens

---

## Endpoint Specification

```
POST /api/auth/google
Content-Type: application/json

Request Body:
{
  "idToken": "eyJhbGc..."
}

Response (200 OK):
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "uuid-here",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "userId": 123
}
```

---

## Implementation Steps

### 1. Add Google Auth Library

Add to `build.gradle`:
```gradle
dependencies {
    // Google ID Token verification
    implementation 'com.google.api-client:google-api-client:2.2.0'
    implementation 'com.google.auth:google-auth-library-oauth2-http:1.19.0'
}
```

### 2. Create DTO

`GoogleLoginRequest.java`:
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleLoginRequest {
    @NotBlank(message = "ID token is required")
    private String idToken;
}
```

### 3. Add Endpoint to AuthController

```java
@PostMapping("/api/auth/google")
public ResponseEntity<TokenResponse> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
    TokenResponse tokens = authService.googleLogin(request.getIdToken());
    return ResponseEntity.ok(tokens);
}
```

### 4. Implement Service Method

```java
public TokenResponse googleLogin(String idToken) {
    try {
        // Verify ID token with Google
        GoogleIdToken googleToken = verifyGoogleIdToken(idToken);
        GoogleIdToken.Payload payload = googleToken.getPayload();

        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String googleId = payload.getSubject();

        // Create or find user
        UserServiceClient.UserResponse userResponse =
            userServiceClient.findOrCreateOAuthUser(email, name, "google", googleId);

        // Generate JWT tokens
        return generateTokenResponse(userResponse.getId(), email);

    } catch (Exception e) {
        throw new InvalidTokenException("Invalid Google ID token");
    }
}

private GoogleIdToken verifyGoogleIdToken(String idToken) throws Exception {
    GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
        new NetHttpTransport(),
        JacksonFactory.getDefaultInstance()
    )
    .setAudience(Collections.singletonList(googleClientId))
    .build();

    GoogleIdToken token = verifier.verify(idToken);
    if (token == null) {
        throw new InvalidTokenException("Invalid ID token");
    }
    return token;
}
```

### 5. Configure Client ID

`application.yml`:
```yaml
google:
  client-id: ${GOOGLE_WEB_CLIENT_ID:your-web-client-id}
```

---

## Security Considerations

1. **Verify ID Token**: Always verify with Google, never trust client
2. **Check Audience**: Ensure token was issued for your app
3. **Check Expiration**: ID tokens expire quickly
4. **Validate Email**: Check email is verified
5. **Rate Limiting**: Prevent brute force attacks

---

## Testing

```bash
# Get ID token from Android device logs
adb logcat | grep "idToken"

# Test endpoint
curl -X POST http://localhost:8081/api/auth/google \
  -H "Content-Type: application/json" \
  -d '{"idToken": "eyJhbGc..."}'
```
