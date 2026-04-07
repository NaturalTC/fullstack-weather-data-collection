package com.github.fullstackweatherdatacollectionplatform.controller;

import com.github.fullstackweatherdatacollectionplatform.dto.AdminStatsDTO;
import com.github.fullstackweatherdatacollectionplatform.model.City;
import com.github.fullstackweatherdatacollectionplatform.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController          // marks this as a REST controller — methods return JSON directly
@RequestMapping("/admin") // all endpoints prefixed with /admin — protected by Spring Security
@RequiredArgsConstructor  // Lombok — generates constructor for all final fields
@Tag(name = "Admin", description = "Protected admin endpoints — HTTP Basic Auth required")  // Swagger UI grouping
public class AdminController {

    private final AdminService adminService;  // injected by Spring via constructor

    // GET /admin/stats — returns total records, last fetch time, and per-city counts
    @GetMapping("/stats")
    @Operation(summary = "System stats", description = "Returns total record count, last fetch timestamp, and record count per city")
    public AdminStatsDTO getStats() {
        return adminService.getStats();
    }

    // POST /admin/cities — adds a new city to the ingestion schedule
    // @RequestBody — Spring parses the JSON request body into a CityRequest record automatically
    // ResponseEntity<City> — wraps the response so we can control the HTTP status code
    @PostMapping("/cities")
    @Operation(summary = "Add a city", description = "Resolves coordinates via OpenWeatherMap and adds the city to the ingestion schedule")
    public ResponseEntity<City> addCity(@RequestBody CityRequest req) {
        City saved = adminService.addCity(req.name(), req.state(), req.country());
        return ResponseEntity.ok(saved);  // returns 200 OK with the saved city as the response body
    }

    // DELETE /admin/cities/{name} — removes a city and all its weather records
    // @PathVariable — binds the {name} segment of the URL to the method parameter
    // ResponseEntity<Void> — no response body needed, just an HTTP status
    @DeleteMapping("/cities/{name}")
    @Operation(summary = "Remove a city", description = "Deletes the city and all associated weather records")
    public ResponseEntity<Void> removeCity(@PathVariable String name) {
        adminService.removeCity(name);
        return ResponseEntity.noContent().build();  // returns 204 No Content — standard for successful DELETE
    }

    // POST /admin/fetch — manually triggers the weather ingestion job immediately
    // normally runs automatically every 10 minutes — this lets the admin force it
    @PostMapping("/fetch")
    @Operation(summary = "Trigger manual fetch", description = "Runs the ingestion job immediately for all monitored cities")
    public ResponseEntity<String> triggerFetch() {
        adminService.triggerFetch();
        return ResponseEntity.ok("ok");
    }

    // record used as the request body shape for POST /admin/cities
    // Spring automatically deserializes the incoming JSON { "name": "...", "state": "...", "country": "..." } into this
    record CityRequest(String name, String state, String country) {}
}
