package com.oddiya.plan.service;

import com.oddiya.plan.dto.*;
import com.oddiya.plan.entity.PlanDetail;
import com.oddiya.plan.entity.TravelPlan;
import com.oddiya.plan.exception.LlmServiceException;
import com.oddiya.plan.repository.TravelPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanService {
    private final TravelPlanRepository planRepository;
    private final LlmAgentClient llmAgentClient;

    @Transactional
    public Mono<PlanResponse> createPlan(Long userId, CreatePlanRequest request) {
        log.info("[PlanService] Creating plan for user={}, title='{}', dates={} to {}",
            userId, request.getTitle(), request.getStartDate(), request.getEndDate());

        // LLM-Only Architecture: No fallback to hardcoded data
        LlmRequest llmRequest = new LlmRequest();
        llmRequest.setTitle(request.getTitle());
        llmRequest.setStartDate(request.getStartDate().toString());
        llmRequest.setEndDate(request.getEndDate().toString());
        llmRequest.setBudget("medium");  // Default budget
        llmRequest.setLocation(extractLocationFromTitle(request.getTitle()));

        log.debug("[PlanService] Calling LLM Agent with location={}, budget={}",
            llmRequest.getLocation(), llmRequest.getBudget());

        return llmAgentClient.generatePlan(llmRequest)
                .doOnSuccess(response ->
                    log.debug("[PlanService] LLM Agent returned plan with {} days",
                        response.getDays() != null ? response.getDays().size() : 0))
                .map(llmResponse -> {
                    TravelPlan plan = new TravelPlan();
                    plan.setUserId(userId);
                    plan.setTitle(llmResponse.getTitle() != null ? llmResponse.getTitle() : request.getTitle());
                    plan.setStartDate(request.getStartDate());
                    plan.setEndDate(request.getEndDate());

                    if (llmResponse.getDays() != null) {
                        List<PlanDetail> details = llmResponse.getDays().stream()
                                .map(dayPlan -> {
                                    PlanDetail detail = new PlanDetail();
                                    detail.setPlan(plan);
                                    detail.setDay(dayPlan.getDay());
                                    detail.setLocation(dayPlan.getLocation());
                                    detail.setActivity(dayPlan.getActivity());
                                    return detail;
                                })
                                .collect(Collectors.toList());
                        plan.setDetails(details);
                    }

                    TravelPlan savedPlan = planRepository.save(plan);
                    log.info("[PlanService] Plan created successfully: id={}, userId={}, days={}",
                        savedPlan.getId(), userId, savedPlan.getDetails().size());
                    return PlanResponse.fromEntity(savedPlan);
                })
                .onErrorResume(error -> {
                    // LLM-Only: Return meaningful error instead of fake data
                    log.error("[PlanService] LLM Agent failed for user {}: {}", userId, error.getMessage(), error);
                    return Mono.error(new LlmServiceException(
                        "LLM Agent failed to generate travel plan", error
                    ));
                });
    }

    /**
     * Extract location from title for LLM context.
     * This is just for passing to LLM - Claude will handle all location-specific data.
     */
    private String extractLocationFromTitle(String title) {
        // Simple extraction: first word is usually the location
        String[] words = title.split(" ");
        if (words.length > 0) {
            return words[0].trim();
        }
        return title;  // Use full title if no spaces
    }

    public List<PlanResponse> getUserPlans(Long userId) {
        log.debug("[PlanService] Fetching plans for user={}", userId);
        List<PlanResponse> plans = planRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(PlanResponse::fromEntity)
                .collect(Collectors.toList());
        log.info("[PlanService] Retrieved {} plans for user={}", plans.size(), userId);
        return plans;
    }

    public PlanResponse getPlan(Long planId, Long userId) {
        log.debug("[PlanService] Fetching plan: id={}, userId={}", planId, userId);
        TravelPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> {
                    log.warn("[PlanService] Plan not found: id={}", planId);
                    return new RuntimeException("Plan not found");
                });

        if (!plan.getUserId().equals(userId)) {
            log.warn("[PlanService] Unauthorized access attempt: planId={}, userId={}, ownerId={}",
                planId, userId, plan.getUserId());
            throw new RuntimeException("Unauthorized access to plan");
        }

        log.info("[PlanService] Plan retrieved: id={}, userId={}", planId, userId);
        return PlanResponse.fromEntity(plan);
    }

    @Transactional
    public PlanResponse updatePlan(Long planId, Long userId, CreatePlanRequest request) {
        TravelPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        if (!plan.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to plan");
        }

        plan.setTitle(request.getTitle());
        plan.setStartDate(request.getStartDate());
        plan.setEndDate(request.getEndDate());

        TravelPlan updatedPlan = planRepository.save(plan);
        return PlanResponse.fromEntity(updatedPlan);
    }

    @Transactional
    public void deletePlan(Long planId, Long userId) {
        TravelPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        if (!plan.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to plan");
        }

        planRepository.delete(plan);
    }
    
    @Transactional
    public PlanResponse confirmPlan(Long planId, Long userId) {
        TravelPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        if (!plan.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        plan.setStatus("CONFIRMED");
        plan.setConfirmedAt(java.time.LocalDateTime.now());

        TravelPlan updated = planRepository.save(plan);
        return PlanResponse.fromEntity(updated);
    }
    
    @Transactional
    public PlanResponse completePlan(Long planId, Long userId) {
        TravelPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        if (!plan.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        plan.setStatus("COMPLETED");
        plan.setCompletedAt(java.time.LocalDateTime.now());

        TravelPlan updated = planRepository.save(plan);
        return PlanResponse.fromEntity(updated);
    }
    
    public List<String> getPhotoUrls(Long planId) {
        // Photos는 별도 PhotoService에서 조회
        // TravelPlan과 decoupled됨
        return new java.util.ArrayList<>();  // Empty list, PhotoService에서 처리
    }
}

