package de.hub.mse.server.controller;

import de.hub.mse.server.exceptions.NotFoundException;
import de.hub.mse.server.management.FileDescriptor;
import de.hub.mse.server.service.ExecutionService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/v1/execution")
public class ExecutionController {

    private final ExecutionService executionService;

    @Autowired
    public ExecutionController(ExecutionService executionService) {
        this.executionService = executionService;
    }
    @GetMapping("/{id}")
    public ResponseEntity<DescriptorResponse> getNextFileDescriptor(@PathVariable String id) throws NotFoundException {
        var start = System.currentTimeMillis();
        var result =  executionService.getNextFileDescriptorForClient(id)
                .map(descriptor -> ResponseEntity.ok(
                        new DescriptorResponse(descriptor, executionService.getRecursiveFileIdsOfDescriptor(descriptor))
                ))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NO_CONTENT));
        log.info("FileDescriptor selection took {}ms", (System.currentTimeMillis() - start));
        return result;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DescriptorResponse {
        FileDescriptor descriptor;
        Set<String> fileSet;
    }
}
