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



@Service
@AllArgsConstructor
public class WeatherQueryService {

    private static final Map<Integer, String> WMO_LABELS = Map.ofEntries(
        Map.entry(0,  "clear sky"),      Map.entry(1,  "mainly clear"),
        Map.entry(2,  "partly cloudy"),  Map.entry(3,  "overcast"),
        Map.entry(45, "fog"),            Map.entry(48, "icy fog"),
        Map.entry(51, "light drizzle"),  Map.entry(53, "drizzle"),
        Map.entry(55, "heavy drizzle"),  Map.entry(61, "light rain"),
        Map.entry(63, "moderate rain"),  Map.entry(65, "heavy rain"),
        Map.entry(71, "light snow"),     Map.entry(73, "snow"),
        Map.entry(75, "heavy snow"),     Map.entry(80, "light showers"),
        Map.entry(81, "showers"),        Map.entry(82, "heavy showers"),
        Map.entry(95, "thunderstorm"),   Map.entry(96, "thunderstorm with hail")
    );

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

    // returns a 5-day forecast — Open-Meteo daily format (max/min/precip/code per day)
    @Cacheable(value = "forecast", key = "#cityName")
    public List<ForecastDayDTO> getForecast(String cityName)
    {
        City city = cityRepository.findByName(cityName).orElse(null);
        if (city == null) return List.of();

        JsonNode response = weatherApiClient.fetchForecast(city.getLatitude(), city.getLongitude());
        JsonNode daily    = response.get("daily");
        JsonNode times    = daily.get("time");
        JsonNode highs    = daily.get("temperature_2m_max");
        JsonNode lows     = daily.get("temperature_2m_min");
        JsonNode pops     = daily.get("precipitation_probability_max");
        JsonNode codes    = daily.get("weather_code");

        List<ForecastDayDTO> result = new ArrayList<>();
        for (int i = 0; i < times.size(); i++) {
            String desc = WMO_LABELS.getOrDefault(codes.get(i).asInt(), "unknown");
            result.add(new ForecastDayDTO(
                times.get(i).asString(),
                highs.get(i).asDouble(),
                lows.get(i).asDouble(),
                pops.get(i).isNull() ? 0 : pops.get(i).asInt(),
                desc
            ));
        }
        return result;
    }

    // returns the Air Quality Index — Open-Meteo air quality API, US AQI scale mapped to 1-5
    @Cacheable(value = "aqi", key = "#cityName")
    public AqiDTO getAqi(String cityName)
    {
        City city = cityRepository.findByName(cityName).orElse(null);
        if (city == null) return null;

        JsonNode response = weatherApiClient.fetchAqi(city.getLatitude(), city.getLongitude());
        JsonNode currentNode = response.get("current");
        if (currentNode == null || currentNode.get("us_aqi") == null) return null;

        int usAqi = currentNode.get("us_aqi").asInt();
        int index = usAqi <= 50 ? 1 : usAqi <= 100 ? 2 : usAqi <= 150 ? 3 : usAqi <= 200 ? 4 : 5;

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
