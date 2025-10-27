package com.oddiya.plan.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class LlmRequest {
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
}

