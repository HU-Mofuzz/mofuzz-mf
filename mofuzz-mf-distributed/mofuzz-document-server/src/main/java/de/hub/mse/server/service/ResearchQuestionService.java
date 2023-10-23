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
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;
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
    private LoadingCache<Pair<String, String>, List<ExecutionResult>> resultCache;

    public ResearchQuestionService(ExecutionResultRepository resultRepository) {
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
        int index = message.indexOf('\n');
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
        int count = 0;
        for (int i = 0; i < results.size(); i++) {
            var result = results.get(i);
            if(predicate.test(result)) {
                count += deltaMapper.apply(result);
            }
            track.add(i+1, count);
        }
        return track;
    }

    private static int min(List<Integer> list) {
        var min = Integer.MAX_VALUE;
        for(var item : list) {
            if(item < min) {
                min = item;
            }
        }
        return min;
    }

    private static double median(List<Integer> list) {
        list.sort(Integer::compareTo);
        if(list.size() % 2 == 0) {
            return (list.get(list.size()/2) + (double)list.get((list.size()/2) - 1)) / 2;
        } else {
            return (double)list.get(list.size()/2);
        }
    }

    private static int max(List<Integer> list) {
        var max = Integer.MIN_VALUE;
        for(var item : list) {
            if(item > max) {
                max = item;
            }
        }
        return max;
    }

    private static DataTrack<Integer, Integer> createCountTrack(List<ExecutionResult> results, Predicate<ExecutionResult> predicate) {
        return createCountTrackWithDelta(results, predicate, result -> 1);
    }

    private static ResearchQuestionData.StatisticalTracks<Integer, Integer>
    fromDataTracks(List<DataTrack<Integer, Integer>> tracks) {
        DataTrack<Integer, Integer> minTrack = new DataTrack<>();
        DataTrack<Integer, Double> avgTrack = new DataTrack<>();
        DataTrack<Integer, Integer> maxTrack = new DataTrack<>();
        for (int i = 0; i < tracks.get(0).size(); i++) {
            final int index = i;
            final int x = tracks.get(0).get(index).getX();
            var values = tracks.stream()
                    .map(track -> track.get(index).getY())
                    .toList();

            minTrack.add(x, min(new ArrayList<>(values)));
            avgTrack.add(x, median(new ArrayList<>(values)));
            maxTrack.add(x, max(new ArrayList<>(values)));

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

    private  ResearchQuestionData.ClientTracks<Integer, Integer>
    clientTracksForExperimentIds(List<String> ids, Predicate<ExecutionResult> predicate) {

        return ResearchQuestionData.ClientTracks.<Integer, Integer>builder()
                .linuxClient(fromDataTracks(dataTracksForExperimentsClientAndPredicate(ids, LINUX_CLIENT, predicate)))
                .laptopClient(fromDataTracks(dataTracksForExperimentsClientAndPredicate(ids, LAPTOP_CLIENT, predicate)))
                .towerClient(fromDataTracks(dataTracksForExperimentsClientAndPredicate(ids, TOWER_CLIENT, predicate)))
                .build();
    }

    private static <X extends Number, Y extends Number> double[] getLastValueOfTracks(List<DataTrack<X, Y>> tracks) {
        var result = new double[tracks.size()];
        for (int i = 0; i < tracks.size(); i++) {
            var track = tracks.get(i);
            result[i] = track.get(track.size() -1).getY().doubleValue();
        }
        return result;
    }

    private static <X extends Number, Y extends Number> double mannWhitneyTestPValueOf(DataTrack<X, Y> trackA,
                                                                                       DataTrack<X, Y> trackB) {
        return mannWhitneyTestPValueOf(new ArrayList<>(trackA.stream()
                .map(DataTrack::singleton)
                .toList()),
                new ArrayList<>(trackB.stream()
                        .map(DataTrack::singleton)
                        .toList()));
    }

    private static <X extends Number, Y extends Number> double mannWhitneyTestPValueOf(List<DataTrack<X, Y>> tracksA,
                                                                                        List<DataTrack<X, Y>> tracksB) {
        var aLastValues = getLastValueOfTracks(tracksA);
        var bLastValues = getLastValueOfTracks(tracksB);

        var test = new MannWhitneyUTest();
        return test.mannWhitneyUTest(aLastValues, bLastValues);
    }

    private ResearchQuestionData.ClientTrackPair<Integer, Integer> clientTracksPairsForPredicate(Predicate<ExecutionResult> predicate) {

        var linuxBaselineTracks = dataTracksForExperimentsClientAndPredicate(BASELINE_IDS, LINUX_CLIENT, predicate);
        var laptopBaselineTracks = dataTracksForExperimentsClientAndPredicate(BASELINE_IDS, LAPTOP_CLIENT, predicate);
        var towerBaselineTracks = dataTracksForExperimentsClientAndPredicate(BASELINE_IDS, TOWER_CLIENT, predicate);

        var linuxExperimentTracks = dataTracksForExperimentsClientAndPredicate(EXPERIMENT_IDS, LINUX_CLIENT, predicate);
        var laptopExperimentTracks = dataTracksForExperimentsClientAndPredicate(EXPERIMENT_IDS, LAPTOP_CLIENT, predicate);
        var towerExperimentTracks = dataTracksForExperimentsClientAndPredicate(EXPERIMENT_IDS, TOWER_CLIENT, predicate);

        return new ResearchQuestionData.ClientTrackPair<>(
                ResearchQuestionData.ClientTracks.<Integer, Integer>builder()
                        .linuxClient(fromDataTracks(linuxBaselineTracks))
                        .laptopClient(fromDataTracks(laptopBaselineTracks))
                        .towerClient(fromDataTracks(towerBaselineTracks))
                        .build(),
                ResearchQuestionData.ClientTracks.<Integer, Integer>builder()
                        .linuxClient(fromDataTracks(linuxExperimentTracks))
                        .laptopClient(fromDataTracks(laptopExperimentTracks))
                        .towerClient(fromDataTracks(towerExperimentTracks))
                        .build(),
                new ResearchQuestionData.MannWhitneyUTestStatistic(
                        mannWhitneyTestPValueOf(linuxBaselineTracks, linuxExperimentTracks),
                        mannWhitneyTestPValueOf(laptopBaselineTracks, laptopExperimentTracks),
                        mannWhitneyTestPValueOf(towerBaselineTracks, towerExperimentTracks)
                )
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
        var typeData = clientTracksPairsForPredicate(new ExceptionTypePredicate());

        return ResearchQuestionData.QuestionOneData.builder()
                .crashes(clientTracksPairsForPredicate(ExecutionResult::isCrash))
                .exceptionTypes(typeData)
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
        var baselineTotalDurations = absoluteDataOfResultsLong(BASELINE_IDS, ExecutionResult::getDuration);
        var experimentTotalDurations = absoluteDataOfResultsLong(EXPERIMENT_IDS, ExecutionResult::getDuration);

        var mannWhitneyStatistic = new ResearchQuestionData.MannWhitneyUTestStatistic(
                mannWhitneyTestPValueOf(baselineTotalDurations.getLinuxClient(), experimentTotalDurations.getLinuxClient()),
                mannWhitneyTestPValueOf(baselineTotalDurations.getLaptopClient(), experimentTotalDurations.getLaptopClient()),
                mannWhitneyTestPValueOf(baselineTotalDurations.getLaptopClient(), experimentTotalDurations.getLaptopClient())
        );

        return ResearchQuestionData.QuestionTwoData.builder()
                .absoluteTimeouts(clientTracksPairsForPredicate(ExecutionResult::isHang))
                .baselineTotalErrors(absoluteDataOfResultsInt(BASELINE_IDS, ExecutionResult::getErrorCount))
                .errorsInSheets(gatherErrorsInSheets())
                .totalDuration(new ResearchQuestionData.ClientDataPair<>(baselineTotalDurations, experimentTotalDurations, mannWhitneyStatistic))
                .differentErrors(gatherDifferentErrors())
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
            linuxBaselineTrack.add(i+1, getAverageDurationForExperimentAndClient(id, LINUX_CLIENT));
            laptopBaselineTrack.add(i+1, getAverageDurationForExperimentAndClient(id, LAPTOP_CLIENT));
            towerBaselineTrack.add(i+1, getAverageDurationForExperimentAndClient(id, TOWER_CLIENT));
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
                new ResearchQuestionData.ClientData<>(linuxExperimentTrack, laptopExperimentTrack, towerExperimentTrack),
                new ResearchQuestionData.MannWhitneyUTestStatistic(
                        mannWhitneyTestPValueOf(linuxBaselineTrack, linuxExperimentTrack),
                        mannWhitneyTestPValueOf(laptopBaselineTrack, laptopExperimentTrack),
                        mannWhitneyTestPValueOf(towerBaselineTrack, towerExperimentTrack)
                )
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

    private Set<ResearchQuestionData.DifferentErrorInfo> getFilesWithDifferentErrorsForLaptopAndTower(String experiment) {
        var differentErrors = new HashSet<ResearchQuestionData.DifferentErrorInfo>();
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
            if(laptopResult.isCrash() || towerResult.isCrash() || laptopResult.isHang() || towerResult.isHang()) {
                continue;
            }
            if(laptopResult.getErrorCount() != towerResult.getErrorCount()) {
                differentErrors.add(new ResearchQuestionData.DifferentErrorInfo(
                        laptopResult.getFileDescriptor(),
                        laptopResult.getErrorCount(),
                        towerResult.getErrorCount()
                ));
            }
        }
        return differentErrors;
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

    private Map<String, Set<ResearchQuestionData.DifferentErrorInfo>> gatherDifferentErrors() {
        Map<String, Set<ResearchQuestionData.DifferentErrorInfo>> result = new HashMap<>();
        for (String experiment : BASELINE_IDS) {
            var difference = getFilesWithDifferentErrorsForLaptopAndTower(experiment);
            if (!difference.isEmpty()) {
                result.put(experiment, difference);
            }
        }
        return result;
    }

    private ResearchQuestionData.QuestionThreeData gatherQuestionThreeData() {

        var baselineTracks = absoluteDataOfResultsInt(BASELINE_IDS, r -> (r.isHang() ? 1 : 0));
        var experimentTracks = absoluteDataOfResultsInt(EXPERIMENT_IDS, r -> (r.isHang() ? 1 : 0));

        return ResearchQuestionData.QuestionThreeData.builder()
                .totalTimeouts(new ResearchQuestionData.ClientDataPair<>(
                        baselineTracks, experimentTracks,
                        new ResearchQuestionData.MannWhitneyUTestStatistic(
                                mannWhitneyTestPValueOf(baselineTracks.getLinuxClient(), experimentTracks.getLinuxClient()),
                                mannWhitneyTestPValueOf(baselineTracks.getLaptopClient(), experimentTracks.getLaptopClient()),
                                mannWhitneyTestPValueOf(baselineTracks.getTowerClient(), experimentTracks.getTowerClient())
                        )
                ))
                .averageExecutionTime(gatherAverageExecutionTimes())
                .differentExceptions(gatherDifferentExceptions())
                .build();
    }

    private static class  ExceptionTypePredicate implements Predicate<ExecutionResult> {
        private final Map<String, Map<String, Set<String>>> uniqueException = new HashMap<>();

        @Override
        public boolean test(ExecutionResult result) {
            var newCrash = false;
            if(result.isCrash()) {
                var type = extractExceptionType(result.getException());

                var clientExperiments = uniqueException.getOrDefault(result.getOriginClient(), new HashMap<>());
                var experimentExceptions = clientExperiments.getOrDefault(result.getExperiment(), new HashSet<>());
                if(!experimentExceptions.contains(type)) {
                    experimentExceptions.add(type);
                    newCrash = true;
                }
                clientExperiments.put(result.getExperiment(), experimentExceptions);
                uniqueException.put(result.getOriginClient(), clientExperiments);
            }
            return newCrash;
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
