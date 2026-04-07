package com.github.fullstackweatherdatacollectionplatform.dto;

import com.github.fullstackweatherdatacollectionplatform.model.City;

// DTO for the City entity — sent to the frontend when listing all monitored cities
// exposes name, state, country, and coordinates — the frontend uses lat/lon to place markers on the map
public record CityDTO(
        String name,
        String state,
        String country,
        double latitude,
        double longitude
) {
    // static factory method — converts a City entity into this DTO
    // called in WeatherQueryService with: .map(CityDTO::from)
    public static CityDTO from(City entity) {
        return new CityDTO(
                entity.getName(),
                entity.getState(),
                entity.getCountry(),
                entity.getLatitude(),
                entity.getLongitude()
        );
    }
}
