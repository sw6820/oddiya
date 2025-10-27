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
                    // Fallback: create realistic plan if LLM fails
                    String location = extractLocation(request.getTitle());
                    
                    TravelPlan plan = new TravelPlan();
                    plan.setUserId(userId);
                    plan.setTitle(request.getTitle());
                    plan.setStartDate(request.getStartDate());
                    plan.setEndDate(request.getEndDate());
                    
                    // Generate better default activities based on location
                    List<PlanDetail> details = generateDefaultActivities(location, plan);
                    plan.setDetails(details);
                    
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
    
    private List<PlanDetail> generateDefaultActivities(String location, TravelPlan plan) {
        // Generate realistic default activities by location
        java.time.LocalDate start = plan.getStartDate();
        java.time.LocalDate end = plan.getEndDate();
        int numDays = (int) java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
        
        List<PlanDetail> details = new java.util.ArrayList<>();
        
        for (int day = 1; day <= numDays; day++) {
            PlanDetail detail = new PlanDetail();
            detail.setPlan(plan);
            detail.setDay(day);
            
            if (location.equals("Seoul")) {
                switch (day) {
                    case 1:
                        detail.setLocation("Gyeongbokgung Palace & Bukchon");
                        detail.setActivity("Morning: Royal palace tour (₩3,000), Afternoon: Traditional Hanok village walk (Free), Evening: Insadong traditional street (₩30,000 dinner)");
                        break;
                    case 2:
                        detail.setLocation("Myeongdong & N Seoul Tower");
                        detail.setActivity("Morning: Shopping at Myeongdong (₩50,000), Afternoon: Cable car to N Seoul Tower (₩14,000), Evening: Han River picnic (₩20,000)");
                        break;
                    case 3:
                        detail.setLocation("Gangnam & COEX");
                        detail.setActivity("Morning: Gangnam shopping (₩40,000), Afternoon: COEX Aquarium (₩27,000), Evening: K-Star Road (₩25,000)");
                        break;
                    default:
                        detail.setLocation("Seoul City Exploration");
                        detail.setActivity("Explore local markets, try street food, and enjoy the city atmosphere");
                        break;
                }
            } else if (location.equals("Busan")) {
                switch (day) {
                    case 1:
                        detail.setLocation("Haeundae Beach & Dongbaek Island");
                        detail.setActivity("Morning: Beach walk (Free), Afternoon: Dongbaek Island trail (Free), Evening: Seafood dinner at Jagalchi Market (₩40,000)");
                        break;
                    case 2:
                        detail.setLocation("Gamcheon Culture Village");
                        detail.setActivity("Morning: Colorful village tour (Free), Afternoon: Songdo Beach cable car (₩15,000), Evening: Gwangalli Beach (₩30,000)");
                        break;
                    default:
                        detail.setLocation("Busan Coastal Tour");
                        detail.setActivity("Explore beaches and seafood markets");
                        break;
                }
            } else if (location.equals("Jeju")) {
                switch (day) {
                    case 1:
                        detail.setLocation("Seongsan Ilchulbong & Seopjikoji");
                        detail.setActivity("Morning: Sunrise peak hike (₩5,000), Afternoon: Seopjikoji coast walk (Free), Evening: Black pork BBQ (₩35,000)");
                        break;
                    case 2:
                        detail.setLocation("Hallasan & Folk Village");
                        detail.setActivity("Morning: National park trail (₩5,000), Afternoon: Folk village (₩11,000), Evening: Local seafood (₩40,000)");
                        break;
                    default:
                        detail.setLocation("Jeju Nature Exploration");
                        detail.setActivity("Explore natural beauty and local culture");
                        break;
                }
            } else {
                detail.setLocation(location + " Highlights");
                detail.setActivity("Explore major attractions, try local cuisine, and enjoy the culture");
            }
            
            details.add(detail);
        }
        
        return details;
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

