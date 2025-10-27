package com.oddiya.user.integration;

import com.oddiya.user.dto.CreateUserRequest;
import com.oddiya.user.dto.UpdateUserRequest;
import com.oddiya.user.entity.User;
import com.oddiya.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.0-alpine")
            .withDatabaseName("oddiya")
            .withUsername("oddiya_user")
            .withPassword("test")
            .withInitScript("test-schema.sql");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void testCreateUserViaInternalApi() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("test@example.com");
        request.setName("Test User");
        request.setProvider("google");
        request.setProviderId("google-123");

        mockMvc.perform(post("/api/v1/users/internal/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    void testGetCurrentUser() throws Exception {
        User user = new User();
        user.setEmail("existing@example.com");
        user.setName("Existing User");
        user.setProvider("google");
        user.setProviderId("google-456");
        User saved = userRepository.save(user);

        mockMvc.perform(get("/api/v1/users/me")
                        .header("X-User-Id", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("existing@example.com"));
    }

    @Test
    void testUpdateUser() throws Exception {
        User user = new User();
        user.setEmail("update@example.com");
        user.setName("Old Name");
        user.setProvider("google");
        user.setProviderId("google-789");
        User saved = userRepository.save(user);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("New Name");

        mockMvc.perform(patch("/api/v1/users/me")
                        .header("X-User-Id", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"));
    }
}

