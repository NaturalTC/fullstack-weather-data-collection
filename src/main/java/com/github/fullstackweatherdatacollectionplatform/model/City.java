package com.github.fullstackweatherdatacollectionplatform.model;

import jakarta.persistence.*;

@Entity          // tells JPA/Hibernate this class maps to a database table
@Table(name = "city")  // explicitly names the table "city" in MySQL
public class City {

    @Id  // marks this field as the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // auto-increment — MySQL assigns the ID, not us
    private Long id;

    @Column(nullable = false, unique = true)  // name cannot be null and must be unique — no duplicate cities
    private String name;

    @Column(nullable = false)
    private String state;    // state code e.g. "MA"

    @Column(nullable = false)
    private String country;  // country code e.g. "US"

    @Column(nullable = false)
    private double latitude;   // used to call OpenWeatherMap by coordinates instead of city name

    @Column(nullable = false)
    private double longitude;

    public City() {}  // required by JPA — Hibernate needs a no-arg constructor to instantiate entities

    // convenience constructor used when seeding cities on startup and when adding cities via admin panel
    public City(String name, String state, String country, double latitude, double longitude) {
        this.name = name;
        this.state = state;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getState() { return state; }

    public void setState(String state) { this.state = state; }

    public String getCountry() { return country; }

    public void setCountry(String country) { this.country = country; }

    public double getLatitude() { return latitude; }

    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }

    public void setLongitude(double longitude) { this.longitude = longitude; }
}
