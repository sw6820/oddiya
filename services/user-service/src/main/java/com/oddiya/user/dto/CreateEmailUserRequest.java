package com.oddiya.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a user with email/password authentication
 * Used by Auth Service for signup endpoint
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateEmailUserRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Provider is required")
    private String provider;

    @NotBlank(message = "Password hash is required")
    private String passwordHash;
}
