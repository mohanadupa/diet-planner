package com.dietmanager.service;

import com.dietmanager.dto.AppDtos;
import com.dietmanager.model.DietPlan;
import com.dietmanager.model.MealEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MealPlanGenerationService {
    private static final Logger log = LoggerFactory.getLogger(MealPlanGenerationService.class);
    private static final String[] DAYS = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    private static final Duration CACHE_TTL = Duration.ofHours(24);

    private final MealProvider mealProvider;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public MealPlanGenerationService(MealProvider mealProvider) {
        this.mealProvider = mealProvider;
    }

    public GenerationResult generateMonthPlan(AppDtos.HealthRequest request) {
        String cacheKey = buildCacheKey(request);
        CacheEntry hit = cache.get(cacheKey);
        if (hit != null && hit.expiresAt().isAfter(Instant.now())) {
            return new GenerationResult(copyPlans(hit.plans()), false, true);
        }

        FetchResult fetchResult = fetchWithFallback(request);
        List<String> mealNames = fetchResult.mealNames();
        List<DietPlan> plans = buildPlansFromMeals(mealNames);
        cache.put(cacheKey, new CacheEntry(copyPlans(plans), Instant.now().plus(CACHE_TTL)));
        return new GenerationResult(plans, fetchResult.fallbackUsed(), false);
    }

    private FetchResult fetchWithFallback(AppDtos.HealthRequest request) {
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                long start = System.currentTimeMillis();
                List<String> meals = mealProvider.fetchMealNames(request);
                long elapsed = System.currentTimeMillis() - start;
                log.info("Diet provider call attempt {} finished in {} ms with {} meals", attempt, elapsed, meals.size());
                if (!meals.isEmpty()) {
                    return new FetchResult(meals, false);
                }
            } catch (Exception ex) {
                log.warn("Diet provider attempt {} failed: {}", attempt, ex.getMessage());
            }
        }
        return new FetchResult(fallbackMeals(request.goalType.name()), true);
    }

    private List<DietPlan> buildPlansFromMeals(List<String> mealNames) {
        List<DietPlan> month = new ArrayList<>();
        for (int week = 1; week <= 4; week++) {
            List<MealEntry> entries = new ArrayList<>();
            for (int i = 0; i < DAYS.length; i++) {
                MealEntry meal = new MealEntry();
                meal.setDay(DAYS[i]);
                meal.setMorning(slotMeal(mealNames, (week * 7 + i) % mealNames.size(), "Morning oats + fruit"));
                meal.setAfternoon(slotMeal(mealNames, (week * 7 + i + 2) % mealNames.size(), "Lunch rice + dal"));
                meal.setEvening(slotMeal(mealNames, (week * 7 + i + 4) % mealNames.size(), "Evening nuts + tea"));
                meal.setNight(slotMeal(mealNames, (week * 7 + i + 6) % mealNames.size(), "Dinner roti + veggies"));
                meal.setCompleted(false);
                entries.add(meal);
            }
            DietPlan plan = new DietPlan();
            plan.setWeekNumber(week);
            plan.setMeals(entries);
            month.add(plan);
        }
        return month;
    }

    private String slotMeal(List<String> meals, int index, String fallback) {
        if (meals == null || meals.isEmpty()) {
            return fallback;
        }
        String meal = meals.get(Math.floorMod(index, meals.size()));
        return meal == null || meal.isBlank() ? fallback : meal;
    }

    private String buildCacheKey(AppDtos.HealthRequest request) {
        return String.join("|",
                String.valueOf(request.age),
                String.valueOf(request.gender),
                String.valueOf(request.heightCm),
                String.valueOf(request.weightKg),
                String.valueOf(request.bmi),
                String.valueOf(request.goalType),
                String.valueOf(request.activityLevel)
        );
    }

    private List<String> fallbackMeals(String goal) {
        if ("WEIGHT_GAIN".equals(goal)) {
            return List.of(
                    "Banana peanut smoothie", "Paneer rice bowl", "Chicken quinoa plate", "Oats milk porridge",
                    "Egg toast combo", "Greek yogurt nuts", "Dal roti sabzi", "Fruit dry-fruit shake"
            );
        }
        return List.of(
                "Sprouts salad bowl", "Vegetable soup", "Brown rice dal", "Grilled tofu plate",
                "Fruit yogurt cup", "Chickpea salad", "Boiled egg salad", "Steamed veggie wrap"
        );
    }

    private List<DietPlan> copyPlans(List<DietPlan> source) {
        List<DietPlan> copy = new ArrayList<>();
        for (DietPlan plan : source) {
            DietPlan p = new DietPlan();
            p.setWeekNumber(plan.getWeekNumber());
            List<MealEntry> meals = new ArrayList<>();
            for (MealEntry meal : plan.getMeals()) {
                MealEntry m = new MealEntry();
                m.setDay(meal.getDay());
                m.setMorning(meal.getMorning());
                m.setAfternoon(meal.getAfternoon());
                m.setEvening(meal.getEvening());
                m.setNight(meal.getNight());
                m.setCompleted(meal.isCompleted());
                meals.add(m);
            }
            p.setMeals(meals);
            copy.add(p);
        }
        return copy;
    }

    private record CacheEntry(List<DietPlan> plans, Instant expiresAt) {
    }

    private record FetchResult(List<String> mealNames, boolean fallbackUsed) {
    }

    public record GenerationResult(List<DietPlan> plans, boolean fallbackUsed, boolean cacheHit) {
    }
}
