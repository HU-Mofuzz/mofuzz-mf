package de.hub.mse.server.service;

import de.hub.mse.server.exceptions.NotFoundException;
import de.hub.mse.server.exceptions.ValidationException;
import de.hub.mse.server.management.ClientDescriptor;
import de.hub.mse.server.repository.ClientDescriptorRepository;
import de.hub.mse.server.repository.ExperimentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class ClientDescriptorService {

    private final ClientDescriptorRepository descriptorRepository;
    private final ExperimentRepository experimentRepository;

    @Autowired
    public ClientDescriptorService(ClientDescriptorRepository descriptorRepository, ExperimentRepository experimentRepository) {
        this.descriptorRepository = descriptorRepository;
        this.experimentRepository = experimentRepository;
    }

    public void createClientDescriptor(ClientDescriptor descriptor) {
        descriptor.sanitize();
        descriptor.setId(null);
        descriptorRepository.save(descriptor);
    }

    public List<ClientDescriptor> getDescriptors() {
        return descriptorRepository.findAll(Sort.by("name"));
    }

    public void changeAssignedExperiment(String id, List<String> experimentIds) throws NotFoundException {
        if(experimentIds == null || !experimentIds.stream().allMatch(experimentRepository::existsById)) {
            throw new ValidationException();
        }
        var descriptor = descriptorRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        descriptor.setAssignedExperiments(Collections.unmodifiableList(experimentIds));
        descriptorRepository.save(descriptor);
    }

    public void changeClientDescriptor(String id, ClientDescriptor descriptor) throws NotFoundException {
        descriptor.sanitize();
        descriptorRepository.findById(id).orElseThrow(NotFoundException::new);

        descriptor.setId(id);
        descriptorRepository.save(descriptor);
    }
}
