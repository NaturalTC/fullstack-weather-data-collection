package com.github.fullstackweatherdatacollectionplatform.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "weather_data")
public class WeatherData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // City name returned by the API (e.g., "Province of Turin")
    @Column(nullable = false)
    private String cityName;

    // Temperature in the units specified by the API call (metric = Celsius)
    @Column(nullable = false)
    private double temperature;

    // "Feels like" temperature accounting for wind chill and humidity
    @Column(nullable = false)
    private double feelsLike;

    // Humidity percentage (0-100)
    @Column(nullable = false)
    private int humidity;

    // Atmospheric pressure in hPa
    @Column(nullable = false)
    private int pressure;

    // Wind speed in meters/sec (metric) or miles/hour (imperial)
    @Column(nullable = false)
    private double windSpeed;

    // Short text description of conditions (e.g., "moderate rain", "clear sky")
    @Column(nullable = false)
    private String description;

    // Timestamp from the weather station — when the data was actually recorded
    @Column(nullable = false)
    private LocalDateTime timestamp;

    // Timestamp from our system — when the scheduler fetched this data
    @Column(nullable = false)
    private LocalDateTime fetchedAt;

    public WeatherData() {
    }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getCityName() { return cityName; }

    public void setCityName(String cityName) { this.cityName = cityName; }

    public double getTemperature() { return temperature; }

    public void setTemperature(double temperature) { this.temperature = temperature; }

    public double getFeelsLike() { return feelsLike; }

    public void setFeelsLike(double feelsLike) { this.feelsLike = feelsLike; }

    public int getHumidity() { return humidity; }

    public void setHumidity(int humidity) { this.humidity = humidity; }

    public int getPressure() { return pressure; }

    public void setPressure(int pressure) { this.pressure = pressure; }

    public double getWindSpeed() { return windSpeed; }

    public void setWindSpeed(double windSpeed) { this.windSpeed = windSpeed; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getTimestamp() { return timestamp; }

    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public LocalDateTime getFetchedAt() { return fetchedAt; }

    public void setFetchedAt(LocalDateTime fetchedAt) { this.fetchedAt = fetchedAt; }
}
