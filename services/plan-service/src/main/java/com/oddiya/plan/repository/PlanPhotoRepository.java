package com.oddiya.plan.repository;

import com.oddiya.plan.entity.PlanPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanPhotoRepository extends JpaRepository<PlanPhoto, Long> {
    List<PlanPhoto> findByPlanIdOrderByUploadOrderAsc(Long planId);
    Long countByPlanId(Long planId);
}

