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
import org.eclipse.emf.ecore.xmi.impl.XMLInfoImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLMapImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SvgGenerator extends AbstractGenerator<Resource, String, GeneratorConfig> {

    private static final float ATTRIB_GENERATE_CHANCE = 0.5f;

    private final ResourceSet resourceSet = new ResourceSetImpl();
    private final EPackage svgPackage;
    private final EClass svgClass;

    @SneakyThrows
    protected SvgGenerator(GeneratorConfig config) {
        super(Resource.class, config);

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
    }

    @Override
    public Resource internalExecute(SourceOfRandomness sourceOfRandomness, LinkPool<String> linkPool) {
        return null;
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

    private String generateRandomSvgObject(String objectId, SourceOfRandomness source) throws IOException {
        EClass clazz;
        do  {
            clazz = EmfUtil.getRandomEClassFromPackage(svgPackage, source);
        } while (clazz.getEIDAttribute() == null);

        XMLResource modelResource = (XMLResource)resourceSet.createResource(URI.createFileURI("a.svg"));
        modelResource.setEncoding("UTF-8");

        EObject svgObject = generateEObject(svgClass, source);
        EObject object = generateEObject(clazz, source);
        object.eSet(clazz.getEIDAttribute(), objectId);

        modelResource.getContents().add(svgObject);
        modelResource.getContents().add(object);
        StringWriter writer = new StringWriter();
        modelResource.save(writer, Map.of(
                XMLResource.OPTION_SAVE_TYPE_INFORMATION, true,
                XMLResource.OPTION_XML_MAP, new SvgXmlMap(svgPackage)
        ));

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

    private static class SvgXmlMap extends XMLMapImpl {

        Map<String, String> nameMap = new HashMap<>();


        public SvgXmlMap(EPackage svgPackage) {
            super();
            var aType = (EClass) svgPackage.getEClassifier("AType");
            nameMap.putAll(aType.getEAllReferences().stream()
                    .collect(Collectors.toMap(ref -> ref.getEType().getName(), EReference::getName))
            );

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
