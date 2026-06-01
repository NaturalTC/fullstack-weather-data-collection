package com.github.fullstackweatherdatacollectionplatform.repository;

import com.github.fullstackweatherdatacollectionplatform.model.City;
import com.github.fullstackweatherdatacollectionplatform.model.WeatherData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

// @Repository — marks this as a data access layer bean, Spring manages it and translates DB exceptions
// extends JpaRepository<WeatherData, Long> — gives us free CRUD methods (save, findAll, findById, delete, count, etc.)
// WeatherData = the entity this repo manages, Long = the type of its primary key
@Repository
public interface WeatherDataRepository extends JpaRepository<WeatherData, Long> {

    // Spring Data JPA generates the SQL automatically from the method name
    // "findBy" + "City" + "OrderBy" + "FetchedAt" + "Desc" → SELECT * FROM weather_data WHERE city_id = ? ORDER BY fetched_at DESC
    List<WeatherData> findByCityOrderByFetchedAtDesc(City city);

    // generates: SELECT * FROM weather_data WHERE city_id = ? AND fetched_at BETWEEN ? AND ?
    // used for time-series chart queries with a date range
    List<WeatherData> findByCityAndFetchedAtBetween(City city, LocalDateTime start, LocalDateTime end);

    // "Top" = LIMIT 1 — gets the single most recent record for a city
    // used to display current weather conditions for a specific city
    WeatherData findTopByCityOrderByFetchedAtDesc(City city);

    // Spring Data can't auto-generate this so we write it manually with @Query (JPQL — Java-style SQL)
    // gets the latest record per city by finding the max ID in each city group
    // MAX(id) works as a proxy for "most recent" since IDs are auto-incremented
    @Query("SELECT w FROM WeatherData w WHERE w.id IN " +
           "(SELECT MAX(w2.id) FROM WeatherData w2 GROUP BY w2.city)")
    List<WeatherData> findLatestPerCity();

    // nativeQuery = true means this is real SQL (not JPQL) — needed for MySQL-specific functions like DATE()
    // aggregates temperature per day: groups all records for a city by date and calculates min/max/avg
    // returns List<Object[]> because the result isn't a single entity — each row is [date, min, max, avg]
    @Query(value = "SELECT DATE(fetched_at) as date, MIN(temperature) as minTemperature, " +
                   "MAX(temperature) as maxTemperature, AVG(temperature) as avgTemperature " +
                   "FROM weather_data WHERE city_id = :cityId " +
                   "GROUP BY DATE(fetched_at) ORDER BY DATE(fetched_at) DESC",
           nativeQuery = true)
    List<Object[]> findDailySummaryByCityId(@Param("cityId") Long cityId);  // @Param binds the :cityId placeholder to the method argument

    // LIMIT 1 across all cities — used in admin stats to show when the last successful fetch happened
    WeatherData findTopByOrderByFetchedAtDesc();

    // JPQL query — returns city name and record count grouped by city
    // returns List<Object[]> because each row is [cityName (String), count (Long)] not a WeatherData entity
    @Query("SELECT w.city.name, COUNT(w) FROM WeatherData w GROUP BY w.city.name")
    List<Object[]> countPerCity();

    // @Modifying — required whenever the query changes data (INSERT/UPDATE/DELETE), not just reads
    // @Transactional — wraps in a transaction: if anything fails, the whole delete rolls back to prevent partial deletes
    @Modifying
    @Transactional
    void deleteByCityId(Long cityId);

    // Returns distinct dates that already have data for a city — used to skip days during historical import
    @Query(value = "SELECT DISTINCT DATE(fetched_at) FROM weather_data WHERE city_id = :cityId AND fetched_at BETWEEN :start AND :end",
           nativeQuery = true)
    List<java.sql.Date> findDatesWithData(@Param("cityId") Long cityId,
                                          @Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end);

    // native SQL JOIN query — JOINs weather_data and city tables to get city names alongside avg temps
    // DATE_SUB(NOW(), INTERVAL :days DAY) = only include records from the last N days
    // GROUP BY city and date = one avg temp row per city per day
    // returns List<Object[]> because each row is [cityName, date, avgTemp] — not a single entity
    @Query(value = "SELECT c.name, DATE(w.fetched_at), AVG(w.temperature) " +
                   "FROM weather_data w JOIN city c ON w.city_id = c.id " +
                   "WHERE w.fetched_at >= DATE_SUB(NOW(), INTERVAL :days DAY) " +
                   "GROUP BY c.name, DATE(w.fetched_at) " +
                   "ORDER BY DATE(w.fetched_at), c.name",
           nativeQuery = true)
    List<Object[]> findDailyAvgTempPerCity(@Param("days") int days);
}
