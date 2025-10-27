package com.oddiya.plan.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class LlmResponse {
    private String title;
    private List<DayPlan> days;
    private Integer totalEstimatedCost;
    private String currency;
    private String weatherSummary;
    private List<String> tips;

    @Data
    public static class DayPlan {
        private Integer day;
        private String location;
        private String activity;
        private Map<String, Object> details;
        private Integer estimatedCost;
        private String weatherTip;
    }
}

