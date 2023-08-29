package de.hub.mse.server.service;

import de.hub.mse.server.management.ExecutionResult;
import de.hub.mse.server.repository.ExecutionResultRepository;
import de.hub.mse.server.repository.ExperimentRepository;
import de.hub.mse.server.repository.FileDescriptorRepository;
import de.hub.mse.server.repository.HealthSnapshotRepository;
import de.hub.mse.server.service.analysis.ClientResultCount;
import de.hub.mse.server.service.analysis.PageResponse;
import de.hub.mse.server.service.analysis.ResultStatistic;
import de.hub.mse.server.service.analysis.data.ExperimentHealthData;
import de.hub.mse.server.service.analysis.data.TimeDataTrack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AnalysisService {

    private final ExecutionResultRepository resultRepository;
    private final ExperimentRepository experimentRepository;
    private final FileDescriptorRepository fileRepository;
    private final HealthSnapshotRepository healthRepository;

    @Autowired
    public AnalysisService(ExecutionResultRepository resultRepository,
                           ExperimentRepository experimentRepository,
                           FileDescriptorRepository fileRepository,
                           HealthSnapshotRepository healthRepository) {
        this.resultRepository = resultRepository;
        this.experimentRepository = experimentRepository;
        this.fileRepository = fileRepository;
        this.healthRepository = healthRepository;
    }

    private static Pageable sortBy(String sort, String order, int page, int pageSize) {
        if(sort == null || sort.isEmpty()) {
            sort = "timestamp";
        }

        Sort sorting;
        if(order == null || order.toLowerCase().startsWith("d")) {
            sorting = Sort.by(sort).descending();
        } else {
            sorting = Sort.by(sort).ascending();
        }
        return PageRequest.of(page, pageSize, sorting);
    }

    public int getResultCountForExperiment(String experiment) {
        return resultRepository.countByExperiment(experiment);
    }

    public int getResultCountForClient(String experimentId, String clientId) {
        return resultRepository.countByExperimentIsAndOriginClient(experimentId, clientId);
    }

    public int getGeneratedDocumentCount(String experimentId) {
        var experiment = experimentRepository.findById(experimentId);
        return experiment.map(value -> fileRepository.countByExperimentIsAndDepth(experimentId, value.getTreeDepth()))
                        .orElse(0);
    }

    public ResultStatistic getStatisticForExperiment(String experimentId) {
        return ResultStatistic.builder()
                .crashResults(resultRepository.countByCrashTrueAndHangFalseAndExperiment(experimentId))
                .hangResults(resultRepository.countByHangTrueAndExperiment(experimentId))
                .regularResults(resultRepository.countByHangFalseAndCrashFalseAndExperiment(experimentId))
                .uniqueExceptions(resultRepository.getUniqueStacktracesWithCount(experimentId))
                .longestDuration(resultRepository.getLongestDuration(experimentId))
                .shortestDuration(resultRepository.getShortestDuration(experimentId))
                .averageDuration(Math.round(resultRepository.getAverageDuration(experimentId)))
                .build();
    }

    public ResultStatistic getStatisticForClient(String experimentId, String clientId) {
        return ResultStatistic.builder()
                .crashResults(resultRepository.countByCrashTrueAndHangFalseAndExperimentAndOriginClient(experimentId, clientId))
                .hangResults(resultRepository.countByHangTrueAndExperimentAndOriginClient(experimentId, clientId))
                .regularResults(resultRepository.countByHangFalseAndCrashFalseAndExperimentAndOriginClient(experimentId, clientId))
                .uniqueExceptions(resultRepository.getUniqueStacktracesWithCountForClient(experimentId, clientId))
                .longestDuration(resultRepository.getLongestDurationForClient(experimentId, clientId))
                .shortestDuration(resultRepository.getShortestDurationForClient(experimentId, clientId))
                .averageDuration(Math.round(resultRepository.getAverageDurationForClient(experimentId, clientId)))
                .build();
    }

    public PageResponse<ExecutionResult> getResults(String experimentId, String sort,
                                                    String order, int page, int pageSize) {
        return PageResponse.of(resultRepository.findAllByExperiment(experimentId, sortBy(sort, order, page, pageSize)));
    }

    public PageResponse<ExecutionResult> getResultsForClient(String experimentId, String clientId,
                                                             String sort, String order, int page, int pageSize) {
        return PageResponse.of(resultRepository.findAllByExperimentAndOriginClient(experimentId, clientId,
                sortBy(sort, order, page, pageSize)));
    }

    public List<ClientResultCount> getClientsWithResultsOfExperiment(String experimentId) {
        return resultRepository.getClientsWithResultsForExperiment(experimentId);
    }

    @Transactional(readOnly = true)
    public ExperimentHealthData getDataForExperiment(String experimentId, String clientId, int page, int pageSize) {
        // Prepare data structures
        var cpuTrack = new TimeDataTrack<Double>();
        var memoryTrack = new TimeDataTrack<Double>();
        var diskTrack = new TimeDataTrack<Double>();
        var resultTrack = new TimeDataTrack<Long>();

        // Determine start and end
        var results = resultRepository.findAllByExperimentAndOriginClient(experimentId, clientId,
                sortBy("timestamp", "asc", page, pageSize));
        var earliestResult = results.getContent().get(0);
        var latestResult = results.getContent().get(results.getSize() - 1);
        var startTime = earliestResult.getTimestamp();
        var endTime = latestResult.getTimestamp() + latestResult.getDuration();
        long resultCount = resultRepository
                .countByExperimentAndOriginClientAndTimestampLessThan(experimentId, clientId, startTime);

        // fill health data tracks
        healthRepository.getSnapshotsInTimespan(clientId, startTime, endTime)
                .forEach(snapshot -> {
                    cpuTrack.add(snapshot.getCpu(), snapshot.getTimestamp());
                    memoryTrack.add(snapshot.getMemory(), snapshot.getTimestamp());
                    diskTrack.add(snapshot.getDisk(), snapshot.getTimestamp());
                });

        // fill result counter
        for(var result : results) {
            resultTrack.add(resultCount++, result.getTimestamp());
        }
        resultTrack.add(resultCount, endTime);

        return ExperimentHealthData.builder()
                .cpu(cpuTrack)
                .memory(memoryTrack)
                .disk(diskTrack)
                .results(resultTrack)
                .totalPages(results.getTotalPages())
                .totalElements(results.getTotalElements())
                .build();
    }
}
