package com.github.fullstackweatherdatacollectionplatform.model;

import jakarta.persistence.*;

@Entity
@Table(name = "weather_condition")
public class WeatherCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String description;

    public WeatherCondition() {}

    public WeatherCondition(String description) {
        this.description = description;
    }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }
}
