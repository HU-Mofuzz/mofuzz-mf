package de.hub.mse.client.experiment;

import de.hub.mse.client.backend.BackendConnector;
import de.hub.mse.client.backend.ReportingWorker;
import de.hub.mse.client.experiment.execution.Application;
import de.hub.mse.client.experiment.execution.InterruptionHook;
import de.hub.mse.client.files.FileCache;
import de.hub.mse.client.result.ExecutionResult;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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

    private Set<String> previousFileSet = Collections.emptySet();

    public ExecutionWorker(BackendConnector connector, FileCache cache, Application application) {
        this.backendConnector = connector;
        this.cache = cache;
        this.application = application;
    }


    private void prepareWorkingDirectory(Set<String> fileSet) {
        Set<String> filesToCache;

        // delete all files that where in the previous but are not in the new one
        Set<String> filesToDelete = previousFileSet.stream()
                .filter(id -> !fileSet.contains(id))
                .collect(Collectors.toSet());

        // cache all the files, are in the new set, but were not in the previous one
        filesToCache = fileSet.stream()
                .filter(id -> !previousFileSet.contains(id))
                .collect(Collectors.toSet());

        AtomicInteger deletedFiles = new AtomicInteger();
        filesToDelete.forEach(id -> preparationPool.submit(() -> {
            log.info("Deleted {}/{}", deletedFiles.incrementAndGet(), filesToDelete.size());
            Paths.get(CONFIG.getWorkingDirectory(), FileCache.keyToFilename(id)).toFile().delete();
        }));

        List<CompletableFuture<?>> futures = new ArrayList<>();
        AtomicBoolean exceptionOccured = new AtomicBoolean();
        AtomicInteger loadedFiles = new AtomicInteger();
        filesToCache.forEach(id ->
            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    var start = System.currentTimeMillis();
                    if(exceptionOccured.get()) {
                        return;
                    }
                    cache.loadAndCopy(id, CONFIG.getWorkingDirectory());
                    log.info("Prepared {}/{} (took {}ms)", loadedFiles.incrementAndGet(),
                            filesToCache.size(), (System.currentTimeMillis() - start));
                } catch (IOException e) {
                    exceptionOccured.set(true);
                }
            }, preparationPool)));

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).get();
        } catch (InterruptedException e) {
            exceptionOccured.set(true);
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            exceptionOccured.set(true);
        }
        if(exceptionOccured.get()) {
            throw new IllegalStateException("Unable to prepare fileset!");
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
                    prepareWorkingDirectory(response.getFileSet());
                    previousFileSet = response.getFileSet();
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
