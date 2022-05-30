package de.hub.mse.emf.multifile.base;

import edu.berkeley.cs.jqf.fuzz.ei.ZestGuidance;
import edu.berkeley.cs.jqf.fuzz.guidance.GuidanceException;
import edu.berkeley.cs.jqf.fuzz.guidance.Result;
import lombok.Setter;
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

    @Setter
    private InputStream input = null;

    public DocumentAwareGraphAwareGuidance(String testName, Duration duration, Long trials, File outputDirectory, DocumentAwareResultListener listener) throws IOException {
        super(testName, duration, trials, outputDirectory, new Random());
        this.listener = listener;
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

    @Override
    public InputStream getInput() throws GuidanceException {
        if(input == null) {
            return super.getInput();
        } else {
            return input;
        }
    }

    @Override
    public void handleResult(Result result, Throwable error) throws GuidanceException {
        super.handleResult(result, error);
        if(listener != null) {
            this.listener.handleResultForGeneratedArgs(lastArgs, result, error);
        }
    }

    @FunctionalInterface
    public interface DocumentAwareResultListener {
        void handleResultForGeneratedArgs(Object[] generated, Result result, Throwable error);
    }
}
