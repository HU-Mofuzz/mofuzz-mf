package de.hub.mse.server.service;

import de.hub.mse.server.management.ExecutionResult;
import de.hub.mse.server.repository.ExecutionResultRepository;
import de.hub.mse.server.repository.ExperimentRepository;
import de.hub.mse.server.repository.FileDescriptorRepository;
import de.hub.mse.server.repository.HealthSnapshotRepository;
import de.hub.mse.server.service.analysis.ClientResultCount;
import de.hub.mse.server.service.analysis.FilenameUtil;
import de.hub.mse.server.service.analysis.PageResponse;
import de.hub.mse.server.service.analysis.ResultStatistic;
import de.hub.mse.server.service.analysis.data.DataTrack;
import de.hub.mse.server.service.analysis.data.ExperimentHealthData;
import de.hub.mse.server.service.analysis.data.ResearchQuestionData;
import de.hub.mse.server.service.analysis.data.TimeDataTrack;
import de.hub.mse.server.service.execution.FilePersistence;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
public class AnalysisService {

    private static final String LINUX_CLIENT = "b24ea9b9-6dcf-486f-a63d-476b08048f87";
    private static final String LAPTOP_CLIENT = "32b1e12b-bfd2-4ff8-8e7e-9903a35774e3";
    private static final String TOWER_CLIENT = "46eceb24-3aa9-486c-a06f-65760a8557f8";

    private static final List<String> BASELINE_IDS = Arrays.asList(
            "107ac4d5-064c-469d-b381-d150d630aa97",
            "e096d84a-c8fd-41aa-bccd-67707b896659",
            "233b5348-4ccb-4a6b-8b30-7a30ae0d63cb",
            "aa71a203-bdbd-47b0-a206-a54c0bc906a9",
            "c6cf1547-9ced-445d-88fb-7f8b19f24585",
            "45d232b4-3223-41d9-b254-195a095176f7",
            "a5537783-044b-45a9-bb20-00204b79666f",
            "21f72440-87e2-4707-a78b-046d7085a2f9",
            "e2127e16-1267-4a46-aa6a-e5960e0978be",
            "c7ef7a5a-1a44-42c8-8e18-d306668ce0dd"
    );
    private static final List<String> EXPERIMENT_IDS = Arrays.asList(
            "7c8ad0e1-c296-4ec8-9e15-d504a74a1505",
            "40a3c331-33bf-4acc-85c5-217fbdb040e3",
            "15ae3d4f-fb91-442f-8e39-f6f6ad1a97d4",
            "7482f0e4-210c-4c15-9b7f-e6a44d92596d",
            "7d0ed8d6-90a6-4dbb-b03f-5901d61ea0f6",
            "34338bc8-dd3f-44c1-a860-a417144a1a60",
            "1f5624a2-dc8a-4c16-bac5-d761a5c1b8d5",
            "cd0db311-f758-4e1e-b56c-8e690be26498",
            "89784fd6-0ef3-4c40-8638-5c2df2bf04aa",
            "87aad030-d67a-4024-9199-f77b86df9cdb"
    );

    private final ExecutionResultRepository resultRepository;
    private final ExperimentRepository experimentRepository;
    private final FileDescriptorRepository fileRepository;
    private final HealthSnapshotRepository healthRepository;

    private final ExecutionService executionService;

    private final FilePersistence filePersistence;

