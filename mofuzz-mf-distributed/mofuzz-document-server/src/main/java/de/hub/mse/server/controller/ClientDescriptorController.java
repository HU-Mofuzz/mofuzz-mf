package de.hub.mse.server.controller;

import de.hub.mse.server.exceptions.NotFoundException;
import de.hub.mse.server.management.ClientDescriptor;
import de.hub.mse.server.management.Experiment;
import de.hub.mse.server.service.ClientDescriptorService;
import de.hub.mse.server.service.ExecutionResultService;
import de.hub.mse.server.service.ExperimentService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clients")
public class ClientDescriptorController {

    private final ClientDescriptorService descriptorService;
    private final ExperimentService experimentService;
    private final ExecutionResultService resultService;

    @Autowired
    public ClientDescriptorController(ClientDescriptorService descriptorService, ExperimentService experimentService,
                                      ExecutionResultService resultService) {
        this.descriptorService = descriptorService;
        this.experimentService = experimentService;
        this.resultService = resultService;
    }

    @PostMapping
    public void createClientDescriptor(@RequestBody ClientDescriptor descriptor) {
        descriptorService.createClientDescriptor(descriptor);
    }

    @GetMapping
    public List<ClientDescriptor> getClientDescriptors() {
        return descriptorService.getDescriptors();
    }

    @PostMapping("/{id}")
    public void changeClientDescriptor(@PathVariable String id,
                                         @RequestBody ClientDescriptor descriptor) throws NotFoundException {
        descriptorService.changeClientDescriptor(id, descriptor);
    }

    @GetMapping("/progress/{id}")
    public ExperimentProgress getProgress(@PathVariable String id, @RequestParam String experiment) {
        return new ExperimentProgress(resultService.getResultCountForExperiment(experiment, id),
                experimentService.getGeneratedDocumentCount(experiment),
                experimentService.getExperiment(experiment).map(Experiment::getDocumentCount).orElse(0));
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    private static class ExperimentProgress {
        private int existingResults;
        private int generatedDocuments;
        private int totalDocumentCount;
    }
}
