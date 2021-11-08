package de.hub.mse.emf.multifile.impl.svg;

import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import de.hub.mse.emf.multifile.base.GeneratorConfig;
import lombok.experimental.UtilityClass;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import ru.vyarus.java.generics.resolver.error.GenericsException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class SvgUtil {

    private Pattern SVG_ID_PATTERN = Pattern.compile("(id=\"[a-zA-Z0-9]+\")");

    public Set<String> extractLinks(GeneratorConfig config) {

        Set<String> links = new HashSet<>();

        for(String file : config.getExistingFiles()) {
            try {
                var svgString = Files.readString(
                        Paths.get(config.getWorkingDirectory(), file)
                );

                Matcher matcher = SVG_ID_PATTERN.matcher(svgString);
                while (matcher.find()) {
                    String found = matcher.group()
                            .replace("id=", "")
                            .replace("\"", "");
                    links.add(getObjectIdForFile(file, found));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return links;
    }

    public String getRandomFileName() {
        return UUID.randomUUID().toString() + ".svg";
    }

    public String getObjectIdForFile(String fileName, String objectId) {
        return fileName+"#"+objectId;
    }

    public static String getRandomObjectId() {
        return UUID.randomUUID().toString()
                .replace("-", "");
    }
}
