package com.github.fullstackweatherdatacollectionplatform.repository;

import com.github.fullstackweatherdatacollectionplatform.model.WeatherCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// @Repository — marks this as a data access bean, Spring manages it
// extends JpaRepository<WeatherCondition, Long> — free CRUD methods included
@Repository
public interface WeatherConditionRepository extends JpaRepository<WeatherCondition, Long> {

    // used during ingestion to check if a condition already exists before creating a duplicate
    // returns Optional so the caller can use .orElseGet() to create it only if it doesn't exist
    // see WeatherIngestionService: findByDescription(...).orElseGet(() -> save(new WeatherCondition(...)))
    Optional<WeatherCondition> findByDescription(String description);
}
