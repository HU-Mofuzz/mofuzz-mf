package de.hub.mse.client.experiment.execution;

import java.util.concurrent.CountDownLatch;

public class InterruptionHook {
    private boolean raised = false;

    private final CountDownLatch latch = new CountDownLatch(1);

    public boolean isRaised() {
        return raised;
    }

    public CountDownLatch interrupt() {
        this.raised = true;
        return latch;
    }

    public void accept() {
        latch.countDown();
    }
}
