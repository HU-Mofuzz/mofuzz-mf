package de.hub.mse.emf.multifile.impl.svg;

import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import de.hub.mse.emf.multifile.base.GeneratorConfig;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMLInfoImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLMapImpl;
import ru.vyarus.java.generics.resolver.error.GenericsException;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
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

    public String eObjectToXml(ResourceSet resourceSet, Map<String, String> nameMap, EObject eObject) {
        XMLResource modelResource = (XMLResource)resourceSet.createResource(URI.createFileURI("a.svg"));
        modelResource.setEncoding("UTF-8");
        modelResource.getContents().add(eObject);
        StringWriter writer = new StringWriter();
        try {
            modelResource.save(writer, Map.of(
                    XMLResource.OPTION_SAVE_TYPE_INFORMATION, true,
                    XMLResource.OPTION_XML_MAP, new SvgXmlMap(nameMap)
            ));
        } catch (IOException e) {
            return StringUtils.EMPTY;
        }

        return writer.toString();
    }

    private static class SvgXmlMap extends XMLMapImpl {


        private final Map<String, String> nameMap;

        public SvgXmlMap(Map<String, String> nameMap) {
            super();
            this.nameMap = nameMap;
        }

        @Override
        public XMLResource.XMLInfo getInfo(ENamedElement element) {
            var info =  super.getInfo(element);
            String search;
            if(info == null) {
                info = new XMLInfoImpl();
                search = element.getName();
            } else {
                search = info.getName();
            }
            info.setName(nameMap.getOrDefault(search, search));
            return info;
        }
    }
}
