package com.oddiya.auth.controller;

import com.oddiya.auth.dto.OAuthCallbackRequest;
import com.oddiya.auth.dto.RefreshTokenRequest;
import com.oddiya.auth.dto.TokenResponse;
import com.oddiya.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/oauth2")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;
    
    /**
     * Initiate OAuth flow - redirects to Google OAuth
     */
    @GetMapping("/authorize/google")
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
     */
    @PostMapping("/callback/google")
    public ResponseEntity<TokenResponse> callbackGoogle(@Valid @RequestBody OAuthCallbackRequest request) {
        TokenResponse tokens = authService.handleOAuthCallback("google", request.getCode(), request.getState());
        return ResponseEntity.ok(tokens);
    }
    
    /**
     * Refresh access token using refresh token
     */
    @PostMapping("/token")
    public ResponseEntity<TokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        TokenResponse tokens = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(tokens);
    }
    
    /**
     * JWKS endpoint for public key (RS256)
     */
    @GetMapping("/.well-known/jwks.json")
    public ResponseEntity<String> getJwks() {
        String jwks = authService.getJwks();
        return ResponseEntity.ok(jwks);
    }
}

