package de.hub.mse.emf.multifile.impl.svg;

import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import de.hub.mse.emf.multifile.base.AbstractGenerator;
import de.hub.mse.emf.multifile.base.GeneratorConfig;
import de.hub.mse.emf.multifile.base.LinkPool;
import de.hub.mse.emf.multifile.base.emf.EmfCache;
import de.hub.mse.emf.multifile.base.emf.EmfUtil;
import lombok.SneakyThrows;
import org.eclipse.emf.ecore.*;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static de.hub.mse.emf.multifile.impl.svg.SvgUtil.SVG_CLASS;
import static de.hub.mse.emf.multifile.impl.svg.SvgUtil.SVG_PACKAGE;

public class SvgGenerator extends AbstractGenerator<File, String, GeneratorConfig> {

    private static final float ATTRIB_GENERATE_CHANCE = 0.5f;

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
            throw new IllegalArgumentException("Can't generate SVGs for PreparationMode: " + config.getPreparationMode().name());
        }

        return pool;
    }

    private String generateRandomSvgObject(String objectId, SourceOfRandomness source) throws IOException, ParserConfigurationException, SAXException, TransformerException {
        //generate base node
        EObject svgObject = generateEObject(SVG_CLASS, source);

        //generate children
        for (int i = 0; i < config.getModelWidth(); i++) {
            EClass objectClazz;
            do {
                //find object that can have an id to make it referencable later
                objectClazz = SvgUtil.getRandomSVGReference(source);
            } while (objectClazz.getEIDAttribute() == null);

            EObject object = generateEObject(objectClazz, source);
            object.eSet(objectClazz.getEIDAttribute(), objectId);

            if (!EmfUtil.makeContain(svgObject, object)) {
                //try with next one
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

        return XmlUtil.documentToString(svgXmlDoc);
    }

    private EObject generateEObject(EClass clazz, SourceOfRandomness randomness) {
        // increase depth
        currentDepth++;

        // outer object
        var object = SVG_PACKAGE.getEFactoryInstance().create(clazz);
        for (var attribute : EmfCache.getAttributes(clazz)) {
            if (randomness.nextFloat() < ATTRIB_GENERATE_CHANCE) {
                EmfUtil.setRandomValueForAttribute(object, attribute, randomness);
            }
        }

        // inner object
        if (currentDepth < config.getModelDepth() && !clazz.getEAllContainments().isEmpty()) {
            for (int i = 0; i < randomness.nextInt(config.getModelWidth()); i++) {
                var innerClass = EmfUtil.getRandomReferenceEClassFromEClass(clazz, randomness);
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


        var svgDoc = XmlUtil.eObjectToDocument(svgObject);

        SvgUtil.addLinkAndAttributesToSvgElement(svgDoc, linkPool.getRandomLink(sourceOfRandomness));

        Files.writeString(target.toPath(), XmlUtil.documentToString(svgDoc));

        return target;
    }
}
