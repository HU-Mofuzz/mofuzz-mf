package de.hub.mse.emf.multifile.util;

import com.beust.jcommander.JCommander;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import de.hub.mse.emf.multifile.GeneratorConfig;
import de.hub.mse.emf.multifile.PreparationMode;
import de.hub.mse.emf.multifile.impl.svg.SvgGenerator;
import de.hub.mse.emf.multifile.impl.svg.SvgUtil;
import de.hub.mse.emf.multifile.Args;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.stream.Collectors;

@UtilityClass
public class ArgUtil {

    @SneakyThrows
    public void parseArgs(Args argObject, String[] args) {
        var commander = JCommander.newBuilder()
                .expandAtSign(true)
                .addObject(argObject)
                .build();
        commander.parse(args);

        if(argObject.isHelp()) {
            commander.usage();
            return;
        }

        if(!argObject.isFailDirectorySet()) {
            argObject.setFailDirectory(Files.createTempDirectory("failes").toFile().getAbsolutePath());
        }
        if(!argObject.isWorkingDirectorySet()) {
            argObject.setWorkingDirectory(Files.createTempDirectory("svg_test").toFile().getAbsolutePath());
        }
        if(!argObject.isTestDirectorySet()) {
            argObject.setTestDirectory(Files.createTempDirectory("test").toFile().getAbsolutePath());
        }
        System.out.println("Fail directory: " + argObject.getFailDirectory());
        System.out.println("Working directory: " + argObject.getWorkingDirectory());
        System.out.println("Test directory: " + argObject.getTestDirectory());

        var config = GeneratorConfig.getInstance();
        config.setFilesToGenerate(argObject.getFilesToGenerate());
        config.setWorkingDirectory(argObject.getWorkingDirectory());
        config.setModelDepth(argObject.getModelDepth());
        config.setModelWidth(argObject.getModelWidth());
    }

    public void prepareFiles() {
        System.out.println("Preparing files...");
        var config  = GeneratorConfig.getInstance();
        config.setPreparationMode(PreparationMode.GENERATE_FILES);
        var generator = new SvgGenerator();
        generator.generate(new SourceOfRandomness(new Random()), null);
        var pool = generator.getLinkPool();
        config.setExistingFiles(pool.stream()
                .map(SvgUtil::getFilenameFromObjectId)
                .map(filename -> Paths.get(config.getWorkingDirectory(), filename).toString())
                .collect(Collectors.toList()));
        System.out.println("Preparation done!");
        config.setPreparationMode(PreparationMode.FILES_EXIST);
    }
}
