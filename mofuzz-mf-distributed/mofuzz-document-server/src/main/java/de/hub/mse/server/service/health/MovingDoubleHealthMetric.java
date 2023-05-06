package de.hub.mse.server.service.health;

public class MovingDoubleHealthMetric extends MovingHealthMetric<Double> {

    public MovingDoubleHealthMetric(long maxAgeSeconds, int countForConfidence) {
        super(maxAgeSeconds, countForConfidence);
    }

    public Double calculateAverage() {
        return aggregate(values -> values.stream()
                .mapToDouble(Double::doubleValue)
                .sum() / values.size());
    }
}
