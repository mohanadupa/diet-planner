package com.dietmanager.persistence.repository;

import com.dietmanager.persistence.entity.HealthProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HealthProfileRepository extends JpaRepository<HealthProfileEntity, Long> {
    Optional<HealthProfileEntity> findByUserId(Long userId);
}
