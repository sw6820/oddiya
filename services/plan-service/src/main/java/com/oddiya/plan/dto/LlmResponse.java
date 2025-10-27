package com.oddiya.plan.dto;

import lombok.Data;

import java.util.List;

@Data
public class LlmResponse {
    private String title;
    private List<DayPlan> days;

    @Data
    public static class DayPlan {
        private Integer day;
        private String location;
        private String activity;
    }
}

