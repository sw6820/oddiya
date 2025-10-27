package com.oddiya.user.service;

import com.oddiya.user.dto.UpdateUserRequest;
import com.oddiya.user.entity.User;
import com.oddi出面a.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetCurrentUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setName("Test User");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        var result = userService.getCurrentUser(1L);
        
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void testUpdateCurrentUser() {
        User user = new User();
        user.setId(1L);
        user.setName("Old Name");

        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("New Name");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        var result = userService.updateCurrentUser(1L, request);
        
        assertNotNull(result);
        verify(userRepository, times(1)).save(any(User.class));
    }
}

