package com.oddiya.plan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripCollectionResponse {
    private TripStatistics statistics;
    private List<CompletedTrip> completedTrips;
    private List<UpcomingTrip> upcomingTrips;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TripStatistics {
        private Integer totalTrips;
        private List<String> citiesVisited;
        private Integer totalVideos;
        private Integer totalDays;
        private Integer totalCost;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompletedTrip {
        private PlanResponse plan;
        private List<PhotoResponse> photos;
        private VideoSummary video;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpcomingTrip {
        private Long id;
        private String title;
        private String startDate;
        private Integer daysUntil;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PhotoResponse {
        private Long id;
        private String url;
        private Integer order;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VideoSummary {
        private Long id;
        private String videoUrl;
        private String status;
    }
}

