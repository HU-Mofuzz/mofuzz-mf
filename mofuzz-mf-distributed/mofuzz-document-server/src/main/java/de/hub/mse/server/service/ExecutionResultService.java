package de.hub.mse.server.service;

import de.hub.mse.server.exceptions.NotFoundException;
import de.hub.mse.server.management.ExecutionResult;
import de.hub.mse.server.repository.ClientDescriptorRepository;
import de.hub.mse.server.repository.ExecutionResultRepository;
import de.hub.mse.server.repository.ExperimentRepository;
import de.hub.mse.server.repository.FileDescriptorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExecutionResultService {

    private final ExecutionResultRepository resultRepository;
    private final ExperimentRepository experimentRepository;
    private final ClientDescriptorRepository clientRepository;
    private final FileDescriptorRepository fileRepository;

    @Autowired
    public ExecutionResultService(ExecutionResultRepository resultRepository,
                                  ExperimentRepository experimentRepository,
                                  ClientDescriptorRepository clientRepository,
                                  FileDescriptorRepository fileRepository) {
        this.resultRepository = resultRepository;
        this.experimentRepository = experimentRepository;
        this.clientRepository = clientRepository;
        this.fileRepository = fileRepository;
    }

    public void reportResult(ExecutionResult result) throws NotFoundException {
        result.sanitize();

        if(!experimentRepository.existsById(result.getExperiment())
        || !clientRepository.existsById(result.getOriginClient())
        || !fileRepository.existsById(result.getFileDescriptor())) {
            throw new NotFoundException();
        }

        result.setId(null);
        resultRepository.save(result);
    }
}
