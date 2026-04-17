package com.dietmanager.dto;

import com.dietmanager.model.enums.GoalType;
import com.dietmanager.model.enums.ActivityLevel;
import com.dietmanager.model.enums.GenderType;

public class AppDtos {
    public static class HealthRequest {
        public String userId;
        public int age;
        public GenderType gender;
        public ActivityLevel activityLevel;
        public double heightCm;
        public double weightKg;
        public double bmi;
        public GoalType goalType;
    }

    public static class ToggleMealRequest {
        public String userId;
        public int weekNumber;
        public String day;
        public boolean completed;
    }

    public static class ThemeRequest {
        public String userId;
        public String theme;
    }
}
