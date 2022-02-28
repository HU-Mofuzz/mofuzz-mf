package de.hub.mse.emf.multifile.impl.svg;

import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import de.hub.mse.emf.multifile.base.GeneratorConfig;
import de.hub.mse.emf.multifile.base.PreparationMode;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.Random;

@Ignore
public class SvgGeneratorTest {

    @Test
    public void testConstruct() {
        new SvgGenerator();
    }

    @Test
    public void testFileGeneration() {
        var instance = GeneratorConfig.getInstance();
        instance.setWorkingDirectory("C:\\Users\\Laokoon\\Desktop\\Test");
        instance.setPreparationMode(PreparationMode.GENERATE_FILES);
        instance.setFilesToGenerate(1);
        var generator = new SvgGenerator();

        File file = generator.generate(new SourceOfRandomness(new Random()), null);
        System.out.println(file.getAbsolutePath());
    }

    @Test
    public void testReferenceClasses() {

        ResourceSet resourceSet = new ResourceSetImpl();

        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new XMIResourceFactoryImpl());


        Resource svgPackageResource = resourceSet.getResource(URI.createURI("src/main/resources/model/svg.ecore", false), true);
        Resource xlinkPackageResource = resourceSet.getResource(URI.createURI("src/main/resources/model/xlink.ecore", false), true);

        var svgPackage = (EPackage)svgPackageResource.getContents().get(0);
        EPackage xlinkPackage = (EPackage)xlinkPackageResource.getContents().get(0);

        resourceSet.getPackageRegistry().put(svgPackage.getNsURI(), svgPackage);
        resourceSet.getPackageRegistry().put(xlinkPackage.getNsURI(), xlinkPackage);
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("svg", new XMLResourceFactoryImpl());

        EcoreUtil.resolveAll(svgPackage);
        EcoreUtil.resolveAll(xlinkPackage);

        xlinkPackage.getEClassifiers().forEach(clazz -> System.out.println("XLink: "+ clazz.getName()));
        svgPackage.getEClassifiers().forEach(clazz -> System.out.println("SVG: "+ clazz.getName()));

        var xlinkClasses = xlinkPackage.getEClassifiers().stream().map(EClassifier::getName).toList();
        var svgClasses = svgPackage.getEClassifiers().stream().map(EClassifier::getName).toList();

        for(String clazz : xlinkClasses) {
            if(svgClasses.contains(clazz)) {
                System.out.println("Both: "+clazz);
            }
        }

    }

    private String findTargetName(EStructuralFeature feature) {

        return "";
    }

    @Test
    public void attributeNameTest() {
        ResourceSet resourceSet = new ResourceSetImpl();

        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new XMIResourceFactoryImpl());


        Resource svgPackageResource = resourceSet.getResource(URI.createURI("src/main/resources/model/svg.ecore", false), true);
        Resource xlinkPackageResource = resourceSet.getResource(URI.createURI("src/main/resources/model/xlink.ecore", false), true);

        var svgPackage = (EPackage)svgPackageResource.getContents().get(0);
        EPackage xlinkPackage = (EPackage)xlinkPackageResource.getContents().get(0);

        resourceSet.getPackageRegistry().put(svgPackage.getNsURI(), svgPackage);
        resourceSet.getPackageRegistry().put(xlinkPackage.getNsURI(), xlinkPackage);
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("svg", new XMLResourceFactoryImpl());

        EcoreUtil.resolveAll(svgPackage);

        var aType = (EClass) svgPackage.getEClassifier("AType");

        aType.getEStructuralFeatures().forEach(eStructuralFeature -> {
            var attribName = eStructuralFeature.getName();
            var targetName = findTargetName(eStructuralFeature);
            System.out.println(attribName +" -> "+ targetName);

        });
    }

}