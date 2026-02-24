package com.github.fullstackweatherdatacollectionplatform.controller;

import com.github.fullstackweatherdatacollectionplatform.dto.*;
import com.github.fullstackweatherdatacollectionplatform.service.WeatherQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Weather", description = "Public weather data endpoints — no authentication required")
public class WeatherController {

    private final WeatherQueryService weatherQueryService;

    public WeatherController(WeatherQueryService weatherQueryService) {
        this.weatherQueryService = weatherQueryService;
    }

    @GetMapping("/cities")
    @Operation(summary = "List all monitored cities")
    public List<CityDTO> getAllCities() {
        return weatherQueryService.getAllCities();
    }

    @GetMapping("/weather")
    @Operation(summary = "Get weather history", description = "Returns all stored records, optionally filtered by city name")
    public List<WeatherDataDTO> getAllWeather(@RequestParam(required = false) String city) {
        return weatherQueryService.getAllWeather(city);
    }

    @GetMapping("/weather/latest")
    @Operation(summary = "Get latest weather per city", description = "Returns the most recent record for each city, or for a specific city if provided")
    public List<WeatherDataDTO> getLatestWeather(@RequestParam(required = false) String city) {
        return weatherQueryService.getLatestWeather(city);
    }

    @GetMapping("/weather/summary")
    @Operation(summary = "Get daily summary", description = "Returns daily min/max/avg temperature for a city")
    public List<WeatherSummaryDTO> getDailySummary(@RequestParam String city) {
        return weatherQueryService.getDailySummary(city);
    }

    @GetMapping("/forecast")
    @Operation(summary = "Get 5-day forecast", description = "Live from OpenWeatherMap — not stored in the database")
    public List<ForecastDayDTO> getForecast(@RequestParam String city) {
        return weatherQueryService.getForecast(city);
    }

    @GetMapping("/aqi")
    @Operation(summary = "Get air quality index", description = "Live AQI from OpenWeatherMap for a city")
    public AqiDTO getAqi(@RequestParam String city) {
        return weatherQueryService.getAqi(city);
    }

    @GetMapping("/weather/heatmap")
    @Operation(summary = "Get heatmap data", description = "Daily average temperature per city for the last 7 days")
    public List<HeatmapEntryDTO> getHeatmap() {
        return weatherQueryService.getHeatmap();
    }
}
