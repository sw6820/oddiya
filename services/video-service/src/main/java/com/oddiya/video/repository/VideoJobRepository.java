package com.oddiya.video.repository;

import com.oddiya.video.entity.VideoJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VideoJobRepository extends JpaRepository<VideoJob, Long> {
    Optional<VideoJob> findByIdempotencyKey(UUID idempotencyKey);
    List<VideoJob> findByUserIdOrderByCreatedAtDesc(Long userId);
}

