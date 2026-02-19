package com.github.fullstackweatherdatacollectionplatform.controller;

import com.github.fullstackweatherdatacollectionplatform.dto.CityDTO;
import com.github.fullstackweatherdatacollectionplatform.dto.WeatherDataDTO;
import com.github.fullstackweatherdatacollectionplatform.service.WeatherQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WeatherController.class)
class WeatherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WeatherQueryService weatherQueryService;

    private WeatherDataDTO sampleWeather() {
        return new WeatherDataDTO(
                "Boston", "US", 72.0, 70.0, 55, 1013, 5.0,
                "clear sky",
                LocalDateTime.of(2026, 2, 17, 12, 0),
                LocalDateTime.of(2026, 2, 17, 12, 1)
        );
    }

    // --- GET /api/weather ---

    @Test
    void getWeather_returnsAllRecords() throws Exception {
        when(weatherQueryService.getAllWeather(null)).thenReturn(List.of(sampleWeather()));

        mockMvc.perform(get("/api/weather"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cityName").value("Boston"))
                .andExpect(jsonPath("$[0].temperature").value(72.0))
                .andExpect(jsonPath("$[0].description").value("clear sky"));
    }

    @Test
    void getWeather_withCityParam_passesToService() throws Exception {
        when(weatherQueryService.getAllWeather("Boston")).thenReturn(List.of(sampleWeather()));

        mockMvc.perform(get("/api/weather").param("city", "Boston"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cityName").value("Boston"));

        verify(weatherQueryService).getAllWeather("Boston");
    }

    @Test
    void getWeather_noData_returnsEmptyArray() throws Exception {
        when(weatherQueryService.getAllWeather(null)).thenReturn(List.of());

        mockMvc.perform(get("/api/weather"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // --- GET /api/weather/latest ---

    @Test
    void getLatestWeather_returnsLatest() throws Exception {
        when(weatherQueryService.getLatestWeather(null)).thenReturn(List.of(sampleWeather()));

        mockMvc.perform(get("/api/weather/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cityName").value("Boston"))
                .andExpect(jsonPath("$[0].humidity").value(55));
    }

    @Test
    void getLatestWeather_withCityParam_passesToService() throws Exception {
        when(weatherQueryService.getLatestWeather("Boston")).thenReturn(List.of(sampleWeather()));

        mockMvc.perform(get("/api/weather/latest").param("city", "Boston"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cityName").value("Boston"));

        verify(weatherQueryService).getLatestWeather("Boston");
    }

    // --- GET /api/cities ---

    @Test
    void getCities_returnsCityList() throws Exception {
        CityDTO city = new CityDTO("Boston", "MA", "US", 42.36, -71.06);
        when(weatherQueryService.getAllCities()).thenReturn(List.of(city));

        mockMvc.perform(get("/api/cities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Boston"))
                .andExpect(jsonPath("$[0].country").value("US"))
                .andExpect(jsonPath("$[0].latitude").value(42.36));
    }

    @Test
    void getCities_noCities_returnsEmptyArray() throws Exception {
        when(weatherQueryService.getAllCities()).thenReturn(List.of());

        mockMvc.perform(get("/api/cities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}
