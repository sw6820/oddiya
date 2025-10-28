package com.oddiya.plan.controller;

import com.oddiya.plan.dto.AddPhotoRequest;
import com.oddiya.plan.dto.PhotoUploadRequest;
import com.oddiya.plan.dto.PresignedUrlResponse;
import com.oddiya.plan.entity.PlanPhoto;
import com.oddiya.plan.service.PhotoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/plans/{planId}/photos")
@RequiredArgsConstructor
public class PhotoController {
    private final PhotoService photoService;
    
    @PostMapping("/presigned-url")
    public ResponseEntity<PresignedUrlResponse> getPresignedUrl(
            @PathVariable Long planId,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody PhotoUploadRequest request
    ) {
        return ResponseEntity.ok(photoService.getPresignedUrl(planId, userId, request));
    }
    
    @PostMapping
    public ResponseEntity<PlanPhoto> addPhoto(
            @PathVariable Long planId,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody AddPhotoRequest request
    ) {
        return ResponseEntity.ok(photoService.addPhoto(planId, userId, request));
    }
    
    @GetMapping
    public ResponseEntity<List<PlanPhoto>> getPlanPhotos(
            @PathVariable Long planId,
            @RequestHeader("X-User-Id") Long userId
    ) {
        return ResponseEntity.ok(photoService.getPlanPhotos(planId));
    }
}

