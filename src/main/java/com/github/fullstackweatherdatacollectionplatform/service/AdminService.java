package com.github.fullstackweatherdatacollectionplatform.service;

import com.github.fullstackweatherdatacollectionplatform.client.WeatherApiClient;
import com.github.fullstackweatherdatacollectionplatform.client.WeatherApiResponse;
import com.github.fullstackweatherdatacollectionplatform.dto.AdminStatsDTO;
import com.github.fullstackweatherdatacollectionplatform.model.City;
import com.github.fullstackweatherdatacollectionplatform.model.WeatherData;
import com.github.fullstackweatherdatacollectionplatform.repository.CityRepository;
import com.github.fullstackweatherdatacollectionplatform.repository.WeatherDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final WeatherDataRepository weatherDataRepository;
    private final CityRepository cityRepository;
    private final WeatherIngestionService weatherIngestionService;
    private final WeatherApiClient weatherApiClient;

    public AdminStatsDTO getStats() {
        long total = weatherDataRepository.count();

        WeatherData latest = weatherDataRepository.findTopByOrderByFetchedAtDesc();
        LocalDateTime lastFetch = latest != null ? latest.getFetchedAt() : null;

        List<Object[]> rows = weatherDataRepository.countPerCity();
        Map<String, Long> perCity = new LinkedHashMap<>();
        for (Object[] row : rows) {
            perCity.put((String) row[0], (Long) row[1]);
        }

        return new AdminStatsDTO(total, lastFetch, perCity);
    }

    public City addCity(String name, String state, String country) {
        WeatherApiResponse response = weatherApiClient.fetchWeather(name + "," + state + "," + country);
        City city = new City(response.cityName(), state, country, response.latitude(), response.longitude());
        return cityRepository.save(city);
    }

    @Transactional
    public void removeCity(String name) {
        cityRepository.findByName(name).ifPresent(city -> {
            weatherDataRepository.deleteByCityId(city.getId());
            cityRepository.delete(city);
        });
    }

    public void triggerFetch() {
        weatherIngestionService.ingestWeatherData();
    }
}
