package com.oddiya.plan.service;

import com.oddiya.plan.dto.PresignedUrlResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {
    
    @Value("${aws.s3.bucket:oddiya-storage}")
    private String bucketName;
    
    @Value("${aws.region:ap-northeast-2}")
    private String region;
    
    public PresignedUrlResponse generatePresignedUrl(Long userId, Long planId, String fileName) {
        // Generate unique key
        String fileExtension = fileName.substring(fileName.lastIndexOf("."));
        String photoKey = String.format("photos/user%d/plan%d/%s%s", 
            userId, planId, UUID.randomUUID(), fileExtension);
        
        // For local development, return mock URL
        if (isLocalMode()) {
            String photoUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", 
                bucketName, region, photoKey);
            
            return new PresignedUrlResponse(
                photoUrl,  // Use as upload URL for now
                photoKey,
                photoUrl
            );
        }
        
        // TODO: Real S3 presigned URL generation
        // software.amazon.awssdk.services.s3.presigner.S3Presigner
        // presigner.presignPutObject(...)
        
        String photoUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", 
            bucketName, region, photoKey);
            
        return new PresignedUrlResponse(
            photoUrl,
            photoKey,
            photoUrl
        );
    }
    
    private boolean isLocalMode() {
        return System.getenv("ENVIRONMENT") == null || 
               "development".equals(System.getenv("ENVIRONMENT"));
    }
}

