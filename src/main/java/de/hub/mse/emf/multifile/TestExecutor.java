package de.hub.mse.emf.multifile;

import com.beust.jcommander.JCommander;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import de.hub.mse.emf.multifile.base.DocumentAwareGraphAwareGuidance;
import de.hub.mse.emf.multifile.base.GeneratorConfig;
import de.hub.mse.emf.multifile.base.PreparationMode;
import de.hub.mse.emf.multifile.impl.svg.SvgGenerator;
import de.hub.mse.emf.multifile.impl.svg.SvgUtil;
import de.hub.mse.emf.multifile.util.RunDescriptor;
import de.hub.mse.emf.multifile.util.XmlUtil;
import edu.berkeley.cs.jqf.fuzz.ei.ZestGuidance;
import edu.berkeley.cs.jqf.fuzz.guidance.Result;
import edu.berkeley.cs.jqf.fuzz.junit.GuidedFuzzing;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class TestExecutor {

    public static Args ARGS = new Args();
    private static Set<String> stackTraces = new HashSet<>();
    private static Map<String, Integer> nameCountMap = new HashMap<>();

    public static void main(String[] args) throws IOException {
        System.setProperty("jqf.ei.MAX_INPUT_SIZE", "10000000");
        var commander = JCommander.newBuilder()
                .addObject(ARGS)
                .build();
        commander.parse(args);

        if (ARGS.isHelp()) {
            commander.usage();
            return;
        }

        if (!ARGS.isFailDirectorySet()) {
            ARGS.setFailDirectory(Files.createTempDirectory("failes").toFile().getAbsolutePath());
        }
        if (!ARGS.isWorkingDirectorySet()) {
            ARGS.setWorkingDirectory(Files.createTempDirectory("svg_test").toFile().getAbsolutePath());
        }
        if (!ARGS.isTestDirectorySet()) {
            ARGS.setTestDirectory(Files.createTempDirectory("test").toFile().getAbsolutePath());
        }
        System.out.println("Fail directory: " + ARGS.getFailDirectory());
        System.out.println("Working directory: " + ARGS.getWorkingDirectory());
        System.out.println("Test directory: " + ARGS.getTestDirectory());

        var config = GeneratorConfig.getInstance();
        config.setFilesToGenerate(ARGS.getFilesToGenerate());
        config.setWorkingDirectory(ARGS.getWorkingDirectory());
        config.setModelDepth(ARGS.getModelDepth());
        config.setModelWidth(ARGS.getModelWidth());
        config.setLinkProbability(ARGS.getLinkProbability());
        config.setLinkNumber(ARGS.getLinkNumber());
        config.setPreparationMode(PreparationMode.GENERATE_FILES);

        if (config.shouldGenerateLinks()) {
            System.out.println("Preparing files...");
            var generator = new SvgGenerator();
            generator.generate(new SourceOfRandomness(new Random()), null);
            var pool = generator.getLinkPool();
            config.setExistingFiles(pool.stream()
                    .map(SvgUtil::getFilenameFromObjectId)
                    .map(filename -> Paths.get(config.getWorkingDirectory(), filename).toString())
                    .collect(Collectors.toList()));
            System.out.println("Preparation done!");
        } else {
            System.out.println("Run without linked files!");
        }
        config.setPreparationMode(PreparationMode.FILES_EXIST);

        try {
             /**
             GuidedFuzzing.run(XmlTest.class, ARGS.getTestMethod(),
             new ZestGuidance(ARGS.getTestMethod(), Duration.ofMinutes(ARGS.getDurationMinutes()),  new File(ARGS.getTestDirectory())), System.out);
              **/
            GuidedFuzzing.run(XmlTest.class, ARGS.getTestMethod(),
                    new DocumentAwareGraphAwareGuidance(ARGS.getTestMethod(),
                            Duration.ofMinutes(ARGS.getDurationMinutes()),
                            null,
                            new File(ARGS.getTestDirectory()),
                            TestExecutor::handleXMLResult),
                    System.out);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static void handleXMLResult(Object[] files, Result result, Throwable throwable) throws IOException {
        if (result == Result.FAILURE && files.length == 1 && files[0] instanceof Document) {
            var doc = (Document) files[0];
            String name;
            String stackTrace;
            if (throwable == null) {
                name = "NONE";
                stackTrace = StringUtils.EMPTY;
            } else {
                name = throwable.getClass().getName();
                stackTrace = throwableToStacktraceText(throwable);
            }
            if (!stackTraces.contains(stackTrace)) {
                if (stackTrace.length() > 0) {
                    stackTraces.add(stackTrace);
                }
                File errorDirectory = Paths.get(ARGS.getFailDirectory(), name).toFile();
                if (!errorDirectory.exists()) {
                    errorDirectory.mkdir();
                }
                // sub dir
                var count = nameCountMap.getOrDefault(name, 0);
                nameCountMap.put(name, count + 1);
                String subDir = Integer.toString(count);
                Paths.get(ARGS.getFailDirectory(), name, subDir).toFile().mkdir();

                // create descriptor
                var descriptor = new RunDescriptor();

                String content = XmlUtil.documentToString(doc);

                // create file
                Paths.get(ARGS.getFailDirectory(), name, subDir, "files").toFile().mkdir();

                Path p = Paths.get(ARGS.getFailDirectory(), name, subDir, "files", UUID.randomUUID() + ".svg");
                Files.write(p, content.lines().collect(Collectors.toList()));

                descriptor.setMainFile(p.toFile().getAbsolutePath());
                descriptor.setThrowable(throwable);

                String descriptorText = XmlUtil.documentToString(descriptor.toXML());
                try {
                    Files.writeString(Paths.get(ARGS.getFailDirectory(), name, subDir, "descriptor.xml"), descriptorText);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private static void handleResult(Object[] files, Result result, Throwable throwable) {
        if (result == Result.FAILURE && files.length == 1 && files[0] instanceof File) {
            var mainFile = (File) files[0];
            String name;
            String stackTrace;
            if (throwable == null) {
                name = "NONE";
                stackTrace = StringUtils.EMPTY;
            } else {
                name = throwable.getClass().getName();
                stackTrace = throwableToStacktraceText(throwable);
            }
            if (!stackTraces.contains(stackTrace)) {
                if (stackTrace.length() > 0) {
                    stackTraces.add(stackTrace);
                }
                File errorDirectory = Paths.get(ARGS.getFailDirectory(), name).toFile();
                if (!errorDirectory.exists()) {
                    errorDirectory.mkdir();
                }
                // sub dir
                var count = nameCountMap.getOrDefault(name, 0);
                nameCountMap.put(name, count + 1);
                String subDir = Integer.toString(count);
                Paths.get(ARGS.getFailDirectory(), name, subDir).toFile().mkdir();

                // create descriptor
                var descriptor = new RunDescriptor();
                var linkedFiles = SvgUtil.extractUseLinksRecursive(mainFile);
                descriptor.setFiles(linkedFiles);
                descriptor.setMainFile(mainFile.getName());
                descriptor.setThrowable(throwable);

                String descriptorText = XmlUtil.documentToString(descriptor.toXML());
                try {
                    Files.writeString(Paths.get(ARGS.getFailDirectory(), name, subDir, "descriptor.xml"), descriptorText);
                    // copy file tree
                    Paths.get(ARGS.getFailDirectory(), name, subDir, "files").toFile().mkdir();
                    for (String link : descriptor.getFiles()) {
                        var src = Paths.get(mainFile.getParent(), link);
                        if (src.toFile().exists()) {
                            Files.copy(src, Paths.get(ARGS.getFailDirectory(), name, subDir, "files", link));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static String throwableToStacktraceText(Throwable throwable) {
        var writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }
}
