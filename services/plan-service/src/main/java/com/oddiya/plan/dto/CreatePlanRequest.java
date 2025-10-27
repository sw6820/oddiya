package com.oddiya.plan.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreatePlanRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
}

