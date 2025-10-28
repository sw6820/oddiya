package com.oddiya.plan.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class DefaultActivityLoader {
    
    private Map<String, Object> activities;
    
    public DefaultActivityLoader() {
        loadActivities();
    }
    
    private void loadActivities() {
        try {
            ClassPathResource resource = new ClassPathResource("default-activities.yaml");
            InputStream inputStream = resource.getInputStream();
            
            Yaml yaml = new Yaml();
            activities = yaml.load(inputStream);
            
            log.info("Loaded default activities from YAML");
        } catch (Exception e) {
            log.error("Failed to load default activities: {}", e.getMessage());
            activities = new HashMap<>();
        }
    }
    
    @SuppressWarnings("unchecked")
    public Map<String, String> getActivityForDay(String location, int day) {
        if (activities == null || !activities.containsKey(location)) {
            return getGenericActivity();
        }
        
        Map<String, Object> locationData = (Map<String, Object>) activities.get(location);
        String dayKey = "day" + day;
        
        if (locationData.containsKey(dayKey)) {
            return (Map<String, String>) locationData.get(dayKey);
        } else {
            // Use day 1 as fallback
            return (Map<String, String>) locationData.getOrDefault("day1", getGenericActivity());
        }
    }
    
    private Map<String, String> getGenericActivity() {
        Map<String, String> generic = new HashMap<>();
        generic.put("location", "도심 명소 탐방");
        generic.put("activity", "Morning: 주요 관광지 방문 (₩20,000), Afternoon: 현지 맛집 투어 (₩25,000), Evening: 야경 명소 (₩20,000)");
        return generic;
    }
}

