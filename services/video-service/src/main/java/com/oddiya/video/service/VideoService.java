package com.oddiya.video.service;

import com.oddiya.video.dto.CreateVideoRequest;
import com.oddiya.video.dto.VideoJobResponse;
import com.oddiya.video.entity.VideoJob;
import com.oddiya.video.repository.VideoJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VideoService {
    private final VideoJobRepository jobRepository;
    private final SqsService sqsService;

    @Transactional
    public VideoJobResponse createVideoJob(Long userId, UUID idempotencyKey, CreateVideoRequest request) {
        // Check idempotency
        Optional<VideoJob> existing = jobRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return VideoJobResponse.fromEntity(existing.get());
        }

        // Create new job
        VideoJob job = new VideoJob();
        job.setUserId(userId);
        job.setStatus("PENDING");
        job.setPhotoUrls(request.getPhotoUrls().toArray(new String[0]));
        job.setTemplate(request.getTemplate());
        job.setIdempotencyKey(idempotencyKey);

        VideoJob savedJob = jobRepository.save(job);

        // Publish to SQS
        sqsService.publishVideoJob(
                savedJob.getId(),
                savedJob.getUserId(),
                savedJob.getPhotoUrls(),
                savedJob.getTemplate()
        );

        return VideoJobResponse.fromEntity(savedJob);
    }

    public List<VideoJobResponse> getUserJobs(Long userId) {
        return jobRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(VideoJobResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public VideoJobResponse getJob(Long jobId, Long userId) {
        VideoJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (!job.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to job");
        }

        return VideoJobResponse.fromEntity(job);
    }
}

