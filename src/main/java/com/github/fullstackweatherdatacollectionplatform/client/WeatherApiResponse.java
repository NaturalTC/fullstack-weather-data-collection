package com.github.fullstackweatherdatacollectionplatform.client;

import java.time.LocalDateTime;

public record WeatherApiResponse(
        String cityName,
        String country,
        double latitude,
        double longitude,
        double temperature,
        double feelsLike,
        int humidity,
        int pressure,
        double windSpeed,
        String description,
        LocalDateTime timestamp,
        LocalDateTime fetchedAt
) {}
