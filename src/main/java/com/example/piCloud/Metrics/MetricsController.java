package com.example.piCloud.Metrics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"api/v1/metrics"})
public class MetricsController {
    @Autowired
    private MetricsService metricsService;

    @GetMapping({"/storageUsage"})
    public ResponseEntity<?> getDriveUsage(@RequestParam String drivePath) {
        try {
            return ResponseEntity.ok(Double.valueOf(this.metricsService.getDriveUsage(drivePath)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping({"/monitorStorage"})
    public ResponseEntity<?> monitorStorage(@RequestParam String drivePath) {
        try {
            return ResponseEntity.ok(this.metricsService.monitorStorage(drivePath));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}

