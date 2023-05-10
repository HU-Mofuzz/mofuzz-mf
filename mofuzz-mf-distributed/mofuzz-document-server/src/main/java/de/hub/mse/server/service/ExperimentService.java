package de.hub.mse.server.service;

import de.hub.mse.server.management.Experiment;
import de.hub.mse.server.repository.ExperimentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExperimentService {

    private final ExperimentRepository experimentRepository;

    @Autowired
    public ExperimentService(ExperimentRepository experimentRepository) {
        this.experimentRepository = experimentRepository;
    }

    public void createExperiment(Experiment experiment) {
        experiment.sanitize();
        experiment.setId(null);
        experimentRepository.save(experiment);
    }

    public List<Experiment> getExperiments() {
        return experimentRepository.findAll(Sort.by("description"));
    }
}
