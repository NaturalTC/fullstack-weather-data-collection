package com.github.fullstackweatherdatacollectionplatform.dto;

import com.github.fullstackweatherdatacollectionplatform.model.WeatherData;

import java.time.LocalDateTime;

public record WeatherDataDTO(
        String cityName,
        String country,
        double temperature,
        double feelsLike,
        int humidity,
        int pressure,
        double windSpeed,
        String description,
        LocalDateTime fetchedAt
) {
    public static WeatherDataDTO from(WeatherData entity) {
        return new WeatherDataDTO(
                entity.getCity().getName(),
                entity.getCity().getCountry(),
                entity.getTemperature(),
                entity.getFeelsLike(),
                entity.getHumidity(),
                entity.getPressure(),
                entity.getWindSpeed(),
                entity.getCondition().getDescription(),
                entity.getFetchedAt()
        );
    }
}
