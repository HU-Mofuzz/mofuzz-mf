package de.hub.mse.emf.multifile.impl.svg;

import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import de.hub.mse.emf.multifile.base.AbstractGenerator;
import de.hub.mse.emf.multifile.base.GeneratorConfig;
import de.hub.mse.emf.multifile.base.LinkPool;
import de.hub.mse.emf.multifile.base.emf.EmfCache;
import de.hub.mse.emf.multifile.base.emf.EmfUtil;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.emf.ecore.*;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static de.hub.mse.emf.multifile.impl.svg.SvgUtil.*;

public class SvgGenerator extends AbstractGenerator<File, String, GeneratorConfig> {

    private static final float ATTRIB_GENERATE_CHANCE = 0.5f;
    private static final float LINK_USE_CHANCE = 0.5f;

    private int currentDepth;

    @SneakyThrows
    public SvgGenerator() {
        super(File.class, GeneratorConfig.getInstance());
    }

    @Override
    protected LinkPool<String> collectLinksFromConfig(SourceOfRandomness sourceOfRandomness) {
        var pool = new LinkPool<String>();

        if (config.shouldUseExistingFiles()) {
            // extract links
            pool.addAll(SvgUtil.extractLinks(config));
        } else if (config.shouldGenerateFiles()) {
            // generate files
            for (int i = 0; i < config.getFilesToGenerate(); i++) {
                String fileName = SvgUtil.getRandomFileName();
                try {
                    var content = generateRandomSvgObject(sourceOfRandomness);
                    Files.writeString(Paths.get(config.getWorkingDirectory(), fileName), content.getLeft(),
                            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    content.getRight().stream().map(id -> getObjectIdForFile(fileName, id)).forEach(pool::add);

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
        EmfUtil.setRandomValueForAttribute(svgObject, SvgUtil.VIEW_BOX_ATTRIBUTE, source);
        EmfUtil.setRandomValueForAttribute(svgObject, SvgUtil.WIDTH_ATTRIBUTE, source);
        EmfUtil.setRandomValueForAttribute(svgObject, SvgUtil.HEIGHT_ATTRIBUTE, source);

        //needed for export (write file)
        var svgXmlDoc = XmlUtil.eObjectToDocument(svgObject);

        var svgNode = (Element) svgXmlDoc.getElementsByTagName("svg").item(0);
        svgNode.setAttribute("version", "1.1");
        svgNode.setAttribute("xmlns", "http://www.w3.org/2000/svg");

        return Pair.of(XmlUtil.documentToString(svgXmlDoc), aggregatedIds);
    }

    private EObject generateEObject(EClass clazz, SourceOfRandomness randomness) {
        // increase depth
        currentDepth++;

        // outer object
        var object = SVG_PACKAGE.getEFactoryInstance().create(clazz);
        for (var attribute : EmfCache.getAttributes(clazz)) {
            var metaData = attribute.getEAnnotation("http:///org/eclipse/emf/ecore/util/ExtendedMetaData");
            String prefix;
            if(metaData != null && metaData.getDetails() != null && metaData.getDetails().get("namespace") != null &&
                    metaData.getDetails().get("namespace").equals("http://www.w3.org/1999/xlink")) {
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
            var remainingReferences = currentDepth >  config.getModelDepth()? 0 :
                    Math.max(0, config.getModelWidth() - requiredReferences.size());

            var referencesToCreate = new ArrayList<>(requiredReferences.stream().map(EReference::eClass).toList());
            for (int i = 0; i < remainingReferences; i++) {
                referencesToCreate.add(EmfUtil.getRandomReferenceEClassFromEClass(clazz, randomness));
            }

            for (int i = 0; i < referencesToCreate.size(); i++) {
                var innerClass = referencesToCreate.get(i);
                var innerObject = generateEObject(innerClass, randomness);
                if(!EmfUtil.makeContain(object, innerObject)) {
                    i--;
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

        EObject svgObject = generateEObject(SVG_CLASS, sourceOfRandomness);

        //a valid svg needs a view box, width and height and version + grammar
        EmfUtil.setRandomValueForAttribute(svgObject, SvgUtil.VIEW_BOX_ATTRIBUTE, sourceOfRandomness);
        EmfUtil.setRandomValueForAttribute(svgObject, SvgUtil.WIDTH_ATTRIBUTE, sourceOfRandomness);
        EmfUtil.setRandomValueForAttribute(svgObject, SvgUtil.HEIGHT_ATTRIBUTE, sourceOfRandomness);



        for (int i = 0; i < config.getModelWidth(); i++) {

            if (sourceOfRandomness.nextDouble() < LINK_USE_CHANCE) {

                EmfUtil.makeContain(svgObject, generateUseElement(linkPool.getRandomLink(sourceOfRandomness),sourceOfRandomness));
            } else {
                String objectId = SvgUtil.getRandomObjectId();
                if (addRandomObjectToSvgObject(svgObject, objectId, sourceOfRandomness)) {

                    linkPool.add(getObjectIdForFile(target.getName(), objectId));
                } else {
                    i--; //try again
                }
            }
        }

        var svgDoc = XmlUtil.eObjectToDocument(svgObject);


        var svgNode = (Element) svgDoc.getElementsByTagName("svg").item(0);

        svgNode.setAttribute("version", "1.2");
        svgNode.setAttribute("xmlns", "http://www.w3.org/2000/svg");
        svgNode.setAttribute("xmlns:xlink", "https://www.w3.org/1999/xlink");

        Files.writeString(target.toPath(), XmlUtil.documentToString(svgDoc));

            return target;
        }
    }
