package com.github.fullstackweatherdatacollectionplatform.dto;

import java.time.LocalDate;

public record WeatherSummaryDTO(
        LocalDate date,
        double minTemperature,
        double maxTemperature,
        double avgTemperature
) {}
