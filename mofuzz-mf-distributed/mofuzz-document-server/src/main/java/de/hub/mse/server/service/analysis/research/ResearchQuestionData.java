package de.hub.mse.server.service.analysis.research;

import de.hub.mse.server.service.analysis.data.DataTrack;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ResearchQuestionData {

    private QuestionOneData questionOneData;
    private QuestionTwoData questionTwoData;
    private QuestionThreeData questionThreeData;

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class ClientData<X extends Number, Y extends Number> {
        private DataTrack<X, Y> linuxClient;
        private DataTrack<X, Y> laptopClient;
        private DataTrack<X, Y> towerClient;
    }

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class StatisticalTracks<X extends Number, Y extends Number> {
        private DataTrack<X, Y> min;
        private DataTrack<X, Double> median;
        private DataTrack<X, Y> max;
    }

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class ClientTracks<X extends Number, Y extends Number> {
        private StatisticalTracks<X, Y> linuxClient;
        private StatisticalTracks<X, Y> laptopClient;
        private StatisticalTracks<X, Y> towerClient;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class ClientTrackPair<X extends Number, Y extends Number> {
        private ClientTracks<X, Y> baseline;
        private ClientTracks<X, Y> experiment;

        private MannWhitneyUTestStatistic mannWhitneyUTestStatistic;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class ClientDataPair<X extends Number, Y extends Number> {
        private ClientData<X, Y> baseline;
        private ClientData<X, Y> experiment;

        private MannWhitneyUTestStatistic mannWhitneyUTestStatistic;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class MannWhitneyUTestStatistic {
        private double linuxClientP;
        private double laptopClientP;
        private double towerClientP;
    }

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class QuestionOneData {
        private ClientTrackPair<Integer, Integer> crashes;

        private ClientTrackPair<Integer, Integer> exceptionTypes;
        private ClientTracks<Integer, Integer> errorsInSheets;
    }

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class QuestionTwoData {
        private ClientTrackPair<Integer, Integer> absoluteTimeouts;

        private ClientData<Integer, Integer> baselineTotalErrors;

        private ClientData<Integer, Long> baselineTotalDuration;
    }

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class QuestionThreeData {

        private ClientDataPair<Integer, Integer> totalTimeouts;
        private ClientDataPair<Integer, Double> averageExecutionTime;
        private Map<String, Set<String>> differentExceptions;
    }
}
