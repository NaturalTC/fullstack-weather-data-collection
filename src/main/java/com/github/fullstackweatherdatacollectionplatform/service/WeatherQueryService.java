package com.github.fullstackweatherdatacollectionplatform.service;

import com.github.fullstackweatherdatacollectionplatform.dto.CityDTO;
import com.github.fullstackweatherdatacollectionplatform.dto.WeatherDataDTO;
import com.github.fullstackweatherdatacollectionplatform.dto.WeatherSummaryDTO;
import com.github.fullstackweatherdatacollectionplatform.model.City;
import com.github.fullstackweatherdatacollectionplatform.repository.CityRepository;
import com.github.fullstackweatherdatacollectionplatform.repository.WeatherDataRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class WeatherQueryService {

    private final WeatherDataRepository weatherDataRepository;
    private final CityRepository cityRepository;

    public List<WeatherDataDTO> getAllWeather(String cityName)
    {
        if (cityName != null && !cityName.isBlank()) {
            City city = cityRepository.findByName(cityName).orElse(null);
            if (city == null) {
                return List.of();
            }
            return weatherDataRepository.findByCityOrderByFetchedAtDesc(city)
                    .stream()
                    .map(WeatherDataDTO::from)
                    .toList();
        }
        return weatherDataRepository.findAll()
                .stream()
                .map(WeatherDataDTO::from)
                .toList();
    }

    public List<WeatherDataDTO> getLatestWeather(String cityName)
    {
        if (cityName != null && !cityName.isBlank()) {
            City city = cityRepository.findByName(cityName).orElse(null);
            if (city == null) {
                return List.of();
            }
            var latest = weatherDataRepository.findTopByCityOrderByFetchedAtDesc(city);
            return latest != null ? List.of(WeatherDataDTO.from(latest)) : List.of();
        }
        return weatherDataRepository.findLatestPerCity()
                .stream()
                .map(WeatherDataDTO::from)
                .toList();
    }

    public List<CityDTO> getAllCities()
    {
        return cityRepository.findAll()
                .stream()
                .map(CityDTO::from)
                .toList();
    }

    public List<WeatherSummaryDTO> getDailySummary(String cityName)
    {
        City city = cityRepository.findByName(cityName).orElse(null);
        if (city == null) return List.of();
        return weatherDataRepository.findDailySummaryByCityId(city.getId())
                .stream()
                .map(row -> new WeatherSummaryDTO(
                        ((java.sql.Date) row[0]).toLocalDate(),
                        ((Number) row[1]).doubleValue(),
                        ((Number) row[2]).doubleValue(),
                        ((Number) row[3]).doubleValue()
                ))
                .toList();
    }
}
