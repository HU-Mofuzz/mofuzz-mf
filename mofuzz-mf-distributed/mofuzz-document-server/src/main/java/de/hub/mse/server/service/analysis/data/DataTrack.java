package de.hub.mse.server.service.analysis.data;

import lombok.experimental.Delegate;

import java.util.ArrayList;
import java.util.List;

public class DataTrack<X extends Number, Y extends Number> implements List<DataPoint<X, Y>> {

    @Delegate
    private final List<DataPoint<X, Y>> data = new ArrayList<>();

    public void add(X x, Y y) {
        this.add(new DataPoint<>(x, y));
    }
}
