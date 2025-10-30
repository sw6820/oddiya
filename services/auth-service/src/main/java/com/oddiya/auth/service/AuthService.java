package com.oddiya.auth.service;

import com.oddiya.auth.config.JwtConfig;
import com.oddiya.auth.dto.LoginRequest;
import com.oddiya.auth.dto.SignupRequest;
import com.oddiya.auth.dto.TokenResponse;
import com.oddiya.auth.exception.InvalidTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtService jwtService;
    private final JwtConfig jwtConfig;
    private final RedisTemplate<String, String> redisTemplate;
    private final OAuthService oAuthService;
    private final UserServiceClient userServiceClient;
    private final PasswordEncoder passwordEncoder;

    /**
     * Email/Password Signup
     */
    public TokenResponse signup(SignupRequest request) {
        // Hash password
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // Create user via User Service internal API
        UserServiceClient.UserResponse userResponse = userServiceClient.createUser(
            request.getEmail(),
            request.getName(),
            "email",
            hashedPassword
        );

        Long userId = userResponse.getId();
        String email = userResponse.getEmail();

        // Generate tokens
        return generateTokenResponse(userId, email);
    }

    /**
     * Email/Password Login
     */
    public TokenResponse login(LoginRequest request) {
        // Find user by email
        UserServiceClient.UserResponse userResponse = userServiceClient.findUserByEmail(request.getEmail());

        if (userResponse == null) {
            throw new InvalidTokenException("Invalid email or password");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), userResponse.getPasswordHash())) {
            throw new InvalidTokenException("Invalid email or password");
        }

        Long userId = userResponse.getId();
        String email = userResponse.getEmail();

        // Generate tokens
        return generateTokenResponse(userId, email);
    }

    /**
     * OAuth Callback Handler
     */
    public TokenResponse handleOAuthCallback(String provider, String code, String state) {
        // Exchange code for access token
        String accessToken = oAuthService.exchangeCodeForToken(code, provider);

        // Get user info from OAuth provider
        OAuthService.GoogleUserInfo userInfo = oAuthService.getUserInfoFromGoogle(accessToken);

        // Create or find user via User Service internal API
        UserServiceClient.UserResponse userResponse = userServiceClient.createOrFindUser(
            new UserServiceClient.CreateUserRequest(
                userInfo.getEmail(),
                userInfo.getName(),
                provider,
                userInfo.getId()
            )
        );

        Long userId = userResponse.getId();
        String email = userResponse.getEmail();

        // Generate tokens
        return generateTokenResponse(userId, email);
    }

    /**
     * Generate token response (extracted for reuse)
     */
    private TokenResponse generateTokenResponse(Long userId, String email) {
        String jwtAccessToken = jwtService.generateToken(userId, email);
        String refreshToken = UUID.randomUUID().toString();

        // Store refresh token in Redis
        storeRefreshToken(refreshToken, userId);

        return TokenResponse.builder()
                .accessToken(jwtAccessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtConfig.getAccessTokenValidity())
                .userId(userId)
                .build();
    }

    public TokenResponse refreshToken(String refreshToken) {
        // Check if refresh token exists in Redis
        String userIdStr = redisTemplate.opsForValue().get("refresh_token:" + refreshToken);
        
        if (userIdStr == null) {
            throw new com.oddiya.auth.exception.InvalidTokenException("Invalid refresh token");
        }
        
        Long userId = Long.parseLong(userIdStr);
        
        // Generate new access token (mock email for now)
        String accessToken = jwtService.generateToken(userId, "user@example.com");
        
        // Optionally rotate refresh token
        String newRefreshToken = UUID.randomUUID().toString();
        storeRefreshToken(newRefreshToken, userId);
        redisTemplate.delete("refresh_token:" + refreshToken);
        
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtConfig.getAccessTokenValidity())
                .build();
    }

    public String getJwks() {
        return jwtService.getPublicKeyAsJwk();
    }

    private void storeRefreshToken(String refreshToken, Long userId) {
        String key = "refresh_token:" + refreshToken;
        redisTemplate.opsForValue().set(key, userId.toString(), 
            jwtConfig.getRefreshTokenValidity(), TimeUnit.SECONDS);
    }
}

