package com.oddiya.video.service;

import com.oddiya.video.dto.CreateVideoRequest;
import com.oddiya.video.entity.VideoJob;
import com.oddiya.video.repository.VideoJobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VideoServiceTest {
    @Mock
    private VideoJobRepository jobRepository;

    @Mock
    private SqsService sqsService;

    @InjectMocks
    private VideoService videoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateVideoJob() {
        UUID idempotencyKey = UUID.randomUUID();
        CreateVideoRequest request = new CreateVideoRequest();
        request.setPhotoUrls(List.of("https://s3.amazonaws.com/photo1.jpg"));
        request.setTemplate("default");

        VideoJob savedJob = new VideoJob();
        savedJob.setId(1L);
        savedJob.setUserId(100L);
        savedJob.setStatus("PENDING");
        savedJob.setIdempotencyKey(idempotencyKey);

        when(jobRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(jobRepository.save(any(VideoJob.class))).thenReturn(savedJob);

        var result = videoService.createVideoJob(100L, idempotencyKey, request);

        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
        verify(sqsService, times(1)).publishVideoJob(any(), any(), any(), any());
    }

    @Test
    void testIdempotency() {
        UUID idempotencyKey = UUID.randomUUID();
        CreateVideoRequest request = new CreateVideoRequest();
        request.setPhotoUrls(List.of("https://s3.amazonaws.com/photo1.jpg"));

        VideoJob existingJob = new VideoJob();
        existingJob.setId(1L);
        existingJob.setIdempotencyKey(idempotencyKey);
        existingJob.setStatus("COMPLETED");

        when(jobRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.of(existingJob));

        var result = videoService.createVideoJob(100L, idempotencyKey, request);

        assertEquals("COMPLETED", result.getStatus());
        verify(sqsService, never()).publishVideoJob(any(), any(), any(), any());
    }
}

