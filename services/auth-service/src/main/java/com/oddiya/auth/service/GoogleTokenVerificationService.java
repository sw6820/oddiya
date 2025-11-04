package com.oddiya.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Service for verifying Google ID Tokens from mobile apps
 * Used when mobile apps use Google Sign-In SDK
 */
@Slf4j
@Service
public class GoogleTokenVerificationService {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    private final com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier verifier;

    public GoogleTokenVerificationService(@Value("${spring.security.oauth2.client.registration.google.client-id}") String clientId) {
        this.verifier = new com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                new GsonFactory()
        )
        .setAudience(Collections.singletonList(clientId))
        .build();
    }

    /**
     * Verify Google ID Token and extract user information
     *
     * @param idTokenString ID Token from mobile app
     * @return User information from token
     * @throws RuntimeException if token is invalid
     */
    public GoogleUserInfo verifyIdToken(String idTokenString) {
        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken == null) {
                log.error("Invalid Google ID Token");
                throw new RuntimeException("Invalid Google ID Token");
            }

            Payload payload = idToken.getPayload();

            GoogleUserInfo userInfo = new GoogleUserInfo();
            userInfo.setSub(payload.getSubject());
            userInfo.setEmail(payload.getEmail());
            userInfo.setName((String) payload.get("name"));
            userInfo.setPicture((String) payload.get("picture"));
            userInfo.setEmailVerified(payload.getEmailVerified());

            log.info("Verified Google ID Token for user: {}", userInfo.getEmail());

            return userInfo;

        } catch (Exception e) {
            log.error("Failed to verify Google ID Token", e);
            throw new RuntimeException("Failed to verify Google ID Token", e);
        }
    }

    @Data
    public static class GoogleUserInfo {
        private String sub;          // Google User ID
        private String email;
        private String name;
        private String picture;
        private Boolean emailVerified;
    }
}
