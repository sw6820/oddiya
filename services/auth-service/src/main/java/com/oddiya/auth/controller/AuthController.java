package com.oddiya.auth.controller;

import com.oddiya.auth.dto.*;
import com.oddiya.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri:http://localhost:8080/api/v1/auth/oauth/google/callback}")
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

        // prompt=select_account: Best for both web and mobile
        // - Shows account selection every time
        // - Users can switch accounts easily
        // - Doesn't force password re-entry (better UX for mobile)
        // - Standard industry practice (used by most apps)
        String googleAuthUrl = String.format(
            "https://accounts.google.com/o/oauth2/v2/auth?client_id=%s&redirect_uri=%s&response_type=code&scope=openid+profile+email&state=%s&prompt=select_account",
            googleClientId,
            redirectUri,
            state
        );
        return ResponseEntity.status(302).location(URI.create(googleAuthUrl)).build();
    }

    /**
     * Handle OAuth callback and generate tokens (GET - from Google redirect)
     * Redirects to frontend with tokens as URL parameters for browser-based flow
     * GET /api/v1/auth/oauth/google/callback
     */
    @GetMapping("/api/v1/auth/oauth/google/callback")
    public ResponseEntity<Void> callbackGoogleGet(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "error_description", required = false) String errorDescription,
            jakarta.servlet.http.HttpServletRequest request) {

        // Log all parameters for debugging
        System.out.println("=== OAuth Callback Parameters ===");
        System.out.println("code: " + code);
        System.out.println("state: " + state);
        System.out.println("error: " + error);
        System.out.println("error_description: " + errorDescription);
        System.out.println("Query String: " + request.getQueryString());
        System.out.println("================================");

        // Handle OAuth errors from Google
        if (error != null) {
            throw new RuntimeException("OAuth error: " + error + " - " + errorDescription);
        }

        if (code == null) {
            throw new RuntimeException("No authorization code received from Google. Query: " + request.getQueryString());
        }

        TokenResponse tokens = authService.handleOAuthCallback("google", code, state);

        // Redirect to frontend with tokens as URL parameters
        String redirectUrl = String.format(
            "http://localhost:8080/?accessToken=%s&refreshToken=%s&userId=%d",
            tokens.getAccessToken(),
            tokens.getRefreshToken(),
            tokens.getUserId()
        );

        return ResponseEntity.status(302).location(URI.create(redirectUrl)).build();
    }

    /**
     * Handle OAuth callback and generate tokens (POST - for mobile app)
     * POST /api/auth/oauth2/callback/google
     */
    @PostMapping("/api/auth/oauth2/callback/google")
    public ResponseEntity<TokenResponse> callbackGoogle(@Valid @RequestBody OAuthCallbackRequest request) {
        TokenResponse tokens = authService.handleOAuthCallback("google", request.getCode(), request.getState());
        return ResponseEntity.ok(tokens);
    }

    /**
     * OAuth2 success handler - generates JWT tokens
     * GET /oauth2/success
     */
    @GetMapping("/oauth2/success")
    public ResponseEntity<TokenResponse> oauthSuccess(@AuthenticationPrincipal OAuth2User oauthUser) {
        // Extract user info from OAuth2User
        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");
        String oauthId = oauthUser.getAttribute("sub");

        // Generate tokens
        TokenResponse tokens = authService.handleOAuthSuccess(email, name, "google", oauthId);
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

