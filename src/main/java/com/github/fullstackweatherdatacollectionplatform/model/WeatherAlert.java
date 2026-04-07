package com.github.fullstackweatherdatacollectionplatform.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "weather_alerts")
public class WeatherAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // the city this alert watches — must match a city name in the city table
    @Column(nullable = false)
    private String cityName;

    // which weather field to watch: TEMPERATURE, FEELS_LIKE, HUMIDITY, WIND_SPEED, PRESSURE
    @Column(nullable = false)
    private String metric;

    // comparison direction: ABOVE or BELOW
    @Column(nullable = false)
    private String operator;

    // the value to compare against — e.g. 90.0 for "temperature above 90°F"
    @Column(nullable = false)
    private double threshold;

    // user-given name to identify this alert, e.g. "Boston heat alert"
    @Column(nullable = false)
    private String label;

    // email address to notify when this alert fires — required
    @Column(nullable = false)
    private String recipientEmail;

    // true if this alert's condition was met during the last ingestion cycle
    @Column(nullable = false)
    private boolean triggered = false;

    // when the condition was first/last met — null until triggered
    private LocalDateTime triggeredAt;

    public WeatherAlert() {}

    public WeatherAlert(String cityName, String metric, String operator, double threshold, String label, String recipientEmail) {
        this.cityName = cityName;
        this.metric = metric;
        this.operator = operator;
        this.threshold = threshold;
        this.label = label;
        this.recipientEmail = recipientEmail;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }

    public String getMetric() { return metric; }
    public void setMetric(String metric) { this.metric = metric; }

    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }

    public double getThreshold() { return threshold; }
    public void setThreshold(double threshold) { this.threshold = threshold; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }

    public boolean isTriggered() { return triggered; }
    public void setTriggered(boolean triggered) { this.triggered = triggered; }

    public LocalDateTime getTriggeredAt() { return triggeredAt; }
    public void setTriggeredAt(LocalDateTime triggeredAt) { this.triggeredAt = triggeredAt; }
}
