package com.oddiya.video.dto;

import com.oddiya.video.entity.VideoJob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoJobResponse {
    private Long id;
    private Long userId;
    private String status;
    private List<String> photoUrls;
    private String template;
    private String videoUrl;
    private UUID idempotencyKey;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static VideoJobResponse fromEntity(VideoJob job) {
        return VideoJobResponse.builder()
                .id(job.getId())
                .userId(job.getUserId())
                .status(job.getStatus())
                .photoUrls(job.getPhotoUrls() != null ? Arrays.asList(job.getPhotoUrls()) : null)
                .template(job.getTemplate())
                .videoUrl(job.getVideoUrl())
                .idempotencyKey(job.getIdempotencyKey())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }
}

