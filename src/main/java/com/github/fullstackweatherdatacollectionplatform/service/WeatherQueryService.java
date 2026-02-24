package com.github.fullstackweatherdatacollectionplatform.service;

import com.github.fullstackweatherdatacollectionplatform.client.WeatherApiClient;
import com.github.fullstackweatherdatacollectionplatform.dto.*;
import com.github.fullstackweatherdatacollectionplatform.model.City;
import com.github.fullstackweatherdatacollectionplatform.repository.CityRepository;
import com.github.fullstackweatherdatacollectionplatform.repository.WeatherDataRepository;
import lombok.AllArgsConstructor;
import tools.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.StreamSupport;

@Service
@AllArgsConstructor
public class WeatherQueryService {

    private final WeatherDataRepository weatherDataRepository;
    private final CityRepository cityRepository;
    private final WeatherApiClient weatherApiClient;

    public List<WeatherDataDTO> getAllWeather(String cityName)
    {
        if (cityName != null && !cityName.isBlank()) {
            City city = cityRepository.findByName(cityName).orElse(null);
            if (city == null) {
                return List.of();
            }
            return weatherDataRepository.findByCityOrderByFetchedAtDesc(city)
                    .stream()
                    .map(WeatherDataDTO::from)
                    .toList();
        }
        return weatherDataRepository.findAll()
                .stream()
                .map(WeatherDataDTO::from)
                .toList();
    }

    public List<WeatherDataDTO> getLatestWeather(String cityName)
    {
        if (cityName != null && !cityName.isBlank()) {
            City city = cityRepository.findByName(cityName).orElse(null);
            if (city == null) {
                return List.of();
            }
            var latest = weatherDataRepository.findTopByCityOrderByFetchedAtDesc(city);
            return latest != null ? List.of(WeatherDataDTO.from(latest)) : List.of();
        }
        return weatherDataRepository.findLatestPerCity()
                .stream()
                .map(WeatherDataDTO::from)
                .toList();
    }

    public List<CityDTO> getAllCities()
    {
        return cityRepository.findAll()
                .stream()
                .map(CityDTO::from)
                .toList();
    }

    public List<WeatherSummaryDTO> getDailySummary(String cityName)
    {
        City city = cityRepository.findByName(cityName).orElse(null);
        if (city == null) return List.of();
        return weatherDataRepository.findDailySummaryByCityId(city.getId())
                .stream()
                .map(row -> new WeatherSummaryDTO(
                        row[0] instanceof java.sql.Date d ? d.toLocalDate() : (java.time.LocalDate) row[0],
                        ((Number) row[1]).doubleValue(),
                        ((Number) row[2]).doubleValue(),
                        ((Number) row[3]).doubleValue()
                ))
                .toList();
    }

    public List<ForecastDayDTO> getForecast(String cityName)
    {
        City city = cityRepository.findByName(cityName).orElse(null);
        if (city == null) return List.of();

        JsonNode response = weatherApiClient.fetchForecast(city.getLatitude(), city.getLongitude());
        JsonNode list = response.get("list");

        // Group 3-hour slots by date
        Map<String, List<JsonNode>> byDay = new LinkedHashMap<>();
        for (JsonNode item : list) {
            String date = item.get("dt_txt").asString().substring(0, 10);
            byDay.computeIfAbsent(date, k -> new ArrayList<>()).add(item);
        }

        return byDay.entrySet().stream()
                .map(entry -> {
                    List<JsonNode> slots = entry.getValue();
                    double high = slots.stream().mapToDouble(s -> s.get("main").get("temp_max").asDouble()).max().orElse(0);
                    double low  = slots.stream().mapToDouble(s -> s.get("main").get("temp_min").asDouble()).min().orElse(0);
                    double avgPop = slots.stream().mapToDouble(s -> s.get("pop").asDouble()).average().orElse(0);
                    String desc = slots.get(slots.size() / 2).get("weather").get(0).get("description").asString();
                    return new ForecastDayDTO(entry.getKey(), high, low, (int) (avgPop * 100), desc);
                })
                .toList();
    }

    public AqiDTO getAqi(String cityName)
    {
        City city = cityRepository.findByName(cityName).orElse(null);
        if (city == null) return null;

        JsonNode response = weatherApiClient.fetchAqi(city.getLatitude(), city.getLongitude());
        int index = response.get("list").get(0).get("main").get("aqi").asInt();
        String label = switch (index) {
            case 1 -> "Good";
            case 2 -> "Fair";
            case 3 -> "Moderate";
            case 4 -> "Poor";
            case 5 -> "Very Poor";
            default -> "Unknown";
        };
        return new AqiDTO(index, label);
    }

    public List<HeatmapEntryDTO> getHeatmap()
    {
        return weatherDataRepository.findDailyAvgTempPerCity(7)
                .stream()
                .map(row -> new HeatmapEntryDTO(
                        (String) row[0],
                        row[1] instanceof java.sql.Date d ? d.toLocalDate().toString() : row[1].toString(),
                        ((Number) row[2]).doubleValue()
                ))
                .toList();
    }
}
