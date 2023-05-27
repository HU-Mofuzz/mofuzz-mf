package de.hub.mse.client.backend;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public abstract class ReportingWorker implements Runnable {

    private boolean stopped;

    protected abstract void work() throws InterruptedException ;
    @Override
    public void run() {
        try {
            work();
        } catch (InterruptedException e) {
            log.error("Interrupted while waiting to work!", e);
            Thread.currentThread().interrupt();
        }
    }
}
