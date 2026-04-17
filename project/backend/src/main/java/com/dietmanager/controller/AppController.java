package com.dietmanager.controller;

import com.dietmanager.dto.AppDtos;
import com.dietmanager.dto.AuthDtos;
import com.dietmanager.model.DietPlan;
import com.dietmanager.model.UserAccount;
import com.dietmanager.service.AppService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AppController {
    private final AppService service;

    public AppController(AppService service) {
        this.service = service;
    }

    @PostMapping("/auth/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody AuthDtos.SignupRequest request) {
        return ResponseEntity.ok(service.signup(request));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthDtos.LoginRequest request) {
        return ResponseEntity.ok(service.login(request));
    }

    @PostMapping("/health-profile")
    public ResponseEntity<?> saveHealth(@RequestBody AppDtos.HealthRequest request, Principal principal) {
        assertAuthorizedUser(request.userId, principal);
        return ResponseEntity.ok(service.saveHealth(request));
    }

    @GetMapping("/diet-plans")
    public List<DietPlan> getPlans(@RequestParam String userId, Principal principal) {
        assertAuthorizedUser(userId, principal);
        return service.getPlans(userId);
    }

    @GetMapping("/diet-plans/meta")
    public Map<String, Object> getPlansMeta(@RequestParam String userId, Principal principal) {
        assertAuthorizedUser(userId, principal);
        return service.getPlanMeta(userId);
    }

    @PutMapping("/diet-meals/toggle")
    public ResponseEntity<?> toggle(@RequestBody AppDtos.ToggleMealRequest request, Principal principal) {
        assertAuthorizedUser(request.userId, principal);
        service.toggleMeal(request);
        return ResponseEntity.ok(Map.of("message", "Meal updated"));
    }

    @GetMapping("/profile/stats")
    public Map<String, Object> stats(@RequestParam String userId, Principal principal) {
        assertAuthorizedUser(userId, principal);
        return service.getProfileStats(userId);
    }

    @GetMapping("/user")
    public UserAccount user(@RequestParam String userId, Principal principal) {
        assertAuthorizedUser(userId, principal);
        return service.getUser(userId);
    }

    @PutMapping("/user/theme")
    public UserAccount theme(@RequestBody AppDtos.ThemeRequest request, Principal principal) {
        assertAuthorizedUser(request.userId, principal);
        return service.updateTheme(request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    private void assertAuthorizedUser(String userId, Principal principal) {
        if (principal == null || userId == null || !userId.equals(principal.getName())) {
            throw new IllegalArgumentException("Unauthorized user access");
        }
    }
}
