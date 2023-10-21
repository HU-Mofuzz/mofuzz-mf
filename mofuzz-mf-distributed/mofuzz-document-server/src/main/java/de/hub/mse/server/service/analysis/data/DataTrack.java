package de.hub.mse.server.service.analysis.data;

import lombok.experimental.Delegate;

import java.util.ArrayList;
import java.util.List;

public class DataTrack<X extends Number, Y extends Number> implements List<DataPoint<X, Y>> {

    @Delegate
    private final List<DataPoint<X, Y>> data = new ArrayList<>();

    public static final <X extends Number, Y extends Number> DataTrack<X,Y> singleton(DataPoint<X, Y> point) {
        var track = new DataTrack<X, Y>();
        track.add(point);
        return track;
    }

    public void add(X x, Y y) {
        this.add(new DataPoint<>(x, y));
    }
}
