package com.github.fullstackweatherdatacollectionplatform.controller;

import com.github.fullstackweatherdatacollectionplatform.dto.AdminStatsDTO;
import com.github.fullstackweatherdatacollectionplatform.model.City;
import com.github.fullstackweatherdatacollectionplatform.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats")
    public AdminStatsDTO getStats() {
        return adminService.getStats();
    }

    @PostMapping("/cities")
    public ResponseEntity<City> addCity(@RequestBody CityRequest req) {
        City saved = adminService.addCity(req.name(), req.state(), req.country());
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/cities/{name}")
    public ResponseEntity<Void> removeCity(@PathVariable String name) {
        adminService.removeCity(name);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/fetch")
    public ResponseEntity<String> triggerFetch() {
        adminService.triggerFetch();
        return ResponseEntity.ok("ok");
    }

    record CityRequest(String name, String state, String country) {}
}
