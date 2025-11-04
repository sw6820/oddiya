package com.oddiya.plan.service;

import com.oddiya.plan.dto.PlanResponse;
import com.oddiya.plan.dto.TripCollectionResponse;
import com.oddiya.plan.entity.PlanPhoto;
import com.oddiya.plan.entity.TravelPlan;
import com.oddiya.plan.repository.PlanPhotoRepository;
import com.oddiya.plan.repository.TravelPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripCollectionService {
    private final TravelPlanRepository planRepository;
    private final PlanPhotoRepository photoRepository;
    
    public TripCollectionResponse getUserTripsWithMedia(Long userId) {
        List<TravelPlan> allPlans = planRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        // Statistics
        List<String> cities = allPlans.stream()
            .map(p -> extractCity(p.getTitle()))
            .distinct()
            .collect(Collectors.toList());
        
        int totalDays = allPlans.stream()
            .mapToInt(p -> (int) ChronoUnit.DAYS.between(p.getStartDate(), p.getEndDate()) + 1)
            .sum();
        
        int totalCost = allPlans.stream()
            .mapToInt(p -> p.getTotalCost() != null ? p.getTotalCost() : 0)
            .sum();
        
        TripCollectionResponse.TripStatistics stats = TripCollectionResponse.TripStatistics.builder()
            .totalTrips(allPlans.size())
            .citiesVisited(cities)
            .totalVideos(0)  // TODO: Count from video service
            .totalDays(totalDays)
            .totalCost(totalCost)
            .build();
        
        // Completed trips
        List<TripCollectionResponse.CompletedTrip> completed = allPlans.stream()
            .filter(p -> "COMPLETED".equals(p.getStatus()) || p.getEndDate().isBefore(LocalDate.now()))
            .map(plan -> {
                List<PlanPhoto> photos = photoRepository.findByPlanIdOrderByUploadOrderAsc(plan.getId());
                
                return TripCollectionResponse.CompletedTrip.builder()
                    .plan(PlanResponse.fromEntity(plan))
                    .photos(photos.stream()
                        .map(photo -> TripCollectionResponse.PhotoResponse.builder()
                            .id(photo.getId())
                            .url(photo.getPhotoUrl())
                            .order(photo.getUploadOrder())
                            .build())
                        .collect(Collectors.toList()))
                    .video(null)  // TODO: Fetch from video service
                    .build();
            })
            .collect(Collectors.toList());
        
        // Upcoming trips
        List<TripCollectionResponse.UpcomingTrip> upcoming = allPlans.stream()
            .filter(p -> "CONFIRMED".equals(p.getStatus()) && p.getStartDate().isAfter(LocalDate.now()))
            .map(plan -> {
                int daysUntil = (int) ChronoUnit.DAYS.between(LocalDate.now(), plan.getStartDate());
                
                return TripCollectionResponse.UpcomingTrip.builder()
                    .id(plan.getId())
                    .title(plan.getTitle())
                    .startDate(plan.getStartDate().toString())
                    .daysUntil(daysUntil)
                    .build();
            })
            .collect(Collectors.toList());
        
        return TripCollectionResponse.builder()
            .statistics(stats)
            .completedTrips(completed)
            .upcomingTrips(upcoming)
            .build();
    }
    
    private String extractCity(String title) {
        if (title.contains("서울") || title.toLowerCase().contains("seoul")) return "Seoul";
        if (title.contains("부산") || title.toLowerCase().contains("busan")) return "Busan";
        if (title.contains("제주") || title.toLowerCase().contains("jeju")) return "Jeju";
        return "Korea";
    }
}

