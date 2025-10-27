package com.oddiya.plan.service;

import com.oddiya.plan.dto.CreatePlanRequest;
import com.oddiya.plan.dto.LlmResponse;
import com.oddiya.plan.entity.TravelPlan;
import com.oddiya.plan.repository.TravelPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PlanServiceTest {
    @Mock
    private TravelPlanRepository planRepository;

    @Mock
    private LlmAgentClient llmAgentClient;

    @InjectMocks
    private PlanService planService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreatePlan() {
        CreatePlanRequest request = new CreatePlanRequest();
        request.setTitle("Test Plan");
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(3));

        LlmResponse.DayPlan dayPlan = new LlmResponse.DayPlan();
        dayPlan.setDay(1);
        dayPlan.setLocation("Seoul");
        dayPlan.setActivity("Visit palaces");

        LlmResponse llmResponse = new LlmResponse();
        llmResponse.setDays(List.of(dayPlan));

        TravelPlan savedPlan = new TravelPlan();
        savedPlan.setId(1L);
        savedPlan.setUserId(100L);
        savedPlan.setTitle("Test Plan");

        when(llmAgentClient.generatePlan(any())).thenReturn(Mono.just(llmResponse));
        when(planRepository.save(any(TravelPlan.class))).thenReturn(savedPlan);

        Mono<Object> result = planService.createPlan(100L, request);

        assertNotNull(result);
        verify(llmAgentClient, times(1)).generatePlan(any());
        verify(planRepository, times(1)).save(any(TravelPlan.class));
    }
}

