package com.oddiya.plan.controller;

import com.oddiya.plan.dto.TripCollectionResponse;
import com.oddiya.plan.service.TripCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final TripCollectionService tripCollectionService;
    
    @GetMapping("/trips")
    public ResponseEntity<TripCollectionResponse> getUserTrips(
            @RequestHeader("X-User-Id") Long userId
    ) {
        return ResponseEntity.ok(tripCollectionService.getUserTripsWithMedia(userId));
    }
}

