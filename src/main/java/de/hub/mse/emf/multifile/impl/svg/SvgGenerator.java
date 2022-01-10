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

    @SneakyThrows
    public SvgGenerator() {
        super(File.class, GeneratorConfig.getInstance());
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
            clazz = SvgUtil.getRandomSVGReference(source);
        } while (clazz.getEIDAttribute() == null);

        EObject svgObject = generateEObject(SVG_CLASS, source);
        EObject object = generateEObject(clazz, source);
        object.eSet(clazz.getEIDAttribute(), objectId);

       /**
        * this does not work and throws nullpointer
        * svgObject.eSet(svgObject.eContainmentFeature(),object); **/

        var svgDoc = XmlUtil.eObjectToDocument(svgObject);

        var svgNode = (Element)svgDoc.getElementsByTagName("svg").item(0);

        var objectDoc = XmlUtil.eObjectToDocument(object);

        var objectNode = objectDoc.getDocumentElement();

        svgDoc.adoptNode(objectNode);

        XmlUtil.clearChildren(svgNode);
        svgNode.appendChild(objectNode);

        svgNode.setAttribute("viewBox", "0 0 100 100");
        svgNode.setAttribute("width", "100");
        svgNode.setAttribute("height", "100");
        svgNode.setAttribute("version", "1.1");
        svgNode.setAttribute("xmlns", "http://www.w3.org/2000/svg");

        return XmlUtil.documentToString(svgDoc);
    }

    private EObject generateEObject(EClass clazz, SourceOfRandomness randomness) {
        var object =  SVG_PACKAGE.getEFactoryInstance().create(clazz);
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

        EObject svgObject = generateEObject(SVG_CLASS, sourceOfRandomness);

        var svgDoc = XmlUtil.eObjectToDocument(svgObject);

        SvgUtil.addLinkAndAttributesToSvgElement(svgDoc, linkPool.getRandomLink(sourceOfRandomness));

        Files.writeString(target.toPath(), XmlUtil.documentToString(svgDoc));

        return target;
    }
}
