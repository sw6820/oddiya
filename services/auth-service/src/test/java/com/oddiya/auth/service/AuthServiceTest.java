package com.oddiya.auth.service;

import com.oddiya.auth.config.JwtConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private AuthService authService;
    private JwtService jwtService;
    private JwtConfig jwtConfig;
    private RedisTemplate<String, String> redisTemplate;
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    void setUp() {
        jwtConfig = new JwtConfig();
        jwtService = new JwtService(jwtConfig);
        redisTemplate = mock(RedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        
        authService = new AuthService(jwtService, jwtConfig, redisTemplate);
    }

    @Test
    void testHandleOAuthCallback_ReturnsValidTokenResponse() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doNothing().when(valueOperations).set(anyString(), anyString(), any(Long.class), any());

        // When
        var response = authService.handleOAuthCallback("google", "code123", "state456");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isNotNull();
        assertThat(response.getRefreshToken()).isNotNull();
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(3600L);
        
        // Verify Redis was called to store refresh token
        verify(redisTemplate.opsForValue(), atLeastOnce()).set(anyString(), anyString(), eq(Long.class), any());
    }

    @Test
    void testGetJwks_ReturnsJwkString() {
        // When
        String jwks = authService.getJwks();

        // Then
        assertThat(jwks).isNotNull();
        assertThat(jwks).contains("kty");
    }
}

