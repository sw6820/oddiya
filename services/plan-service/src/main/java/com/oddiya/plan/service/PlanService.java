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
                        detail.setLocation("경복궁 & 북촌한옥마을");
                        detail.setActivity("Morning: 궁궐 투어 및 수문장 교대식 관람 (₩3,000), Afternoon: 전통 한옥마을 산책 (무료), Evening: 인사동 전통거리 저녁식사 (₩30,000)");
                        break;
                    case 2:
                        detail.setLocation("명동 & N서울타워");
                        detail.setActivity("Morning: 명동 쇼핑 및 거리음식 (₩50,000), Afternoon: 남산 케이블카 및 N서울타워 전망대 (₩14,000), Evening: 한강공원 야경 감상 (₩20,000)");
                        break;
                    case 3:
                        detail.setLocation("강남 & 코엑스");
                        detail.setActivity("Morning: 강남역 쇼핑 및 카페 (₩40,000), Afternoon: 코엑스 아쿠아리움 (₩27,000), Evening: K-스타로드 및 강남 맛집 투어 (₩25,000)");
                        break;
                    default:
                        detail.setLocation("서울 도심 탐방");
                        detail.setActivity("Morning: 전통시장 구경 (₩20,000), Afternoon: 카페거리 탐방 (₩15,000), Evening: 야경 명소 방문 (₩25,000)");
                        break;
                }
            } else if (location.equals("Busan")) {
                switch (day) {
                    case 1:
                        detail.setLocation("해운대 해수욕장 & 동백섬");
                        detail.setActivity("Morning: 해변 산책 및 카페 (무료), Afternoon: 동백섬 둘레길 (무료), Evening: 자갈치시장 해산물 저녁 (₩40,000)");
                        break;
                    case 2:
                        detail.setLocation("감천문화마을");
                        detail.setActivity("Morning: 알록달록 벽화마을 투어 (무료), Afternoon: 송도 해상케이블카 (₩15,000), Evening: 광안리 해변 야경 (₩30,000)");
                        break;
                    default:
                        detail.setLocation("부산 해안 투어");
                        detail.setActivity("Morning: 해변 산책 (무료), Afternoon: 해산물 시장 구경 (₩20,000), Evening: 야경 명소 (₩25,000)");
                        break;
                }
            } else if (location.equals("Jeju")) {
                switch (day) {
                    case 1:
                        detail.setLocation("성산일출봉 & 섭지코지");
                        detail.setActivity("Morning: 일출봉 등반 및 해돋이 (₩5,000), Afternoon: 섭지코지 해안산책 (무료), Evening: 흑돼지 구이 맛집 (₩35,000)");
                        break;
                    case 2:
                        detail.setLocation("한라산 & 제주민속촌");
                        detail.setActivity("Morning: 한라산 둘레길 트레킹 (₩5,000), Afternoon: 제주민속촌 관람 (₩11,000), Evening: 해산물 요리 (₩40,000)");
                        break;
                    default:
                        detail.setLocation("제주 자연 탐방");
                        detail.setActivity("Morning: 오름 등반 (무료), Afternoon: 카페거리 투어 (₩20,000), Evening: 올레시장 먹거리 (₩25,000)");
                        break;
                }
            } else {
                detail.setLocation(location + " 명소 탐방");
                detail.setActivity("Morning: 주요 관광지 방문 (₩20,000), Afternoon: 현지 맛집 투어 (₩25,000), Evening: 야경 명소 (₩20,000)");
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
        TravelPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));
        
        return plan.getPhotos().stream()
                .sorted((a, b) -> Integer.compare(
                    a.getUploadOrder() != null ? a.getUploadOrder() : 0,
                    b.getUploadOrder() != null ? b.getUploadOrder() : 0
                ))
                .map(PlanPhoto::getPhotoUrl)
                .collect(java.util.stream.Collectors.toList());
    }
}

