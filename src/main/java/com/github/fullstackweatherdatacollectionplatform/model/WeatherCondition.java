package com.github.fullstackweatherdatacollectionplatform.model;

import jakarta.persistence.*;

@Entity
@Table(name = "weather_condition")
// this table exists for normalization — instead of storing "moderate rain" as a string in every
// weather_data row, we store it once here and reference it by ID via a foreign key
// this prevents string duplication across thousands of rows
public class WeatherCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // auto-increment primary key
    private Long id;

    @Column(nullable = false, unique = true)  // unique — "moderate rain" only exists once in this table
    private String description;  // e.g. "moderate rain", "clear sky", "broken clouds"

    public WeatherCondition() {}  // required by JPA — Hibernate needs a no-arg constructor

    public WeatherCondition(String description) {
        this.description = description;
    }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }
}
