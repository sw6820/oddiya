package com.oddiya.auth.config;

import org.junit.jupiter.api.Test;

import java.security.KeyPair;

import static org.assertj.core.api.Assertions.assertThat;

class JwtConfigTest {

    @Test
    void testJwtConfigInitialization() {
        // Given & When
        JwtConfig config = new JwtConfig();

        // Then
        assertThat(config).isNotNull();
        assertThat(config.getAccessTokenValidity()).isEqualTo(3600L);
        assertThat(config.getRefreshTokenValidity()).isEqualTo(1209600L);
        assertThat(config.getKeyPair()).isNotNull();
    }

    @Test
    void testKeyPairIsValid() {
        // Given
        JwtConfig config = new JwtConfig();

        // When
        KeyPair keyPair = config.getKeyPair();

        // Then
        assertThat(keyPair.getPrivate()).isNotNull();
        assertThat(keyPair.getPublic()).isNotNull();
        assertThat(keyPair.getPublic().getAlgorithm()).isEqualTo("RSA");
    }

}

