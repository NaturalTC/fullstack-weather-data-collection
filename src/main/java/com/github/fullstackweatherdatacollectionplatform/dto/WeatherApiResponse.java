package com.github.fullstackweatherdatacollectionplatform.dto;

import java.time.LocalDateTime;

// record — a Java feature that creates an immutable data container with no boilerplate
// automatically generates constructor, getters, equals, hashCode, and toString
// used as a clean internal transport object between WeatherApiClient and WeatherIngestionService
public record WeatherApiResponse(
        String cityName,        // city name returned by OpenWeatherMap
        String country,         // country code (e.g. "US")
        double latitude,        // coordinates — stored so we can seed the City entity
        double longitude,
        double temperature,     // current temperature in imperial (°F)
        double feelsLike,       // feels-like temperature accounting for wind/humidity
        int humidity,           // humidity percentage (0-100)
        int pressure,           // atmospheric pressure in hPa
        double windSpeed,       // wind speed in mph (imperial)
        String description,     // condition description e.g. "moderate rain", "clear sky"
        LocalDateTime fetchedAt // UTC timestamp of when this data was fetched — generated in WeatherApiClient
) {}
