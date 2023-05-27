package de.hub.mse.client.result;

import de.hub.mse.client.backend.BackendConnector;
import de.hub.mse.client.backend.ReportingWorker;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
public class ResultWorker extends ReportingWorker {
    private final BlockingQueue<ExecutionResult> resultQueue = new LinkedBlockingQueue<>();
    private final BackendConnector connector;

    public ResultWorker(BackendConnector connector) {
        this.connector = connector;
    }

    public void report(ExecutionResult result) {
        resultQueue.add(result);
    }

    @Override
    protected void work() throws InterruptedException {
        while (!isStopped()) {
            connector.reportResult(resultQueue.take());
            log.info("Reported result, {} results waiting", resultQueue.size());
        }
    }
}
