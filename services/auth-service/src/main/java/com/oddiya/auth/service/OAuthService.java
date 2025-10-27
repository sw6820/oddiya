package com.oddiya.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthService {

    private final WebClient webClient;

    public GoogleUserInfo getUserInfoFromGoogle(String accessToken) {
        try {
            return webClient.get()
                    .uri("https://www.googleapis.com/oauth2/v2/userinfo")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(GoogleUserInfo.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
        } catch (Exception e) {
            log.error("Failed to fetch user info from Google", e);
            throw new RuntimeException("Failed to fetch user info from OAuth provider", e);
        }
    }

    // Mock method - in production, implement actual OAuth token exchange
    public String exchangeCodeForToken(String code, String provider) {
        log.info("Exchanging code for token for provider: {}", provider);
        // TODO: Implement actual OAuth token exchange
        // For now, return a mock token
        return "mock_access_token_for_" + code;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class GoogleUserInfo {
        private String id;
        private String email;
        private String name;
        private String picture;
    }
}

