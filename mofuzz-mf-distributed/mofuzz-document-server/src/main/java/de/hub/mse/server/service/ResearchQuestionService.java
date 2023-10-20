package de.hub.mse.server.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import de.hub.mse.server.management.ExecutionResult;
import de.hub.mse.server.repository.ExecutionResultRepository;
import de.hub.mse.server.service.analysis.data.DataTrack;
import de.hub.mse.server.service.analysis.research.ResearchQuestionData;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ResearchQuestionService {

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
    private LoadingCache<Pair<String, String>, List<ExecutionResult>> resultCache;

    public ResearchQuestionService(ExecutionResultRepository resultRepository) {
        this.resultRepository = resultRepository;
        resultCache = CacheBuilder.newBuilder()
                .build(new CacheLoader<>() {
                    @Override
                    public List<ExecutionResult> load(Pair<String, String> key) throws Exception {
                        return resultRepository.findByExperimentAndOriginClientOrderByTimestampAsc(key.getFirst(), key.getSecond());
                    }
                });
    }

    @Cacheable("researchData")
    public ResearchQuestionData getResearchQuestionData() {
        return new ResearchQuestionData(gatherQuestionOneData(), gatherQuestionTwoData(), gatherQuestionThreeData());
    }

    private static String extractExceptionType(String message) {
        int index = message.indexOf(':');
        if(index < 0) {
            return message;
        } else {
            return message.substring(0, index);
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

    private List<ExecutionResult> wrappedResultAccess(String experiment, String clientId) {
        try {
            return resultCache.get(Pair.of(experiment, clientId));
        } catch (ExecutionException e) {
            return Collections.emptyList();
        }
    }

    private List<DataTrack<Integer, Integer>> dataTracksForExperimentsClientAndPredicate(List<String> experiments,
                                                                                         String clientId,
                                                                                         Predicate<ExecutionResult> predicate) {
        return experiments.stream()
                .map(id -> wrappedResultAccess(id, clientId))
                .map(results -> createCountTrack(results, predicate))
                .collect(Collectors.toList());
    }

    private List<DataTrack<Integer, Integer>> dataTracksForExperimentsClientPredicateAndDelta(List<String> experiments,
                                                                                              String clientId,
                                                                                              Predicate<ExecutionResult> predicate,
                                                                                              Function<ExecutionResult, Integer> deltaMapper) {
        return experiments.stream()
                .map(id -> wrappedResultAccess(id, clientId))
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
                .map(id -> wrappedResultAccess(id, LINUX_CLIENT))
                .toList();
        var laptopResults = BASELINE_IDS.stream()
                .map(id -> wrappedResultAccess(id, LAPTOP_CLIENT))
                .toList();
        var towerResults = BASELINE_IDS.stream()
                .map(id -> wrappedResultAccess(id, TOWER_CLIENT))
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
                .map(results -> createCountTrackWithDelta(results, result -> true, ExecutionResult::getErrorCount))
                .collect(Collectors.toList()));
        var laptopTracks = fromDataTracks(laptopResults.stream()
                .map(results -> createCountTrackWithDelta(results, result -> true, ExecutionResult::getErrorCount))
                .collect(Collectors.toList()));
        var towerTracks = fromDataTracks(towerResults.stream()
                .map(results -> createCountTrackWithDelta(results, result -> true, ExecutionResult::getErrorCount))
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

    private  ResearchQuestionData.ClientData<Integer, Long> absoluteDataOfResultsLong(List<String> experiments,
                                                                            Function<ExecutionResult, Long> mapper) {
        var linuxTrack = new DataTrack<Integer, Long>();
        var laptopTrack = new DataTrack<Integer, Long>();
        var towerTrack = new DataTrack<Integer, Long>();

        for (int i = 0; i < experiments.size(); i++) {
            String id = experiments.get(i);
            linuxTrack.add(i, wrappedResultAccess(id, LINUX_CLIENT).stream()
                    .mapToLong(mapper::apply)
                    .sum());
            laptopTrack.add(i, wrappedResultAccess(id, LAPTOP_CLIENT).stream()
                    .mapToLong(mapper::apply)
                    .sum());
            towerTrack.add(i, wrappedResultAccess(id, TOWER_CLIENT).stream()
                    .mapToLong(mapper::apply)
                    .sum());
        }
        return ResearchQuestionData.ClientData.<Integer, Long>builder()
                .linuxClient(linuxTrack)
                .laptopClient(laptopTrack)
                .towerClient(towerTrack)
                .build();
    }

    private  ResearchQuestionData.ClientData<Integer, Integer> absoluteDataOfResultsInt(List<String> experiments,
                                                                                  Function<ExecutionResult, Integer> mapper) {
        var linuxTrack = new DataTrack<Integer, Integer>();
        var laptopTrack = new DataTrack<Integer, Integer>();
        var towerTrack = new DataTrack<Integer, Integer>();

        for (int i = 0; i < experiments.size(); i++) {
            String id = experiments.get(i);
            linuxTrack.add(i, wrappedResultAccess(id, LINUX_CLIENT).stream()
                    .mapToInt(mapper::apply)
                    .sum());
            laptopTrack.add(i, wrappedResultAccess(id, LAPTOP_CLIENT).stream()
                    .mapToInt(mapper::apply)
                    .sum());
            towerTrack.add(i, wrappedResultAccess(id, TOWER_CLIENT).stream()
                    .mapToInt(mapper::apply)
                    .sum());
        }
        return ResearchQuestionData.ClientData.<Integer, Integer>builder()
                .linuxClient(linuxTrack)
                .laptopClient(laptopTrack)
                .towerClient(towerTrack)
                .build();
    }

    private ResearchQuestionData.QuestionTwoData gatherQuestionTwoData() {
        log.info("Gathering question two data");


        return ResearchQuestionData.QuestionTwoData.builder()
                .absoluteTimeouts(clientTracksPairsForPredicate(ExecutionResult::isHang))
                .baselineTotalErrors(absoluteDataOfResultsInt(BASELINE_IDS, ExecutionResult::getErrorCount))
                .baselineTotalDuration(absoluteDataOfResultsLong(BASELINE_IDS, ExecutionResult::getDuration))
                .build();
    }

    private Double getAverageDurationForExperimentAndClient(String experiment, String client) {
        return wrappedResultAccess(experiment, client).stream()
                .mapToLong(ExecutionResult::getDuration)
                .average().orElse(-1d);
    }

    private ResearchQuestionData.ClientDataPair<Integer, Double> gatherAverageExecutionTimes() {
        var linuxBaselineTrack = new DataTrack<Integer, Double>();
        var laptopBaselineTrack = new DataTrack<Integer, Double>();
        var towerBaselineTrack = new DataTrack<Integer, Double>();

        for (int i = 0; i < BASELINE_IDS.size(); i++) {
            String id = BASELINE_IDS.get(i);
            linuxBaselineTrack.add(i, getAverageDurationForExperimentAndClient(id, LINUX_CLIENT));
            laptopBaselineTrack.add(i, getAverageDurationForExperimentAndClient(id, LAPTOP_CLIENT));
            towerBaselineTrack.add(i, getAverageDurationForExperimentAndClient(id, TOWER_CLIENT));
        }

        var linuxExperimentTrack = new DataTrack<Integer, Double>();
        var laptopExperimentTrack = new DataTrack<Integer, Double>();
        var towerExperimentTrack = new DataTrack<Integer, Double>();

        for (int i = 0; i < EXPERIMENT_IDS.size(); i++) {
            String id = EXPERIMENT_IDS.get(i);
            linuxExperimentTrack.add(i, getAverageDurationForExperimentAndClient(id, LINUX_CLIENT));
            laptopExperimentTrack.add(i, getAverageDurationForExperimentAndClient(id, LAPTOP_CLIENT));
            towerExperimentTrack.add(i, getAverageDurationForExperimentAndClient(id, TOWER_CLIENT));
        }
        return new ResearchQuestionData.ClientDataPair<>(
                new ResearchQuestionData.ClientData<>(linuxBaselineTrack, laptopBaselineTrack, towerBaselineTrack),
                new ResearchQuestionData.ClientData<>(linuxExperimentTrack, laptopExperimentTrack, towerExperimentTrack)
        );
    }

    private Set<String> getFilesWithDifferentExceptionsForLaptopAndTower(String experiment) {
        var differentTypes = new HashSet<String>();
        var laptopResults = wrappedResultAccess(experiment, LAPTOP_CLIENT);
        var towerResults = wrappedResultAccess(experiment, TOWER_CLIENT);

        for(var laptopResult : laptopResults) {
            var towerResultOptional = towerResults.stream()
                    .filter(r -> r.getFileDescriptor().equals(laptopResult.getFileDescriptor()))
                    .findFirst();
            if(towerResultOptional.isEmpty()) {
                continue;
            }
            var towerResult = towerResultOptional.get();
            if(laptopResult.isCrash() && towerResult.isCrash()) {
                var laptopType = extractExceptionType(laptopResult.getException());
                var towerType = extractExceptionType(laptopResult.getException());
                if(!laptopType.equals(towerType)) {
                    differentTypes.add(laptopResult.getFileDescriptor());
                }
            } else if(laptopResult.isCrash() ^ towerResult.isCrash()) {
                differentTypes.add(laptopResult.getFileDescriptor());
            }
        }
        return differentTypes;
    }

    private Map<String, Set<String>> gatherDifferentExceptions() {
        Map<String, Set<String>> result = new HashMap<>();

        for (String experiment : BASELINE_IDS) {
            Set<String> difference = getFilesWithDifferentExceptionsForLaptopAndTower(experiment);
            if (!difference.isEmpty()) {
                result.put(experiment, difference);
            }
        }

        for (String experiment : EXPERIMENT_IDS) {
            Set<String> difference = getFilesWithDifferentExceptionsForLaptopAndTower(experiment);
            if (!difference.isEmpty()) {
                result.put(experiment, difference);
            }
        }

        return result;
    }

    private ResearchQuestionData.QuestionThreeData gatherQuestionThreeData() {

        return ResearchQuestionData.QuestionThreeData.builder()
                .totalTimeouts(new ResearchQuestionData.ClientDataPair<>(
                        absoluteDataOfResultsInt(BASELINE_IDS, r -> (r.isHang() ? 1 : 0)),
                        absoluteDataOfResultsInt(EXPERIMENT_IDS, r -> (r.isHang() ? 1 : 0))
                ))
                .averageExecutionTime(gatherAverageExecutionTimes())
                .differentExceptions(gatherDifferentExceptions())
                .build();
    }

    private static class ExceptionTypePredicate implements Predicate<ExecutionResult> {
        private final Set<String> uniqueException = new HashSet<>();

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
