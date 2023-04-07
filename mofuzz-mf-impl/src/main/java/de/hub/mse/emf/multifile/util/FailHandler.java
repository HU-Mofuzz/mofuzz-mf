package de.hub.mse.emf.multifile.util;

import de.hub.mse.emf.multifile.DocumentAwareGraphAwareGuidance;
import de.hub.mse.emf.multifile.impl.svg.SvgUtil;
import de.hub.mse.emf.multifile.Args;
import edu.berkeley.cs.jqf.fuzz.guidance.Result;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@AllArgsConstructor
public class FailHandler implements DocumentAwareGraphAwareGuidance.DocumentAwareResultListener {

    private final Args argObject;
    private final Set<String> stackTraces = new HashSet<>();
    private final Map<String, Integer> nameCountMap = new HashMap<>();

    public void handleResultForGeneratedArgs(Object[] files, Result result, Throwable throwable) {
        if(result == Result.FAILURE && files.length == 1 && files[0] instanceof File) {
            var mainFile = (File) files[0];
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
                File errorDirectory = Paths.get(argObject.getFailDirectory(), name).toFile();
                if (!errorDirectory.exists()) {
                    errorDirectory.mkdir();
                }
                // sub dir
                var count = nameCountMap.getOrDefault(name, 0);
                nameCountMap.put(name, count+1);
                String subDir = Integer.toString(count);
                Paths.get(argObject.getFailDirectory(), name, subDir).toFile().mkdir();

                // create descriptor
                var descriptor = new RunDescriptor();
                var linkedFiles = SvgUtil.extractUseLinksRecursive(mainFile);
                descriptor.setFiles(linkedFiles);
                descriptor.setMainFile(mainFile.getName());
                descriptor.setThrowable(throwable);

                String descriptorText = XmlUtil.documentToString(descriptor.toXML());
                try {
                    Files.writeString(Paths.get(argObject.getFailDirectory(), name, subDir, "descriptor.xml"), descriptorText);
                    // copy file tree
                    Paths.get(argObject.getFailDirectory(), name, subDir, "files").toFile().mkdir();
                    for(String link : descriptor.getFiles()) {
                        var src = Paths.get(mainFile.getParent(), link);
                        if(src.toFile().exists()) {
                            Files.copy(src, Paths.get(argObject.getFailDirectory(), name, subDir, "files", link));
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
