package com.dietmanager.persistence.repository;

import com.dietmanager.model.enums.db.PlanStatusType;
import com.dietmanager.persistence.entity.PlanCycleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlanCycleRepository extends JpaRepository<PlanCycleEntity, Long> {
    Optional<PlanCycleEntity> findFirstByUserIdAndStatusOrderByIdDesc(Long userId, PlanStatusType status);
}
