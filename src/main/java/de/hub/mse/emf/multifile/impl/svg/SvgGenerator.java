package de.hub.mse.emf.multifile.impl.svg;

import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import de.hub.mse.emf.multifile.base.AbstractGenerator;
import de.hub.mse.emf.multifile.base.GeneratorConfig;
import de.hub.mse.emf.multifile.base.LinkPool;
import de.hub.mse.emf.multifile.base.emf.EmfCache;
import de.hub.mse.emf.multifile.base.emf.EmfUtil;
import lombok.SneakyThrows;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SvgGenerator extends AbstractGenerator<File, String, GeneratorConfig> {

    private static final float ATTRIB_GENERATE_CHANCE = 0.5f;

    private final ResourceSet resourceSet = new ResourceSetImpl();
    private final EPackage svgPackage;
    private final EClass svgClass;

    private final Map<String, String> nameMap = new HashMap<>();

    private DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    @SneakyThrows
    protected SvgGenerator(GeneratorConfig config) {
        super(File.class, config);

        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new XMIResourceFactoryImpl());


        Resource svgPackageResource = resourceSet.getResource(URI.createURI("src/main/resources/model/svg.ecore", false), true);
        Resource xlinkPackageResource = resourceSet.getResource(URI.createURI("src/main/resources/model/xlink.ecore", false), true);

        svgPackage = (EPackage)svgPackageResource.getContents().get(0);
        EPackage xlinkPackage = (EPackage)xlinkPackageResource.getContents().get(0);

        this.resourceSet.getPackageRegistry().put(svgPackage.getNsURI(), svgPackage);
        this.resourceSet.getPackageRegistry().put(xlinkPackage.getNsURI(), xlinkPackage);
        this.resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("svg", new XMLResourceFactoryImpl());

        EcoreUtil.resolveAll(svgPackage);

        svgClass = (EClass) svgPackage.getEClassifier("SvgType");

        var aType = (EClass) svgPackage.getEClassifier("AType");
        nameMap.putAll(aType.getEAllReferences().stream()
                .collect(Collectors.toMap(ref -> ref.getEType().getName(), EReference::getName))
        );
        aType.getEStructuralFeatures().forEach(feature -> {
            var attribName = feature.getName();
            String targetName = null;
            for(var annotation : feature.getEAnnotations()) {
                for(var detail : annotation.getDetails()) {
                    if(detail.getKey().equals("name")) {
                        targetName = detail.getValue();
                    }
                }
            }
            if(targetName != null) {
                nameMap.put(attribName, targetName);
            }
        });
    }

    @Override
    protected LinkPool<String> collectLinksFromConfig(SourceOfRandomness sourceOfRandomness) {
        var pool = new LinkPool<String>();

        if(config.shouldUseExistingFiles()) {
            // extract links
            pool.addAll(SvgUtil.extractLinks(config));
        } else if(config.shouldGenerateFiles()) {
            // generate files
            for (int i = 0; i < config.getObjectsToGenerate(); i++) {
                String fileName = SvgUtil.getRandomFileName();
                String objectId = SvgUtil.getRandomObjectId();
                try {
                    String content = generateRandomSvgObject(objectId, sourceOfRandomness);
                    Files.writeString(Paths.get(config.getWorkingDirectory(), fileName), content,
                            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    pool.add(SvgUtil.getObjectIdForFile(fileName, objectId));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            throw new IllegalArgumentException("Can't generate SVGs for PreparationMode: "+config.getPreparationMode().name());
        }

        return pool;
    }

    private String generateRandomSvgObject(String objectId, SourceOfRandomness source) throws IOException, ParserConfigurationException, SAXException, TransformerException {
        EClass clazz;
        do  {
            clazz = EmfUtil.getRandomEClassFromPackage(svgClass, source);
        } while (clazz.getEIDAttribute() == null);

        EObject svgObject = generateEObject(svgClass, source);
        EObject object = generateEObject(clazz, source);
        object.eSet(clazz.getEIDAttribute(), objectId);

        var builder = documentBuilderFactory.newDocumentBuilder();

        var svgDoc = builder.parse(new InputSource(
                new StringReader(SvgUtil.eObjectToXml(resourceSet, nameMap, svgObject))));

        var svgNode = (Element)svgDoc.getElementsByTagName("svg").item(0);

        var objectDoc = builder.parse(new InputSource(
                new StringReader(SvgUtil.eObjectToXml(resourceSet, nameMap, object))));

        var objectNode = objectDoc.getDocumentElement();
        svgDoc.adoptNode(objectNode);

        svgNode.appendChild(objectNode);

        DOMSource domSource = new DOMSource(svgDoc);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.transform(domSource, result);
        return writer.toString();
    }

    private EObject generateEObject(EClass clazz, SourceOfRandomness randomness) {
        var object =  svgPackage.getEFactoryInstance().create(clazz);
        for(var attribute : EmfCache.getAttributes(clazz)) {
            if(randomness.nextFloat() < ATTRIB_GENERATE_CHANCE) {
                EmfUtil.setRandomValueForAttribute(object, attribute, randomness);
            }
        }

        return object;
    }

    @Override
    public File internalExecute(SourceOfRandomness sourceOfRandomness, LinkPool<String> linkPool) throws Exception {
        File target = new File(Paths.get(config.getWorkingDirectory(), SvgUtil.getRandomFileName()).toUri());

        if (!target.createNewFile()) {
           throw new IllegalStateException("Target file could not be created!");
        }

        EObject svgObject = generateEObject(svgClass, sourceOfRandomness);

        var builder = documentBuilderFactory.newDocumentBuilder();
        documentBuilderFactory.setNamespaceAware(false);

        var svgDoc = builder.parse(new InputSource(
                new StringReader(SvgUtil.eObjectToXml(resourceSet, nameMap, svgObject))));

        var svgNode = (Element)svgDoc.getElementsByTagName("svg").item(0);

        var useElement = svgDoc.createElement("use");
        useElement.setAttributeNS("https://www.w3.org/1999/xlink", "xlink:href",
                linkPool.getRandomLink(sourceOfRandomness));

        svgNode.appendChild(useElement);

        DOMSource domSource = new DOMSource(svgDoc);
        FileWriter writer = new FileWriter(target);
        StreamResult result = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.transform(domSource, result);

        return target;
    }
}
