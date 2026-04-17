package com.dietmanager.model;

import java.util.List;

public class DietPlan {
    private int weekNumber;
    private List<MealEntry> meals;

    public int getWeekNumber() {
        return weekNumber;
    }

    public void setWeekNumber(int weekNumber) {
        this.weekNumber = weekNumber;
    }

    public List<MealEntry> getMeals() {
        return meals;
    }

    public void setMeals(List<MealEntry> meals) {
        this.meals = meals;
    }
}
