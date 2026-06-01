package com.github.fullstackweatherdatacollectionplatform.service;

import com.github.fullstackweatherdatacollectionplatform.model.ApiKey;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class RateLimitService {

    private static final Map<String, Long> PLAN_LIMITS = Map.of(
        "FREE",  1_000L,
        "PRO",   50_000L,
        "SCALE", Long.MAX_VALUE
    );

    // Key: "keyId:YYYY-MM-DD" → request count today
    private final ConcurrentHashMap<String, AtomicLong> counters = new ConcurrentHashMap<>();

    public boolean isAllowed(ApiKey key) {
        String plan = key.getUser().getPlan();
        long limit  = PLAN_LIMITS.getOrDefault(plan, 1_000L);
        if (limit == Long.MAX_VALUE) return true;

        String bucket = key.getId() + ":" + LocalDate.now();
        long count = counters.computeIfAbsent(bucket, k -> new AtomicLong(0)).incrementAndGet();
        return count <= limit;
    }

    public long getUsageToday(ApiKey key) {
        String bucket = key.getId() + ":" + LocalDate.now();
        AtomicLong counter = counters.get(bucket);
        return counter == null ? 0L : counter.get();
    }

    public long getLimitForPlan(String plan) {
        return PLAN_LIMITS.getOrDefault(plan, 1_000L);
    }
}
