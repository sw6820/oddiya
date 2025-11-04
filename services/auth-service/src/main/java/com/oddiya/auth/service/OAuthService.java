package com.oddiya.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthService {

    private final WebClient webClient;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

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

    /**
     * Exchange authorization code for access token
     */
    public String exchangeCodeForToken(String code, String provider) {
        log.info("Exchanging code for token for provider: {}", provider);

        if ("google".equals(provider)) {
            return exchangeGoogleCodeForToken(code);
        }

        throw new IllegalArgumentException("Unsupported OAuth provider: " + provider);
    }

    private String exchangeGoogleCodeForToken(String code) {
        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("code", code);
            formData.add("client_id", googleClientId);
            formData.add("client_secret", googleClientSecret);
            formData.add("redirect_uri", redirectUri);
            formData.add("grant_type", "authorization_code");

            GoogleTokenResponse tokenResponse = webClient.post()
                    .uri("https://oauth2.googleapis.com/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(GoogleTokenResponse.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            if (tokenResponse == null || tokenResponse.getAccessToken() == null) {
                throw new RuntimeException("Failed to get access token from Google");
            }

            log.info("Successfully exchanged code for Google access token");
            return tokenResponse.getAccessToken();

        } catch (Exception e) {
            log.error("Failed to exchange code for Google token", e);
            throw new RuntimeException("Failed to exchange OAuth code for token", e);
        }
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

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class GoogleTokenResponse {
        private String access_token;
        private String refresh_token;
        private Integer expires_in;
        private String scope;
        private String token_type;
        private String id_token;

        public String getAccessToken() {
            return access_token;
        }
    }
}

