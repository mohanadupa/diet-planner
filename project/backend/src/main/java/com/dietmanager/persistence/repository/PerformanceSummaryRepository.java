package com.dietmanager.persistence.repository;

import com.dietmanager.persistence.entity.PerformanceSummaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PerformanceSummaryRepository extends JpaRepository<PerformanceSummaryEntity, Long> {
    Optional<PerformanceSummaryEntity> findByPlanCycleId(Long planCycleId);
}
