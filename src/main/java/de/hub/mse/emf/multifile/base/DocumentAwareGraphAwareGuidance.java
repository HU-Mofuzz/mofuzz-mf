package de.hub.mse.emf.multifile.base;

import edu.berkeley.cs.jqf.fuzz.ei.ZestGuidance;
import edu.berkeley.cs.jqf.fuzz.guidance.GuidanceException;
import edu.berkeley.cs.jqf.fuzz.guidance.Result;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Random;

@Slf4j
public class DocumentAwareGraphAwareGuidance extends ZestGuidance {
    private final DocumentAwareResultListener listener;
    private Object[] lastArgs;

    private final Random random = new Random();
    private final InputStream randomStream = new InputStream() {
        @Override
        public int read() throws IOException {
            return random.nextInt();
        }
    };

    public DocumentAwareGraphAwareGuidance(String testName, Duration duration, File outputDirectory, DocumentAwareResultListener listener) throws IOException {
        super(testName, duration, outputDirectory);
        this.listener = listener;
    }

    public DocumentAwareGraphAwareGuidance(String testName, Duration duration, File outputDirectory, File[] seedInputFiles, DocumentAwareResultListener listener) throws IOException {
        super(testName, duration, outputDirectory, seedInputFiles);
        this.listener = listener;
    }

    public DocumentAwareGraphAwareGuidance(String testName, Duration duration, File outputDirectory, File seedInputDir, DocumentAwareResultListener listener) throws IOException {
        super(testName, duration, outputDirectory, seedInputDir);
        this.listener = listener;
    }

    @Override
    public void observeGeneratedArgs(Object[] args) {
        this.lastArgs = args;

    }

    @Override
    public void handleResult(Result result, Throwable error) throws GuidanceException {
        super.handleResult(result, error);
        if(listener != null) {
            this.listener.handleResultForGeneratedArgs(lastArgs, result, error);
        }
    }

    @Override
    public boolean hasInput() {
        return super.hasInput();
    }

    @Override
    public InputStream getInput() throws GuidanceException {
        super.getInput();
        return randomStream;
    }

    @FunctionalInterface
    public interface DocumentAwareResultListener {
        void handleResultForGeneratedArgs(Object[] generated, Result result, Throwable error);
    }
}
