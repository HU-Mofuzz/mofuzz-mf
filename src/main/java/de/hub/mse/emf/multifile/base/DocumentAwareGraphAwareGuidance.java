package de.hub.mse.emf.multifile.base;

import de.hub.mse.emf.multifile.base.fuzz.ModelGenerationStatus;
import edu.berkeley.cs.jqf.fuzz.ei.ZestGuidance;
import edu.berkeley.cs.jqf.fuzz.guidance.GuidanceException;
import edu.berkeley.cs.jqf.fuzz.guidance.Result;
import edu.berkeley.cs.jqf.fuzz.guidance.TimeoutException;
import edu.berkeley.cs.jqf.fuzz.util.Coverage;
import edu.berkeley.cs.jqf.instrument.tracing.events.BranchEvent;
import edu.berkeley.cs.jqf.instrument.tracing.events.TraceEvent;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Date;
import java.util.Random;
import java.util.function.Consumer;

@Slf4j
public class DocumentAwareGraphAwareGuidance extends ZestGuidance {
    private final DocumentAwareResultListener listener;
    private ModelGenerationStatus generationStatus;
    private Object[] lastArgs;

    private final Random random = new Random();
    private final InputStream randomStream = new InputStream() {
        @Override
        public int read() throws IOException {
            return random.nextInt();
        }
    };

    public DocumentAwareGraphAwareGuidance(String testName, Duration duration, Long trials, File outputDirectory, DocumentAwareResultListener listener) throws IOException {
        super(testName, duration, trials, outputDirectory, new Random());
        this.listener = listener;
        //this.runCoverage =
        //this.totalCoverage =
        //this.validCoverage =
    }

    public DocumentAwareGraphAwareGuidance(String testName, Duration duration, Long trials, File outputDirectory, File seedInputDir, DocumentAwareResultListener listener) throws IOException {
        super(testName, duration, trials, outputDirectory, seedInputDir, new Random());
        this.listener = listener;
    }

    public DocumentAwareGraphAwareGuidance(String testName, Duration duration, File outputDirectory, File[] seedInput, DocumentAwareResultListener listener) throws IOException {
        super(testName, duration, outputDirectory, seedInput);
        this.listener = listener;
    }

    @Override
    public void observeGeneratedArgs(Object[] args) {
        this.lastArgs = args;
    }


    @SneakyThrows
    @Override
    public void handleResult(Result result, Throwable error) throws GuidanceException {
        super.handleResult(result, error);
        if (listener != null) {
            this.listener.handleResultForGeneratedArgs(lastArgs, result, error);
        }
    }

    @FunctionalInterface
    public interface DocumentAwareResultListener {
        void handleResultForGeneratedArgs(Object[] generated, Result result, Throwable error) throws IOException;
    }

    public void setGenStatus(ModelGenerationStatus genstatus) {
        generationStatus = genstatus;
    }

}
