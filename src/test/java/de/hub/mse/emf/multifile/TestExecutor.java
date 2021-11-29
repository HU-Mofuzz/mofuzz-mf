package de.hub.mse.emf.multifile;

import de.hub.mse.emf.multifile.base.GeneratorConfig;
import de.hub.mse.emf.multifile.base.PreparationMode;
import edu.berkeley.cs.jqf.fuzz.ei.ZestGuidance;
import edu.berkeley.cs.jqf.fuzz.guidance.Guidance;
import edu.berkeley.cs.jqf.fuzz.guidance.GuidanceException;
import edu.berkeley.cs.jqf.fuzz.guidance.Result;
import edu.berkeley.cs.jqf.fuzz.junit.GuidedFuzzing;
import edu.berkeley.cs.jqf.instrument.tracing.events.TraceEvent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Date;
import java.util.Random;
import java.util.function.Consumer;

public class TestExecutor {

    public static void main(String[] args) throws IOException {
        final String workingDir = Files.createTempDirectory("svg_test").toFile().getAbsolutePath();
        final String testDirectory = Files.createTempDirectory("test").toFile().getAbsolutePath();
        final int filesToGenerate = 20;

        System.out.println("Working directory: "+workingDir);
        var config = GeneratorConfig.getInstance();
        config.setObjectsToGenerate(filesToGenerate);
        config.setWorkingDirectory(workingDir);
        config.setPreparationMode(PreparationMode.GENERATE_FILES);

        GuidedFuzzing.run(SvgTest.class, "testBatikTranscoder", new MyGuidance("testBatikTranscoder", Duration.ofSeconds(10), new File(testDirectory)), System.out);
    }

    private static class MyGuidance implements Guidance {

        private final MyIS is = new MyIS();
        private String testName;
        private Duration duration;
        private File outputDirectory;

        protected long endTime = -1;

        /**
         * Creates a new guidance instance.
         *
         * @param testName        the name of test to display on the status screen.
         * @param duration        the amount of time to run fuzzing for, where
         *                        {@code null} indicates unlimited time.
         * @param outputDirectory the directory where fuzzing results will be written.
         * @throws IOException if the output directory could not be prepared
         */
        public MyGuidance(String testName, Duration duration, File outputDirectory) throws IOException {
            this.testName = testName;
            this.duration = duration;
            this.outputDirectory = outputDirectory;
        }

        @Override
        public InputStream getInput() throws IllegalStateException, GuidanceException {
            return is;
        }

        @Override
        public boolean hasInput() {
            if(endTime == -1) {
                endTime = (new Date()).getTime() + (duration.getSeconds() * 1000);
            }
            var now = new Date();
            return now.getTime() < endTime;
        }

        @Override
        public void handleResult(Result result, Throwable error) throws GuidanceException {

        }

        @Override
        public Consumer<TraceEvent> generateCallBack(Thread thread) {
            return null;
        }
    }

    private static class MyIS extends InputStream {

        private final Random random = new Random();

        @Override
        public int read() throws IOException {
            return random.nextInt();
        }
    }
}
