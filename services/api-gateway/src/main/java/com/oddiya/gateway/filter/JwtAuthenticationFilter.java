package com.oddiya.gateway.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.List;

@Slf4j
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Paths that don't require authentication
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/",
            "/api/v1/auth/",
            "/oauth2/",
            "/login/oauth2/",
            "/actuator/",
            "/.well-known/",
            "/"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();

        // Skip authentication for public paths
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // Get Authorization header
        String authHeader = request.getHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for path: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        try {
            // Parse JWT token (without signature validation for now)
            // In production, this should validate the signature using public key from JWKS
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid JWT token format");
            }

            // Decode payload (Base64 URL decode)
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            log.debug("JWT payload: {}", payload);

            // Parse JSON payload
            JsonNode claims = objectMapper.readTree(payload);

            Long userId = claims.has("userId") ? claims.get("userId").asLong() : null;

            if (userId == null) {
                log.warn("Missing userId in JWT token");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            log.info("Authenticated request for userId: {} to path: {}", userId, path);

            // Add X-User-Id header for downstream services
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", String.valueOf(userId))
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
            log.error("JWT validation failed: {}", e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    private boolean isPublicPath(String path) {
        // Exact match for root path
        if (path.equals("/") || path.equals("/index.html") || path.equals("/favicon.ico")) {
            return true;
        }
        // Prefix match for other public paths
        return PUBLIC_PATHS.stream()
                .filter(p -> !p.equals("/"))  // Exclude "/" from prefix matching
                .anyMatch(path::startsWith);
    }

    @Override
    public int getOrder() {
        return -100;  // Execute before routing
    }
}
