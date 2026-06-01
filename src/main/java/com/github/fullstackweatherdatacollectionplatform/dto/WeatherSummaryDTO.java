package com.github.fullstackweatherdatacollectionplatform.dto;

import java.time.LocalDate;

public record WeatherSummaryDTO(
        LocalDate date,
        double minTemperature,
        double maxTemperature,
        double avgTemperature
) {
    public WeatherSummaryDTO toMetric() {
        return new WeatherSummaryDTO(date,
                r1((minTemperature - 32) * 5.0 / 9.0),
                r1((maxTemperature - 32) * 5.0 / 9.0),
                r1((avgTemperature - 32) * 5.0 / 9.0));
    }
    private static double r1(double v) { return Math.round(v * 10.0) / 10.0; }
}
