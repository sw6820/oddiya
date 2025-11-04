package com.oddiya.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()      // Mobile app auth endpoints
                .requestMatchers("/api/v1/auth/**").permitAll()   // Auth endpoints (v1)
                .requestMatchers("/oauth2/**").permitAll()        // OAuth authorize endpoints
                .requestMatchers("/login/oauth2/**").permitAll()  // OAuth callback endpoints
                .requestMatchers("/actuator/**").permitAll()      // Health check
                .requestMatchers("/.well-known/**").permitAll()   // JWKS endpoint
                .anyRequest().authenticated()
            );

        return http.build();
    }

    /**
     * Password encoder bean for hashing and verifying passwords
     * Uses BCrypt with strength 10
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}

