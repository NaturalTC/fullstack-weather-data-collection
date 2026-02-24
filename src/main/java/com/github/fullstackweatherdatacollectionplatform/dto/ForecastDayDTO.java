package com.github.fullstackweatherdatacollectionplatform.dto;

public record ForecastDayDTO(
        String date,
        double high,
        double low,
        int precipChance,
        String description
) {}
