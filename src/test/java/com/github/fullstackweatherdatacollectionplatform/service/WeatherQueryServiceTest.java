package com.github.fullstackweatherdatacollectionplatform.service;

import com.github.fullstackweatherdatacollectionplatform.dto.CityDTO;
import com.github.fullstackweatherdatacollectionplatform.dto.WeatherDataDTO;
import com.github.fullstackweatherdatacollectionplatform.model.City;
import com.github.fullstackweatherdatacollectionplatform.model.WeatherCondition;
import com.github.fullstackweatherdatacollectionplatform.model.WeatherData;
import com.github.fullstackweatherdatacollectionplatform.repository.CityRepository;
import com.github.fullstackweatherdatacollectionplatform.repository.WeatherDataRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherQueryServiceTest {

    @Mock
    private WeatherDataRepository weatherDataRepository;

    @Mock
    private CityRepository cityRepository;

    @InjectMocks
    private WeatherQueryService weatherQueryService;

    private City boston() {
        City city = new City("Boston", "MA", "US", 42.36, -71.06);
        city.setId(1L);
        return city;
    }

    private WeatherData weatherFor(City city) {
        WeatherData wd = new WeatherData();
        wd.setCity(city);
        wd.setTemperature(72.0);
        wd.setFeelsLike(70.0);
        wd.setHumidity(55);
        wd.setPressure(1013);
        wd.setWindSpeed(5.0);
        wd.setCondition(new WeatherCondition("clear sky"));
        wd.setTimestamp(LocalDateTime.of(2026, 2, 17, 12, 0));
        wd.setFetchedAt(LocalDateTime.of(2026, 2, 17, 12, 1));
        return wd;
    }

    // --- getAllWeather ---

    @Test
    void getAllWeather_noFilter_returnsAll() {
        City city = boston();
        WeatherData wd = weatherFor(city);
        when(weatherDataRepository.findAll()).thenReturn(List.of(wd));

        List<WeatherDataDTO> result = weatherQueryService.getAllWeather(null);

        assertEquals(1, result.size());
        assertEquals("Boston", result.get(0).cityName());
        verify(weatherDataRepository).findAll();
        verify(cityRepository, never()).findByName(anyString());
    }

    @Test
    void getAllWeather_withCity_filtersbyCity() {
        City city = boston();
        WeatherData wd = weatherFor(city);
        when(cityRepository.findByName("Boston")).thenReturn(Optional.of(city));
        when(weatherDataRepository.findByCityOrderByTimestampDesc(city)).thenReturn(List.of(wd));

        List<WeatherDataDTO> result = weatherQueryService.getAllWeather("Boston");

        assertEquals(1, result.size());
        assertEquals("Boston", result.get(0).cityName());
        assertEquals(72.0, result.get(0).temperature());
    }

    @Test
    void getAllWeather_unknownCity_returnsEmpty() {
        when(cityRepository.findByName("Nowhere")).thenReturn(Optional.empty());

        List<WeatherDataDTO> result = weatherQueryService.getAllWeather("Nowhere");

        assertTrue(result.isEmpty());
        verify(weatherDataRepository, never()).findByCityOrderByTimestampDesc(any());
    }

    // --- getLatestWeather ---

    @Test
    void getLatestWeather_noFilter_returnsLatestPerCity() {
        City city = boston();
        WeatherData wd = weatherFor(city);
        when(weatherDataRepository.findLatestPerCity()).thenReturn(List.of(wd));

        List<WeatherDataDTO> result = weatherQueryService.getLatestWeather(null);

        assertEquals(1, result.size());
        assertEquals("Boston", result.get(0).cityName());
    }

    @Test
    void getLatestWeather_withCity_returnsSingleLatest() {
        City city = boston();
        WeatherData wd = weatherFor(city);
        when(cityRepository.findByName("Boston")).thenReturn(Optional.of(city));
        when(weatherDataRepository.findTopByCityOrderByTimestampDesc(city)).thenReturn(wd);

        List<WeatherDataDTO> result = weatherQueryService.getLatestWeather("Boston");

        assertEquals(1, result.size());
        assertEquals("clear sky", result.get(0).description());
    }

    @Test
    void getLatestWeather_unknownCity_returnsEmpty() {
        when(cityRepository.findByName("Nowhere")).thenReturn(Optional.empty());

        List<WeatherDataDTO> result = weatherQueryService.getLatestWeather("Nowhere");

        assertTrue(result.isEmpty());
    }

    @Test
    void getLatestWeather_cityExistsButNoData_returnsEmpty() {
        City city = boston();
        when(cityRepository.findByName("Boston")).thenReturn(Optional.of(city));
        when(weatherDataRepository.findTopByCityOrderByTimestampDesc(city)).thenReturn(null);

        List<WeatherDataDTO> result = weatherQueryService.getLatestWeather("Boston");

        assertTrue(result.isEmpty());
    }

    // --- getAllCities ---

    @Test
    void getAllCities_returnsMappedDTOs() {
        City city = boston();
        when(cityRepository.findAll()).thenReturn(List.of(city));

        List<CityDTO> result = weatherQueryService.getAllCities();

        assertEquals(1, result.size());
        assertEquals("Boston", result.get(0).name());
        assertEquals("US", result.get(0).country());
        assertEquals(42.36, result.get(0).latitude());
    }

    @Test
    void getAllCities_noCities_returnsEmpty() {
        when(cityRepository.findAll()).thenReturn(List.of());

        List<CityDTO> result = weatherQueryService.getAllCities();

        assertTrue(result.isEmpty());
    }
}
