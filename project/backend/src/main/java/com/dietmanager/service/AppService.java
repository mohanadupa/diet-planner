package com.dietmanager.service;

import com.dietmanager.dto.AppDtos;
import com.dietmanager.dto.AuthDtos;
import com.dietmanager.model.DietPlan;
import com.dietmanager.model.MealEntry;
import com.dietmanager.model.UserAccount;
import com.dietmanager.model.enums.db.DayOfWeekType;
import com.dietmanager.model.enums.db.PlanStatusType;
import com.dietmanager.model.enums.db.SourceType;
import com.dietmanager.persistence.entity.DailyMenuEntity;
import com.dietmanager.persistence.entity.HealthProfileEntity;
import com.dietmanager.persistence.entity.PlanCycleEntity;
import com.dietmanager.persistence.entity.PerformanceSummaryEntity;
import com.dietmanager.persistence.entity.UserEntity;
import com.dietmanager.persistence.entity.WeeklyDietPlanEntity;
import com.dietmanager.persistence.repository.DailyMenuRepository;
import com.dietmanager.persistence.repository.HealthProfileRepository;
import com.dietmanager.persistence.repository.PlanCycleRepository;
import com.dietmanager.persistence.repository.PerformanceSummaryRepository;
import com.dietmanager.persistence.repository.UserRepository;
import com.dietmanager.persistence.repository.WeeklyDietPlanRepository;
import com.dietmanager.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
public class AppService {
    private final Map<Long, MealPlanGenerationService.GenerationResult> generationMeta = new HashMap<>();
    private final MealPlanGenerationService mealPlanGenerationService;
    private final UserRepository userRepository;
    private final HealthProfileRepository healthProfileRepository;
    private final PlanCycleRepository planCycleRepository;
    private final WeeklyDietPlanRepository weeklyDietPlanRepository;
    private final DailyMenuRepository dailyMenuRepository;
    private final PerformanceSummaryRepository performanceSummaryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AppService(
            MealPlanGenerationService mealPlanGenerationService,
            UserRepository userRepository,
            HealthProfileRepository healthProfileRepository,
            PlanCycleRepository planCycleRepository,
            WeeklyDietPlanRepository weeklyDietPlanRepository,
            DailyMenuRepository dailyMenuRepository,
            PerformanceSummaryRepository performanceSummaryRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.mealPlanGenerationService = mealPlanGenerationService;
        this.userRepository = userRepository;
        this.healthProfileRepository = healthProfileRepository;
        this.planCycleRepository = planCycleRepository;
        this.weeklyDietPlanRepository = weeklyDietPlanRepository;
        this.dailyMenuRepository = dailyMenuRepository;
        this.performanceSummaryRepository = performanceSummaryRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthDtos.AuthResponse signup(AuthDtos.SignupRequest request) {
        boolean exists = userRepository.findByEmailIgnoreCase(request.email).isPresent();
        if (exists) {
            throw new IllegalArgumentException("Email already exists");
        }

        UserEntity user = new UserEntity();
        user.setFullName(request.fullName);
        user.setEmail(request.email.toLowerCase(Locale.ROOT));
        user.setPasswordHash(passwordEncoder.encode(request.password));
        user.setTheme("light");
        UserEntity saved = userRepository.save(user);
        String token = jwtService.generateToken(String.valueOf(saved.getId()), saved.getEmail());
        return new AuthDtos.AuthResponse(String.valueOf(saved.getId()), saved.getFullName(), saved.getEmail(), token);
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        UserEntity user = userRepository.findByEmailIgnoreCase(request.email)
                .filter(u -> passwordEncoder.matches(request.password, u.getPasswordHash()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        String token = jwtService.generateToken(String.valueOf(user.getId()), user.getEmail());
        return new AuthDtos.AuthResponse(String.valueOf(user.getId()), user.getFullName(), user.getEmail(), token);
    }

    @Transactional
    public Map<String, Object> saveHealth(AppDtos.HealthRequest request) {
        Long userId = parseUserId(request.userId);
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));

        double bmi = request.bmi > 0 ? request.bmi : calculateBmi(request.heightCm, request.weightKg);
        request.bmi = bmi;

        HealthProfileEntity profile = healthProfileRepository.findByUserId(userId).orElseGet(HealthProfileEntity::new);
        profile.setUser(user);
        profile.setAge(request.age);
        profile.setGender(request.gender);
        profile.setActivityLevel(request.activityLevel);
        profile.setHeightCm(request.heightCm);
        profile.setWeightKg(request.weightKg);
        profile.setBmi(bmi);
        profile.setGoalType(request.goalType);
        HealthProfileEntity savedProfile = healthProfileRepository.save(profile);

        planCycleRepository.findFirstByUserIdAndStatusOrderByIdDesc(userId, PlanStatusType.ACTIVE)
                .ifPresent(active -> {
                    active.setStatus(PlanStatusType.COMPLETED);
                    planCycleRepository.save(active);
                });

        PlanCycleEntity cycle = new PlanCycleEntity();
        cycle.setUser(user);
        cycle.setHealthProfile(savedProfile);
        cycle.setCycleStartDate(LocalDate.now());
        cycle.setCycleEndDate(LocalDate.now().plusDays(27));
        cycle.setStatus(PlanStatusType.ACTIVE);
        PlanCycleEntity savedCycle = planCycleRepository.save(cycle);

        MealPlanGenerationService.GenerationResult result = mealPlanGenerationService.generateMonthPlan(request);
        generationMeta.put(userId, result);
        persistGeneratedPlans(savedCycle, result);
        upsertPerformanceSummary(savedCycle);

        return Map.of(
                "message", "Health details saved",
                "usedFallbackPlan", result.fallbackUsed(),
                "cacheHit", result.cacheHit()
        );
    }

    public List<DietPlan> getPlans(String userId) {
        Long parsedUserId = parseUserId(userId);
        PlanCycleEntity cycle = planCycleRepository.findFirstByUserIdAndStatusOrderByIdDesc(parsedUserId, PlanStatusType.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("No active plan cycle found"));

        List<WeeklyDietPlanEntity> weeks = weeklyDietPlanRepository.findByPlanCycleIdOrderByWeekNumberAsc(cycle.getId());
        List<DietPlan> output = new ArrayList<>();
        for (WeeklyDietPlanEntity week : weeks) {
            List<DailyMenuEntity> menus = dailyMenuRepository.findByWeeklyPlanIdOrderByIdAsc(week.getId());
            DietPlan plan = new DietPlan();
            plan.setWeekNumber(week.getWeekNumber());
            List<MealEntry> meals = new ArrayList<>();
            for (DailyMenuEntity menu : menus) {
                MealEntry m = new MealEntry();
                m.setDay(menu.getDayOfWeek().name());
                m.setMorning(menu.getMorningMeal());
                m.setAfternoon(menu.getAfternoonMeal());
                m.setEvening(menu.getEveningMeal());
                m.setNight(menu.getNightMeal());
                m.setCompleted(menu.isCompleted());
                meals.add(m);
            }
            plan.setMeals(meals);
            output.add(plan);
        }
        return output;
    }

    public Map<String, Object> getPlanMeta(String userId) {
        MealPlanGenerationService.GenerationResult meta = generationMeta.get(parseUserId(userId));
        if (meta == null) {
            return Map.of("usedFallbackPlan", false, "cacheHit", false);
        }
        return Map.of("usedFallbackPlan", meta.fallbackUsed(), "cacheHit", meta.cacheHit());
    }

    @Transactional
    public void toggleMeal(AppDtos.ToggleMealRequest request) {
        Long userId = parseUserId(request.userId);
        PlanCycleEntity cycle = planCycleRepository.findFirstByUserIdAndStatusOrderByIdDesc(userId, PlanStatusType.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("No active plan cycle found"));

        WeeklyDietPlanEntity week = weeklyDietPlanRepository.findByPlanCycleIdOrderByWeekNumberAsc(cycle.getId()).stream()
                .filter(w -> w.getWeekNumber() == request.weekNumber)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Week not found"));

        DayOfWeekType day = parseDay(request.day);
        DailyMenuEntity menu = dailyMenuRepository.findByWeeklyPlanIdAndDayOfWeek(week.getId(), day)
                .orElseThrow(() -> new IllegalArgumentException("Meal day not found"));
        menu.setCompleted(request.completed);
        menu.setCompletedAt(request.completed ? Instant.now() : null);
        dailyMenuRepository.save(menu);
        upsertPerformanceSummary(cycle);
    }

    public Map<String, Object> getProfileStats(String userId) {
        Long parsedUserId = parseUserId(userId);
        PlanCycleEntity cycle = planCycleRepository.findFirstByUserIdAndStatusOrderByIdDesc(parsedUserId, PlanStatusType.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("No active plan cycle found"));
        PerformanceSummaryEntity summary = upsertPerformanceSummary(cycle);

        int percent = (int) Math.round(summary.getCompletionPercentage());
        String rating = toTitleCase(summary.getRating());
        Map<String, Object> out = new HashMap<>();
        out.put("completionPercentage", percent);
        out.put("rating", rating);
        out.put("updateAvailable", summary.getCompletedDays() >= 28);
        return out;
    }

    public UserAccount getUser(String userId) {
        UserEntity entity = userRepository.findById(parseUserId(userId))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        UserAccount user = new UserAccount();
        user.setId(String.valueOf(entity.getId()));
        user.setFullName(entity.getFullName());
        user.setEmail(entity.getEmail());
        user.setTheme(entity.getTheme());
        return user;
    }

    @Transactional
    public UserAccount updateTheme(AppDtos.ThemeRequest request) {
        UserEntity entity = userRepository.findById(parseUserId(request.userId))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        entity.setTheme(request.theme);
        userRepository.save(entity);
        UserAccount user = new UserAccount();
        user.setId(String.valueOf(entity.getId()));
        user.setFullName(entity.getFullName());
        user.setEmail(entity.getEmail());
        user.setTheme(entity.getTheme());
        return user;
    }

    private void persistGeneratedPlans(PlanCycleEntity cycle, MealPlanGenerationService.GenerationResult result) {
        SourceType sourceType = result.fallbackUsed() ? SourceType.FALLBACK : SourceType.API;
        for (DietPlan generated : result.plans()) {
            WeeklyDietPlanEntity weekly = new WeeklyDietPlanEntity();
            weekly.setPlanCycle(cycle);
            weekly.setWeekNumber(generated.getWeekNumber());
            weekly.setTitle("Week " + generated.getWeekNumber() + " Plan");
            weekly.setSourceType(sourceType);
            WeeklyDietPlanEntity savedWeek = weeklyDietPlanRepository.save(weekly);

            for (MealEntry mealEntry : generated.getMeals()) {
                DailyMenuEntity daily = new DailyMenuEntity();
                daily.setWeeklyPlan(savedWeek);
                daily.setDayOfWeek(parseDay(mealEntry.getDay()));
                daily.setMorningMeal(mealEntry.getMorning());
                daily.setAfternoonMeal(mealEntry.getAfternoon());
                daily.setEveningMeal(mealEntry.getEvening());
                daily.setNightMeal(mealEntry.getNight());
                daily.setCompleted(mealEntry.isCompleted());
                daily.setCompletedAt(null);
                dailyMenuRepository.save(daily);
            }
        }
    }

    private Long parseUserId(String userId) {
        try {
            return Long.parseLong(userId);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid user ID");
        }
    }

    private DayOfWeekType parseDay(String day) {
        if (day == null || day.isBlank()) {
            throw new IllegalArgumentException("Day is required");
        }
        for (DayOfWeekType candidate : DayOfWeekType.values()) {
            if (candidate.name().equalsIgnoreCase(day)) {
                return candidate;
            }
        }
        throw new IllegalArgumentException("Invalid day: " + day);
    }

    private double calculateBmi(double heightCm, double weightKg) {
        if (heightCm <= 0) {
            return 0;
        }
        double heightM = heightCm / 100.0;
        return Math.round((weightKg / (heightM * heightM)) * 100.0) / 100.0;
    }

    private PerformanceSummaryEntity upsertPerformanceSummary(PlanCycleEntity cycle) {
        List<WeeklyDietPlanEntity> weeks = weeklyDietPlanRepository.findByPlanCycleIdOrderByWeekNumberAsc(cycle.getId());
        int total = 0;
        int done = 0;
        for (WeeklyDietPlanEntity week : weeks) {
            List<DailyMenuEntity> menus = dailyMenuRepository.findByWeeklyPlanIdOrderByIdAsc(week.getId());
            total += menus.size();
            done += (int) menus.stream().filter(DailyMenuEntity::isCompleted).count();
        }
        double percent = total == 0 ? 0 : Math.round((done * 10000.0) / total) / 100.0;
        String rating = percent >= 80 ? "GOOD" : percent >= 50 ? "AVERAGE" : "BAD";

        PerformanceSummaryEntity summary = performanceSummaryRepository.findByPlanCycleId(cycle.getId())
                .orElseGet(PerformanceSummaryEntity::new);
        summary.setUser(cycle.getUser());
        summary.setPlanCycle(cycle);
        summary.setTotalDays(total);
        summary.setCompletedDays(done);
        summary.setCompletionPercentage(percent);
        summary.setRating(rating);
        return performanceSummaryRepository.save(summary);
    }

    private String toTitleCase(String uppercaseValue) {
        String lower = uppercaseValue.toLowerCase(Locale.ROOT);
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
