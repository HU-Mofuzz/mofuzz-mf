package de.hub.mse.server.service.health;

import lombok.Getter;

public class SystemHealth {

    private final MovingDoubleHealthMetric cpuHealth;
    private final MovingDoubleHealthMetric memoryHealth;
    private final MovingDoubleHealthMetric diskHealth;

    @Getter
    private final String systemName;

    public SystemHealth(String systemName, long maxAgeSeconds, int countForConfidence) {
        this.systemName = systemName;
        cpuHealth = new MovingDoubleHealthMetric(maxAgeSeconds, countForConfidence);
        memoryHealth = new MovingDoubleHealthMetric(maxAgeSeconds, countForConfidence);
        diskHealth = new MovingDoubleHealthMetric(maxAgeSeconds, countForConfidence);
    }

    private static void sanitizeValue(double value) {
        if(0.0d > value || value > 1.0d) {
            throw new IllegalArgumentException(value +" is not a plausible system health measurement!");
        }
    }

    private static boolean violatesQuota(MovingDoubleHealthMetric metric, Double quota) {
        return metric.isConfident() && metric.calculateAverage() > quota;
    }
    public void addCpuMeasure(Double value) {
        sanitizeValue(value);
        cpuHealth.addMeasurement(value);
    }

    public void addMemoryMeasure(Double value) {
        sanitizeValue(value);
        memoryHealth.addMeasurement(value);
    }

    public void addDiskMeasure(Double value) {
        sanitizeValue(value);
        diskHealth.addMeasurement(value);
    }

    public boolean violatesQuotas(double cpuQuota, double memoryQuota, double diskQuota) {
        return violatesQuota(cpuHealth, cpuQuota)
                || violatesQuota(memoryHealth, memoryQuota)
                || violatesQuota(diskHealth, diskQuota);
    }

    @Override
    public String toString() {
        return String.format("CPU%s: %s%%\tMEM%s: %s%%\tDISK%s: %s%%",
                confidenceString(cpuHealth), String.format("%.1f", cpuHealth.calculateAverage() * 100d),
                confidenceString(memoryHealth), String.format("%.1f", memoryHealth.calculateAverage() * 100d),
                confidenceString(diskHealth), String.format("%.1f", diskHealth.calculateAverage() * 100d));
    }

    private String confidenceString(MovingHealthMetric<?> metric) {
        return metric.isConfident()? "" : "(uncertain)";
    }
}
