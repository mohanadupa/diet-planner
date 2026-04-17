package com.dietmanager.service;

import com.dietmanager.dto.AppDtos;

import java.util.List;

public interface MealProvider {
    List<String> fetchMealNames(AppDtos.HealthRequest request);
}
