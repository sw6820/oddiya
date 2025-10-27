package com.oddiya.plan.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class LlmRequest {
    private String title;
    private String startDate;  // Changed to String for JSON serialization
    private String endDate;    // Changed to String for JSON serialization
    private String budget;     // low, medium, high
    private String location;   // City name
}

