package com.github.fullstackweatherdatacollectionplatform.service;

import com.github.fullstackweatherdatacollectionplatform.client.WeatherApiClient;
import com.github.fullstackweatherdatacollectionplatform.client.WeatherApiResponse;
import com.github.fullstackweatherdatacollectionplatform.model.City;
import com.github.fullstackweatherdatacollectionplatform.model.WeatherCondition;
import com.github.fullstackweatherdatacollectionplatform.model.WeatherData;
import com.github.fullstackweatherdatacollectionplatform.repository.CityRepository;
import com.github.fullstackweatherdatacollectionplatform.repository.WeatherConditionRepository;
import com.github.fullstackweatherdatacollectionplatform.repository.WeatherDataRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j             // Lombok — auto-generates a "log" field so we can call log.info(), log.error(), etc.
@Service           // marks this as a service layer bean — Spring manages it and it can be injected elsewhere
@RequiredArgsConstructor  // Lombok — auto-generates a constructor for all final fields (constructor injection)
public class WeatherIngestionService {

    // all injected via the Lombok-generated constructor — Spring provides these automatically
    private final WeatherApiClient weatherApiClient;
    private final WeatherDataRepository weatherDataRepository;
    private final CityRepository cityRepository;
    private final WeatherConditionRepository weatherConditionRepository;
    private final AlertService alertService;

    // @PostConstruct — runs once automatically after Spring has fully initialized this bean
    // used to seed the database with default cities on first startup if the table is empty
    // PS - I have added more cities in the deployed server that don't show here
    @PostConstruct
    public void seedCities() {
        if (cityRepository.count() > 0) return;  // if cities already exist, skip — prevents re-seeding on every restart

        List<City> defaults = List.of(
            new City("Boston",      "MA", "US",  42.3601, -71.0589),
            new City("Worcester",   "MA", "US",  42.2626, -71.8023),
            new City("Bangor",      "ME", "US",  44.8016, -68.7712),
            new City("Hartford",    "CT", "US",  41.7658, -72.6851),
            new City("Burlington",  "VT", "US",  44.4759, -73.2121),
            new City("Concord",     "NH", "US",  43.2081, -71.5376),
            new City("Providence",  "RI", "US",  41.8240, -71.4128),
            new City("Portland",    "ME", "US",  43.6615, -70.2553),
            new City("Springfield", "MA", "US",  42.1015, -72.5898)
        );
        cityRepository.saveAll(defaults);  // saveAll = single INSERT statement instead of 9 individual INSERTS
        log.info("Seeded {} default cities.", defaults.size());
    }

    // @Scheduled(fixedRate = 600000) — Spring calls this method automatically every 600,000ms (10 minutes)
    // this is the core of the app — it keeps the database continuously growing with fresh weather data
    // @CacheEvict — clears the DB-backed caches so the next request gets fresh data, not the stale cached result
    @Scheduled(fixedRate = 600000)
    @CacheEvict(value = {"latestWeather", "dailySummary", "heatmap"}, allEntries = true)
    public void ingestWeatherData() {
        for (City city : cityRepository.findAll()) {  // loop over every city in the database
            try {
                // call OpenWeatherMap API using the city's stored coordinates
                WeatherApiResponse response = weatherApiClient.fetchWeatherByCoords(
                        city.getLatitude(), city.getLongitude());

                // check if this condition description already exists in the weather_condition table
                // if it does, reuse it — if not, create and save a new one
                // this is the normalization logic that prevents "moderate rain" from being stored thousands of times
                WeatherCondition condition = weatherConditionRepository.findByDescription(response.description())
                        .orElseGet(() -> weatherConditionRepository.save(new WeatherCondition(response.description())));

                WeatherData data = buildWeatherData(city, condition, response);
                weatherDataRepository.save(data);  // INSERT the new row into weather_data
                log.info("Saved weather data for {}.", city.getName());

                // check if any alert rules for this city are now triggered by the new data
                alertService.checkAlerts(
                        city.getName(),
                        response.temperature(),
                        response.feelsLike(),
                        response.humidity(),
                        response.windSpeed(),
                        response.pressure()
                );
            } catch (Exception e) {
                // catch per-city so one failed city doesn't stop the rest from being processed
                log.error("Failed to fetch weather for {}: {}", city.getName(), e.getMessage());
            }
        }
    }

    // private helper — separates the object construction logic from the loop to keep ingestWeatherData clean
    private static WeatherData buildWeatherData(City city, WeatherCondition condition, WeatherApiResponse response) {
        WeatherData data = new WeatherData();
        data.setCity(city);                          // sets the foreign key relationship to the city
        data.setCondition(condition);                // sets the foreign key relationship to the condition
        data.setTemperature(response.temperature());
        data.setFeelsLike(response.feelsLike());
        data.setHumidity(response.humidity());
        data.setPressure(response.pressure());
        data.setWindSpeed(response.windSpeed());
        data.setFetchedAt(response.fetchedAt());     // UTC timestamp generated in WeatherApiClient
        return data;
    }
}
