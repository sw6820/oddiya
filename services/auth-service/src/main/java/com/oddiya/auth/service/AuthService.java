package com.oddiya.auth.service;

import com.oddiya.auth.config.JwtConfig;
import com.oddiya.auth.dto.TokenResponse;
import com.oddiya.auth.exception.InvalidTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
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
        String accessToken = jwtService.generateToken(userId, email);
        String refreshToken = UUID.randomUUID().toString();
        
        // Store refresh token in Redis
        storeRefreshToken(refreshToken, userId);
        
        return TokenResponse.builder()
                .accessToken(jwtAccessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtConfig.getAccessTokenValidity())
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

