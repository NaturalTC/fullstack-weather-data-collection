package com.github.fullstackweatherdatacollectionplatform.controller;

import com.github.fullstackweatherdatacollectionplatform.dto.WeatherInsightDTO;
import com.github.fullstackweatherdatacollectionplatform.service.WeatherInsightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "AI Insights", description = "AI-powered weather analysis — anomaly detection, severity scoring, and natural language insights")
public class WeatherInsightController {

    private final WeatherInsightService insightService;

    public WeatherInsightController(WeatherInsightService insightService) {
        this.insightService = insightService;
    }

    @GetMapping("/weather/insights")
    @Operation(summary = "AI weather insight", description = "Returns AI-generated summary, trend, anomaly detection, and severity score for a city")
    public WeatherInsightDTO getInsight(@RequestParam String city) {
        return insightService.getInsight(city);
    }
}
