package com.github.fullstackweatherdatacollectionplatform.dto;

public record ForecastDayDTO(
        String date,
        double high,
        double low,
        int precipChance,
        String description
) {
    public ForecastDayDTO toMetric() {
        return new ForecastDayDTO(date,
                r1((high - 32) * 5.0 / 9.0),
                r1((low  - 32) * 5.0 / 9.0),
                precipChance, description);
    }
    private static double r1(double v) { return Math.round(v * 10.0) / 10.0; }
}
