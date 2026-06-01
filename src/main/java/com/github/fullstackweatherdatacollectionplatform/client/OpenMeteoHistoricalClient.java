package com.github.fullstackweatherdatacollectionplatform.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

import java.time.LocalDate;

@Component
public class OpenMeteoHistoricalClient {

    private static final String BASE_URL = "https://archive-api.open-meteo.com/v1/archive";

    private final RestClient restClient;

    public OpenMeteoHistoricalClient(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    public JsonNode fetchHistory(double lat, double lon, LocalDate from, LocalDate to) {
        String url = String.format(
            "%s?latitude=%f&longitude=%f&start_date=%s&end_date=%s" +
            "&hourly=temperature_2m,apparent_temperature,relative_humidity_2m,wind_speed_10m,surface_pressure,weather_code" +
            "&temperature_unit=fahrenheit&wind_speed_unit=mph&timezone=UTC",
            BASE_URL, lat, lon, from, to
        );
        return restClient.get().uri(url).retrieve().body(JsonNode.class);
    }
}
