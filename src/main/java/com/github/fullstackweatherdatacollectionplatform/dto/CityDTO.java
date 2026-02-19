package com.github.fullstackweatherdatacollectionplatform.dto;

import com.github.fullstackweatherdatacollectionplatform.model.City;

public record CityDTO(
        String name,
        String state,
        String country,
        double latitude,
        double longitude
) {
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
