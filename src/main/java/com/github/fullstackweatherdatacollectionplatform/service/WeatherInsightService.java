package com.github.fullstackweatherdatacollectionplatform.service;

import com.github.fullstackweatherdatacollectionplatform.dto.AqiDTO;
import com.github.fullstackweatherdatacollectionplatform.dto.WeatherDataDTO;
import com.github.fullstackweatherdatacollectionplatform.dto.WeatherInsightDTO;
import com.github.fullstackweatherdatacollectionplatform.dto.WeatherSummaryDTO;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Service
public class WeatherInsightService {

    private final WeatherQueryService weatherQueryService;
    private final ObjectMapper objectMapper;

    @Value("${openai.api.key:}")
    private String openAiApiKey;

    private RestClient openAiClient;

    public WeatherInsightService(WeatherQueryService weatherQueryService,
                                 ObjectMapper objectMapper) {
        this.weatherQueryService = weatherQueryService;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void init() {
        openAiClient = RestClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader("Authorization", "Bearer " + openAiApiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Cacheable(value = "insights", key = "#city")
    public WeatherInsightDTO getInsight(String city) {
        // ── 1. Fetch data ──────────────────────────────────────────────
        List<WeatherDataDTO> latestList = weatherQueryService.getLatestWeather(city);
        if (latestList.isEmpty()) {
            WeatherInsightDTO empty = new WeatherInsightDTO();
            empty.setCity(city);
            empty.setSummary("No data available for " + city);
            return empty;
        }
        WeatherDataDTO current = latestList.get(0);

        List<WeatherSummaryDTO> summary = weatherQueryService.getDailySummary(city);

        AqiDTO aqi = null;
        try { aqi = weatherQueryService.getAqi(city); } catch (Exception ignored) {}

        // ── 2. Compute severity score ──────────────────────────────────
        int severity = computeSeverity(current, aqi);

        // ── 3. Compute anomaly via z-score against 7-day history ───────
        double weeklyAvg   = summary.stream().mapToDouble(WeatherSummaryDTO::avgTemperature).average().orElse(current.temperature());
        double variance    = summary.stream().mapToDouble(s -> Math.pow(s.avgTemperature() - weeklyAvg, 2)).average().orElse(0);
        double stdDev      = Math.sqrt(variance);
        double zScore      = stdDev > 0.5 ? (current.temperature() - weeklyAvg) / stdDev : 0;
        boolean anomaly    = Math.abs(zScore) > 1.5;
        double deviation   = current.temperature() - weeklyAvg;

        // ── 4. Build prompt context ────────────────────────────────────
        StringBuilder context = new StringBuilder();
        context.append("City: ").append(city).append("\n");
        context.append("Current Conditions:\n");
        context.append("  Temperature: ").append(String.format("%.1f", current.temperature())).append("°F\n");
        context.append("  Feels Like: ").append(String.format("%.1f", current.feelsLike())).append("°F\n");
        context.append("  Humidity: ").append(current.humidity()).append("%\n");
        context.append("  Wind Speed: ").append(String.format("%.1f", current.windSpeed())).append(" mph\n");
        context.append("  Description: ").append(current.description()).append("\n");
        if (aqi != null) context.append("  AQI: ").append(aqi.label()).append(" (").append(aqi.index()).append(")\n");

        if (!summary.isEmpty()) {
            context.append("\n7-Day Temperature Summary:\n");
            for (WeatherSummaryDTO day : summary) {
                context.append("  ").append(day.date())
                        .append(": avg=").append(String.format("%.1f", day.avgTemperature()))
                        .append("°F, min=").append(String.format("%.1f", day.minTemperature()))
                        .append("°F, max=").append(String.format("%.1f", day.maxTemperature())).append("°F\n");
            }
            context.append("Weekly Avg: ").append(String.format("%.1f", weeklyAvg)).append("°F\n");
            context.append("Current Deviation: ").append(deviation >= 0 ? "+" : "").append(String.format("%.1f", deviation)).append("°F vs weekly avg\n");
        }
        context.append("Computed Severity Score: ").append(severity).append("/100\n");
        context.append("Statistical Anomaly: ").append(anomaly ? "Yes (z-score: " + String.format("%.2f", zScore) + ")" : "No");

        // ── 5. Call OpenAI ─────────────────────────────────────────────
        String aiSummary = "Weather analysis unavailable.";
        String trend = detectTrend(summary, current.temperature());
        String anomalyDesc = null;

        if (!openAiApiKey.isBlank()) {
            try {
                String systemPrompt = """
                    You are a weather analytics API that provides insights for developers.
                    Analyze the weather data and return a JSON object with EXACTLY these fields:
                    - summary: 2-3 sentences covering current conditions, recent trend, and any notable patterns
                    - trend: exactly one of "warming", "cooling", or "stable"
                    - anomaly_description: a brief description if conditions are unusual, or null if typical
                    Return ONLY valid JSON, no markdown, no extra text.
                    """;

                Map<String, Object> requestBody = Map.of(
                        "model", "gpt-4o-mini",
                        "response_format", Map.of("type", "json_object"),
                        "max_tokens", 300,
                        "messages", List.of(
                                Map.of("role", "system", "content", systemPrompt),
                                Map.of("role", "user", "content", context.toString())
                        )
                );

                String responseJson = openAiClient.post()
                        .uri("/chat/completions")
                        .body(requestBody)
                        .retrieve()
                        .body(String.class);

                JsonNode root    = objectMapper.readTree(responseJson);
                String content   = root.get("choices").get(0).get("message").get("content").asString();
                JsonNode parsed  = objectMapper.readTree(content);

                if (parsed.has("summary"))             aiSummary  = parsed.get("summary").asString();
                if (parsed.has("trend"))               trend      = parsed.get("trend").asString();
                if (parsed.has("anomaly_description") && !parsed.get("anomaly_description").isNull())
                    anomalyDesc = parsed.get("anomaly_description").asString();

            } catch (Exception e) {
                aiSummary = "AI insight temporarily unavailable. " +
                        (anomaly ? "Note: current temperature deviates significantly from the weekly average." : "");
            }
        }

        return new WeatherInsightDTO(
                city, aiSummary, trend, anomalyDesc,
                severity, anomaly, current.temperature(), weeklyAvg, deviation
        );
    }

    // ── Severity score: 0 = perfect, 100 = extreme ──────────────────────
    private int computeSeverity(WeatherDataDTO w, AqiDTO aqi) {
        double score = 0;
        double temp  = w.temperature();
        double wind  = w.windSpeed();
        int    hum   = w.humidity();

        // Temperature extremes
        if      (temp >= 100) score += 35;
        else if (temp >=  95) score += 25;
        else if (temp >=  90) score += 15;
        else if (temp >=  85) score +=  5;
        else if (temp <=  10) score += 35;
        else if (temp <=  20) score += 25;
        else if (temp <=  32) score += 15;
        else if (temp <=  40) score +=  5;

        // Wind
        if      (wind >= 50) score += 30;
        else if (wind >= 35) score += 20;
        else if (wind >= 25) score += 12;
        else if (wind >= 15) score +=  4;

        // Humidity extremes
        if      (hum >= 95) score += 15;
        else if (hum >= 85) score +=  8;
        else if (hum <= 15) score +=  8;

        // AQI
        if (aqi != null) {
            if      (aqi.index() == 5) score += 20;
            else if (aqi.index() == 4) score += 12;
            else if (aqi.index() == 3) score +=  5;
        }

        return (int) Math.min(100, Math.max(0, score));
    }

    // Fallback trend detection when OpenAI is unavailable
    private String detectTrend(List<WeatherSummaryDTO> summary, double currentTemp) {
        if (summary.size() < 3) return "stable";
        double oldAvg = summary.subList(0, summary.size() / 2)
                .stream().mapToDouble(WeatherSummaryDTO::avgTemperature).average().orElse(currentTemp);
        double diff = currentTemp - oldAvg;
        if      (diff >  2) return "warming";
        else if (diff < -2) return "cooling";
        else                return "stable";
    }
}
