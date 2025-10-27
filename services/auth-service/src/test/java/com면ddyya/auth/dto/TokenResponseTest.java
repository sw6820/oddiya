package com.oddiya.auth.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenResponseTest {

    @Test
    void testTokenResponseBuilder() {
        // Given & When
        TokenResponse response = TokenResponse.builder()
                .accessToken("test-access-token")
                .refreshToken("test-refresh-token")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .build();

        // Then
        assertThat(response.getAccessToken()).isEqualTo("test-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("test-refresh-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(3600L);
    }

    @Test
    void testTokenResponseDefaultValues() {
        // Given & When
        TokenResponse response = new TokenResponse();

        // Then
        assertThat(response.getTokenType()).isEqualTo("Bearer");
    }

}

