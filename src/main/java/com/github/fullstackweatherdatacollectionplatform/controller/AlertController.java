package com.github.fullstackweatherdatacollectionplatform.controller;

import com.github.fullstackweatherdatacollectionplatform.dto.WeatherAlertDTO;
import com.github.fullstackweatherdatacollectionplatform.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@Tag(name = "Alerts", description = "Manage weather threshold alert rules")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    // GET /api/alerts — returns all alert rules with their current triggered status
    @GetMapping
    @Operation(summary = "List all alert rules")
    public List<WeatherAlertDTO> getAll() {
        return alertService.getAll();
    }

    // POST /api/alerts — creates a new alert rule from the request body
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new alert rule")
    public WeatherAlertDTO create(@RequestBody WeatherAlertDTO dto) {
        return alertService.create(dto);
    }

    // DELETE /api/alerts/{id} — removes an alert rule permanently
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete an alert rule")
    public void delete(@PathVariable Long id) {
        alertService.delete(id);
    }

    // POST /api/alerts/{id}/dismiss — resets triggered=false so the rule watches for the condition again
    @PostMapping("/{id}/dismiss")
    @Operation(summary = "Dismiss a triggered alert (resets to watching)")
    public WeatherAlertDTO dismiss(@PathVariable Long id) {
        alertService.dismiss(id);
        return alertService.getAll().stream()
                .filter(a -> a.id().equals(id))
                .findFirst()
                .orElseThrow();
    }
}
