package com.oddiya.plan.integration;

import com.oddiya.plan.dto.CreatePlanRequest;
import com.oddiya.plan.entity.TravelPlan;
import com.oddiya.plan.repository.TravelPlanRepository;
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

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class PlanIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TravelPlanRepository planRepository;

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
    void testGetUserPlans() throws Exception {
        // Create test plan directly in database
        TravelPlan plan = new TravelPlan();
        plan.setUserId(1L);
        plan.setTitle("Test Plan");
        plan.setStartDate(LocalDate.now().plusDays(1));
        plan.setEndDate(LocalDate.now().plusDays(3));
        planRepository.save(plan);

        mockMvc.perform(get("/api/v1/plans")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Plan"));
    }

    @Test
    void testGetPlanById() throws Exception {
        TravelPlan plan = new TravelPlan();
        plan.setUserId(1L);
        plan.setTitle("Seoul Trip");
        plan.setStartDate(LocalDate.now().plusDays(1));
        plan.setEndDate(LocalDate.now().plusDays(3));
        TravelPlan saved = planRepository.save(plan);

        mockMvc.perform(get("/api/v1/plans/" + saved.getId())
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Seoul Trip"));
    }

    @Test
    void testDatabaseConnectivity() {
        assert postgres.isRunning();
        long count = planRepository.count();
        assert count >= 0;
    }
}

