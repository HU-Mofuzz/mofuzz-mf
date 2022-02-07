package de.hub.mse.emf.multifile.impl.svg;

import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import de.hub.mse.emf.multifile.base.GeneratorConfig;
import de.hub.mse.emf.multifile.base.emf.EmfCache;
import de.hub.mse.emf.multifile.base.emf.EmfUtil;
import de.hub.mse.emf.multifile.util.XmlUtil;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
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
@Slf4j
public class SvgUtil {

    private Pattern SVG_ID_PATTERN = Pattern.compile("(id=\"[a-zA-Z0-9]+\")");

    public static final EPackage SVG_PACKAGE;
    public static final EPackage XLINK_PACKAGE;
    public static final EClass SVG_CLASS;
    public static final EAttribute VIEW_BOX_ATTRIBUTE;
    public static final EAttribute WIDTH_ATTRIBUTE;
    public static final EAttribute HEIGHT_ATTRIBUTE;
    public static final EClass USE_CLASS;
    public static final Map<ENamedElement, String> TYPE_NAME_MAPPING = new HashMap<>();

    static {
        Resource svgPackageResource = RESOURCE_SET.getResource(URI.createURI("svg.ecore", false), true);
        Resource xlinkPackageResource = RESOURCE_SET.getResource(URI.createURI("xlink.ecore", false), true);

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


        var eClasses = SVG_PACKAGE.getEClassifiers().stream().filter(f -> f instanceof EClass).map(EClass.class::cast).toList();

        eClasses.forEach(clazz -> clazz.getEAllReferences().forEach(ref -> TYPE_NAME_MAPPING.putIfAbsent(ref.getEType(), ref.getName())));

        eClasses.forEach(clazz -> clazz.getEStructuralFeatures().forEach(feature -> {
            var attribName = feature.getName();
            String targetName = "";
            for (var annotation : feature.getEAnnotations()) {
                for (var detail : annotation.getDetails()) {
                  if (detail.getKey().equals("name")) {
                        targetName = detail.getValue();
                    }
                }
            }
            if (!StringUtils.isBlank(targetName)) {
                TYPE_NAME_MAPPING.putIfAbsent(feature, targetName);
            }
        }));
    }

    public Set<String> extractLinkables(GeneratorConfig config) {

        Set<String> linkables = new HashSet<>();

        for (String file : config.getExistingFiles()) {
            linkables.addAll(
                    extractLinkables(
                            Paths.get(config.getWorkingDirectory(), file).toFile()
                    )
            );
        }

        return linkables;
    }

    public Set<String> extractLinkables(File file) {
        var linkables = new HashSet<String>();
        try {
            String svgString = Files.readString(file.toPath());
            Matcher matcher = SVG_ID_PATTERN.matcher(svgString);
            while (matcher.find()) {
                String found = matcher.group()
                        .replace("id=", "")
                        .replace("\"", "");
                linkables.add(getObjectIdForFile(file.getName(), found));
            }
        } catch (IOException e) {
            log.error("Error extracting linkables from file: " + file.getAbsolutePath(), e);
        }
        return linkables;
    }

    public Set<String> extractUseLinksRecursive(File file) {
        return extractUseLinksRecursiveInternal(file, new HashSet<>());
    }

    private Set<String> extractUseLinksRecursiveInternal(File file, Set<String> searchedFiles) {
        searchedFiles.add(file.getName());
        var fileElement = XmlUtil.documentFromFile(file).getDocumentElement();
        var links = new HashSet<String>();
        var useLinks = extractAllUseLinksFromElementTree(fileElement);
        for (var useLink : useLinks) {
            if (!searchedFiles.contains(useLink)) {
                links.addAll(extractUseLinksRecursiveInternal(Paths.get(file.getParent(), useLink).toFile(), searchedFiles));
            }
        }
        links.add(file.getName());
        return links;
    }

    private Set<String> extractAllUseLinksFromElementTree(Element element) {
        var links = new HashSet<String>();
        for (int i = 0; i < element.getChildNodes().getLength(); i++) {
            var childNode = element.getChildNodes().item(i);
            if (childNode instanceof Element childElement &&
                    "use".equals(childElement.getTagName()) && childElement.hasAttribute("href")) {
                var objectId = childElement.getAttribute("href");
                var fileName = getFilenameFromObjectId(objectId);
                links.add(fileName);
            } else if (childNode instanceof Element childElement && childNode.hasChildNodes()) {
                links.addAll(extractAllUseLinksFromElementTree(childElement));
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

    public String getFilenameFromObjectId(String objectId) {
        return objectId.split("#")[0];
    }

    public static String getRandomObjectId() {
        var id = UUID.randomUUID().toString()
                .replace("-", "");
        while (CharUtils.isAsciiNumeric(id.charAt(0))) {
            id = id.substring(1);
        }
        return id;
    }

    public EClass getRandomSVGReference(SourceOfRandomness source) {
        return EmfUtil.getRandomReferenceEClassFromEClass(SVG_CLASS, source);
    }

    public EObject generateUseElement(String link, SourceOfRandomness source) {
        var object = SVG_PACKAGE.getEFactoryInstance().create(USE_CLASS);

        var href = EmfCache.getAttributeForClass(USE_CLASS, "href").orElseThrow(IllegalStateException::new);
        var height = EmfCache.getAttributeForClass(USE_CLASS, "height").orElseThrow(IllegalStateException::new);
        var width = EmfCache.getAttributeForClass(USE_CLASS, "width").orElseThrow(IllegalStateException::new);
        var x = EmfCache.getAttributeForClass(USE_CLASS, "x").orElseThrow(IllegalStateException::new);
        var y = EmfCache.getAttributeForClass(USE_CLASS, "y").orElseThrow(IllegalStateException::new);

        object.eSet(href, link);

        EmfUtil.setRandomValueForAttribute(object, height, source);
        EmfUtil.setRandomValueForAttribute(object, width, source);
        EmfUtil.setRandomValueForAttribute(object, x, source);
        EmfUtil.setRandomValueForAttribute(object, y, source);

        return object;
    }

    private String eObjectToXmlString(EObject eObject) {
        XMLResource modelResource = (XMLResource) RESOURCE_SET.createResource(URI.createFileURI("a.svg"));
        modelResource.setEncoding("UTF-8");
        modelResource.getContents().add(eObject);
        StringWriter writer = new StringWriter();
        try {
            modelResource.save(writer, Map.of(
                    XMLResource.OPTION_SAVE_TYPE_INFORMATION, false,
                    XMLResource.OPTION_DOM_USE_NAMESPACES_IN_SCOPE, true,
                    XMLResource.OPTION_XML_MAP, new SvgXmlMap()
            ));
        } catch (IOException e) {
            return StringUtils.EMPTY;
        }

        return writer.toString();
    }

    public Document eObjectToDocument(EObject eObject) {
        return XmlUtil.stringToDocument(eObjectToXmlString(eObject));
    }

    public void clearChildren(Element element) {
        while (element.getFirstChild() != null) {
            element.removeChild(element.getFirstChild());
        }
    }

    public Set<String> extractLinkedFilesRecursive(File file) {
        Set<String> linkedFiles = new HashSet<>();
        linkedFiles.add(file.getName());
        if (file.exists() && file.isFile()) {
            for (String objectId : extractLinkables(file)) {
                var fileName = getFilenameFromObjectId(objectId);
                var linkedFile = Paths.get(file.getParent(), fileName).toFile();
                linkedFiles.addAll(extractLinkedFilesRecursive(linkedFile));
            }
        }
        return linkedFiles;
    }
}
