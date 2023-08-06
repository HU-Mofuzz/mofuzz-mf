package de.hub.mse.client.health;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

@Slf4j
public class UTHealthReport {

    @Test
    public void testFromSystemState() {
        var health = HealthReport.fromSystemState();

        log.info("{}", health);
        assertTrue("Disk metric should be greater 0", health.getDisk() >= 0f);
        assertTrue("Memory metric should be greater 0", health.getMemory() >= 0f);
        assertTrue("CPU metric should be greater 0", health.getCpu() >= 0f);
    }
}
