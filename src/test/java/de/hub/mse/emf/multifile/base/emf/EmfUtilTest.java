package de.hub.mse.emf.multifile.base.emf;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;
import org.junit.Test;

import static org.junit.Assert.*;

public class EmfUtilTest {

    @Test
    public void getAType() {
        final ResourceSet resourceSet = new ResourceSetImpl();
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new XMIResourceFactoryImpl());
        Resource svgPackageResource = resourceSet.getResource(URI.createURI("src/test/resources/model/svg.ecore", false), true);
        Resource xlinkPackageResource = resourceSet.getResource(URI.createURI("src/test/resources/model/xlink.ecore", false), true);

        EPackage svgPackage = (EPackage)svgPackageResource.getContents().get(0);
        EPackage xlinkPackage = (EPackage)xlinkPackageResource.getContents().get(0);

        resourceSet.getPackageRegistry().put(svgPackage.getNsURI(), svgPackage);
        resourceSet.getPackageRegistry().put(xlinkPackage.getNsURI(), xlinkPackage);
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("svg", new XMLResourceFactoryImpl());

        EcoreUtil.resolveAll(svgPackage);

        EClass aType = (EClass) svgPackage.getEClassifier("AType");

        aType.getEAllReferences().stream()
                .forEach(eReference -> {
                    var name = eReference.getName();
                    var typeName = eReference.getEType().getName();
                    System.out.println(typeName + " -> " + name);
                });
    }

}