package de.hub.mse.emf.multifile;

import de.hub.mse.emf.multifile.base.DocumentAwareGraphAwareGuidance;
import de.hub.mse.emf.multifile.base.GeneratorConfig;
import de.hub.mse.emf.multifile.base.PreparationMode;
import de.hub.mse.emf.multifile.impl.svg.SvgUtil;
import de.hub.mse.emf.multifile.util.RunDescriptor;
import de.hub.mse.emf.multifile.util.XmlUtil;
import edu.berkeley.cs.jqf.fuzz.guidance.Result;
import edu.berkeley.cs.jqf.fuzz.junit.GuidedFuzzing;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

public class TestExecutor {

    private static final String failDirectory;
    private static Set<String> stackTraces = new HashSet<>();

    static {
        System.setProperty("jqf.ei.MAX_INPUT_SIZE", Integer.toString(2 << 23));

        try {
            failDirectory = Files.createTempDirectory("failes").toFile().getAbsolutePath();
        } catch (IOException e) {
            throw new Error("Executor init failed to create fail directory");
        }
    }

    public static void main(String[] args) throws IOException {
        final String workingDir = Files.createTempDirectory("svg_test").toFile().getAbsolutePath();
        final String testDirectory = Files.createTempDirectory("test").toFile().getAbsolutePath();
        final int filesToGenerate = 5;

        System.out.println("Working directory: "+workingDir);
        var config = GeneratorConfig.getInstance();
        config.setFilesToGenerate(filesToGenerate);
        config.setWorkingDirectory(workingDir);
        config.setModelDepth(2);
        config.setModelWidth(5);
        config.setPreparationMode(PreparationMode.GENERATE_FILES);

        GuidedFuzzing.run(SvgTest.class, "svgSalamanderTest",
                new DocumentAwareGraphAwareGuidance("svgSalamanderTest", Duration.ofSeconds(60), new File(testDirectory), TestExecutor::handleResult), System.out);
    }

    private static void handleResult(Object[] files, Result result, Throwable throwable) {
        if(result != Result.FAILURE && files.length == 1 && files[0] instanceof File) {
            File mainFile = (File) files[0];
            String name;
            String stackTrace;
            if(throwable == null) {
                name = "NONE";
                stackTrace = StringUtils.EMPTY;
            } else {
                name = throwable.getClass().getName();
                stackTrace = throwableToStacktraceText(throwable);
            }
            if(!stackTraces.contains(stackTrace)) {
               if(stackTrace.length() > 0) {
                   stackTraces.add(stackTrace);
               }
               File errorDirectory = Paths.get(failDirectory, name).toFile();
               String subDir;
               if(errorDirectory.exists()) {
                   subDir = Integer.toString(FileUtils.listFiles(errorDirectory, FalseFileFilter.INSTANCE, DirectoryFileFilter.INSTANCE).size());
               } else {
                   errorDirectory.mkdir();
                   subDir = "0";
               }
               Paths.get(failDirectory, name, subDir).toFile().mkdir();

               // create descriptor
                var descriptor = new RunDescriptor();
                var linkedFiles = SvgUtil.extractUseLinksRecursive(mainFile);
                descriptor.setFiles(linkedFiles);
                descriptor.setMainFile(mainFile.getName());
                descriptor.addProperty("exception", name);

                String descriptorText = XmlUtil.documentToString(descriptor.toXML());
                try {
                    Files.writeString(Paths.get(failDirectory, name, subDir, "descriptor.xml"), descriptorText);
                    // copy file tree
                    Paths.get(failDirectory, name, subDir, "files").toFile().mkdir();
                    for(String link : descriptor.getFiles()) {
                        Files.copy(Paths.get(mainFile.getParent(), link), Paths.get(failDirectory, name, subDir, "files", link));
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
