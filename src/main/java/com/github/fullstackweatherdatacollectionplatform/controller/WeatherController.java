package com.github.fullstackweatherdatacollectionplatform.controller;

import com.github.fullstackweatherdatacollectionplatform.dto.CityDTO;
import com.github.fullstackweatherdatacollectionplatform.dto.WeatherDataDTO;
import com.github.fullstackweatherdatacollectionplatform.dto.WeatherSummaryDTO;
import com.github.fullstackweatherdatacollectionplatform.service.WeatherQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class WeatherController {

    private final WeatherQueryService weatherQueryService;

    public WeatherController(WeatherQueryService weatherQueryService) {
        this.weatherQueryService = weatherQueryService;
    }

    @GetMapping("/weather")
    public List<WeatherDataDTO> getAllWeather(
            @RequestParam(required = false) String city) {
        return weatherQueryService.getAllWeather(city);
    }

    @GetMapping("/weather/latest")
    public List<WeatherDataDTO> getLatestWeather(
            @RequestParam(required = false) String city) {
        return weatherQueryService.getLatestWeather(city);
    }

    @GetMapping("/weather/summary")
    public List<WeatherSummaryDTO> getDailySummary(
            @RequestParam String city) {
        return weatherQueryService.getDailySummary(city);
    }

    @GetMapping("/cities")
    public List<CityDTO> getAllCities() {
        return weatherQueryService.getAllCities();
    }
}
