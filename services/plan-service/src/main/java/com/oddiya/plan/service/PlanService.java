package com.oddiya.plan.service;

import com.oddiya.plan.dto.*;
import com.oddiya.plan.entity.PlanDetail;
import com.oddiya.plan.entity.TravelPlan;
import com.oddiya.plan.repository.TravelPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanService {
    private final TravelPlanRepository planRepository;
    private final LlmAgentClient llmAgentClient;

    @Transactional
    public Mono<PlanResponse> createPlan(Long userId, CreatePlanRequest request) {
        // Create LLM request with enhanced data
        LlmRequest llmRequest = new LlmRequest();
        llmRequest.setTitle(request.getTitle());
        llmRequest.setStartDate(request.getStartDate().toString());
        llmRequest.setEndDate(request.getEndDate().toString());
        llmRequest.setBudget("medium");  // Default budget
        llmRequest.setLocation(extractLocation(request.getTitle()));  // Extract from title

        return llmAgentClient.generatePlan(llmRequest)
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
                    return PlanResponse.fromEntity(savedPlan);
                })
                .onErrorResume(error -> {
                    // Fallback: create simple plan if LLM fails
                    TravelPlan plan = new TravelPlan();
                    plan.setUserId(userId);
                    plan.setTitle(request.getTitle());
                    plan.setStartDate(request.getStartDate());
                    plan.setEndDate(request.getEndDate());
                    
                    PlanDetail detail = new PlanDetail();
                    detail.setPlan(plan);
                    detail.setDay(1);
                    detail.setLocation("City Center");
                    detail.setActivity("Explore and enjoy!");
                    plan.setDetails(List.of(detail));
                    
                    TravelPlan savedPlan = planRepository.save(plan);
                    return Mono.just(PlanResponse.fromEntity(savedPlan));
                });
    }
    
    private String extractLocation(String title) {
        // Extract city name from title (simple logic)
        if (title.contains("서울") || title.toLowerCase().contains("seoul")) return "Seoul";
        if (title.contains("부산") || title.toLowerCase().contains("busan")) return "Busan";
        if (title.contains("제주") || title.toLowerCase().contains("jeju")) return "Jeju";
        return "Seoul";  // Default
    }

    public List<PlanResponse> getUserPlans(Long userId) {
        return planRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(PlanResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public PlanResponse getPlan(Long planId, Long userId) {
        TravelPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        if (!plan.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to plan");
        }

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
}

