package com.github.fullstackweatherdatacollectionplatform.controller;

import com.github.fullstackweatherdatacollectionplatform.client.WeatherApiClient;
import com.github.fullstackweatherdatacollectionplatform.model.WeatherData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WeatherTestController {

    private final WeatherApiClient weatherApiClient;

    public WeatherTestController(WeatherApiClient weatherApiClient) {
        this.weatherApiClient = weatherApiClient;
    }

    // endpoint for testing purposes
    @GetMapping("/api/test/weather")
    public WeatherData testFetch(@RequestParam(defaultValue = "London") String city) {
        return weatherApiClient.fetchWeather(city);
    }
}
