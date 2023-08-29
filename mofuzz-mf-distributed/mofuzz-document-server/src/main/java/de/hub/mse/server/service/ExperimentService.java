package de.hub.mse.server.service;

import de.hub.mse.server.exceptions.NotFoundException;
import de.hub.mse.server.management.Experiment;
import de.hub.mse.server.repository.ExecutionResultRepository;
import de.hub.mse.server.repository.ExperimentRepository;
import de.hub.mse.server.repository.FileDescriptorRepository;
import de.hub.mse.server.service.execution.FilePersistence;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ExperimentService {

    private final ExperimentRepository experimentRepository;
    private final FileDescriptorRepository fileRepository;

    private final FilePersistence filePersistence;

    private final ExecutionResultRepository resultRepository;

    @Autowired
    public ExperimentService(ExperimentRepository experimentRepository, FileDescriptorRepository fileRepository,
                             FilePersistence filePersistence, ExecutionResultRepository resultRepository) {
        this.experimentRepository = experimentRepository;
        this.fileRepository = fileRepository;
        this.filePersistence = filePersistence;
        this.resultRepository = resultRepository;
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

    public void resetExperiment(String experimentId) throws NotFoundException {
        var experiment = experimentRepository.findById(experimentId).orElseThrow(NotFoundException::new);
        log.info("Resetting Experiment [{}]", experiment.getDescription());
        // delete files
        var files = new ArrayList<>(fileRepository.getFileIdsForExperiment(experimentId));
        log.info("Deleting {} files from persistence...", files.size());
        for (int i = 0; i < files.size(); i++) {
            String fileId = files.get(i);
            filePersistence.deleteFile(fileId);
            if(i > 0 && i % 10 == 0) {
                log.info("Deleted {}/{} files of experiment [{}]", i, files.size(), experiment.getDescription());
            }
        }
        // delete results
        var results = resultRepository.findAllByExperiment(experimentId);
        // reset experiment
        experiment.setPrepared(Experiment.PreparationState.UNPREPARED);
        experiment.setSerializedLinks(Collections.emptyList());
        log.info("Purging {} files and {} results from database for experiment [{}]", files.size(), results.size(),
                experiment.getDescription());
        experimentRepository.save(experiment);
        fileRepository.deleteAllById(files);
        resultRepository.deleteAll(results);
        log.info("Resetted experiment [{}]", experiment.getDescription());
    }
}
