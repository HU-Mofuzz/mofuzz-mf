package de.hub.mse.server.service.analysis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ResultStatistic {
    private int crashResults;
    private int hangResults;
    private int regularResults;

    private List<ExceptionCount> uniqueExceptions;

    private ResultDuration longestDuration;
    private ResultDuration shortestDuration;
    private long averageDuration;
}
