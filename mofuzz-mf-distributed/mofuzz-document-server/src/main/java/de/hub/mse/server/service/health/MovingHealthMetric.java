package de.hub.mse.server.service.health;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@AllArgsConstructor
public class MovingHealthMetric<T> {

    private final Queue<HealthMeasurement<T>> measurements = new ConcurrentLinkedQueue<>();

    private final long maxAgeSeconds;
    private final int countForConfidence;

    public void addMeasurement(T value) {
        measurements.add(new HealthMeasurement<>(value));
    }

    private void removeDeprecatedMeasurements() {
        long minDate = System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(maxAgeSeconds);
        measurements.removeIf(measurement -> measurement.timestamp < minDate);
    }

    public <U> U aggregate(Function<Collection<T>, U> aggregateFunction) {
        removeDeprecatedMeasurements();
        return aggregateFunction.apply(measurements.stream()
                .map(HealthMeasurement::getValue)
                .toList());
    }

    public boolean isConfident() {
        removeDeprecatedMeasurements();
        return measurements.size() >= countForConfidence;
    }

    @AllArgsConstructor
    @Data
    private static class HealthMeasurement<V> {
        private final long timestamp;
        private final V value;

        public HealthMeasurement(V value) {
            this(System.currentTimeMillis(), value);
        }
    }
}
