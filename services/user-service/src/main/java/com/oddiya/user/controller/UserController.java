package com.oddiya.user.controller;

import com.oddiya.user.dto.CreateUserRequest;
import com.oddiya.user.dto.UpdateUserRequest;
import com.oddiya.user.dto.UserResponse;
import com.oddiya.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    // Internal API - for Auth Service only
    @PostMapping("/internal/users")
    public ResponseEntity<UserResponse> findOrCreateUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(userService.findOrCreateUser(request));
    }
}

