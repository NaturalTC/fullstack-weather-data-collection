package com.github.fullstackweatherdatacollectionplatform.controller;

import com.github.fullstackweatherdatacollectionplatform.dto.AdminStatsDTO;
import com.github.fullstackweatherdatacollectionplatform.model.City;
import com.github.fullstackweatherdatacollectionplatform.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Protected admin endpoints — HTTP Basic Auth required")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats")
    @Operation(summary = "System stats", description = "Returns total record count, last fetch timestamp, and record count per city")
    public AdminStatsDTO getStats() {
        return adminService.getStats();
    }

    @PostMapping("/cities")
    @Operation(summary = "Add a city", description = "Resolves coordinates via OpenWeatherMap and adds the city to the ingestion schedule")
    public ResponseEntity<City> addCity(@RequestBody CityRequest req) {
        City saved = adminService.addCity(req.name(), req.state(), req.country());
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/cities/{name}")
    @Operation(summary = "Remove a city", description = "Deletes the city and all associated weather records")
    public ResponseEntity<Void> removeCity(@PathVariable String name) {
        adminService.removeCity(name);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/fetch")
    @Operation(summary = "Trigger manual fetch", description = "Runs the ingestion job immediately for all monitored cities")
    public ResponseEntity<String> triggerFetch() {
        adminService.triggerFetch();
        return ResponseEntity.ok("ok");
    }

    record CityRequest(String name, String state, String country) {}
}
