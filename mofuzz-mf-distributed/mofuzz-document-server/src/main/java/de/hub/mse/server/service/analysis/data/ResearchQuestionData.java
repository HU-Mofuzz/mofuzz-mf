package de.hub.mse.server.service.analysis.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ResearchQuestionData {

    private QuestionOneData questionOneData;
    private QuestionTwoData questionTwoData;


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
    }
}
