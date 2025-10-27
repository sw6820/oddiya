package com.oddiya.auth.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class OAuthCallbackRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidOAuthCallbackRequest() {
        // Given
        OAuthCallbackRequest request = new OAuthCallbackRequest("auth-code", "state-value");

        // When
        Set<ConstraintViolation<OAuthCallbackRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void testInvalidOAuthCallbackRequest_NullCode() {
        // Given
        OAuthCallbackRequest request = new OAuthCallbackRequest(null, "state-value");

        // When
        Set<ConstraintViolation<OAuthCallbackRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("required");
    }

    @Test
    void testInvalidOAuthCallbackRequest_NullState() {
        // Given
        OAuthCallbackRequest request = new OAuthCallbackRequest("auth-code", null);

        // When
        Set<ConstraintViolation<OAuthCallbackRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
    }

}

