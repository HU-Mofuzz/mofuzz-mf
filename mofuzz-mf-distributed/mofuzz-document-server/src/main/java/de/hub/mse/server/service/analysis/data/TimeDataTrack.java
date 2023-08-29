package de.hub.mse.server.service.analysis.data;

import lombok.experimental.Delegate;

import java.util.ArrayList;
import java.util.List;

public class TimeDataTrack<T extends Number> implements List<TimeDataPoint<T>> {

    @Delegate
    private final List<TimeDataPoint<T>> data = new ArrayList<>();

    public void add(T data, Long timestamp) {
        this.add(new TimeDataPoint<>(data, timestamp));
    }

}
