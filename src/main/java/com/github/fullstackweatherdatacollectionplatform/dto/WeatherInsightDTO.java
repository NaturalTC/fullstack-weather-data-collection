package com.github.fullstackweatherdatacollectionplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class WeatherInsightDTO {
    private String city;

    // AI-generated
    private String summary;
    private String trend;           // "warming" | "cooling" | "stable"
    private String anomalyDescription;

    // Computed server-side (deterministic, no AI cost)
    private int    severityScore;   // 0–100
    private boolean anomalyFlag;
    private double currentTemp;
    private double weeklyAvgTemp;
    private double tempDeviation;   // degrees above (+) or below (-) weekly avg
}
