package com.github.fullstackweatherdatacollectionplatform.service;

import com.github.fullstackweatherdatacollectionplatform.client.WeatherApiClient;
import com.github.fullstackweatherdatacollectionplatform.client.WeatherApiResponse;
import com.github.fullstackweatherdatacollectionplatform.model.City;
import com.github.fullstackweatherdatacollectionplatform.model.WeatherData;
import com.github.fullstackweatherdatacollectionplatform.repository.CityRepository;
import com.github.fullstackweatherdatacollectionplatform.repository.WeatherDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WeatherIngestionService {

    private final WeatherApiClient weatherApiClient;
    private final WeatherDataRepository weatherDataRepository;
    private final CityRepository cityRepository;

    private static final List<String> CITY_NAMES = List.of("Boston", "Worcester", "Bangor", "Hartford");

    @Scheduled(fixedRate = 60000) // runs every 60 seconds — increase to 3600000 (1 hour) for production
    public void ingestWeatherData() {
        for (String cityName : CITY_NAMES) {
            try {
                WeatherApiResponse response = weatherApiClient.fetchWeather(cityName);

                // Find existing city or create a new one from the API response
                City city = cityRepository.findByName(response.cityName())
                        .orElseGet(() -> cityRepository.save(new City(
                                response.cityName(),
                                response.country(),
                                response.latitude(),
                                response.longitude()
                        )));

                WeatherData data = new WeatherData();
                data.setCity(city);
                data.setTemperature(response.temperature());
                data.setFeelsLike(response.feelsLike());
                data.setHumidity(response.humidity());
                data.setPressure(response.pressure());
                data.setWindSpeed(response.windSpeed());
                data.setDescription(response.description());
                data.setTimestamp(response.timestamp());
                data.setFetchedAt(response.fetchedAt());

                weatherDataRepository.save(data);
                System.out.println("Saved weather data for " + cityName);
            } catch (Exception e) {
                System.out.println("Failed to fetch weather for " + cityName + ": " + e.getMessage());
            }
        }
    }
}
