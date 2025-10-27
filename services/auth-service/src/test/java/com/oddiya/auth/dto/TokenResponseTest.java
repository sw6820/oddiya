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
        assert》)

