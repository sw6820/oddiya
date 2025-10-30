package com.oddiya.plan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "plan_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private TravelPlan plan;

    @Column(nullable = false)
    private Integer day;

    @Column(nullable = false)
    private String location;

    @Column(columnDefinition = "TEXT")
    private String activity;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

