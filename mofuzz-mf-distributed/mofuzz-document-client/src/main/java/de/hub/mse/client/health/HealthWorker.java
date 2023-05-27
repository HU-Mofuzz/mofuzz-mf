package de.hub.mse.client.health;

import de.hub.mse.client.backend.BackendConnector;
import de.hub.mse.client.backend.ReportingWorker;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class HealthWorker extends ReportingWorker {

    private static final int HEALTH_REPORT_INTERVAL_MS = 1000;

    private final BackendConnector connector;
    @SuppressWarnings("BusyWait")
    @Override
    protected void work() throws InterruptedException {
        while (!isStopped()) {
            var start = System.currentTimeMillis();
            connector.reportHealth(HealthReport.fromSystemState());
            var sleepTime = HEALTH_REPORT_INTERVAL_MS - (System.currentTimeMillis() - start);
            if(sleepTime > 0) {
                Thread.sleep(sleepTime);
            }
        }
    }
}
