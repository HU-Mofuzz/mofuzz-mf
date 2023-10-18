package de.hub.mse.server.service.analysis.data;

import lombok.Data;

@Data
public class TimeDataPoint<T extends Number> extends DataPoint<Long, T> {

    public TimeDataPoint(Long x, T y) {
        super(x, y);
    }
}
