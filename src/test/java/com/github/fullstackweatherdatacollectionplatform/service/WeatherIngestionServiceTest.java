package com.github.fullstackweatherdatacollectionplatform.service;

import com.github.fullstackweatherdatacollectionplatform.client.WeatherApiClient;
import com.github.fullstackweatherdatacollectionplatform.client.WeatherApiResponse;
import com.github.fullstackweatherdatacollectionplatform.model.City;
import com.github.fullstackweatherdatacollectionplatform.model.WeatherData;
import com.github.fullstackweatherdatacollectionplatform.repository.CityRepository;
import com.github.fullstackweatherdatacollectionplatform.repository.WeatherDataRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherIngestionServiceTest {

    @Mock
    private WeatherApiClient weatherApiClient;

    @Mock
    private WeatherDataRepository weatherDataRepository;

    @Mock
    private CityRepository cityRepository;

    @InjectMocks
    private WeatherIngestionService weatherIngestionService;

    private WeatherApiResponse dummyResponse() {
        return new WeatherApiResponse(
                "TestCity", "US", 42.0, -71.0,
                72.0, 70.0, 55, 1013, 5.0,
                "clear sky",
                LocalDateTime.now(), LocalDateTime.now()
        );
    }

    @Test
    void ingestWeatherData_savesDataForEachCity() {
        // ARRANGE
        when(weatherApiClient.fetchWeather(anyString())).thenReturn(dummyResponse());
        when(cityRepository.findByName(anyString())).thenReturn(Optional.of(new City()));

        // ACT
        weatherIngestionService.ingestWeatherData();

        // ASSERT
        verify(weatherApiClient, times(4)).fetchWeather(anyString());
        verify(weatherDataRepository, times(4)).save(any(WeatherData.class));
    }

    @Test
    void ingestWeatherData_continuesWhenOneCityFails() {
        // ARRANGE — second city throws an exception
        when(weatherApiClient.fetchWeather(anyString()))
                .thenReturn(dummyResponse())
                .thenThrow(new RuntimeException("API error"))
                .thenReturn(dummyResponse())
                .thenReturn(dummyResponse());
        when(cityRepository.findByName(anyString())).thenReturn(Optional.of(new City()));

        // ACT
        weatherIngestionService.ingestWeatherData();

        // ASSERT — all 4 cities were attempted, but only 3 saved
        verify(weatherApiClient, times(4)).fetchWeather(anyString());
        verify(weatherDataRepository, times(3)).save(any(WeatherData.class));
    }
}
