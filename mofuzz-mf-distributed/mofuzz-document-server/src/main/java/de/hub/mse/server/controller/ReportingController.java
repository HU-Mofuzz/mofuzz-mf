package de.hub.mse.server.controller;

import de.hub.mse.server.exceptions.NotFoundException;
import de.hub.mse.server.exceptions.ResourceConflictException;
import de.hub.mse.server.management.ExecutionResult;
import de.hub.mse.server.service.ExecutionResultService;
import de.hub.mse.server.service.HealthService;
import de.hub.mse.server.service.WatchdogService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/report")
public class ReportingController {

    private final HealthService healthService;
    private final ExecutionResultService resultService;

    private final WatchdogService watchdogService;

    @Autowired
    public ReportingController(HealthService healthService, ExecutionResultService resultService, WatchdogService watchdogService) {
        this.healthService = healthService;
        this.resultService = resultService;
        this.watchdogService = watchdogService;
    }

    @PostMapping("/health/{id}")
    public void reportHealth(@PathVariable String id, @RequestBody HealthReport report) {
        healthService.reportSystemHealth(id, report.cpu, report.memory, report.disk);
        watchdogService.resetHealthWatchdog(id);
    }

    @PostMapping("/result")
    public void reportResult(@RequestBody ExecutionResult result) throws NotFoundException, ResourceConflictException {
        resultService.reportResult(result);
        watchdogService.resetResultWatchDog(result.getOriginClient());
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    private static class HealthReport {
        private double cpu;
        private double memory;
        private double disk;
    }
}
