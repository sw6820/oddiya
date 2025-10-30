package com.oddiya.user.controller;

import com.oddiya.user.dto.CreateEmailUserRequest;
import com.oddiya.user.dto.CreateUserRequest;
import com.oddiya.user.dto.UpdateUserRequest;
import com.oddiya.user.dto.UserResponse;
import com.oddiya.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(userService.getCurrentUser(userId));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUser(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return ResponseEntity.ok(userService.updateCurrentUser(userId, request));
    }

    // Internal API - for Auth Service only (OAuth)
    @PostMapping("/internal/users")
    public ResponseEntity<UserResponse> findOrCreateUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(userService.findOrCreateUser(request));
    }

    // Internal API - for Auth Service only (Email/Password signup)
    @PostMapping("/internal/users/email")
    public ResponseEntity<UserResponse> createUserWithEmail(@Valid @RequestBody CreateEmailUserRequest request) {
        UserResponse user = userService.createUserWithEmail(request);
        return ResponseEntity.ok(user);
    }

    // Internal API - for Auth Service only (Email/Password login)
    @GetMapping("/internal/users/email/{email}")
    public ResponseEntity<UserResponse> findUserByEmail(@PathVariable String email) {
        Optional<UserResponse> user = userService.findUserByEmail(email);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

