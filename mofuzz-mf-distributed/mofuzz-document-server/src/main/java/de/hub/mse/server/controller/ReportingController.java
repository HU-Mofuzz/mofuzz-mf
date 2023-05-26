package de.hub.mse.server.controller;

import de.hub.mse.server.exceptions.NotFoundException;
import de.hub.mse.server.exceptions.ResourceConflictException;
import de.hub.mse.server.management.ExecutionResult;
import de.hub.mse.server.service.ExecutionResultService;
import de.hub.mse.server.service.HealthService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/report")
public class ReportingController {

    private final HealthService healthService;
    private final ExecutionResultService resultService;

    public ReportingController(HealthService healthService, ExecutionResultService resultService) {
        this.healthService = healthService;
        this.resultService = resultService;
    }

    @PostMapping("/health/{id}")
    public void reportHealth(@PathVariable String id, @RequestBody HealthReport report) {
        healthService.reportSystemHealth(id, report.cpu, report.memory, report.disk);
    }

    @PostMapping("/result")
    public void reportResult(@RequestBody ExecutionResult result) throws NotFoundException, ResourceConflictException {
        resultService.reportResult(result);
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
