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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherIngestionService {

    private final WeatherApiClient weatherApiClient;
    private final WeatherDataRepository weatherDataRepository;
    private final CityRepository cityRepository;
    private final WeatherConditionRepository weatherConditionRepository;

    // Seed the 9 original New England cities if the table is empty on first run
    @PostConstruct
    public void seedCities() {
        if (cityRepository.count() > 0) return;

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
        cityRepository.saveAll(defaults);
        log.info("Seeded {} default cities.", defaults.size());
    }

    @Scheduled(fixedRate = 600000) // runs every 10 mins
    public void ingestWeatherData() {
        for (City city : cityRepository.findAll()) {
            try {
                WeatherApiResponse response = weatherApiClient.fetchWeatherByCoords(
                        city.getLatitude(), city.getLongitude());

                WeatherCondition condition = weatherConditionRepository.findByDescription(response.description())
                        .orElseGet(() -> weatherConditionRepository.save(new WeatherCondition(response.description())));

                WeatherData data = buildWeatherData(city, condition, response);
                weatherDataRepository.save(data);
                log.info("Saved weather data for {}.", city.getName());
            } catch (Exception e) {
                log.error("Failed to fetch weather for {}: {}", city.getName(), e.getMessage());
            }
        }
    }

    private static WeatherData buildWeatherData(City city, WeatherCondition condition, WeatherApiResponse response) {
        WeatherData data = new WeatherData();
        data.setCity(city);
        data.setCondition(condition);
        data.setTemperature(response.temperature());
        data.setFeelsLike(response.feelsLike());
        data.setHumidity(response.humidity());
        data.setPressure(response.pressure());
        data.setWindSpeed(response.windSpeed());
        data.setFetchedAt(response.fetchedAt());
        return data;
    }
}
