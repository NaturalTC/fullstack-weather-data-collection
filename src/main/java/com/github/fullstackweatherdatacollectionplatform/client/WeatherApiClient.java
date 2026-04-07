package com.github.fullstackweatherdatacollectionplatform.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/*
    Client for my weatherApi so the service layer can use it
    tells Spring manage this class for me.
 */
@Component
public class WeatherApiClient {

    // Spring's built-in HTTP client. It's what actually makes the GET
    // request to OpenWeatherMap — like a browser but in code.
    private final RestClient restClient;

    /*
        @Value just references my resource app-properties
        each attribute needed for my API call
     */
    @Value("${weather.api.key}")
    private String apiKey;

    @Value("${weather.api.url}")
    private String apiUrl;

    @Value("${weather.forecast.url}")
    private String forecastUrl;

    @Value("${weather.aqi.url}")
    private String aqiUrl;

    /*
        I use dependency injection here with the restClient that is provided by Springboot
        to construct my HTTP restClient, so I can make API calls using it in this class.
        Builder pattern so I have optional configuration when making the call
     */
    public WeatherApiClient(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    // fetches a 5-day forecast by coordinates — returns raw JsonNode because the response is complex
    public JsonNode fetchForecast(double lat, double lon) {
        String url = String.format("%s?lat=%f&lon=%f&appid=%s&units=imperial&cnt=40", forecastUrl, lat, lon, apiKey);
        // .get() = HTTP GET, .uri() = set the URL, .retrieve() = execute, .body() = parse response as JsonNode
        return restClient.get().uri(url).retrieve().body(JsonNode.class);
    }

    // fetches the Air Quality Index by coordinates — returns raw JsonNode, parsed in WeatherQueryService
    public JsonNode fetchAqi(double lat, double lon) {
        String url = String.format("%s?lat=%f&lon=%f&appid=%s", aqiUrl, lat, lon, apiKey);
        return restClient.get().uri(url).retrieve().body(JsonNode.class);
    }

    // fetches current weather by lat/lon — used by the scheduler to ingest data for all cities every 10 mins
    public WeatherApiResponse fetchWeatherByCoords(double lat, double lon) {
        String url = String.format("%s?lat=%f&lon=%f&appid=%s&units=imperial", apiUrl, lat, lon, apiKey);
        return getWeatherApiResponse(url);
    }

    // fetches current weather by city name string — used by admin when adding a new city
    // also resolves lat/lon automatically from the response so we don't need a separate lookup
    public WeatherApiResponse fetchWeather(String city) {
        String url = String.format("%s?q=%s&appid=%s&units=imperial", apiUrl, city, apiKey);
        return getWeatherApiResponse(url);
    }

    // private shared method — fetchWeatherByCoords and fetchWeather build different URLs
    // but parse the response the same way, so parsing logic lives here to avoid duplication
    private WeatherApiResponse getWeatherApiResponse(String url) {
        // make the HTTP GET request and parse the entire response body as a raw JSON tree (JsonNode)
        JsonNode response = restClient.get()
                .uri(url)           // set URL to call
                .retrieve()         // execute the HTTP request
                .body(JsonNode.class); // parse response JSON into a navigable tree

        /*
            navigate the JSON tree using .get("key") — works like opening folders
            single .get("name")          -> top level field:   { "name": "Boston" }
            double .get("sys").get("country") -> nested object: { "sys": { "country": "US" } }
            .get(0)                      ->array index:        "weather": [ { "description": "rain" } ]
            then convert to Java type: .asString(), .asDouble(), .asInt()
            finally, instantiate the WeatherApiResponse Java record with all extracted values
         */
        return new WeatherApiResponse(
                response.get("name").asString(),                              // city name — top level
                response.get("sys").get("country").asString(),                // country — inside "sys" object
                response.get("coord").get("lat").asDouble(),                  // latitude — inside "coord" object
                response.get("coord").get("lon").asDouble(),                  // longitude — inside "coord" object
                response.get("main").get("temp").asDouble(),                  // temperature — inside "main" object
                response.get("main").get("feels_like").asDouble(),            // feels like — inside "main" object
                response.get("main").get("humidity").asInt(),                 // humidity — whole number, asInt()
                response.get("main").get("pressure").asInt(),                 // pressure — whole number, asInt()
                response.get("wind").get("speed").asDouble(),                 // wind speed — inside "wind" object
                response.get("weather").get(0).get("description").asString(), // description — "weather" is an array, get first element
                LocalDateTime.now(ZoneOffset.UTC)                             // fetch timestamp — generated here, not from the API
        );
    }


}
