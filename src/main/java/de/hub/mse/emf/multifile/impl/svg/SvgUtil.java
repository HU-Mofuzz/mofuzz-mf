package de.hub.mse.emf.multifile.impl.svg;

import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import de.hub.mse.emf.multifile.base.GeneratorConfig;
import de.hub.mse.emf.multifile.base.emf.EmfCache;
import de.hub.mse.emf.multifile.base.emf.EmfUtil;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.impl.EDataTypeImpl;
import org.eclipse.emf.ecore.impl.EFactoryImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMLInfoImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLMapImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.transform.Source;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static de.hub.mse.emf.multifile.base.emf.EmfUtil.RESOURCE_SET;

@UtilityClass
public class SvgUtil {

    private Pattern SVG_ID_PATTERN = Pattern.compile("(id=\"[a-zA-Z0-9]+\")");

    public static final EPackage SVG_PACKAGE;
    public static final EPackage XLINK_PACKAGE;
    public static final EClass SVG_CLASS;
    public static final EAttribute VIEW_BOX_ATTRIBUTE;
    public static final EAttribute WIDTH_ATTRIBUTE;
    public static final EAttribute HEIGHT_ATTRIBUTE;
    public static final EClass USE_CLASS;
    public static final Map<String, String> TYPE_NAME_MAPPING = new HashMap<>();

    static {
        Resource svgPackageResource = RESOURCE_SET.getResource(URI.createURI("src/main/resources/model/svg.ecore", false), true);
        Resource xlinkPackageResource = RESOURCE_SET.getResource(URI.createURI("src/main/resources/model/xlink.ecore", false), true);

        SVG_PACKAGE = (EPackage) svgPackageResource.getContents().get(0);
        XLINK_PACKAGE = (EPackage) xlinkPackageResource.getContents().get(0);

        RESOURCE_SET.getPackageRegistry().put(SVG_PACKAGE.getNsURI(), SVG_PACKAGE);
        RESOURCE_SET.getPackageRegistry().put(XLINK_PACKAGE.getNsURI(), XLINK_PACKAGE);
        RESOURCE_SET.getResourceFactoryRegistry().getExtensionToFactoryMap().put("svg", new XMLResourceFactoryImpl());

        EcoreUtil.resolveAll(SVG_PACKAGE);
        EcoreUtil.resolveAll(XLINK_PACKAGE);


        SVG_CLASS = (EClass) SVG_PACKAGE.getEClassifier("SvgType");
        USE_CLASS = (EClass) SVG_PACKAGE.getEClassifier("UseType");

        VIEW_BOX_ATTRIBUTE = EmfCache.getAttributes(SVG_CLASS).stream().filter(a -> a.getName().equals("viewBox")).findFirst().orElseThrow(IllegalStateException::new);
        WIDTH_ATTRIBUTE = EmfCache.getAttributes(SVG_CLASS).stream().filter(a -> a.getName().equals("width")).findFirst().orElseThrow(IllegalStateException::new);
        HEIGHT_ATTRIBUTE = EmfCache.getAttributes(SVG_CLASS).stream().filter(a -> a.getName().equals("height")).findFirst().orElseThrow(IllegalStateException::new);


        var aType = (EClass) SVG_PACKAGE.getEClassifier("AType");
        TYPE_NAME_MAPPING.putAll(aType.getEAllReferences().stream()
                .collect(Collectors.toMap(ref -> ref.getEType().getName(), EReference::getName))
        );
        aType.getEStructuralFeatures().forEach(feature -> {
            var attribName = feature.getName();
            String targetName = null;
            for (var annotation : feature.getEAnnotations()) {
                for (var detail : annotation.getDetails()) {
                    if (detail.getKey().equals("name")) {
                        targetName = detail.getValue();
                    }
                }
            }
            if (targetName != null) {
                TYPE_NAME_MAPPING.put(attribName, targetName);
            }
        });
    }

    public Set<String> extractLinks(GeneratorConfig config) {

        Set<String> links = new HashSet<>();

        for (String file : config.getExistingFiles()) {
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
        return fileName + "#" + objectId;
    }

    public static String getRandomObjectId() {
        return UUID.randomUUID().toString()
                .replace("-", "");
    }

    public EClass getRandomSVGReference(SourceOfRandomness source) {
        return EmfUtil.getRandomReferenceEClassFromEClass(SVG_CLASS, source);
    }

    public EObject generateUseElement(String link, SourceOfRandomness source) {
        var object = SVG_PACKAGE.getEFactoryInstance().create(USE_CLASS);

        var href = EmfCache.getAttributes(USE_CLASS).stream().filter(a -> a.getName().equals("href")).findFirst().orElseThrow(IllegalStateException::new);
        var height = EmfCache.getAttributes(USE_CLASS).stream().filter(a -> a.getName().equals("height")).findFirst().orElseThrow(IllegalStateException::new);
        var width = EmfCache.getAttributes(USE_CLASS).stream().filter(a -> a.getName().equals("width")).findFirst().orElseThrow(IllegalStateException::new);
        var x = EmfCache.getAttributes(USE_CLASS).stream().filter(a -> a.getName().equals("x")).findFirst().orElseThrow(IllegalStateException::new);
        var y = EmfCache.getAttributes(USE_CLASS).stream().filter(a -> a.getName().equals("y")).findFirst().orElseThrow(IllegalStateException::new);

        var hrefClass = (EDataTypeImpl) XLINK_PACKAGE.getEClassifier("HrefType");

        object.eSet(href, link );

        EmfUtil.setRandomValueForAttribute(object, height, source);
        EmfUtil.setRandomValueForAttribute(object, width, source);
        EmfUtil.setRandomValueForAttribute(object, x, source);
        EmfUtil.setRandomValueForAttribute(object, y, source);

        return object;
    }

    public void addLinkAndAttributesToSvgElement(EObject svgNode, String link, SourceOfRandomness source) {


        EmfUtil.makeContain(svgNode, generateUseElement(link, source));


        // var useElement = svgNode.getOwnerDocument().createElement("use");
//
        // useElement.setAttributeNS("https://www.w3.org/1999/xlink", "xlink:href", link);
        // useElement.setAttribute("height", "100");
        // useElement.setAttribute("width", "100");
        // useElement.setAttribute("x", "100");
        // useElement.setAttribute("y", "100");
//
        // svgNode.appendChild(useElement);
    }
}
