package com.dietmanager.persistence.entity;

import com.dietmanager.model.enums.db.DayOfWeekType;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "daily_menus")
public class DailyMenuEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "weekly_plan_id", nullable = false)
    private WeeklyDietPlanEntity weeklyPlan;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeekType dayOfWeek;

    @Column(name = "morning_meal", nullable = false)
    private String morningMeal;

    @Column(name = "afternoon_meal", nullable = false)
    private String afternoonMeal;

    @Column(name = "evening_meal", nullable = false)
    private String eveningMeal;

    @Column(name = "night_meal", nullable = false)
    private String nightMeal;

    @Column(nullable = false)
    private boolean completed;

    @Column(name = "completed_at")
    private Instant completedAt;

    public Long getId() {
        return id;
    }

    public WeeklyDietPlanEntity getWeeklyPlan() {
        return weeklyPlan;
    }

    public void setWeeklyPlan(WeeklyDietPlanEntity weeklyPlan) {
        this.weeklyPlan = weeklyPlan;
    }

    public DayOfWeekType getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeekType dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getMorningMeal() {
        return morningMeal;
    }

    public void setMorningMeal(String morningMeal) {
        this.morningMeal = morningMeal;
    }

    public String getAfternoonMeal() {
        return afternoonMeal;
    }

    public void setAfternoonMeal(String afternoonMeal) {
        this.afternoonMeal = afternoonMeal;
    }

    public String getEveningMeal() {
        return eveningMeal;
    }

    public void setEveningMeal(String eveningMeal) {
        this.eveningMeal = eveningMeal;
    }

    public String getNightMeal() {
        return nightMeal;
    }

    public void setNightMeal(String nightMeal) {
        this.nightMeal = nightMeal;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}
