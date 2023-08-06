package de.hub.mse.client.health;

import com.sun.management.OperatingSystemMXBean;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.lang.management.ManagementFactory;

import static de.hub.mse.client.MofuzzDocumentClientApplication.CONFIG;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
public class HealthReport {

    private static final OperatingSystemMXBean operatingSystemBean =
            (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

    private double cpu;
    private double memory;
    private double disk;

    private static double getMemoryLoadFromBean() {
        var freeSpace = (double)(operatingSystemBean.getFreeMemorySize() + operatingSystemBean.getFreeSwapSpaceSize());
        var totalSpace = (double)(operatingSystemBean.getTotalMemorySize() + operatingSystemBean.getTotalSwapSpaceSize());
        return  1d - (freeSpace / totalSpace);
    }

    private static double getDocumentDirectoryDiskLoad() {
        var documentDir = CONFIG.getWorkingDirAsFile();
        return  1d - ((double)documentDir.getFreeSpace() / (double)documentDir.getTotalSpace());
    }

    public static HealthReport fromSystemState() {
        var cpuLoad = operatingSystemBean.getCpuLoad();
        var memoryLoad = getMemoryLoadFromBean();
        var diskLoad = getDocumentDirectoryDiskLoad();

        return new HealthReport(cpuLoad, memoryLoad, diskLoad);
    }
}
