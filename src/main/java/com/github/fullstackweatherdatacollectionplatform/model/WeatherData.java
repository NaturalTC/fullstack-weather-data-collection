package com.github.fullstackweatherdatacollectionplatform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity                       // tells JPA/Hibernate this class maps to a database table
@AllArgsConstructor
@Table(name = "weather_data") // the main time-series table — grows ~9 rows per 10-minute scheduler cycle
public class WeatherData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // auto-increment primary key assigned by MySQL
    private Long id;

    // ManyToOne — many weather records can belong to one city
    // @JoinColumn creates a "city_id" foreign key column in weather_data pointing to the city table
    // optional = false means every record must have a city — cannot be null
    @ManyToOne(optional = false)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    // temperature in imperial (°F) — units set by the API call
    @Column(nullable = false)
    private double temperature;

    // feels-like temperature accounting for wind chill and humidity
    @Column(nullable = false)
    private double feelsLike;

    // humidity percentage (0-100)
    @Column(nullable = false)
    private int humidity;

    // atmospheric pressure in hPa
    @Column(nullable = false)
    private int pressure;

    // wind speed in mph (imperial)
    @Column(nullable = false)
    private double windSpeed;

    // ManyToOne — many weather records can share the same condition (e.g. thousands of "clear sky" rows)
    // normalized into its own table to avoid storing the same string thousands of times
    @ManyToOne(optional = false)
    @JoinColumn(name = "condition_id", nullable = false)
    private WeatherCondition condition;

    // UTC timestamp of when the scheduler fetched this record — used for time-series charts and summaries
    @Column(nullable = false)
    private LocalDateTime fetchedAt;

    public WeatherData() {}  // required by JPA — Hibernate needs a no-arg constructor to instantiate entities

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public City getCity() { return city; }

    public void setCity(City city) { this.city = city; }

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

    public WeatherCondition getCondition() { return condition; }

    public void setCondition(WeatherCondition condition) { this.condition = condition; }

    public LocalDateTime getFetchedAt() { return fetchedAt; }

    public void setFetchedAt(LocalDateTime fetchedAt) { this.fetchedAt = fetchedAt; }
}
