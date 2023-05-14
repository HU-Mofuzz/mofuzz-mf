package de.hub.mse.server.service;

import de.hub.mse.server.exceptions.NotFoundException;
import de.hub.mse.server.management.ExecutionResult;
import de.hub.mse.server.repository.ClientDescriptorRepository;
import de.hub.mse.server.repository.ExecutionResultRepository;
import de.hub.mse.server.repository.ExperimentRepository;
import de.hub.mse.server.repository.FileDescriptorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
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

        var experiment = experimentRepository.findById(result.getExperiment()).orElseThrow(NotFoundException::new);
        var client = clientRepository.findById(result.getOriginClient()).orElseThrow(NotFoundException::new);

        if(!fileRepository.existsById(result.getFileDescriptor())) {
            throw new NotFoundException();
        }

        result.setId(null);
        resultRepository.save(result);
        log.info("Client [{}] reported result for file [{}] in experiment [{}]", client.getName(),
                result.getFileDescriptor(), experiment.getDescription());
    }
}
