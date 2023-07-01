package de.hub.mse.server.controller;

import de.hub.mse.server.exceptions.NotFoundException;
import de.hub.mse.server.management.Experiment;
import de.hub.mse.server.management.FileDescriptor;
import de.hub.mse.server.service.ExecutionService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/v1/execution")
public class ExecutionController {

    private static final int DEFAULT_TIMEOUT = 30000;

    private final ExecutionService executionService;

    @Autowired
    public ExecutionController(ExecutionService executionService) {
        this.executionService = executionService;
    }
    @GetMapping("/{id}")
    public ResponseEntity<DescriptorResponse> getNextFileDescriptor(@PathVariable String id) throws NotFoundException {
        var start = System.currentTimeMillis();
        var experiment = executionService.getCurrentExperimentForClient(id);
        var result =  executionService.getNextFileDescriptorForClient(id)
                .map(descriptor -> ResponseEntity.ok(
                        new DescriptorResponse(descriptor,
                                executionService.getRecursiveFileIdsOfDescriptor(descriptor),
                                experiment.map(Experiment::getTimeout).orElse(DEFAULT_TIMEOUT),
                                experiment.map(Experiment::getDocumentHeight).orElse(0),
                                experiment.map(Experiment::getDocumentWidth).orElse(0))
                ))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NO_CONTENT));
        log.info("FileDescriptor selection took {}ms", (System.currentTimeMillis() - start));
        return result;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DescriptorResponse {
        private FileDescriptor descriptor;
        private Set<String> fileSet;

        private int timeout;
        private int height;
        private int width;
    }
}
