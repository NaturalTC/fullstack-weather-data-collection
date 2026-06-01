package com.github.fullstackweatherdatacollectionplatform.client;

import com.github.fullstackweatherdatacollectionplatform.dto.WeatherApiResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

/**
 * Weather data client — uses Open-Meteo (free, no API key required).
 * Replaces OpenWeatherMap to eliminate API costs.
 *
 * Endpoints used:
 *   Current:   https://api.open-meteo.com/v1/forecast  (current= params)
 *   Forecast:  https://api.open-meteo.com/v1/forecast  (daily= params)
 *   AQI:       https://air-quality-api.open-meteo.com/v1/air-quality
 *   Geocoding: https://geocoding-api.open-meteo.com/v1/search
 */
@Component
public class WeatherApiClient {

    private static final String FORECAST_URL    = "https://api.open-meteo.com/v1/forecast";
    private static final String AQI_URL         = "https://air-quality-api.open-meteo.com/v1/air-quality";
    private static final String GEOCODING_URL   = "https://geocoding-api.open-meteo.com/v1/search";

    // WMO weather interpretation codes → human-readable descriptions
    private static final Map<Integer, String> WMO = Map.ofEntries(
        Map.entry(0,  "clear sky"),
        Map.entry(1,  "mainly clear"),
        Map.entry(2,  "partly cloudy"),
        Map.entry(3,  "overcast"),
        Map.entry(45, "fog"),
        Map.entry(48, "icy fog"),
        Map.entry(51, "light drizzle"),
        Map.entry(53, "drizzle"),
        Map.entry(55, "heavy drizzle"),
        Map.entry(61, "light rain"),
        Map.entry(63, "moderate rain"),
        Map.entry(65, "heavy rain"),
        Map.entry(71, "light snow"),
        Map.entry(73, "snow"),
        Map.entry(75, "heavy snow"),
        Map.entry(77, "snow grains"),
        Map.entry(80, "light showers"),
        Map.entry(81, "showers"),
        Map.entry(82, "heavy showers"),
        Map.entry(85, "snow showers"),
        Map.entry(86, "heavy snow showers"),
        Map.entry(95, "thunderstorm"),
        Map.entry(96, "thunderstorm with hail"),
        Map.entry(99, "thunderstorm with heavy hail")
    );

    private final RestClient restClient;

    public WeatherApiClient(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    // Current conditions by coordinates — used by the 10-minute ingestion scheduler
    public WeatherApiResponse fetchWeatherByCoords(double lat, double lon) {
        String url = String.format(
            "%s?latitude=%f&longitude=%f" +
            "&current=temperature_2m,apparent_temperature,relative_humidity_2m,wind_speed_10m,surface_pressure,weather_code" +
            "&temperature_unit=fahrenheit&wind_speed_unit=mph&timezone=UTC",
            FORECAST_URL, lat, lon
        );

        JsonNode response = restClient.get().uri(url).retrieve().body(JsonNode.class);
        JsonNode current  = response.get("current");

        String description = WMO.getOrDefault(current.get("weather_code").asInt(), "unknown");

        return new WeatherApiResponse(
            "",
            "US",
            lat, lon,
            current.get("temperature_2m").asDouble(),
            current.get("apparent_temperature").asDouble(),
            current.get("relative_humidity_2m").asInt(),
            (int) current.get("surface_pressure").asDouble(),
            current.get("wind_speed_10m").asDouble(),
            description,
            LocalDateTime.now(ZoneOffset.UTC)
        );
    }

    // Resolve a city name to coordinates using Open-Meteo's free geocoding API
    // Used when adding a new city via the admin panel
    public WeatherApiResponse fetchWeather(String cityInput) {
        String cityName = cityInput.split(",")[0].trim();
        String encoded  = URLEncoder.encode(cityName, StandardCharsets.UTF_8);

        String url = String.format("%s?name=%s&count=5&language=en&format=json", GEOCODING_URL, encoded);
        JsonNode response = restClient.get().uri(url).retrieve().body(JsonNode.class);
        JsonNode results  = response.get("results");

        if (results == null || results.isEmpty()) {
            throw new RuntimeException("City not found: " + cityName);
        }

        // Prefer US results
        JsonNode best = results.get(0);
        for (JsonNode r : results) {
            JsonNode code = r.get("country_code");
            if (code != null && "US".equals(code.asString())) { best = r; break; }
        }

        return new WeatherApiResponse(
            best.get("name").asString(),
            best.has("country_code") ? best.get("country_code").asString() : "US",
            best.get("latitude").asDouble(),
            best.get("longitude").asDouble(),
            0, 0, 0, 0, 0, "unknown",
            LocalDateTime.now(ZoneOffset.UTC)
        );
    }

    // 5-day daily forecast — returns raw JsonNode in Open-Meteo daily format
    // Parsed in WeatherQueryService.getForecast()
    public JsonNode fetchForecast(double lat, double lon) {
        String url = String.format(
            "%s?latitude=%f&longitude=%f" +
            "&daily=temperature_2m_max,temperature_2m_min,precipitation_probability_max,weather_code" +
            "&temperature_unit=fahrenheit&wind_speed_unit=mph&timezone=auto&forecast_days=5",
            FORECAST_URL, lat, lon
        );
        return restClient.get().uri(url).retrieve().body(JsonNode.class);
    }

    // Air quality index — returns raw JsonNode in Open-Meteo AQI format
    // Parsed in WeatherQueryService.getAqi()
    public JsonNode fetchAqi(double lat, double lon) {
        String url = String.format(
            "%s?latitude=%f&longitude=%f&current=us_aqi",
            AQI_URL, lat, lon
        );
        return restClient.get().uri(url).retrieve().body(JsonNode.class);
    }
}
