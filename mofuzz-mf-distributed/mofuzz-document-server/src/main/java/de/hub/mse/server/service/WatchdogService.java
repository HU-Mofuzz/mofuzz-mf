package de.hub.mse.server.service;

import de.hub.mse.server.management.ClientDescriptor;
import de.hub.mse.server.repository.ClientDescriptorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class WatchdogService {

    private static final int WATCHDOG_WARNING_INTERVAL_MIN = 10;

    private static final String WATCHDOG_KEY_HEALTH = "health";
    private static final String WATCHDOG_KEY_RESULT = "result";

    private final Map<String, Map<String, Long>> watchdogTimestamps = new HashMap<>();

    private final ClientDescriptorRepository clientRepository;
    private final MailService mailService;

    @Autowired
    public WatchdogService(ClientDescriptorRepository clientRepository, MailService mailService) {
        this.clientRepository = clientRepository;
        this.mailService = mailService;
    }

    public void resetHealthWatchdog(String id) {
        resetWatchDog(WATCHDOG_KEY_HEALTH, id);
    }

    public void resetResultWatchDog(String id) {
        resetWatchDog(WATCHDOG_KEY_RESULT, id);
    }

    private void resetWatchDog(String watchDogKey, String systemId) {
        if(!watchdogTimestamps.containsKey(watchDogKey)) {
            watchdogTimestamps.put(watchDogKey, new HashMap<>());
        }
        watchdogTimestamps.get(watchDogKey).put(systemId, System.currentTimeMillis());
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
    private void checkTimestamps() {
        var now = System.currentTimeMillis();
        Set<String> systemIdsToRemove = new HashSet<>();
        for(var watchdog : watchdogTimestamps.entrySet()) {
            for(var timestamp : watchdog.getValue().entrySet()) {
                if(timestamp.getValue() < now - TimeUnit.MINUTES.toMillis(WATCHDOG_WARNING_INTERVAL_MIN)) {
                    var clientName = clientRepository.findById(timestamp.getKey())
                            .map(ClientDescriptor::getName).orElse(timestamp.getKey());
                    mailService.sendSimpleMessage("[Mofuzz] Warning for client \""+clientName+"\"",
                            String.format("""
                                    The watchdog monitor detected a violation of the client "%s" since %s!
                                                                
                                    The client raised a "%s" watchdog warning after not reporting for %d minutes.
                                                                
                                    This may requires immediate action!
                                    """, clientName, MailService.timestampToDateString(timestamp.getValue()),
                                    watchdog.getKey(), WATCHDOG_WARNING_INTERVAL_MIN));
                    systemIdsToRemove.add(timestamp.getKey());
                }
            }
        }

        for(var watchdog : watchdogTimestamps.entrySet()) {
            systemIdsToRemove.forEach(id -> watchdog.getValue().remove(id));
        }
    }
}
