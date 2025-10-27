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
        // Create plan without LLM for now (LLM integration can be added later)
        TravelPlan plan = new TravelPlan();
        plan.setUserId(userId);
        plan.setTitle(request.getTitle());
        plan.setStartDate(request.getStartDate());
        plan.setEndDate(request.getEndDate());
        
        // Add simple default details
        PlanDetail detail1 = new PlanDetail();
        detail1.setPlan(plan);
        detail1.setDay(1);
        detail1.setLocation("City Center");
        detail1.setActivity("Explore and enjoy!");
        
        plan.setDetails(List.of(detail1));
        
        TravelPlan savedPlan = planRepository.save(plan);
        return Mono.just(PlanResponse.fromEntity(savedPlan));
        
        // TODO: Integrate with LLM Agent for AI-generated plans
        // Uncomment below when LLM Agent is fully working:
        /*
        LlmRequest llmRequest = new LlmRequest();
        llmRequest.setTitle(request.getTitle());
        llmRequest.setStartDate(request.getStartDate());
        llmRequest.setEndDate(request.getEndDate());

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
                });
        */
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

