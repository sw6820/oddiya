package com.oddiya.auth.controller;

import com.oddiya.auth.dto.TokenResponse;
import com.oddiya.auth.service.AuthService;
import com.oddiya.auth.service.GoogleTokenVerificationService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Mobile-specific authentication endpoints
 * For iOS and Android apps using Google Sign-In SDK
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class MobileAuthController {

    private final AuthService authService;
    private final GoogleTokenVerificationService googleTokenVerificationService;

    /**
     * Verify Google ID Token from mobile app (iOS/Android Google Sign-In SDK)
     * POST /api/v1/auth/google/verify
     *
     * Mobile Flow:
     * 1. User signs in with Google Sign-In SDK
     * 2. App receives ID Token
     * 3. App sends ID Token to this endpoint
     * 4. Backend verifies token and returns JWT tokens
     */
    @PostMapping("/google/verify")
    public ResponseEntity<TokenResponse> verifyGoogleToken(@Valid @RequestBody VerifyTokenRequest request) {
        // Verify Google ID Token
        GoogleTokenVerificationService.GoogleUserInfo userInfo = googleTokenVerificationService.verifyIdToken(request.getIdToken());

        // Create or find user and generate tokens
        TokenResponse tokens = authService.handleOAuthSuccess(
            userInfo.getEmail(),
            userInfo.getName(),
            "google",
            userInfo.getSub()
        );

        return ResponseEntity.ok(tokens);
    }

    /**
     * Apple Sign-In for iOS (to be implemented)
     * POST /api/v1/auth/apple/verify
     */
    @PostMapping("/apple/verify")
    public ResponseEntity<TokenResponse> verifyAppleToken(@Valid @RequestBody VerifyTokenRequest request) {
        // TODO: Implement Apple Sign-In verification
        throw new UnsupportedOperationException("Apple Sign-In not yet implemented");
    }

    @Data
    public static class VerifyTokenRequest {
        private String idToken;
    }
}
