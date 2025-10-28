package com.oddiya.plan.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddPhotoRequest {
    @NotBlank
    private String photoUrl;
    
    @NotBlank
    private String s3Key;
    
    private Integer order;
}

