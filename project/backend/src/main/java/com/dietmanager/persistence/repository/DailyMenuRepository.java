package com.dietmanager.persistence.repository;

import com.dietmanager.model.enums.db.DayOfWeekType;
import com.dietmanager.persistence.entity.DailyMenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DailyMenuRepository extends JpaRepository<DailyMenuEntity, Long> {
    List<DailyMenuEntity> findByWeeklyPlanIdOrderByIdAsc(Long weeklyPlanId);

    Optional<DailyMenuEntity> findByWeeklyPlanIdAndDayOfWeek(Long weeklyPlanId, DayOfWeekType dayOfWeek);
}
