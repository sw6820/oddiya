package com.oddiya.plan.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PhotoUploadRequest {
    @NotBlank
    private String fileName;
    
    private String contentType;
}

