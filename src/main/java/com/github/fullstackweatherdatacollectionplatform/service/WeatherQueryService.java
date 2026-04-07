package com.github.fullstackweatherdatacollectionplatform.service;

import com.github.fullstackweatherdatacollectionplatform.client.WeatherApiClient;
import com.github.fullstackweatherdatacollectionplatform.dto.*;
import com.github.fullstackweatherdatacollectionplatform.model.City;
import com.github.fullstackweatherdatacollectionplatform.repository.CityRepository;
import com.github.fullstackweatherdatacollectionplatform.repository.WeatherDataRepository;
import lombok.AllArgsConstructor;
import tools.jackson.databind.JsonNode;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.StreamSupport;

@Service          // marks this as a service layer bean — Spring manages it and it can be injected into controllers
@AllArgsConstructor // Lombok — generates a constructor for all final fields (constructor injection)
public class WeatherQueryService {

    // all injected by Spring via the Lombok-generated constructor
    private final WeatherDataRepository weatherDataRepository;
    private final CityRepository cityRepository;
    private final WeatherApiClient weatherApiClient; // only needed for forecast and AQI — those are live API calls, not DB reads

    // returns full weather history — optionally filtered by city if a city name is provided
    public List<WeatherDataDTO> getAllWeather(String cityName)
    {
        if (cityName != null && !cityName.isBlank()) {  // if a city filter was provided
            City city = cityRepository.findByName(cityName).orElse(null);  // look up the City entity by name
            if (city == null) {
                return List.of();  // city not found — return empty list instead of throwing an error
            }
            return weatherDataRepository.findByCityOrderByFetchedAtDesc(city)
                    .stream()                    // convert the List to a Stream so we can use .map()
                    .map(WeatherDataDTO::from)   // convert each WeatherData entity to a DTO (method reference)
                    .toList();                   // collect the stream back into a List
        }
        // no city filter — return all records for all cities
        return weatherDataRepository.findAll()
                .stream()
                .map(WeatherDataDTO::from)
                .toList();
    }

    // returns only the most recent record per city — used for the "current conditions" display on the frontend
    // cached so repeated page loads don't re-query the database — evicted after each ingestion cycle
    @Cacheable(value = "latestWeather", key = "#cityName ?: 'all'")
    public List<WeatherDataDTO> getLatestWeather(String cityName)
    {
        if (cityName != null && !cityName.isBlank()) {  // if a specific city was requested
            City city = cityRepository.findByName(cityName).orElse(null);
            if (city == null) {
                return List.of();  // city not found — return empty list
            }
            var latest = weatherDataRepository.findTopByCityOrderByFetchedAtDesc(city);  // LIMIT 1 query
            // ternary — if a record was found wrap it in a list, otherwise return empty list
            return latest != null ? List.of(WeatherDataDTO.from(latest)) : List.of();
        }
        // no city filter — return the latest record for every city in one query
        return weatherDataRepository.findLatestPerCity()
                .stream()
                .map(WeatherDataDTO::from)
                .toList();
    }

    // returns all cities being monitored — used to populate the city selector on the frontend
    public List<CityDTO> getAllCities()
    {
        return cityRepository.findAll()
                .stream()
                .map(CityDTO::from)  // convert each City entity to a CityDTO
                .toList();
    }

    // returns daily min/max/avg temperature for a city — used for the daily summary chart
    @Cacheable(value = "dailySummary", key = "#cityName")
    public List<WeatherSummaryDTO> getDailySummary(String cityName)
    {
        City city = cityRepository.findByName(cityName).orElse(null);
        if (city == null) return List.of();  // city not found — return empty list

        return weatherDataRepository.findDailySummaryByCityId(city.getId())
                .stream()
                // each row is Object[] — we cast each element to its expected type
                // row[0] = date, row[1] = min, row[2] = max, row[3] = avg
                .map(row -> new WeatherSummaryDTO(
                        // the date can come back as java.sql.Date (native query) or LocalDate (JPQL)
                        // this instanceof pattern handles both cases safely
                        row[0] instanceof java.sql.Date d ? d.toLocalDate() : (java.time.LocalDate) row[0],
                        ((Number) row[1]).doubleValue(),  // cast to Number first, then to double — handles both Float and Double from SQL
                        ((Number) row[2]).doubleValue(),
                        ((Number) row[3]).doubleValue()
                ))
                .toList();
    }

