package com.dietmanager.persistence.repository;

import com.dietmanager.persistence.entity.WeeklyDietPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WeeklyDietPlanRepository extends JpaRepository<WeeklyDietPlanEntity, Long> {
    List<WeeklyDietPlanEntity> findByPlanCycleIdOrderByWeekNumberAsc(Long planCycleId);
}
