package com.dietmanager.service;

import com.dietmanager.dto.AppDtos;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ExternalMealProviderService implements MealProvider {
    private final RestClient restClient;
    private final String searchPath;

    public ExternalMealProviderService(
            @Value("${diet.api.base-url:https://www.themealdb.com/api/json/v1/1}") String baseUrl,
            @Value("${diet.api.search-path:/search.php?s=healthy}") String searchPath,
            @Value("${diet.api.connect-timeout-ms:3000}") int connectTimeoutMs,
            @Value("${diet.api.read-timeout-ms:5000}") int readTimeoutMs
    ) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMs);
        requestFactory.setReadTimeout(readTimeoutMs);
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
        this.searchPath = searchPath;
    }

    @Override
    public List<String> fetchMealNames(AppDtos.HealthRequest request) {
        MealSearchResponse response = restClient.get()
                .uri(searchPath)
                .retrieve()
                .body(MealSearchResponse.class);

        if (response == null || response.meals == null) {
            return List.of();
        }

        List<String> names = new ArrayList<>();
        for (MealItem meal : response.meals) {
            if (meal != null && meal.strMeal != null && !meal.strMeal.isBlank()) {
                names.add(meal.strMeal.trim());
            }
            if (names.size() >= 14) {
                break;
            }
        }
        return names.stream().filter(Objects::nonNull).toList();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class MealSearchResponse {
        public List<MealItem> meals;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class MealItem {
        public String strMeal;
    }
}
