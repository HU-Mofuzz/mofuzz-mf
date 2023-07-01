package de.hub.mse.client;

import com.beust.jcommander.JCommander;
import de.hub.mse.client.backend.BackendConnector;
import de.hub.mse.client.config.Config;
import de.hub.mse.client.experiment.ExecutionWorker;
import de.hub.mse.client.files.AwsFileAccessor;
import de.hub.mse.client.files.FileCache;
import de.hub.mse.client.health.HealthWorker;
import de.hub.mse.client.util.ApplicationUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class MofuzzDocumentClientApplication {

    public static final int THREAD_POOL_COUNT = 8;

    public static final Config CONFIG = new Config();

    private static File cacheDir;

    private static final ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_COUNT);

    private static void parseConfig(String[] args) {
        var commander = JCommander.newBuilder()
                .expandAtSign(true)
                .addObject(CONFIG)
                .build();
        commander.parse(args);

        if(CONFIG.isHelp()) {
            commander.usage();
            System.exit(0);
        }
        CONFIG.validate();
    }

    private static void prepareWorkspace() throws IOException {
        File workingDir = CONFIG.getWorkingDirAsFile();
        if(!workingDir.exists() && !workingDir.mkdirs()) {
            throw new IllegalStateException("Unable to create working directory!");
        }
        if(!workingDir.isDirectory()) {
            throw new IllegalStateException("Working directory argument must point to directory");
        }
        FileUtils.cleanDirectory(workingDir);

        cacheDir = Paths.get(workingDir.getAbsolutePath(), "cache").toFile();
        if(!cacheDir.mkdir()) {
            throw new IllegalStateException("Unable to create cache directory at "+cacheDir.getAbsolutePath());
        }
    }

    public static void main(String[] args) {
        parseConfig(args);
        log.info("Preparing workspace...");
        try {
            prepareWorkspace();
        } catch (IOException e) {
            log.error("Error preparing workspace!");
            System.exit(1);
        }
        FileCache cache = new FileCache(cacheDir, new AwsFileAccessor());

        log.info("Validating client id...");
        BackendConnector connector = new BackendConnector();
        connector.validateClientId();

        log.info("Preparing application...");
        var application = ApplicationUtil.getApplicationForType(CONFIG.getClientType());
        try {
            if(!application.prepare()) {
                throw new IllegalStateException("Unable to prepare application!");
            }
            Runtime.getRuntime().addShutdownHook(new Thread(application::cleanup));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to prepare application!", e);
        }

        log.info("Starting worker...");
        executor.submit(new HealthWorker(connector));
        executor.submit(new ExecutionWorker(connector, cache, application));

    }
}
