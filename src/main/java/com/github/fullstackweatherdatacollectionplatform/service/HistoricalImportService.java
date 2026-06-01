package com.github.fullstackweatherdatacollectionplatform.service;

import com.github.fullstackweatherdatacollectionplatform.client.OpenMeteoHistoricalClient;
import com.github.fullstackweatherdatacollectionplatform.model.City;
import com.github.fullstackweatherdatacollectionplatform.model.WeatherCondition;
import com.github.fullstackweatherdatacollectionplatform.model.WeatherData;
import com.github.fullstackweatherdatacollectionplatform.repository.CityRepository;
import com.github.fullstackweatherdatacollectionplatform.repository.WeatherConditionRepository;
import com.github.fullstackweatherdatacollectionplatform.repository.WeatherDataRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class HistoricalImportService {

    private static final Map<Integer, String> WMO = new HashMap<>();

    static {
        WMO.put(0,  "clear sky");
        WMO.put(1,  "mainly clear");
        WMO.put(2,  "partly cloudy");
        WMO.put(3,  "overcast");
        WMO.put(45, "fog");
        WMO.put(48, "icy fog");
        WMO.put(51, "light drizzle");
        WMO.put(53, "drizzle");
        WMO.put(55, "heavy drizzle");
        WMO.put(61, "light rain");
        WMO.put(63, "moderate rain");
        WMO.put(65, "heavy rain");
        WMO.put(71, "light snow");
        WMO.put(73, "snow");
        WMO.put(75, "heavy snow");
        WMO.put(77, "snow grains");
        WMO.put(80, "light showers");
        WMO.put(81, "showers");
        WMO.put(82, "heavy showers");
        WMO.put(85, "snow showers");
        WMO.put(86, "heavy snow showers");
        WMO.put(95, "thunderstorm");
        WMO.put(96, "thunderstorm with hail");
        WMO.put(99, "thunderstorm with heavy hail");
    }

    private final OpenMeteoHistoricalClient openMeteoClient;
    private final CityRepository cityRepository;
    private final WeatherDataRepository weatherDataRepository;
    private final WeatherConditionRepository conditionRepository;

    public HistoricalImportService(OpenMeteoHistoricalClient openMeteoClient,
                                   CityRepository cityRepository,
                                   WeatherDataRepository weatherDataRepository,
                                   WeatherConditionRepository conditionRepository) {
        this.openMeteoClient     = openMeteoClient;
        this.cityRepository      = cityRepository;
        this.weatherDataRepository   = weatherDataRepository;
        this.conditionRepository = conditionRepository;
    }

    @Transactional
    public ImportResult importForCity(String cityName, LocalDate from, LocalDate to) {
        City city = cityRepository.findByName(cityName)
                .orElseThrow(() -> new IllegalArgumentException("City not found: " + cityName));

        // Cap end date to yesterday (Open-Meteo doesn't have today)
        LocalDate effectiveTo = to.isAfter(LocalDate.now().minusDays(1))
                ? LocalDate.now().minusDays(1)
                : to;

        // Find dates that already have data — skip them to avoid duplicates
        Set<LocalDate> existingDates = new HashSet<>();
        weatherDataRepository.findDatesWithData(
                city.getId(),
                from.atStartOfDay(),
                effectiveTo.plusDays(1).atStartOfDay()
        ).forEach(d -> existingDates.add(d.toLocalDate()));

        JsonNode response = openMeteoClient.fetchHistory(
                city.getLatitude(), city.getLongitude(), from, effectiveTo);

        JsonNode hourly = response.get("hourly");
        JsonNode times       = hourly.get("time");
        JsonNode temps       = hourly.get("temperature_2m");
        JsonNode feelsLike   = hourly.get("apparent_temperature");
        JsonNode humidity    = hourly.get("relative_humidity_2m");
        JsonNode pressure    = hourly.get("surface_pressure");
        JsonNode wind        = hourly.get("wind_speed_10m");
        JsonNode codes       = hourly.get("weather_code");

        List<WeatherData> batch = new ArrayList<>();
        int skipped = 0;

        for (int i = 0; i < times.size(); i++) {
            String timeStr = times.get(i).asString(); // "2024-01-01T00:00"
            LocalDateTime ts = LocalDateTime.parse(timeStr);

            if (existingDates.contains(ts.toLocalDate())) {
                skipped++;
                continue;
            }

            // Open-Meteo returns null for missing values — skip those slots
            if (temps.get(i).isNull() || wind.get(i).isNull()) continue;

            String desc = WMO.getOrDefault(codes.get(i).asInt(), "unknown");
            WeatherCondition condition = conditionRepository.findByDescription(desc)
                    .orElseGet(() -> conditionRepository.save(new WeatherCondition(desc)));

            WeatherData wd = new WeatherData();
            wd.setCity(city);
            wd.setTemperature(temps.get(i).asDouble());
            wd.setFeelsLike(feelsLike.get(i).isNull() ? temps.get(i).asDouble() : feelsLike.get(i).asDouble());
            wd.setHumidity(humidity.get(i).isNull() ? 0 : humidity.get(i).asInt());
            wd.setPressure(pressure.get(i).isNull() ? 1013 : (int) pressure.get(i).asDouble());
            wd.setWindSpeed(wind.get(i).asDouble());
            wd.setCondition(condition);
            wd.setFetchedAt(ts);
            batch.add(wd);
        }

        weatherDataRepository.saveAll(batch);
        return new ImportResult(cityName, batch.size(), skipped);
    }

    public record ImportResult(String city, int imported, int skipped) {}
}
