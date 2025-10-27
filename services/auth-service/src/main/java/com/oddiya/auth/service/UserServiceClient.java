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

    public UserResponse createOrFindUser(CreateUserRequest request) {
        try {
            return webClient.post()
                    .uri("/internal/users")
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
    public static class UserResponse {
        private Long id;
        private String email;
        private String name;
        private String provider;
        private String providerId;
    }
}

