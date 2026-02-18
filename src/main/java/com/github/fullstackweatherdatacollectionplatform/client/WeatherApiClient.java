package com.github.fullstackweatherdatacollectionplatform.client;

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

    // Calls the OpenWeatherMap API and returns parsed response data
    public WeatherApiResponse fetchWeather(String city) {
        String url = String.format("%s?q=%s&appid=%s&units=imperial", apiUrl, city, apiKey);

        JsonNode response = restClient.get()
                .uri(url)
                .retrieve()
                .body(JsonNode.class);

        long dt = response.get("dt").asLong();

        return new WeatherApiResponse(
                response.get("name").asText(),
                response.get("sys").get("country").asText(),
                response.get("coord").get("lat").asDouble(),
                response.get("coord").get("lon").asDouble(),
                response.get("main").get("temp").asDouble(),
                response.get("main").get("feels_like").asDouble(),
                response.get("main").get("humidity").asInt(),
                response.get("main").get("pressure").asInt(),
                response.get("wind").get("speed").asDouble(),
                response.get("weather").get(0).get("description").asText(),
                LocalDateTime.ofInstant(Instant.ofEpochSecond(dt), ZoneOffset.UTC),
                LocalDateTime.now()
        );
    }
}
