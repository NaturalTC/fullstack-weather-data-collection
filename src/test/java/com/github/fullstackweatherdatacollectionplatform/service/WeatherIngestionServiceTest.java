package com.github.fullstackweatherdatacollectionplatform.service;

import com.github.fullstackweatherdatacollectionplatform.client.WeatherApiClient;
import com.github.fullstackweatherdatacollectionplatform.model.WeatherData;
import com.github.fullstackweatherdatacollectionplatform.repository.WeatherDataRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // tells JUnit to use Mockito for this test class
class WeatherIngestionServiceTest {

    // TODO: Create a mock of WeatherApiClient using @Mock
    //   - A mock is a fake version of the class — it doesn't call the real API
    //   - You control what it returns using when(...).thenReturn(...)

    @Mock
    private WeatherApiClient weatherApiClient;

    // TODO: Create a mock of WeatherDataRepository using @Mock
    //   - Same idea — fake database, doesn't actually save anything

    @Mock
    private WeatherDataRepository weatherDataRepository;

    // TODO: Create the service under test using @InjectMocks
    //   - This creates a real WeatherIngestionService but injects the mocks above into it

    @InjectMocks
    private WeatherIngestionService weatherIngestionService;

    @Test
    void ingestWeatherData_savesDataForEachCity() {
        // ARRANGE
        when(weatherApiClient.fetchWeather(anyString())).thenReturn(new WeatherData());

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
                .thenReturn(new WeatherData())
                .thenThrow(new RuntimeException("API error"))
                .thenReturn(new WeatherData())
                .thenReturn(new WeatherData());

        // ACT
        weatherIngestionService.ingestWeatherData();

        // ASSERT — all 4 cities were attempted, but only 3 saved
        verify(weatherApiClient, times(4)).fetchWeather(anyString());
        verify(weatherDataRepository, times(3)).save(any(WeatherData.class));
    }
}
