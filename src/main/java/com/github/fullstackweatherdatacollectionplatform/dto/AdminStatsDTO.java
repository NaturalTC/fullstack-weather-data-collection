package com.github.fullstackweatherdatacollectionplatform.dto;

import java.time.LocalDateTime;
import java.util.Map;

// DTO for the admin stats panel — returned by GET /admin/stats
// record = immutable, auto-generates constructor/getters
public record AdminStatsDTO(
        long totalRecords,               // total row count in the weather_data table
        LocalDateTime lastFetch,         // timestamp of the most recently fetched record
        Map<String, Long> recordsPerCity // map of city name → record count e.g. { "Boston": 1440, "Worcester": 1438 }
) {}
