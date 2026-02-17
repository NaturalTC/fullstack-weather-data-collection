package com.github.fullstackweatherdatacollectionplatform.client;

import com.github.fullstackweatherdatacollectionplatform.model.WeatherData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
public class WeatherApiClient {

    private final RestClient restClient;

    @Value("${weather.api.key}")
    private String apiKey;

    @Value("${weather.api.url}")
    private String apiUrl;

    public WeatherApiClient(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    // Calls the OpenWeatherMap API and maps the JSON response to a WeatherData entity
    public WeatherData fetchWeather(String city) {
        // Build the full URL with query parameters: city, API key, and metric units
        String url = String.format("%s?q=%s&appid=%s&units=metric", apiUrl, city, apiKey);

        // Make the GET request and parse the response as a JSON tree
        JsonNode response = restClient.get()
                .uri(url)
                .retrieve()
                .body(JsonNode.class);

        // Map the nested JSON fields into our WeatherData entity
        WeatherData data = new WeatherData();
        data.setCityName(response.get("name").asText());
        data.setTemperature(response.get("main").get("temp").asDouble());
        data.setFeelsLike(response.get("main").get("feels_like").asDouble());
        data.setHumidity(response.get("main").get("humidity").asInt());
        data.setPressure(response.get("main").get("pressure").asInt());
        data.setWindSpeed(response.get("wind").get("speed").asDouble());
        data.setDescription(response.get("weather").get(0).get("description").asText());

        // Convert the Unix timestamp from the API into a LocalDateTime
        long dt = response.get("dt").asLong();
        data.setTimestamp(LocalDateTime.ofInstant(Instant.ofEpochSecond(dt), ZoneOffset.UTC));

        // Record when our system fetched this data
        data.setFetchedAt(LocalDateTime.now());

        return data;
    }
}