    // returns a 5-day forecast — fetched live from OpenWeatherMap, not stored in the database
    // cached for 10 minutes (Caffeine TTL) so we don't call the external API on every request
    @Cacheable(value = "forecast", key = "#cityName")
    public List<ForecastDayDTO> getForecast(String cityName)
    {
        City city = cityRepository.findByName(cityName).orElse(null);
        if (city == null) return List.of();

        // fetch live forecast JSON using the city's stored coordinates
        JsonNode response = weatherApiClient.fetchForecast(city.getLatitude(), city.getLongitude());
        JsonNode list = response.get("list");  // "list" is the array of 3-hour forecast slots in the API response

        // the API returns 40 x 3-hour slots — we need to group them by day to build 5 daily summaries
        // LinkedHashMap preserves insertion order so days stay in chronological order
        Map<String, List<JsonNode>> byDay = new LinkedHashMap<>();
        for (JsonNode item : list) {
            // "dt_txt" looks like "2026-03-10 09:00:00" — substring(0, 10) extracts just the date "2026-03-10"
            String date = item.get("dt_txt").asString().substring(0, 10);
            // computeIfAbsent — if this date key doesn't exist yet, create a new ArrayList for it, then add the slot
            byDay.computeIfAbsent(date, k -> new ArrayList<>()).add(item);
        }

        // now transform each day's list of 3-hour slots into a single ForecastDayDTO
        return byDay.entrySet().stream()
                .map(entry -> {
                    List<JsonNode> slots = entry.getValue();  // all 3-hour slots for this day
                    // find the highest temp_max across all slots for the day
                    double high = slots.stream().mapToDouble(s -> s.get("main").get("temp_max").asDouble()).max().orElse(0);
                    // find the lowest temp_min across all slots for the day
                    double low  = slots.stream().mapToDouble(s -> s.get("main").get("temp_min").asDouble()).min().orElse(0);
                    // "pop" = probability of precipitation (0.0 to 1.0) — average across all slots then multiply by 100 for %
                    double avgPop = slots.stream().mapToDouble(s -> s.get("pop").asDouble()).average().orElse(0);
                    // use the middle slot of the day as the representative description (avoids edge-of-day weather)
                    String desc = slots.get(slots.size() / 2).get("weather").get(0).get("description").asString();
                    return new ForecastDayDTO(entry.getKey(), high, low, (int) (avgPop * 100), desc);
                })
                .toList();
    }

    // returns the Air Quality Index for a city — fetched live from OpenWeatherMap, not stored in the database
    @Cacheable(value = "aqi", key = "#cityName")
    public AqiDTO getAqi(String cityName)
    {
        City city = cityRepository.findByName(cityName).orElse(null);
        if (city == null) return null;

        JsonNode response = weatherApiClient.fetchAqi(city.getLatitude(), city.getLongitude());
        // the AQI value is deeply nested: { "list": [ { "main": { "aqi": 2 } } ] }
        int index = response.get("list").get(0).get("main").get("aqi").asInt();

        // OpenWeatherMap returns AQI as 1-5 — map to human-readable labels
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

    // returns daily average temperature per city for the last 7 days — used for the heatmap visualization
    @Cacheable(value = "heatmap")
    public List<HeatmapEntryDTO> getHeatmap()
    {
        // pass 7 as the number of days — the repository query uses this to filter with DATE_SUB(NOW(), INTERVAL 7 DAY)
        // I have to use row mapping because I am returning an aggregate number - doesn't map to a entity
        return weatherDataRepository.findDailyAvgTempPerCity(7)
                .stream()
                // each row is Object[] — [cityName (String), date, avgTemp (Number)]
                .map(row -> new HeatmapEntryDTO(
                        (String) row[0],  // city name
                        // same sql.Date vs LocalDate handling as getDailySummary — convert to String for the DTO
                        row[1] instanceof java.sql.Date d ? d.toLocalDate().toString() : row[1].toString(),
                        ((Number) row[2]).doubleValue()  // avg temperature
                ))
                .toList();
    }
}
