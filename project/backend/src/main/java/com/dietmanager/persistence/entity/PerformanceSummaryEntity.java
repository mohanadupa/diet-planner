package com.dietmanager.persistence.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "performance_summaries")
public class PerformanceSummaryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @OneToOne(optional = false)
    @JoinColumn(name = "plan_cycle_id", nullable = false, unique = true)
    private PlanCycleEntity planCycle;

    @Column(name = "total_days", nullable = false)
    private int totalDays;

    @Column(name = "completed_days", nullable = false)
    private int completedDays;

    @Column(name = "completion_percentage", nullable = false)
    private double completionPercentage;

    @Column(nullable = false, length = 10)
    private String rating;

    @Column(name = "calculated_at", insertable = false, updatable = false)
    private Instant calculatedAt;

    public Long getId() {
        return id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public PlanCycleEntity getPlanCycle() {
        return planCycle;
    }

    public void setPlanCycle(PlanCycleEntity planCycle) {
        this.planCycle = planCycle;
    }

    public int getTotalDays() {
        return totalDays;
    }

    public void setTotalDays(int totalDays) {
        this.totalDays = totalDays;
    }

    public int getCompletedDays() {
        return completedDays;
    }

    public void setCompletedDays(int completedDays) {
        this.completedDays = completedDays;
    }

    public double getCompletionPercentage() {
        return completionPercentage;
    }

    public void setCompletionPercentage(double completionPercentage) {
        this.completionPercentage = completionPercentage;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }
}
