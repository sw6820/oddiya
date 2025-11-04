package com.oddiya.auth.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Service
public class UserServiceClient {

    @Value("${app.user-service.url}")
    private String userServiceUrl;

    private final WebClient webClient;

    public UserServiceClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8082").build();
    }

    /**
     * Create user with email/password
     */
    public UserResponse createUser(String email, String name, String provider, String passwordHash) {
        try {
            CreateEmailUserRequest request = new CreateEmailUserRequest(email, name, provider, passwordHash);
            return webClient.post()
                    .uri("/api/v1/users/internal/users/email")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(UserResponse.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
        } catch (Exception e) {
            log.error("Failed to create user in User Service", e);
            throw new RuntimeException("Failed to create user", e);
        }
    }

    /**
     * Find user by email
     */
    public UserResponse findUserByEmail(String email) {
        try {
            return webClient.get()
                    .uri("/api/v1/users/internal/users/email/{email}", email)
                    .retrieve()
                    .bodyToMono(UserResponse.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
        } catch (Exception e) {
            log.error("Failed to find user by email in User Service", e);
            return null;  // Return null if not found
        }
    }

    /**
     * Create or find user via OAuth
     */
    public UserResponse createOrFindUser(CreateUserRequest request) {
        try {
            return webClient.post()
                    .uri("/api/v1/users/internal/users")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(UserResponse.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
        } catch (Exception e) {
            log.error("Failed to create/find user in User Service", e);
            throw new RuntimeException("Failed to communicate with User Service", e);
        }
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class CreateUserRequest {
        private String email;
        private String name;
        private String provider;
        private String providerId;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class CreateEmailUserRequest {
        private String email;
        private String name;
        private String provider;
        private String passwordHash;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class UserResponse {
        private Long id;
        private String email;
        private String name;
        private String provider;
        private String providerId;
        private String passwordHash;  // For email/password authentication
    }
}

