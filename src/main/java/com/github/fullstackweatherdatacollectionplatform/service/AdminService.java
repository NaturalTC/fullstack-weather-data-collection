package com.github.fullstackweatherdatacollectionplatform.service;

import com.github.fullstackweatherdatacollectionplatform.client.WeatherApiClient;
import com.github.fullstackweatherdatacollectionplatform.client.WeatherApiResponse;
import com.github.fullstackweatherdatacollectionplatform.dto.AdminStatsDTO;
import com.github.fullstackweatherdatacollectionplatform.model.City;
import com.github.fullstackweatherdatacollectionplatform.model.WeatherData;
import com.github.fullstackweatherdatacollectionplatform.repository.CityRepository;
import com.github.fullstackweatherdatacollectionplatform.repository.WeatherDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service           // marks this as a service layer bean — Spring manages it
@RequiredArgsConstructor  // Lombok — generates constructor for all final fields (constructor injection)
public class AdminService {

    private final WeatherDataRepository weatherDataRepository;
    private final CityRepository cityRepository;
    private final WeatherIngestionService weatherIngestionService;  // injected so we can manually trigger ingestion
    private final WeatherApiClient weatherApiClient;                // injected so we can resolve city coordinates

    // builds the admin stats response — aggregates data from the database
    public AdminStatsDTO getStats() {
        long total = weatherDataRepository.count();  // total rows in weather_data table (free from JpaRepository)

        // get the most recently fetched record to show the last fetch timestamp
        WeatherData latest = weatherDataRepository.findTopByOrderByFetchedAtDesc();
        LocalDateTime lastFetch = latest != null ? latest.getFetchedAt() : null;  // null safe — table might be empty

        // query returns List<Object[]> — each row is [cityName (String), count (Long)]
        List<Object[]> rows = weatherDataRepository.countPerCity();
        Map<String, Long> perCity = new LinkedHashMap<>();  // LinkedHashMap preserves insertion order
        for (Object[] row : rows) {
            perCity.put((String) row[0], (Long) row[1]);  // cast each element to its expected type
        }

        return new AdminStatsDTO(total, lastFetch, perCity);
    }

    // adds a new city to the monitoring schedule
    // auto-resolves lat/lon by calling OpenWeatherMap with the city name — no manual coordinate entry needed
    public City addCity(String name, String state, String country) {
        WeatherApiResponse response;
        try {
            response = weatherApiClient.fetchWeather(name + "," + state + "," + country);
        } catch (Exception e) {
            // if the city isn't found by OpenWeatherMap, throw a 400 Bad Request with a helpful message
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "City not found: " + name + ", " + state + ". Check the name and state code.");
        }
        // build the City entity using the coordinates returned from the API response
        City city = new City(response.cityName(), state, country, response.latitude(), response.longitude());
        return cityRepository.save(city);  // INSERT into the city table — scheduler will pick it up on next cycle
    }

    // removes a city and all its associated weather records
    // @Transactional — if either delete fails, both roll back — prevents orphaned records
    @Transactional
    public void removeCity(String name) {
        cityRepository.findByName(name).ifPresent(city -> {
            weatherDataRepository.deleteByCityId(city.getId());  // must delete weather records first (foreign key constraint)
            cityRepository.delete(city);                          // then delete the city itself
        });
    }

    // allows the admin panel to manually trigger the ingestion job immediately
    // just calls the same method the scheduler calls automatically every 10 minutes
    public void triggerFetch() {
        weatherIngestionService.ingestWeatherData();
    }
}
