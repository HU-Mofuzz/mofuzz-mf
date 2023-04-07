package de.hub.mse.emf.multifile;

import de.hub.mse.emf.multifile.util.ArgUtil;
import de.hub.mse.emf.multifile.util.FailHandler;
import edu.berkeley.cs.jqf.fuzz.junit.GuidedFuzzing;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

public class TestExecutor {

    public static Args ARGS = new Args();

    public static void main(String[] args) throws IOException {
        ArgUtil.parseArgs(ARGS, args);
        ArgUtil.prepareFiles();
        FailHandler handler = new FailHandler(ARGS);
        try {
            GuidedFuzzing.run(SvgTest.class, ARGS.getTestMethod(),
                    new DocumentAwareGraphAwareGuidance(ARGS.getTestMethod(), Duration.ofMinutes(ARGS.getDurationMinutes()), null, new File(ARGS.getTestDirectory()), handler), System.out);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
