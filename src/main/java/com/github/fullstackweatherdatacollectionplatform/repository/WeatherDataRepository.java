package com.github.fullstackweatherdatacollectionplatform.repository;

import com.github.fullstackweatherdatacollectionplatform.model.City;
import com.github.fullstackweatherdatacollectionplatform.model.WeatherData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WeatherDataRepository extends JpaRepository<WeatherData, Long> {

    // Find all records for a specific city, ordered by most recent first
    List<WeatherData> findByCityOrderByFetchedAtDesc(City city);

    // Find records for a city within a date range — core query for time-series charts
    List<WeatherData> findByCityAndFetchedAtBetween(City city, LocalDateTime start, LocalDateTime end);

    // Find the most recent record for a city — useful for "current weather" display
    WeatherData findTopByCityOrderByFetchedAtDesc(City city);

    // Find the most recent record for each city in one query
    @Query("SELECT w FROM WeatherData w WHERE w.id IN " +
           "(SELECT MAX(w2.id) FROM WeatherData w2 GROUP BY w2.city)")
    List<WeatherData> findLatestPerCity();

    // Aggregate min/max/avg temperature grouped by day for a specific city
    @Query(value = "SELECT DATE(fetched_at) as date, MIN(temperature) as minTemperature, " +
                   "MAX(temperature) as maxTemperature, AVG(temperature) as avgTemperature " +
                   "FROM weather_data WHERE city_id = :cityId " +
                   "GROUP BY DATE(fetched_at) ORDER BY DATE(fetched_at) DESC",
           nativeQuery = true)
    List<Object[]> findDailySummaryByCityId(@Param("cityId") Long cityId);
}
