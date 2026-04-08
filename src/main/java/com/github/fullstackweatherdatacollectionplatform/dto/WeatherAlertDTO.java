package com.github.fullstackweatherdatacollectionplatform.dto;

import com.github.fullstackweatherdatacollectionplatform.model.WeatherAlert;

import java.time.LocalDateTime;

// used both as the request body (creating a rule) and as the response shape
// when creating, id / triggered / triggeredAt are ignored — the backend sets those
public record WeatherAlertDTO(
        Long id,
        String cityName,
        String metric,          // TEMPERATURE, FEELS_LIKE, HUMIDITY, WIND_SPEED, PRESSURE
        String operator,        // ABOVE or BELOW
        double threshold,
        String label,
        String recipientEmail,  // email address to notify when the condition is met
        Boolean triggered,      // wrapper type so Jackson can handle null when creating a new alert
        LocalDateTime triggeredAt
) {
    public static WeatherAlertDTO from(WeatherAlert alert) {
        return new WeatherAlertDTO(
                alert.getId(),
                alert.getCityName(),
                alert.getMetric(),
                alert.getOperator(),
                alert.getThreshold(),
                alert.getLabel(),
                alert.getRecipientEmail(),
                alert.isTriggered(),
                alert.getTriggeredAt()
        );
    }
}
