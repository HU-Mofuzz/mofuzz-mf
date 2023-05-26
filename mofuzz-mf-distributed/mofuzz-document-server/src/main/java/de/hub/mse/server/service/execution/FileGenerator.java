package de.hub.mse.server.service.execution;

import de.hub.mse.server.management.Experiment;
import de.hub.mse.server.management.FileDescriptor;

public interface FileGenerator {

    void prepareExecution(Experiment experiment) throws Exception;

    FileDescriptor generateFileBlocking(Experiment experiment);

    void generateBatch(int batchSize, Experiment experiment);

    void reIndexPoolOfExperiment(Experiment experiment);
}
