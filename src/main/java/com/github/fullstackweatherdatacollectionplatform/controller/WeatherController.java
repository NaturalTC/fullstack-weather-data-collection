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

@RestController   // marks this as a REST controller — every method returns JSON directly (not a view/template)
@RequestMapping("/api")  // all endpoints in this class are prefixed with /api
@Tag(name = "Weather", description = "Public weather data endpoints — no authentication required")  // Swagger UI grouping
public class WeatherController {

    // constructor injection — Spring provides WeatherQueryService automatically
    private final WeatherQueryService weatherQueryService;

    public WeatherController(WeatherQueryService weatherQueryService) {
        this.weatherQueryService = weatherQueryService;
    }

    // GET /api/cities — returns all cities currently being monitored
    @GetMapping("/cities")
    @Operation(summary = "List all monitored cities")  // Swagger UI description
    public List<CityDTO> getAllCities() {
        return weatherQueryService.getAllCities();
    }

    // GET /api/weather?city=Boston — returns full weather history, optionally filtered by city
    // @RequestParam(required = false) — city param is optional, returns all cities if omitted
    @GetMapping("/weather")
    @Operation(summary = "Get weather history", description = "Returns all stored records, optionally filtered by city name. Add ?unit=metric for Celsius.")
    public List<WeatherDataDTO> getAllWeather(
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "imperial") String unit) {
        var data = weatherQueryService.getAllWeather(city);
        return "metric".equalsIgnoreCase(unit) ? data.stream().map(WeatherDataDTO::toMetric).toList() : data;
    }

    @GetMapping("/weather/latest")
    @Operation(summary = "Get latest weather per city", description = "Returns the most recent record for each city. Add ?unit=metric for Celsius.")
    public List<WeatherDataDTO> getLatestWeather(
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "imperial") String unit) {
        var data = weatherQueryService.getLatestWeather(city);
        return "metric".equalsIgnoreCase(unit) ? data.stream().map(WeatherDataDTO::toMetric).toList() : data;
    }

    @GetMapping("/weather/summary")
    @Operation(summary = "Get daily summary", description = "Returns daily min/max/avg temperature for a city. Add ?unit=metric for Celsius.")
    public List<WeatherSummaryDTO> getDailySummary(
            @RequestParam String city,
            @RequestParam(defaultValue = "imperial") String unit) {
        var data = weatherQueryService.getDailySummary(city);
        return "metric".equalsIgnoreCase(unit) ? data.stream().map(WeatherSummaryDTO::toMetric).toList() : data;
    }

    @GetMapping("/forecast")
    @Operation(summary = "Get 5-day forecast", description = "Live from OpenWeatherMap. Add ?unit=metric for Celsius.")
    public List<ForecastDayDTO> getForecast(
            @RequestParam String city,
            @RequestParam(defaultValue = "imperial") String unit) {
        var data = weatherQueryService.getForecast(city);
        return "metric".equalsIgnoreCase(unit) ? data.stream().map(ForecastDayDTO::toMetric).toList() : data;
    }

    // GET /api/aqi?city=Boston — returns live Air Quality Index directly from OpenWeatherMap
    // not stored in the database — fetched fresh on every request
    @GetMapping("/aqi")
    @Operation(summary = "Get air quality index", description = "Live AQI from OpenWeatherMap for a city")
    public AqiDTO getAqi(@RequestParam String city) {
        return weatherQueryService.getAqi(city);
    }

    // GET /api/weather/heatmap — returns daily avg temp per city for the last 7 days
    // used by the frontend to render the temperature heatmap visualization
    @GetMapping("/weather/heatmap")
    @Operation(summary = "Get heatmap data", description = "Daily average temperature per city for the last 7 days")
    public List<HeatmapEntryDTO> getHeatmap() {
        return weatherQueryService.getHeatmap();
    }
}
