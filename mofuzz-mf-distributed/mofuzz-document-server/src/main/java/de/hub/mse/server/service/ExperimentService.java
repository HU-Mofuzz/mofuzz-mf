package de.hub.mse.server.service;

import de.hub.mse.server.management.Experiment;
import de.hub.mse.server.repository.ExperimentRepository;
import de.hub.mse.server.repository.FileDescriptorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ExperimentService {

    private final ExperimentRepository experimentRepository;
    private final FileDescriptorRepository fileRepository;

    @Autowired
    public ExperimentService(ExperimentRepository experimentRepository, FileDescriptorRepository fileRepository) {
        this.experimentRepository = experimentRepository;
        this.fileRepository = fileRepository;
    }

    public void createExperiment(Experiment experiment) {
        experiment.sanitize();
        experiment.setId(null);
        experiment.setPrepared(Experiment.PreparationState.UNPREPARED);
        experimentRepository.save(experiment);
    }

    public List<Experiment> getExperiments() {
        return experimentRepository.findAll(Sort.by("description"));
    }

    public Optional<Experiment> getExperiment(String id) {
        return experimentRepository.findById(id);
    }

    public int getGeneratedDocumentCount(String experimentId) {
        var experiment = experimentRepository.findById(experimentId);
        return experiment.map(
                value -> fileRepository.countByExperimentIsAndDepth(experimentId, value.getTreeDepth())).
                orElse(0);
    }
}
