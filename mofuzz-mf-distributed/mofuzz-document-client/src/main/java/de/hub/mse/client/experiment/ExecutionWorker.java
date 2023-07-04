package de.hub.mse.client.experiment;

import de.hub.mse.client.backend.BackendConnector;
import de.hub.mse.client.backend.ReportingWorker;
import de.hub.mse.client.experiment.execution.Application;
import de.hub.mse.client.experiment.execution.InterruptionHook;
import de.hub.mse.client.files.FileCache;
import de.hub.mse.client.result.ExecutionResult;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOExceptionList;
import org.apache.commons.io.function.IOConsumer;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.hub.mse.client.MofuzzDocumentClientApplication.CONFIG;

@Slf4j
@AllArgsConstructor
public class ExecutionWorker extends ReportingWorker {

    private static final int NO_RESPONSE_WAIT_MS = 30000;
    private static final int MAX_RETRIES = 3;

    private final BackendConnector backendConnector;
    private final FileCache cache;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final Application application;


    private void prepareWorkingDirectory(Set<String> fileSet) {

        try {
            IOConsumer.forAll(FileUtils::forceDelete,
                    CONFIG.getWorkingDirAsFile().listFiles(f -> !f.isDirectory()));
        } catch (IOExceptionList e) {
            throw new IllegalStateException("Unable to clean working directory", e);
        }

        try {
            for(String id : fileSet) {
                cache.loadAndCopy(id, CONFIG.getWorkingDirectory());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to prepare fileset", e);
        }
    }

    private void reInitialize() {
        try {
            application.cleanup();
        } catch (Exception e) {}
        try {
            application.prepare();
        } catch (Exception e) {
            throw new RuntimeException("Application unrecoverable!", e);
        }
    }


    private ExecutionResult coreExecute(DescriptorResponse response, String previousFile) throws InterruptedException {
        long start = System.currentTimeMillis();
        var executionResult = ExecutionResult.builder()
                .originClient(CONFIG.getClientId())
                .experiment(response.getDescriptor().getExperiment())
                .fileDescriptor(response.getDescriptor().getId())
                .previousFile(previousFile)
                .timestamp(start)
                .build();
        var hook = new InterruptionHook();
        try {
            var retry = 0;
            AtomicBoolean shouldRetry = new AtomicBoolean(false);
            do {
                executionResult.setException(null);
                executionResult.setCrash(false);
                executor.submit(() -> {
                    try {
                        executionResult.setErrorCount(application.execute(
                                cache.loadAndCopy(response.getDescriptor().getId(), CONFIG.getWorkingDirectory()),
                                response.getHeight(), response.getWidth(), hook
                        ));
                    } catch (Exception e) {
                        executionResult.setException(e.toString());
                        executionResult.setCrash(true);
                        log.error("Exception while executing experiment!", e);
                        shouldRetry.set(application.shouldRetry(e));
                    }
                }).get(response.getTimeout(), TimeUnit.MILLISECONDS);
            } while (++retry < MAX_RETRIES && shouldRetry.get());
        } catch (Exception e) {
            var isHang = e instanceof TimeoutException;
            executionResult.setException(e.toString());
            executionResult.setCrash(!isHang);
            executionResult.setHang(isHang);
            if(isHang) {
                log.info("Execution reached timeout, continuing...");
                if(!hook.interrupt().await(10, TimeUnit.MILLISECONDS)) {
                    reInitialize();
                }
            } else {
                log.error("Exception while executing experiment!", e);
            }
        } finally {
            executionResult.setDuration(System.currentTimeMillis() - start);
        }

        if(executionResult.isCrash()) {
            reInitialize();
        }
        return executionResult;
    }


    @Override
    protected void work() throws InterruptedException {
        String previousFile = null;
        while (true) {
            log.info("Loading next file");
            var response = backendConnector.getNextFileDescriptor();
            boolean unableToPrepare = false;
            if(response == null) {
                log.info("Got empty response, will retry later...");
                unableToPrepare = true;
            } else {
                log.info("Preparing workspace");
                prepareWorkingDirectory(response.getFileSet());
            }
            if(unableToPrepare || !application.isExecutionPrepared()) {
                log.error("Unable to prepare execution, trying again later...");
                Thread.sleep(NO_RESPONSE_WAIT_MS);
                continue;
            }
            log.info("Entering execution phase");
            backendConnector.reportResult(coreExecute(response, previousFile));
            previousFile = response.getDescriptor().getId();
        }
    }
}