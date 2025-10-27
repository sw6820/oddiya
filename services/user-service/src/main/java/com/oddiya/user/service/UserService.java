package com.oddiya.user.service;

import com.oddiya.user.dto.CreateUserRequest;
import com.oddiya.user.dto.UpdateUserRequest;
import com.oddiya.user.dto.UserResponse;
import com.oddiya.user.entity.User;
import com.oddiya.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserResponse getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return UserResponse.fromEntity(user);
    }

    @Transactional
    public UserResponse updateCurrentUser(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getName() != null) {
            user.setName(request.getName());
        }

        User updatedUser = userRepository.save(user);
        return UserResponse.fromEntity(updatedUser);
    }

    // Internal API - called by Auth Service only
    @Transactional
    public UserResponse findOrCreateUser(CreateUserRequest request) {
        Optional<User> existingUser = userRepository.findByProviderAndProviderId(
                request.getProvider(),
                request.getProviderId()
        );

        if (existingUser.isPresent()) {
            return UserResponse.fromEntity(existingUser.get());
        }

        // Check if email exists with different provider
        Optional<User> userByEmail = userRepository.findByEmail(request.getEmail());
        if (userByEmail.isPresent()) {
            User user = userByEmail.get();
            // Update existing user with new provider info
            user.setProvider(request.getProvider());
            user.setProviderId(request.getProviderId());
            user.setName(request.getName());
            User updatedUser = userRepository.save(user);
            return UserResponse.fromEntity(updatedUser);
        }

        // Create new user
        User newUser = new User();
        newUser.setEmail(request.getEmail());
        newUser.setName(request.getName());
        newUser.setProvider(request.getProvider());
        newUser.setProviderId(request.getProviderId());
        
        User savedUser = userRepository.save(newUser);
        return UserResponse.fromEntity(savedUser);
    }

    public Optional<UserResponse> findByProviderAndProviderId(String provider, String providerId) {
        return userRepository.findByProviderAndProviderId(provider, providerId)
                .map(UserResponse::fromEntity);
    }
}

