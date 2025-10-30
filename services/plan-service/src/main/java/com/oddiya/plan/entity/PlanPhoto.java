package com.oddiya.plan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "plan_photos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanPhoto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private TravelPlan plan;

    @Column(nullable = false)
    private String photoUrl;

    @Column(nullable = false)
    private String s3Key;

    private Integer uploadOrder;

    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();
}

