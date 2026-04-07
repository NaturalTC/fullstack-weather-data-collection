package com.github.fullstackweatherdatacollectionplatform.repository;

import com.github.fullstackweatherdatacollectionplatform.model.WeatherAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WeatherAlertRepository extends JpaRepository<WeatherAlert, Long> {

    // returns all alert rules watching a specific city — used during ingestion to check conditions
    List<WeatherAlert> findByCityName(String cityName);
}
