package com.oddiya.auth.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidRefreshTokenRequest() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");

        // When
        Set<ConstraintViolation<RefreshTokenRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void testInvalidRefreshTokenRequest_NullToken() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest(null);

        // When
        Set<ConstraintViolation<RefreshTokenRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("required");
    }

    @Test
    void testInvalidRefreshTokenRequest_EmptyToken() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest("");

        // When
        Set<ConstraintViolation<RefreshTokenRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
    }

}

