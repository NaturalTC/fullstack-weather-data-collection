package com.github.fullstackweatherdatacollectionplatform.service;

import com.github.fullstackweatherdatacollectionplatform.client.WeatherApiClient;
import com.github.fullstackweatherdatacollectionplatform.model.WeatherData;
import com.github.fullstackweatherdatacollectionplatform.repository.WeatherDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor // LOMBOK
public class WeatherIngestionService {

    // both fields are final because springboot will handle the constructors automatically
    // LOMBOK (RequiredArgsConstructor) only works with final attributes
    private final WeatherApiClient weatherApiClient;
    private final WeatherDataRepository weatherDataRepository;

    @Scheduled(fixedRate = 60000) // runs every 60 seconds — increase to 3600000 (1 hour) for production
    public void ingestWeatherData() {
        // TODO: Define a list of cities to collect weather data for
        //   - e.g., List<String> cities = List.of("London", "New York", "Tokyo")

        List<String> cities = List.of("Boston", "Worcester", "Bangor", "Hartford");
        // TODO: Loop through each city:
        //   1. Call weatherApiClient.fetchWeather(city) to get a WeatherData object
        //   2. Save it to the database using weatherDataRepository.save(data)
        //   3. Log a message so you can see it working (use slf4j logger or System.out for now)

        for(String city: cities)
        {
            try{
                WeatherData currentWeather = weatherApiClient.fetchWeather(city);
                weatherDataRepository.save(currentWeather);
                System.out.println("Saved weather data for " + city);
            } catch (Exception e) {
                System.out.println("Failed to fetch weather for " + city + ": " + e.getMessage());
            }
        }
    }
}
