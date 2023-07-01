package de.hub.mse.client.experiment.execution;

import java.io.File;

public interface Application {

    boolean prepare() throws Exception;

    boolean isExecutionPrepared();

    int execute(File file, int height, int width, InterruptionHook hook) throws Exception;

    boolean shouldRetry(Exception exception);

    void cleanup();
}