    @Autowired
    public AnalysisService(ExecutionResultRepository resultRepository,
                           ExperimentRepository experimentRepository,
                           FileDescriptorRepository fileRepository,
                           HealthSnapshotRepository healthRepository,
                           ExecutionService executionService,
                           FilePersistence filePersistence) {
        this.resultRepository = resultRepository;
        this.experimentRepository = experimentRepository;
        this.fileRepository = fileRepository;
        this.healthRepository = healthRepository;
        this.executionService = executionService;
        this.filePersistence = filePersistence;
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
                    cpuTrack.add(snapshot.getTimestamp(), snapshot.getCpu());
                    memoryTrack.add(snapshot.getTimestamp(), snapshot.getMemory());
                    diskTrack.add(snapshot.getTimestamp(), snapshot.getDisk());
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

    public void zipFileTreeOfDescriptor(String fileId, ZipOutputStream stream) {
        var descriptor = fileRepository.findById(fileId);
        addFileToZip(fileId, fileId, stream);
        for(String element : descriptor.map(executionService::getRecursiveFileIdsOfDescriptor)
                                        .orElse(Collections.emptySet())) {
            addFileToZip(element, fileId, stream);
        }
    }
    public ResearchQuestionData getResearchQuestionData() {
        return new ResearchQuestionData(gatherQuestionOneData(), gatherQuestionTwoData());
    }

    private void addFileToZip(String fileId, String mainFile, ZipOutputStream stream) {
        try(var file = filePersistence.getFile(fileId, key -> FilenameUtil.mapFileKey(key, mainFile))) {
            if(file.getContentLength() > 0) {
                var entry = new ZipEntry(file.getFilename());
                entry.setSize(file.getContentLength());
                entry.setTime(System.currentTimeMillis());
                stream.putNextEntry(entry);
                StreamUtils.copy(file.getContent(), stream);
                stream.closeEntry();
            }
        } catch (Exception e) {
            log.error("Error creating zip file!", e);
        }
    }

    private static DataTrack<Integer, Integer> createCountTrackWithDelta(List<ExecutionResult> results,
                                                                         Predicate<ExecutionResult> predicate,
                                                                         Function<ExecutionResult, Integer> deltaMapper) {
        var track = new DataTrack<Integer, Integer>();
        track.add(0, 0);
        int count = 0;
        for (int i = 0; i < results.size(); i++) {
            var result = results.get(i);
            if(predicate.test(result)) {
                count += deltaMapper.apply(result);
            }
            track.add(i, count);
        }
        return track;
    }

    private static double median(List<Integer> list) {
        list.sort(Integer::compareTo);
        if(list.size() % 2 == 0) {
            return (list.get(list.size()/2) + (double)list.get((list.size()/2) - 1)) / 2;
        } else {
            return (double)list.get(list.size()/2);
        }
    }

    private static DataTrack<Integer, Integer> createCountTrack(List<ExecutionResult> results, Predicate<ExecutionResult> predicate) {
        return createCountTrackWithDelta(results, predicate, result -> 1);
    }

    private static ResearchQuestionData.StatisticalTracks<Integer, Integer>
                fromDataTracks(List<DataTrack<Integer, Integer>> tracks) {
        DataTrack<Integer, Integer> minTrack = new DataTrack<>();
        DataTrack<Integer, Double> avgTrack = new DataTrack<>();
        DataTrack<Integer, Integer> maxTrack = new DataTrack<>();
        for (int i = 0; i < tracks.get(0).toArray().length; i++) {
            final int index = i;
            var values = tracks.stream()
                    .map(track -> track.get(index).getY())
                    .toList();



            minTrack.add(index, values.stream().mapToInt(Integer::intValue).min().orElse(0));
            avgTrack.add(index, median(new ArrayList<>(values)));
            maxTrack.add(index, values.stream().mapToInt(Integer::intValue).max().orElse(0));

        }
        return ResearchQuestionData.StatisticalTracks.<Integer, Integer>builder()
                .min(minTrack)
                .median(avgTrack)
                .max(maxTrack)
                .build();
    }

    private List<DataTrack<Integer, Integer>> dataTracksForExperimentsClientAndPredicate(List<String> experiments,
                                                                                         String clientId,
                                                                                         Predicate<ExecutionResult> predicate) {
        return experiments.stream()
                .map(id -> resultRepository.findByExperimentAndOriginClientOrderByTimestampAsc(id, clientId))
                .map(results -> createCountTrack(results, predicate))
                .collect(Collectors.toList());
    }

    private List<DataTrack<Integer, Integer>> dataTracksForExperimentsClientPredicateAndDelta(List<String> experiments,
                                                                                         String clientId,
                                                                                         Predicate<ExecutionResult> predicate,
                                                                                         Function<ExecutionResult, Integer> deltaMapper) {
        return experiments.stream()
                .map(id -> resultRepository.findByExperimentAndOriginClientOrderByTimestampAsc(id, clientId))
                .map(results -> createCountTrackWithDelta(results, predicate, deltaMapper))
                .collect(Collectors.toList());
    }

    private  ResearchQuestionData.ClientTracks<Integer, Integer>
            clientTracksForExperimentIds(List<String> ids, Predicate<ExecutionResult> predicate) {

        return ResearchQuestionData.ClientTracks.<Integer, Integer>builder()
                .linuxClient(fromDataTracks(dataTracksForExperimentsClientAndPredicate(ids, LINUX_CLIENT, predicate)))
                .laptopClient(fromDataTracks(dataTracksForExperimentsClientAndPredicate(ids, LAPTOP_CLIENT, predicate)))
                .towerClient(fromDataTracks(dataTracksForExperimentsClientAndPredicate(ids, TOWER_CLIENT, predicate)))
                .build();
    }

    private ResearchQuestionData.ClientTrackPair<Integer, Integer> clientTracksPairsForPredicate(Predicate<ExecutionResult> predicate) {
        return new ResearchQuestionData.ClientTrackPair<>(
                clientTracksForExperimentIds(BASELINE_IDS, predicate),
                clientTracksForExperimentIds(EXPERIMENT_IDS, predicate)
        );
    }

    private ResearchQuestionData.ClientTracks<Integer, Integer> gatherErrorsInSheets() {
        var linuxResults = BASELINE_IDS.stream()
                .map(id -> resultRepository.findByExperimentAndOriginClientOrderByTimestampAsc(id, LINUX_CLIENT))
                .toList();
        var laptopResults = BASELINE_IDS.stream()
                .map(id -> resultRepository.findByExperimentAndOriginClientOrderByTimestampAsc(id, LAPTOP_CLIENT))
                .toList();
        var towerResults = BASELINE_IDS.stream()
                .map(id -> resultRepository.findByExperimentAndOriginClientOrderByTimestampAsc(id, TOWER_CLIENT))
                .toList();

        // sort all results in the same document order
        var comparator = new ExecutionResultComparator(linuxResults.get(0).stream()
                .map(ExecutionResult::getFileDescriptor)
                .toList());
        linuxResults.forEach(result-> result.sort(comparator));
        laptopResults.forEach(result-> result.sort(comparator));
        towerResults.forEach(result-> result.sort(comparator));

        // convert to tracks
        var linuxTracks = fromDataTracks(linuxResults.stream()
                .map(results -> createCountTrackWithDelta(results, result -> true, result -> result.getErrorCount()))
                .collect(Collectors.toList()));
        var laptopTracks = fromDataTracks(laptopResults.stream()
                .map(results -> createCountTrackWithDelta(results, result -> true, result -> result.getErrorCount()))
                .collect(Collectors.toList()));
        var towerTracks = fromDataTracks(towerResults.stream()
                .map(results -> createCountTrackWithDelta(results, result -> true, result -> result.getErrorCount()))
                .collect(Collectors.toList()));

        return ResearchQuestionData.ClientTracks.<Integer, Integer>builder()
                .linuxClient(linuxTracks)
                .laptopClient(laptopTracks)
                .towerClient(towerTracks)
                .build();
    }

    private ResearchQuestionData.QuestionOneData gatherQuestionOneData() {
        log.info("Gathering question one data");
        return ResearchQuestionData.QuestionOneData.builder()
                .crashes(clientTracksPairsForPredicate(ExecutionResult::isCrash))
                .exceptionTypes(clientTracksPairsForPredicate(new ExceptionTypePredicate()))
                .errorsInSheets(gatherErrorsInSheets())
                .build();
    }

    private ResearchQuestionData.QuestionTwoData gatherQuestionTwoData() {
        log.info("Gathering question two data");
        return ResearchQuestionData.QuestionTwoData.builder()
                .absoluteTimeouts(clientTracksPairsForPredicate(ExecutionResult::isHang))
                .build();
    }

    private static class ExceptionTypePredicate implements Predicate<ExecutionResult> {
        private final Set<String> uniqueException = new HashSet<>();

        private String extractExceptionType(String message) {
            int index = message.indexOf(':');
            if(index < 0) {
                return message;
            } else {
                return message.substring(0, index);
            }
        }

        @Override
        public boolean test(ExecutionResult result) {
            if(result.isCrash()) {
                var type = extractExceptionType(result.getException());
                if(!uniqueException.contains(type)) {
                    uniqueException.add(type);
                    return true;
                }
            }
            return false;
        }
    }

    @AllArgsConstructor
    private static class ExecutionResultComparator implements Comparator<ExecutionResult> {

        private final List<String> referenceOrder;
        @Override
        public int compare(ExecutionResult a, ExecutionResult b) {
            if(a == null && b == null) {
                return 0;
            } else if(a == null ^ b == null) {
                return a == null? -1 : 1;
            }
            return referenceOrder.indexOf(a.getFileDescriptor()) - referenceOrder.indexOf(b.getFileDescriptor());
        }
    }
}
