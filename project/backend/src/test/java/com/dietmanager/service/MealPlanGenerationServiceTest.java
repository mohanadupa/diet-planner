package com.dietmanager.service;

import com.dietmanager.dto.AppDtos;
import com.dietmanager.model.DietPlan;
import com.dietmanager.model.enums.ActivityLevel;
import com.dietmanager.model.enums.GenderType;
import com.dietmanager.model.enums.GoalType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class MealPlanGenerationServiceTest {

    @Test
    void shouldGenerateFourWeeksAndSevenDaysFromProviderMeals() {
        MealProvider provider = request -> List.of("Meal A", "Meal B", "Meal C", "Meal D");
        MealPlanGenerationService service = new MealPlanGenerationService(provider);

        MealPlanGenerationService.GenerationResult result = service.generateMonthPlan(sampleRequest());

        Assertions.assertFalse(result.fallbackUsed());
        Assertions.assertEquals(4, result.plans().size());
        for (DietPlan plan : result.plans()) {
            Assertions.assertEquals(7, plan.getMeals().size());
        }
    }

    @Test
    void shouldFallbackWhenProviderFails() {
        MealProvider provider = request -> {
            throw new RuntimeException("provider down");
        };
        MealPlanGenerationService service = new MealPlanGenerationService(provider);

        MealPlanGenerationService.GenerationResult result = service.generateMonthPlan(sampleRequest());

        Assertions.assertTrue(result.fallbackUsed());
        Assertions.assertEquals(4, result.plans().size());
        Assertions.assertFalse(result.plans().get(0).getMeals().get(0).getMorning().isBlank());
    }

    @Test
    void shouldUseCacheForSameProfileInput() {
        final int[] calls = {0};
        MealProvider provider = request -> {
            calls[0]++;
            return List.of("Meal A", "Meal B");
        };
        MealPlanGenerationService service = new MealPlanGenerationService(provider);
        AppDtos.HealthRequest request = sampleRequest();

        MealPlanGenerationService.GenerationResult first = service.generateMonthPlan(request);
        MealPlanGenerationService.GenerationResult second = service.generateMonthPlan(request);

        Assertions.assertFalse(first.cacheHit());
        Assertions.assertTrue(second.cacheHit());
        Assertions.assertEquals(1, calls[0]);
    }

    private AppDtos.HealthRequest sampleRequest() {
        AppDtos.HealthRequest request = new AppDtos.HealthRequest();
        request.age = 28;
        request.gender = GenderType.MALE;
        request.activityLevel = ActivityLevel.MODERATE;
        request.heightCm = 174;
        request.weightKg = 71;
        request.bmi = 23.45;
        request.goalType = GoalType.WEIGHT_LOSS;
        request.userId = "u1";
        return request;
    }
}
