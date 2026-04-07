package com.github.fullstackweatherdatacollectionplatform.dto;

import com.github.fullstackweatherdatacollectionplatform.model.WeatherData;

import java.time.LocalDateTime;

// DTO (Data Transfer Object) — defines exactly what the API sends to the frontend
// we never return the raw WeatherData entity directly because:
// 1. it exposes internal DB structure (foreign key IDs, etc.)
// 2. it can cause lazy-loading issues with JPA relationships
// record = immutable data container, auto-generates constructor/getters/equals/toString
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
    // static factory method — converts a WeatherData entity into this DTO
    // called in WeatherQueryService with: .map(WeatherDataDTO::from)
    // note: we reach into the relationships here — entity.getCity().getName() navigates the ManyToOne join
    public static WeatherDataDTO from(WeatherData entity) {
        return new WeatherDataDTO(
                entity.getCity().getName(),              // from the related City entity
                entity.getCity().getCountry(),           // from the related City entity
                entity.getTemperature(),
                entity.getFeelsLike(),
                entity.getHumidity(),
                entity.getPressure(),
                entity.getWindSpeed(),
                entity.getCondition().getDescription(),  // from the related WeatherCondition entity
                entity.getFetchedAt()
        );
    }
}
