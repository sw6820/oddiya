package com.oddiya.auth.service;

import com.oddiya.auth.config.JwtConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtConfig jwtConfig = new JwtConfig();
        jwtService = new JwtService(jwtConfig);
    }

    @Test
    void testGenerateToken_WithValidInput_ReturnsToken() {
        // Given
        Long userId = 1L;
        String email = "test@example.com";

        // When
        String token = jwtService.generateToken(userId, email);

        // Then
        assertThat(token).isNotNull();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
    }

    @Test
    void testValidateToken_WithValidToken_ReturnsTrue() {
        // Given
        String token = jwtService.generateToken(1L, "test@example.com");

        // When
        Boolean isValid = jwtService.validateToken(token);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void testValidateToken_WithInvalidToken_ReturnsFalse() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        Boolean isValid = jwtService.validateToken(invalidToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void testExtractUserId_WithValidToken_ReturnsUserId() {
        // Given
        Long userId = 123L;
        String token = jwtService.generateToken(userId, "test@example.com");

        // When
        Long extractedUserId = jwtService.extractUserId(token);

        // Then
        assertThat(extractedUserId).isEqualTo(userId);
    }

    @Test
    void testExtractEmail_WithValidToken_ReturnsEmail() {
        // Given
        String email = "test@example.com";
        String token = jwtService.generateToken(1L, email);

        // When
        String extractedEmail = jwtService.extractEmail(token);

        // Then
        assertThat(extractedEmail).isEqualTo(email);
    }
}

