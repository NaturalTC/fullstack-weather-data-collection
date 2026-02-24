package com.github.fullstackweatherdatacollectionplatform.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record AdminStatsDTO(
        long totalRecords,
        LocalDateTime lastFetch,
        Map<String, Long> recordsPerCity
) {}
