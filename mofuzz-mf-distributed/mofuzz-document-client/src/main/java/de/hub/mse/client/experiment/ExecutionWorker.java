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
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static de.hub.mse.client.MofuzzDocumentClientApplication.CONFIG;

@Slf4j
@AllArgsConstructor
public class ExecutionWorker extends ReportingWorker {


    private static final int NO_RESPONSE_WAIT_MS = 30000;
    private static final int MAX_RETRIES = 3;

    private static final int PARALLEL_PREPARE = 10;
    private final ExecutorService preparationPool = Executors.newFixedThreadPool(PARALLEL_PREPARE);

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

        List<CompletableFuture<?>> futures = new ArrayList<>();
        AtomicBoolean exceptionOccured = new AtomicBoolean();
        AtomicInteger loadedFiles = new AtomicInteger();
        List<Exception> exceptions = new ArrayList<>();
        for(String id : fileSet) {
            futures.add(
                    CompletableFuture.runAsync(() -> {
                        try {
                            var start = System.currentTimeMillis();
                            if(exceptionOccured.get()) {
                                return;
                            }
                            cache.loadAndCopy(id, CONFIG.getWorkingDirectory());
                            log.info("Prepared {}/{} (took {}ms)", loadedFiles.incrementAndGet(),
                                    fileSet.size(), (System.currentTimeMillis() - start));
                        } catch (IOException e) {
                            exceptionOccured.set(true);
                            exceptions.add(e);
                        }
                    }, preparationPool));
        }
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).get();
        } catch (InterruptedException e) {
            exceptionOccured.set(true);
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            exceptionOccured.set(true);
        }
        if(exceptionOccured.get()) {
            if(exceptions.isEmpty()) {
                throw new IllegalStateException("Unable to prepare fileset!");
            } else {
                throw new IllegalStateException("Unable to prepare fileset!", exceptions.get(0));
            }
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
                hook.interrupt().await();
            } else {
                log.error("Exception while executing experiment!", e);
            }
        } finally {
            executionResult.setDuration(System.currentTimeMillis() - start);
        }

        if(executionResult.isCrash() || executionResult.isHang()) {
            reInitialize();
        }
        return executionResult;
    }


    @Override
    protected void work() throws InterruptedException {

        String previousFile = null;
        while (true) {
            try {
                log.info("Loading next file");
                var response = backendConnector.getNextFileDescriptor();
                boolean unableToPrepare = false;
                if(response == null) {
                    log.info("Got empty response, will retry later...");
                    unableToPrepare = true;
                } else {
                    log.info("Preparing workspace");
                    if(response.getFileSet() == null) {
                        prepareWorkingDirectory(Collections.emptySet());
                    } else {
                        prepareWorkingDirectory(response.getFileSet());
                    }
                }
                if(unableToPrepare || !application.isExecutionPrepared()) {
                    log.error("Unable to prepare execution, trying again later...");
                    Thread.sleep(NO_RESPONSE_WAIT_MS);
                    continue;
                }
                log.info("Entering execution phase");
                backendConnector.reportResult(coreExecute(response, previousFile));
                previousFile = response.getDescriptor().getId();
            } catch (Exception e) {
                log.error("Uncaught exception while executing!", e);
                reInitialize();
            }
        }
    }
}
