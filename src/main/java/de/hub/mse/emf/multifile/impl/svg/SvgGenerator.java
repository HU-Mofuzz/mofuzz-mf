package de.hub.mse.emf.multifile.impl.svg;

import com.pholser.junit.quickcheck.Pair;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import de.hub.mse.emf.multifile.base.AbstractGenerator;
import de.hub.mse.emf.multifile.base.GeneratorConfig;
import de.hub.mse.emf.multifile.base.LinkPool;
import de.hub.mse.emf.multifile.base.emf.EmfCache;
import de.hub.mse.emf.multifile.base.emf.EmfUtil;
import de.hub.mse.emf.multifile.impl.svg.attributes.AttributeGeneratorMap;
import de.hub.mse.emf.multifile.util.XmlUtil;
import lombok.SneakyThrows;
import org.eclipse.emf.ecore.*;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.print.DocFlavor;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static de.hub.mse.emf.multifile.impl.svg.SvgUtil.*;

public class SvgGenerator extends AbstractGenerator<File, String, GeneratorConfig> {

    private static final float ATTRIB_GENERATE_CHANCE = 0.5f;
    private int currentDepth;

    @SneakyThrows
    public SvgGenerator() {
        super(File.class, GeneratorConfig.getInstance());
    }
    @SneakyThrows
    public SvgGenerator(Class<File> type, GeneratorConfig config) {
        super(type,config);
    }

