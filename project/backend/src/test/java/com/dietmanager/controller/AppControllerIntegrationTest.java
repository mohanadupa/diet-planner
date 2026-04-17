package com.dietmanager.controller;

import com.dietmanager.dto.AppDtos;
import com.dietmanager.model.enums.ActivityLevel;
import com.dietmanager.model.enums.GenderType;
import com.dietmanager.model.enums.GoalType;
import com.dietmanager.service.MealProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AppControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MealProvider mealProvider;

    @Test
    void healthProfileShouldGenerateDietPlansAndExposeMeta() throws Exception {
        when(mealProvider.fetchMealNames(any())).thenReturn(List.of("Protein Bowl", "Veg Soup", "Fruit Plate"));

        String signupBody = objectMapper.writeValueAsString(Map.of(
                "fullName", "Test User",
                "email", "test@example.com",
                "password", "secret123"
        ));

        String signupResponse = mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String userId = objectMapper.readTree(signupResponse).get("userId").asText();
        String token = objectMapper.readTree(signupResponse).get("token").asText();
        AppDtos.HealthRequest request = new AppDtos.HealthRequest();
        request.userId = userId;
        request.age = 30;
        request.gender = GenderType.FEMALE;
        request.activityLevel = ActivityLevel.HIGH;
        request.heightCm = 165;
        request.weightKg = 62;
        request.bmi = 22.77;
        request.goalType = GoalType.WEIGHT_GAIN;

        mockMvc.perform(post("/api/health-profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usedFallbackPlan").value(false));

        mockMvc.perform(get("/api/diet-plans")
                        .header("Authorization", "Bearer " + token)
                        .param("userId", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].weekNumber").value(1))
                .andExpect(jsonPath("$[0].meals.length()").value(7));

        mockMvc.perform(get("/api/diet-plans/meta")
                        .header("Authorization", "Bearer " + token)
                        .param("userId", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usedFallbackPlan").value(false));
    }
}
