package de.hub.mse.server.service.analysis.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ExperimentHealthData {

    private TimeDataTrack<Double> cpu;
    private TimeDataTrack<Double> memory;
    private TimeDataTrack<Double> disk;

    private TimeDataTrack<Long> results;
    private long totalPages;
    private long totalElements;

}