    @Override
    protected LinkPool<String> collectLinksFromConfig(SourceOfRandomness sourceOfRandomness) {
        var pool = new LinkPool<String>();

        if (config.shouldUseExistingFiles()) {
            // extract links
            pool.addAll(SvgUtil.extractLinkables(config));
        } else if (config.shouldGenerateFiles()) {
            // generate files
            for (int i = 0; i < config.getFilesToGenerate(); i++) {
                System.out.println("Generating " + (i + 1) + "/" + config.getFilesToGenerate());
                String fileName = SvgUtil.getRandomFileName();
                try {
                    var content = generateRandomSvgObject(sourceOfRandomness);
                    Files.writeString(Paths.get(config.getWorkingDirectory(), fileName), content.first,
                            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    content.second.stream().map(id -> getObjectIdForFile(fileName, id)).forEach(pool::add);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            throw new IllegalArgumentException("Can't generate SVGs for PreparationMode: " + config.getPreparationMode().name());
        }

        return pool;
    }


    private boolean addRandomObjectToSvgObject(EObject svgObject, String objectId, SourceOfRandomness source) {
        EClass objectClazz;
        do {
            //find object that can have an id to make it referencable later
            objectClazz = SvgUtil.getRandomSVGReference(source);
        } while (objectClazz.getEIDAttribute() == null);

        EObject object = generateEObject(objectClazz, source);
        object.eSet(objectClazz.getEIDAttribute(), objectId);

        return EmfUtil.makeContain(svgObject, object);
    }

    private Pair<String, Set<String>> generateRandomSvgObject(SourceOfRandomness source) throws IOException, ParserConfigurationException, SAXException, TransformerException {
        //generate base node
        EObject svgObject = generateEObject(SVG_CLASS, source);
        Set<String> aggregatedIds = new HashSet<>();

        String svgObjectId = SvgUtil.getRandomObjectId();
        svgObject.eSet(SVG_CLASS.getEIDAttribute(), svgObjectId);

        aggregatedIds.add((svgObjectId));

        //generate children
        for (int i = 0; i < config.getModelWidth(); i++) {
            String objectId = SvgUtil.getRandomObjectId();
            if (addRandomObjectToSvgObject(svgObject, objectId, source)) {
                aggregatedIds.add(objectId);
            } else {
                //try again
                i--;
            }
        }

        //a valid svg needs a view box, width and height and version + grammar
        svgObject.eSet(SvgUtil.VIEW_BOX_ATTRIBUTE, AttributeGeneratorMap.VIEW_BOX_GENERATOR.generateRandom(source));
        svgObject.eSet(SvgUtil.WIDTH_ATTRIBUTE, Integer.toString(Math.abs(source.nextInt())));
        svgObject.eSet(SvgUtil.HEIGHT_ATTRIBUTE, Integer.toString(Math.abs(source.nextInt())));

        //needed for export (write file)
        var svgXmlDoc = SvgUtil.eObjectToDocument(svgObject);

        var svgNode = (Element) svgXmlDoc.getElementsByTagName("svg").item(0);
        svgNode.setAttribute("version", "1.1");
        svgNode.setAttribute("xmlns", "http://www.w3.org/2000/svg");

        return new Pair<> (XmlUtil.documentToString(svgXmlDoc), aggregatedIds);
    }

    private EObject generateEObject(EClass clazz, SourceOfRandomness randomness) {
        if (clazz.getEPackage() != SVG_PACKAGE.getEFactoryInstance().getEPackage()) {
            return null;
        }
        // increase depth
        currentDepth++;

        // outer object
        var object = SVG_PACKAGE.getEFactoryInstance().create(clazz);
        for (var attribute : EmfCache.getAttributes(clazz)) {
            if (attribute.getName().startsWith("group") || attribute.getName().startsWith("mixed") || attribute.getName().startsWith("descTitleMetadata") || attribute.getName().startsWith("class")) {
                continue;
            }
            if (attribute.isRequired() || randomness.nextFloat() < ATTRIB_GENERATE_CHANCE) {
                EmfUtil.setRandomValueForAttribute(object, attribute, randomness);
            }
        }

        // inner object
        var requiredReferences = EmfCache.getRequiredContainmentReferences(clazz);
        if (requiredReferences.size() > 0 ||
                (currentDepth < config.getModelDepth() && !clazz.getEAllContainments().isEmpty())) {
            var remainingReferences = currentDepth > config.getModelDepth() ? 0 :
                    Math.max(0, config.getModelWidth() - requiredReferences.size());

            var referencesToCreate = new ArrayList<>(requiredReferences.stream().map(EReference::getEType)
                    .map(EClass.class::cast).collect(Collectors.toList()));
            for (int i = 0; i < remainingReferences; i++) {
                referencesToCreate.add(EmfUtil.getRandomReferenceEClassFromEClass(clazz, randomness));
            }
            var retry = 0;
            for (int i = 0; i < referencesToCreate.size(); i++) {
                var innerClass = referencesToCreate.get(i);
                var innerObject = generateEObject(innerClass, randomness);
                if (!EmfUtil.makeContain(object, innerObject)) {
                    i--;
                    if (++retry > 10) {
                        throw new Error("Endless loop detected!!");
                    }
                }
            }
        }
        currentDepth--;
        return object;
    }

    @Override
    public File internalExecute(SourceOfRandomness sourceOfRandomness, LinkPool<String> linkPool) throws Exception {
        File target = new File(Paths.get(config.getWorkingDirectory(), SvgUtil.getRandomFileName()).toUri());

        if (!target.createNewFile()) {
            throw new IllegalStateException("Target file could not be created!");
        }

        EObject svgObject = SVG_PACKAGE.getEFactoryInstance().create(SVG_CLASS);

        //a valid svg needs a view box, width and height and version + grammar
        svgObject.eSet(SvgUtil.VIEW_BOX_ATTRIBUTE, AttributeGeneratorMap.VIEW_BOX_GENERATOR.generateRandom(sourceOfRandomness));
        svgObject.eSet(SvgUtil.WIDTH_ATTRIBUTE, Integer.toString(Math.abs(sourceOfRandomness.nextInt())));
        svgObject.eSet(SvgUtil.HEIGHT_ATTRIBUTE, Integer.toString(Math.abs(sourceOfRandomness.nextInt())));

        int numberOfGeneratedLinks = 0;
        for (int i = 0; i < config.getModelWidth(); i++) {

            if (sourceOfRandomness.nextDouble() < config.getLinkProbability()
                    && numberOfGeneratedLinks < config.getLinkNumber()) {
                EmfUtil.makeContain(svgObject, generateUseElement(linkPool.getRandomLink(sourceOfRandomness), sourceOfRandomness));
                numberOfGeneratedLinks++;
            } else {
                String objectId = SvgUtil.getRandomObjectId();
                if (addRandomObjectToSvgObject(svgObject, objectId, sourceOfRandomness)) {
                    if (config.shouldGenerateLinks()) {
                        linkPool.add(getObjectIdForFile(target.getName(), objectId));
                    }
                } else {
                    i--; //try again
                }
            }
        }

        //if not all links were generated during normal execute
        for (int i = numberOfGeneratedLinks; i < config.getLinkNumber(); i++) {
            EmfUtil.makeContain(svgObject, generateUseElement(linkPool.getRandomLink(sourceOfRandomness), sourceOfRandomness));
        }


        var svgDoc = SvgUtil.eObjectToDocument(svgObject);


        var svgNode = (Element) svgDoc.getElementsByTagName("svg").item(0);

        svgNode.setAttribute("version", "1.1");
        svgNode.setAttribute("xmlns", "http://www.w3.org/2000/svg");
        svgNode.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");

        Files.writeString(target.toPath(), XmlUtil.documentToString(svgDoc));

        return target;
    }
}
