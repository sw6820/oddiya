package com.oddiya.plan.service;

import com.oddiya.plan.dto.*;
import com.oddiya.plan.entity.PlanDetail;
import com.oddiya.plan.entity.TravelPlan;
import com.oddiya.plan.exception.LlmServiceException;
import com.oddiya.plan.repository.TravelPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Plan Service - Generates plans via LLM Agent and persists to database.
 * All planning logic is handled by Python LLM Agent.
 * Java service saves/retrieves plans from PostgreSQL.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlanService {
    private final LlmAgentClient llmAgentClient;
    private final TravelPlanRepository travelPlanRepository;

    @Value("${llm.agent.base-url}")
    private String llmAgentBaseUrl;

    /**
     * Create travel plan by calling Python LLM Agent and save to database.
     */
    public Mono<PlanResponse> createPlan(Long userId, CreatePlanRequest request) {
        log.info("[PlanService] Creating plan for user={}, destination='{}'",
            userId, request.getDestination());

        // Forward request to Python LLM Agent
        LlmRequest llmRequest = new LlmRequest();
        llmRequest.setLocation(request.getDestination());
        llmRequest.setStartDate(request.getStartDate().toString());
        llmRequest.setEndDate(request.getEndDate().toString());
        llmRequest.setBudget(request.getBudget() != null ? String.valueOf(request.getBudget()) : null);

        log.debug("[PlanService] → Python LLM Agent: {}", llmRequest);

        // Call Python LLM Agent and save result to database
        return llmAgentClient.generatePlan(llmRequest)
                .flatMap(llmResponse -> {
                    log.info("[PlanService] ← Python LLM Agent returned plan: {} days",
                        llmResponse.getDays() != null ? llmResponse.getDays().size() : 0);

                    // Create entity from LLM response
                    TravelPlan plan = new TravelPlan();
                    plan.setUserId(userId);
                    plan.setTitle(llmResponse.getTitle());
                    plan.setStartDate(request.getStartDate());
                    plan.setEndDate(request.getEndDate());
                    plan.setBudgetLevel(request.getBudget() != null ? String.valueOf(request.getBudget()) : "medium");
                    plan.setStatus("DRAFT");
                    plan.setCreatedAt(LocalDateTime.now());
                    plan.setUpdatedAt(LocalDateTime.now());

                    // Add plan details
                    if (llmResponse.getDays() != null) {
                        List<PlanDetail> details = llmResponse.getDays().stream()
                                .map(dayPlan -> {
                                    PlanDetail detail = new PlanDetail();
                                    detail.setPlan(plan);
                                    detail.setDay(dayPlan.getDay());
                                    detail.setLocation(dayPlan.getLocation());
                                    detail.setActivity(dayPlan.getActivity());
                                    detail.setCreatedAt(LocalDateTime.now());
                                    return detail;
                                })
                                .collect(Collectors.toList());
                        plan.setDetails(details);
                    }

                    // Save to database in background thread
                    return Mono.fromCallable(() -> {
                        TravelPlan savedPlan = travelPlanRepository.save(plan);
                        log.info("[PlanService] ✅ Plan saved to database: id={}", savedPlan.getId());
                        return convertToResponse(savedPlan);
                    });
                })
                .onErrorResume(error -> {
                    log.error("[PlanService] Failed to create plan: {}", error.getMessage());
                    return Mono.error(new LlmServiceException(
                        "Failed to generate plan via LLM Agent", error
                    ));
                });
    }

    /**
     * Get all plans for a user from database.
     */
    public List<PlanResponse> getUserPlans(Long userId) {
        log.info("[PlanService] Fetching all plans for user={}", userId);
        List<TravelPlan> plans = travelPlanRepository.findByUserIdOrderByCreatedAtDesc(userId);
        log.info("[PlanService] Found {} plans for user={}", plans.size(), userId);

        return plans.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific plan by ID.
     */
    public PlanResponse getPlan(Long planId, Long userId) {
        log.info("[PlanService] Fetching plan id={} for user={}", planId, userId);
        TravelPlan plan = travelPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found: " + planId));

        if (!plan.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to plan " + planId);
        }

        return convertToResponse(plan);
    }

    /**
     * Update a plan (dates, destination, etc).
     */
    public PlanResponse updatePlan(Long planId, Long userId, CreatePlanRequest request) {
        log.info("[PlanService] Updating plan id={} for user={}", planId, userId);
        TravelPlan plan = travelPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found: " + planId));

        if (!plan.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to plan " + planId);
        }

        plan.setStartDate(request.getStartDate());
        plan.setEndDate(request.getEndDate());
        plan.setUpdatedAt(LocalDateTime.now());

        TravelPlan updatedPlan = travelPlanRepository.save(plan);
        return convertToResponse(updatedPlan);
    }

    /**
     * Delete a plan.
     */
    public void deletePlan(Long planId, Long userId) {
        log.info("[PlanService] Deleting plan id={} for user={}", planId, userId);
        TravelPlan plan = travelPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found: " + planId));

        if (!plan.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to plan " + planId);
        }

        travelPlanRepository.delete(plan);
        log.info("[PlanService] ✅ Plan deleted: id={}", planId);
    }

    /**
     * Confirm a plan (change status to CONFIRMED).
     */
    public PlanResponse confirmPlan(Long planId, Long userId) {
        log.info("[PlanService] Confirming plan id={} for user={}", planId, userId);
        TravelPlan plan = travelPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found: " + planId));

        if (!plan.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to plan " + planId);
        }

        plan.setStatus("CONFIRMED");
        plan.setConfirmedAt(LocalDateTime.now());
        plan.setUpdatedAt(LocalDateTime.now());

        TravelPlan confirmedPlan = travelPlanRepository.save(plan);
        return convertToResponse(confirmedPlan);
    }

    /**
     * Complete a plan (change status to COMPLETED).
     */
    public PlanResponse completePlan(Long planId, Long userId) {
        log.info("[PlanService] Completing plan id={} for user={}", planId, userId);
        TravelPlan plan = travelPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found: " + planId));

        if (!plan.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to plan " + planId);
        }

        plan.setStatus("COMPLETED");
        plan.setCompletedAt(LocalDateTime.now());
        plan.setUpdatedAt(LocalDateTime.now());

        TravelPlan completedPlan = travelPlanRepository.save(plan);
        return convertToResponse(completedPlan);
    }

    /**
     * Helper method to convert entity to response DTO.
     */
    private PlanResponse convertToResponse(TravelPlan plan) {
        PlanResponse response = new PlanResponse();
        response.setId(plan.getId());
        response.setUserId(plan.getUserId());
        response.setTitle(plan.getTitle());
        response.setStartDate(plan.getStartDate());
        response.setEndDate(plan.getEndDate());
        response.setCreatedAt(plan.getCreatedAt());
        response.setUpdatedAt(plan.getUpdatedAt());

        if (plan.getDetails() != null && !plan.getDetails().isEmpty()) {
            List<PlanDetailResponse> detailResponses = plan.getDetails().stream()
                    .map(detail -> {
                        PlanDetailResponse detailResponse = new PlanDetailResponse();
                        detailResponse.setId(detail.getId());
                        detailResponse.setDay(detail.getDay());
                        detailResponse.setLocation(detail.getLocation());
                        detailResponse.setActivity(detail.getActivity());
                        return detailResponse;
                    })
                    .collect(Collectors.toList());
            response.setDetails(detailResponses);
        }

        return response;
    }
}

