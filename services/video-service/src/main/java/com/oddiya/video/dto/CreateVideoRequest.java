package com.oddiya.video.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreateVideoRequest {
    @NotEmpty(message = "Photo URLs are required")
    private List<String> photoUrls;

    private String template = "default";
}

