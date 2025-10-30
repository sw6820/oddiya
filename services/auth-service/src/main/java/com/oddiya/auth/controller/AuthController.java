package com.oddiya.auth.controller;

import com.oddiya.auth.dto.*;
import com.oddiya.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri:http://localhost:8080/oauth2/callback/google}")
    private String redirectUri;

    // ============================================================================
    // Email/Password Authentication (for Mobile App)
    // ============================================================================

    /**
     * Email/Password Signup
     * POST /api/auth/signup
     */
    @PostMapping("/api/auth/signup")
    public ResponseEntity<TokenResponse> signup(@Valid @RequestBody SignupRequest request) {
        TokenResponse tokens = authService.signup(request);
        return ResponseEntity.ok(tokens);
    }

    /**
     * Email/Password Login
     * POST /api/auth/login
     */
    @PostMapping("/api/auth/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse tokens = authService.login(request);
        return ResponseEntity.ok(tokens);
    }

    /**
     * Refresh Access Token
     * POST /api/auth/refresh
     */
    @PostMapping("/api/auth/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        TokenResponse tokens = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(tokens);
    }

    // ============================================================================
    // OAuth 2.0 Authentication (Google)
    // ============================================================================
    
    /**
     * Initiate OAuth flow - redirects to Google OAuth
     * GET /oauth2/authorize/google
     */
    @GetMapping("/oauth2/authorize/google")
    public ResponseEntity<Void> authorizeGoogle() {
        String state = UUID.randomUUID().toString();
        // Store state in session or Redis for CSRF protection
        // For now, we'll use a simple redirect URL
        String googleAuthUrl = String.format(
            "https://accounts.google.com/o/oauth2/v2/auth?client_id=%s&redirect_uri=%s&response_type=code&scope=openid+profile+email&state=%s",
            "your-google-client-id",  // Should be from config
            redirectUri,
            state
        );
        return ResponseEntity.status(302).location(URI.create(googleAuthUrl)).build();
    }

    /**
     * Handle OAuth callback and generate tokens
     * POST /api/auth/oauth2/callback/google
     */
    @PostMapping("/api/auth/oauth2/callback/google")
    public ResponseEntity<TokenResponse> callbackGoogle(@Valid @RequestBody OAuthCallbackRequest request) {
        TokenResponse tokens = authService.handleOAuthCallback("google", request.getCode(), request.getState());
        return ResponseEntity.ok(tokens);
    }

    /**
     * JWKS endpoint for public key (RS256)
     * GET /.well-known/jwks.json
     */
    @GetMapping("/.well-known/jwks.json")
    public ResponseEntity<String> getJwks() {
        String jwks = authService.getJwks();
        return ResponseEntity.ok(jwks);
    }
}

