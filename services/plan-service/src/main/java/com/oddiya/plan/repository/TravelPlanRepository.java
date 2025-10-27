package com.oddiya.plan.repository;

import com.oddiya.plan.entity.TravelPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TravelPlanRepository extends JpaRepository<TravelPlan, Long> {
    List<TravelPlan> findByUserIdOrderByCreatedAtDesc(Long userId);
}

