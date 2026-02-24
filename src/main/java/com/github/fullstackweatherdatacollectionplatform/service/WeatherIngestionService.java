package com.github.fullstackweatherdatacollectionplatform.service;

import com.github.fullstackweatherdatacollectionplatform.client.WeatherApiClient;
import com.github.fullstackweatherdatacollectionplatform.client.WeatherApiResponse;
import com.github.fullstackweatherdatacollectionplatform.model.City;
import com.github.fullstackweatherdatacollectionplatform.model.WeatherCondition;
import com.github.fullstackweatherdatacollectionplatform.model.WeatherData;
import com.github.fullstackweatherdatacollectionplatform.repository.CityRepository;
import com.github.fullstackweatherdatacollectionplatform.repository.WeatherConditionRepository;
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
    private final WeatherConditionRepository weatherConditionRepository;

    private static final List<String> CITY_NAMES = List.of(
            "Boston,MA,US", "Worcester,MA,US", "Bangor,ME,US", "Hartford,CT,US",
            "Burlington,VT,US", "Concord,NH,US", "Providence,RI,US", "Portland,ME,US",
            "Springfield,MA,US"
    );

    @Scheduled(fixedRate = 600000) // runs every 10 mins
    public void ingestWeatherData()
    {
        for (String cityName : CITY_NAMES) {
            try {
                WeatherApiResponse response = weatherApiClient.fetchWeather(cityName);

                // Parse state code from query string e.g. "Boston,MA,US" → "MA"
                String[] parts = cityName.split(",");
                String state = parts.length > 1 ? parts[1] : "";

                // Find existing city or create a new one from the API response
                City city = cityRepository.findByName(response.cityName())
                        .orElseGet(() -> cityRepository.save(new City(
                                response.cityName(),
                                state,
                                response.country(),
                                response.latitude(),
                                response.longitude()
                        )));

                // Find existing condition or create a new one
                WeatherCondition condition = weatherConditionRepository.findByDescription(response.description())
                        .orElseGet(() -> weatherConditionRepository.save(new WeatherCondition(response.description())));

                WeatherData data = getWeatherData(city, condition, response);
                weatherDataRepository.save(data);
                System.out.println("Saved weather data for " + cityName);
            } catch (Exception e) {
                System.out.println("Failed to fetch weather for " + cityName + ": " + e.getMessage());
            }
        }
    }

    private static WeatherData getWeatherData(City city, WeatherCondition condition, WeatherApiResponse response)
    {
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
