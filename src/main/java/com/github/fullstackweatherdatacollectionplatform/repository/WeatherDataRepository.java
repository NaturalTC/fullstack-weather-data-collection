package com.github.fullstackweatherdatacollectionplatform.repository;

import com.github.fullstackweatherdatacollectionplatform.model.City;
import com.github.fullstackweatherdatacollectionplatform.model.WeatherData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WeatherDataRepository extends JpaRepository<WeatherData, Long> {

    // Find all records for a specific city, ordered by most recent first
    List<WeatherData> findByCityOrderByTimestampDesc(City city);

    // Find records for a city within a date range — core query for time-series charts
    List<WeatherData> findByCityAndTimestampBetween(City city, LocalDateTime start, LocalDateTime end);

    // Find the most recent record for a city — useful for "current weather" display
    WeatherData findTopByCityOrderByTimestampDesc(City city);

    // Find the most recent record for each city in one query
    @Query("SELECT w FROM WeatherData w WHERE w.timestamp = " +
           "(SELECT MAX(w2.timestamp) FROM WeatherData w2 WHERE w2.city = w.city)")
    List<WeatherData> findLatestPerCity();
}
