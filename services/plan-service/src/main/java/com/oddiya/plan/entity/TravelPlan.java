package com.oddiya.plan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "travel_plans", schema = "plan_service")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TravelPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlanDetail> details = new ArrayList<>();

    // Photo 관계 제거 - 완전 분리 (의존성 없음)
    // Photos는 PhotoService에서 plan_id로 조회

    @Column(length = 20)
    private String status = "DRAFT";  // DRAFT, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED

    private LocalDateTime confirmedAt;

    private LocalDateTime completedAt;

    @Column(length = 10)
    private String budgetLevel = "medium";  // low, medium, high

    private Integer totalCost = 0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

