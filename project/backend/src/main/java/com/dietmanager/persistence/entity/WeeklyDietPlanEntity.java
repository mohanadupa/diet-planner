package com.dietmanager.persistence.entity;

import com.dietmanager.model.enums.db.SourceType;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "weekly_diet_plans")
public class WeeklyDietPlanEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "plan_cycle_id", nullable = false)
    private PlanCycleEntity planCycle;

    @Column(name = "week_number", nullable = false)
    private int weekNumber;

    @Column(nullable = false, length = 120)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private SourceType sourceType;

    @Column(name = "generated_at", insertable = false, updatable = false)
    private Instant generatedAt;

    public Long getId() {
        return id;
    }

    public PlanCycleEntity getPlanCycle() {
        return planCycle;
    }

    public void setPlanCycle(PlanCycleEntity planCycle) {
        this.planCycle = planCycle;
    }

    public int getWeekNumber() {
        return weekNumber;
    }

    public void setWeekNumber(int weekNumber) {
        this.weekNumber = weekNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }
}
