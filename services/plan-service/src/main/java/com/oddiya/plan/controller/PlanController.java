package com.oddiya.plan.controller;

import com.oddiya.plan.dto.CreatePlanRequest;
import com.oddiya.plan.dto.PlanResponse;
import com.oddiya.plan.service.PlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/plans")
@RequiredArgsConstructor
public class PlanController {
    private final PlanService planService;

    @PostMapping
    public Mono<ResponseEntity<PlanResponse>> createPlan(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CreatePlanRequest request
    ) {
        return planService.createPlan(userId, request)
                .map(ResponseEntity::ok);
    }

    @GetMapping
    public ResponseEntity<List<PlanResponse>> getUserPlans(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(planService.getUserPlans(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlanResponse> getPlan(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId
    ) {
        return ResponseEntity.ok(planService.getPlan(id, userId));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PlanResponse> updatePlan(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CreatePlanRequest request
    ) {
        return ResponseEntity.ok(planService.updatePlan(id, userId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deletePlan(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId
    ) {
        planService.deletePlan(id, userId);
        return ResponseEntity.ok(Map.of("message", "Plan deleted successfully"));
    }
}

