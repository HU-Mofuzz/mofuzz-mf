package de.hub.mse.emf.multifile;

import de.hub.mse.emf.multifile.base.GeneratorConfig;
import de.hub.mse.emf.multifile.base.PreparationMode;
import edu.berkeley.cs.jqf.fuzz.ei.ZestGuidance;
import edu.berkeley.cs.jqf.fuzz.junit.GuidedFuzzing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;

public class TestExecutor {

    static {
        System.setProperty("jqf.ei.MAX_INPUT_SIZE", Integer.toString(1048576));
    }

    public static void main(String[] args) throws IOException {
        final String workingDir = Files.createTempDirectory("svg_test").toFile().getAbsolutePath();
        final String testDirectory = Files.createTempDirectory("test").toFile().getAbsolutePath();
        final int filesToGenerate = 20;

        System.out.println("Working directory: "+workingDir);
        var config = GeneratorConfig.getInstance();
        config.setFilesToGenerate(filesToGenerate);
        config.setWorkingDirectory(workingDir);
        config.setPreparationMode(PreparationMode.GENERATE_FILES);

        GuidedFuzzing.run(SvgTest.class, "svgSalamanderTest", new ZestGuidance("svgSalamanderTest", Duration.ofSeconds(10), new File(testDirectory)), System.out);
    }
}
