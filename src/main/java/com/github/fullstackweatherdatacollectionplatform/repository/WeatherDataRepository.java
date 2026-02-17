package com.github.fullstackweatherdatacollectionplatform.repository;

import com.github.fullstackweatherdatacollectionplatform.model.WeatherData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WeatherDataRepository extends JpaRepository<WeatherData, Long> {

    // Find all records for a specific city, ordered by most recent first
    List<WeatherData> findByCityNameOrderByTimestampDesc(String cityName);

    // Find records for a city within a date range — core query for time-series charts
    List<WeatherData> findByCityNameAndTimestampBetween(String cityName, LocalDateTime start, LocalDateTime end);

    // Find the most recent record for a city — useful for "current weather" display
    WeatherData findTopByCityNameOrderByTimestampDesc(String cityName);
}
