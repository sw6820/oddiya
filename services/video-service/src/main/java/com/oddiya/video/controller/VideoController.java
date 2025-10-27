package com.oddiya.video.controller;

import com.oddiya.video.dto.CreateVideoRequest;
import com.oddiya.video.dto.VideoJobResponse;
import com.oddiya.video.service.VideoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/videos")
@RequiredArgsConstructor
public class VideoController {
    private final VideoService videoService;

    @PostMapping
    public ResponseEntity<VideoJobResponse> createVideo(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("Idempotency-Key") UUID idempotencyKey,
            @Valid @RequestBody CreateVideoRequest request
    ) {
        VideoJobResponse response = videoService.createVideoJob(userId, idempotencyKey, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<VideoJobResponse>> getUserVideos(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(videoService.getUserJobs(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VideoJobResponse> getVideo(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId
    ) {
        return ResponseEntity.ok(videoService.getJob(id, userId));
    }
}

