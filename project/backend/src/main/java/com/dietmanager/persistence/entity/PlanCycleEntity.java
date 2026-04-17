package com.dietmanager.persistence.entity;

import com.dietmanager.model.enums.db.PlanStatusType;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "plan_cycles")
public class PlanCycleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "health_profile_id", nullable = false)
    private HealthProfileEntity healthProfile;

    @Column(name = "cycle_start_date", nullable = false)
    private LocalDate cycleStartDate;

    @Column(name = "cycle_end_date", nullable = false)
    private LocalDate cycleEndDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanStatusType status = PlanStatusType.ACTIVE;

    public Long getId() {
        return id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public HealthProfileEntity getHealthProfile() {
        return healthProfile;
    }

    public void setHealthProfile(HealthProfileEntity healthProfile) {
        this.healthProfile = healthProfile;
    }

    public LocalDate getCycleStartDate() {
        return cycleStartDate;
    }

    public void setCycleStartDate(LocalDate cycleStartDate) {
        this.cycleStartDate = cycleStartDate;
    }

    public LocalDate getCycleEndDate() {
        return cycleEndDate;
    }

    public void setCycleEndDate(LocalDate cycleEndDate) {
        this.cycleEndDate = cycleEndDate;
    }

    public PlanStatusType getStatus() {
        return status;
    }

    public void setStatus(PlanStatusType status) {
        this.status = status;
    }
}
