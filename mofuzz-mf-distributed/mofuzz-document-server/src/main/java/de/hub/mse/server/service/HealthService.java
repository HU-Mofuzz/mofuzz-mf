package de.hub.mse.server.service;

import com.google.common.collect.Maps;
import com.sun.management.OperatingSystemMXBean;
import de.hub.mse.server.config.ServiceConfig;
import de.hub.mse.server.service.health.SystemHealth;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class HealthService {

    private static final String NAME_DOCUMENT_SERVER = "Document Server";
    private static final Long MOVING_HEALTH_MAX_AGE_SEC = 60L;
    private static final int SERVER_HEALTH_CONFIDENCE = 30;

    private static final int HEALTH_WARNING_MIN_INTERVAL_MINUTES = 60;
    private final ServiceConfig serviceConfig;

    private final HealthMonitor healthMonitor;

    private final SystemHealth serverHealth = new SystemHealth(NAME_DOCUMENT_SERVER, MOVING_HEALTH_MAX_AGE_SEC,
                                                                SERVER_HEALTH_CONFIDENCE);
    private final OperatingSystemMXBean operatingSystemBean =
            (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

    public HealthService(ServiceConfig serviceConfig, MailService mailService) {
        this.serviceConfig = serviceConfig;
        this.healthMonitor = new HealthMonitor(mailService, serviceConfig);
    }

    @Scheduled(fixedRate = 15000)
    private void checkSystemLoad() {
        var cpuLoad = operatingSystemBean.getCpuLoad() * 100d;
        var memoryLoad = getMemoryLoadFromBean(operatingSystemBean) * 100d;
        var diskLoad = getDocumentDirectoryDiskLoad(serviceConfig) * 100d;

        log.info("[System Loads - now]\tCPU: {}%\tMEM: {}%\tDISK: {}%", String.format("%.1f", cpuLoad),
                String.format("%.1f", memoryLoad), String.format("%.1f", diskLoad));
        log.info("[System Loads - avg]\t{}", serverHealth);

        healthMonitor.monitorQuotas(serverHealth);
    }

    @Scheduled(fixedRate = 1000)
    private void measureSystemLoad() {
        var cpuLoad = operatingSystemBean.getCpuLoad();
        var memoryLoad = getMemoryLoadFromBean(operatingSystemBean);
        var diskLoad = getDocumentDirectoryDiskLoad(serviceConfig);

        serverHealth.addCpuMeasure(cpuLoad);
        serverHealth.addMemoryMeasure(memoryLoad);
        serverHealth.addDiskMeasure(diskLoad);
    }

    private static double getMemoryLoadFromBean(OperatingSystemMXBean bean) {
        var freeSpace = (double)(bean.getFreeMemorySize() + bean.getFreeSwapSpaceSize());
        var totalSpace = (double)(bean.getTotalMemorySize() + bean.getTotalSwapSpaceSize());
        return  1d - (freeSpace / totalSpace);
    }

    private static double getDocumentDirectoryDiskLoad(ServiceConfig serviceConfig) {
        var documentDir = new File(serviceConfig.getDocumentDirectory());
        return  1d - ((double)documentDir.getFreeSpace() / (double)documentDir.getTotalSpace());
    }

    @AllArgsConstructor
    @Slf4j
    private static class HealthMonitor {

        private final MailService mailService;
        private final ServiceConfig serviceConfig;

        private final Map<String, Long> lastWarningTimestamps = Maps.newConcurrentMap();
        private final Map<String, Long> firstViolationTimestamps = Maps.newConcurrentMap();

        private boolean rateLimitAllowsWarning(SystemHealth systemHealth) {
            var lastWarning = lastWarningTimestamps.getOrDefault(systemHealth.getSystemName(), 0L);
            return lastWarning < System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(HEALTH_WARNING_MIN_INTERVAL_MINUTES);
        }

        private static String timestampToDateString(Long timestamp) {
            var date = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp/1000), ZoneId.systemDefault());
            return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(date);
        }

        public void monitorQuotas(SystemHealth systemHealth) {
            if(systemHealth.violatesQuotas(serviceConfig.getHealthCpuWarnQuota(),
                    serviceConfig.getHealthMemoryWarnQuota(), serviceConfig.getHealthDiskWarnQuota())) {
                log.error("System [{}] violates configured quotas: {}", systemHealth.getSystemName(), systemHealth);

                var firstViolation = firstViolationTimestamps.getOrDefault(systemHealth.getSystemName(), System.currentTimeMillis());
                firstViolationTimestamps.put(systemHealth.getSystemName(), firstViolation);

                if(rateLimitAllowsWarning(systemHealth)) {
                    try {
                        String mailTitle = "[Mofuzz] Warning for system \""+systemHealth.getSystemName()+"\"";
                        String mailBody = String.format("""
                            The health monitor detected a violation of the system "%s" since %s!
                            
                            The system violates the configured quotas of CPU: %.1f%%\tMEM: %.1f%%\tDISK: %.1f%%
                            by having observed a confident average of    %s.
                            
                            This may requires immediate action!
                            """, systemHealth.getSystemName(), timestampToDateString(firstViolation),
                                serviceConfig.getHealthCpuWarnQuota() * 100d,
                                serviceConfig.getHealthMemoryWarnQuota() * 100d,
                                serviceConfig.getHealthDiskWarnQuota() * 100d, systemHealth);
                        mailService.sendSimpleMessageOrThrow(mailTitle, mailBody);
                        lastWarningTimestamps.put(systemHealth.getSystemName(), System.currentTimeMillis());
                    } catch (Exception e) {
                        log.error("Exception while sending health warning mail, will retry: ", e);
                    }
                }
            } else {
                firstViolationTimestamps.remove(systemHealth.getSystemName());
            }
        }
    }
}
