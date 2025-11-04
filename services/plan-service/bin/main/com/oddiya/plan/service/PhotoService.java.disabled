package com.oddiya.plan.service;

import com.oddiya.plan.dto.AddPhotoRequest;
import com.oddiya.plan.dto.PhotoUploadRequest;
import com.oddiya.plan.dto.PresignedUrlResponse;
import com.oddiya.plan.entity.PlanPhoto;
import com.oddiya.plan.entity.TravelPlan;
import com.oddiya.plan.repository.PlanPhotoRepository;
import com.oddiya.plan.repository.TravelPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PhotoService {
    private final TravelPlanRepository planRepository;
    private final PlanPhotoRepository photoRepository;
    private final S3Service s3Service;
    
    public PresignedUrlResponse getPresignedUrl(Long planId, Long userId, PhotoUploadRequest request) {
        // Verify plan belongs to user
        TravelPlan plan = planRepository.findById(planId)
            .orElseThrow(() -> new RuntimeException("Plan not found"));
        
        if (!plan.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        
        // Generate presigned URL
        return s3Service.generatePresignedUrl(userId, planId, request.getFileName());
    }
    
    @Transactional
    public PlanPhoto addPhoto(Long planId, Long userId, AddPhotoRequest request) {
        TravelPlan plan = planRepository.findById(planId)
            .orElseThrow(() -> new RuntimeException("Plan not found"));
        
        if (!plan.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        
        PlanPhoto photo = new PlanPhoto();
        photo.setPlan(plan);
        photo.setPhotoUrl(request.getPhotoUrl());
        photo.setS3Key(request.getS3Key());
        photo.setUploadOrder(request.getOrder());
        
        PlanPhoto saved = photoRepository.save(photo);
        
        // Update plan status if needed
        if ("CONFIRMED".equals(plan.getStatus())) {
            plan.setStatus("IN_PROGRESS");
            planRepository.save(plan);
        }
        
        return saved;
    }
    
    public List<PlanPhoto> getPlanPhotos(Long planId) {
        return photoRepository.findByPlanIdOrderByUploadOrderAsc(planId);
    }
}

