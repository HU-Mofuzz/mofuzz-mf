package de.hub.mse.emf.multifile;

import de.hub.mse.emf.multifile.util.XmlUtil;
import lombok.SneakyThrows;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Element;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;

public class EcoreFilter {

    private final String pathToEcore = "C:\\Users\\juene\\IdeaProjects\\mofuzz-mf\\src\\test\\resources\\model\\svg.ecore";
    private final String pathToResult = "C:\\Users\\juene\\IdeaProjects\\mofuzz-mf\\src\\test\\resources\\model\\svg_new.ecore";

    int found = 0;

    @SneakyThrows
    @Test
    @Ignore
    public void filterEcore() {
        var ecoreFile = new File(pathToEcore);

        var ecoreString = Files.readString(ecoreFile.toPath(), StandardCharsets.UTF_8);
        var ecoreDoc = XmlUtil.stringToDocument(ecoreString);
        System.out.println("Starting to filter");
        filterChild(ecoreDoc.getDocumentElement());
        System.out.println("Filtering done");

        var outString = XmlUtil.documentToString(ecoreDoc);

        var outFile = new File(pathToResult);
        Files.writeString(outFile.toPath(), outString, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        System.out.println("Found: "+found);
    }
/**<eStructuralFeatures xsi:type="ecore:EAttribute"
 * name="space"
 * eType="ecore:EEnum ../../org.eclipse.emf.ecore/model/XMLNamespace.ecore#//SpaceType1">
 *
 *<eStructuralFeatures xsi:type="ecore:EAttribute" name="base" eType="ecore:EDataType http://www.eclipse.org/emf/2003/XMLType#//AnyURI">
 * **/
    private boolean filterChild(Element element) {
        if(element.getTagName().equals("eStructuralFeatures") &&  element.hasAttribute("eType")) {
            var eType = element.getAttribute("eType");
            return eType != null && (
                        eType.endsWith("http://www.eclipse.org/emf/2003/XMLType#//AnyURI") ||
                        eType.contains("org.eclipse.emf.ecore/model/XMLNamespace.ecore")
                    );
        }

        var children = element.getChildNodes();
        var childrenToRemove = new HashSet<Element>();
        for(int i = 0; i < children.getLength(); i++) {
            var child = children.item(i);
            if(child instanceof Element) {
                var childElement = (Element) child;
                if(filterChild(childElement)) {
                    found++;
                    childrenToRemove.add(childElement);
                }
            }
        }
        childrenToRemove.forEach(element::removeChild);
        return false;
    }
}
