package de.hub.mse.server.service;

import com.google.common.collect.Maps;
import com.sun.management.OperatingSystemMXBean;
import de.hub.mse.server.config.ServiceConfig;
import de.hub.mse.server.management.HealthSnapshot;
import de.hub.mse.server.repository.HealthSnapshotRepository;
import de.hub.mse.server.service.health.SystemHealth;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class HealthService {

    public static final String TOPIC_HEALTH = "/mofuzz/health";
    private static final String NAME_DOCUMENT_SERVER = "server";
    private static final Long MOVING_HEALTH_MAX_AGE_SEC = 60L;
    private static final int SYSTEM_HEALTH_CONFIDENCE = 30;

    private static final int HEALTH_WARNING_MIN_INTERVAL_MINUTES = 60;

    private static final int NO_HEARTBEAT_WARNING_MINUTES = 5;


    private final ServiceConfig serviceConfig;
    private final MailService mailService;
    private final SimpMessagingTemplate messagingTemplate;
    private final HealthSnapshotRepository healthRepository;

    private final HealthMonitor healthMonitor;

    private final Map<String, SystemHealth> systemHealthMap = Maps.newConcurrentMap();

    private final Map<String, Long> lastHearthBeat = Maps.newConcurrentMap();

    private final OperatingSystemMXBean operatingSystemBean =
            (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();


    @Autowired
    public HealthService(ServiceConfig serviceConfig, MailService mailService,
                         SimpMessagingTemplate messagingTemplate, HealthSnapshotRepository
                         healthRepository) {
        this.serviceConfig = serviceConfig;
        this.mailService = mailService;
        this.messagingTemplate = messagingTemplate;
        this.healthRepository = healthRepository;
        this.healthMonitor = new HealthMonitor(mailService, serviceConfig);
    }

    @Scheduled(fixedRate = 15000)
    private void checkSystemLoad() {
        var cpuLoad = operatingSystemBean.getCpuLoad() * 100d;
        var memoryLoad = getMemoryLoadFromBean(operatingSystemBean) * 100d;
        var diskLoad = getDocumentDirectoryDiskLoad(serviceConfig) * 100d;

        log.info("[System Loads - now]\tCPU: {}%\tMEM: {}%\tDISK: {}%", String.format("%.1f", cpuLoad),
                String.format("%.1f", memoryLoad), String.format("%.1f", diskLoad));
        log.info("[System Loads - avg]\t{}", systemHealthMap.get(NAME_DOCUMENT_SERVER));
    }

    @Scheduled(fixedRate = 1000)
    private void measureSystemLoad() {
        var cpuLoad = operatingSystemBean.getCpuLoad();
        var memoryLoad = getMemoryLoadFromBean(operatingSystemBean);
        var diskLoad = getDocumentDirectoryDiskLoad(serviceConfig);

        reportSystemHealth(NAME_DOCUMENT_SERVER, cpuLoad, memoryLoad, diskLoad);
    }

    @Scheduled(fixedRate = 60000)
    public void checkHearthBeats() {
        for(Map.Entry<String, Long> entry : lastHearthBeat.entrySet()) {
            Long minimumAge = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(NO_HEARTBEAT_WARNING_MINUTES);
           if (entry.getValue() < minimumAge) {
               String mailTitle = "[Mofuzz] Warning for system \""+entry.getKey()+"\"";
               String mailBody = String.format("""
                            The health monitor detected a violation of the system "%s" since %s!
                            
                            The system violates the configured minimum heartbeat period for health measurement reporting of %d minutes.
                            
                            This may requires immediate action!
                            """, entry.getKey(), MailService.timestampToDateString(entry.getValue()),
                       NO_HEARTBEAT_WARNING_MINUTES);
               mailService.sendSimpleMessageOrThrow(mailTitle, mailBody);
           }
        }
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

    public void reportSystemHealth(String name, double cpu, double memory, double disk) {
        var health = systemHealthMap.getOrDefault(name,
                new SystemHealth(name, MOVING_HEALTH_MAX_AGE_SEC, SYSTEM_HEALTH_CONFIDENCE));
        health.addCpuMeasure(cpu);
        health.addMemoryMeasure(memory);
        health.addDiskMeasure(disk);
        systemHealthMap.put(name, health);

        healthMonitor.monitorQuotas(health);
        lastHearthBeat.put(name, System.currentTimeMillis());

        HealthSnapshot snapshot = HealthSnapshot.builder()
                .system(name)
                .cpu(cpu)
                .memory(memory)
                .disk(disk)
                .timestamp(System.currentTimeMillis())
                .build();

        healthRepository.save(snapshot);
        messagingTemplate.convertAndSend(TOPIC_HEALTH+"/"+name, snapshot);
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
                            """, systemHealth.getSystemName(), MailService.timestampToDateString(firstViolation),
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
