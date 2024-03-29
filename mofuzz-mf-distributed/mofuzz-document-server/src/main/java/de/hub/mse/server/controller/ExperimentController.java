package de.hub.mse.server.controller;

import de.hub.mse.server.exceptions.NotFoundException;
import de.hub.mse.server.management.Experiment;
import de.hub.mse.server.service.ExperimentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/v1/experiment")
public class ExperimentController {

    private final ExperimentService experimentService;

    @Autowired
    public ExperimentController(ExperimentService experimentService) {
        this.experimentService = experimentService;
    }

    @PostMapping
    public void createExperiment(@RequestBody Experiment experiment) {
        experimentService.createExperiment(experiment);
    }

    @GetMapping
    public List<Experiment> getExperiments() {
        return experimentService.getExperiments().stream()
                .peek(experiment -> experiment.setSerializedLinks(Collections.emptyList()))
                .toList();
    }

    @PostMapping("/reset/{id}")
    public void resetExperiment(@PathVariable String id) throws NotFoundException {
        experimentService.resetExperiment(id);
    }

}
