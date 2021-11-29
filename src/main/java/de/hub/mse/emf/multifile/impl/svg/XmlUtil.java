package de.hub.mse.emf.multifile.impl.svg;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.Map;

import static de.hub.mse.emf.multifile.base.emf.EmfUtil.RESOURCE_SET;

@UtilityClass
public class XmlUtil {

    private final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
    private final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();

    static {
        DOCUMENT_BUILDER_FACTORY.setNamespaceAware(false);
    }

    public Document stringToDocument(String str) {
        try {
            var builder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
            return builder.parse(new InputSource(
                    new StringReader(str)));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String documentToString(Document doc) {
        DOMSource domSource = new DOMSource(doc);
        StreamResult result = new StreamResult(new StringWriter());
        try {
            var transformer = TRANSFORMER_FACTORY.newTransformer();
            transformer.transform(domSource, result);
            return result.getWriter().toString();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return StringUtils.EMPTY;
    }

    private String eObjectToXmlString(EObject eObject) {
        XMLResource modelResource = (XMLResource)RESOURCE_SET.createResource(URI.createFileURI("a.svg"));
        modelResource.setEncoding("UTF-8");
        modelResource.getContents().add(eObject);
        StringWriter writer = new StringWriter();
        try {
            modelResource.save(writer, Map.of(
                    XMLResource.OPTION_SAVE_TYPE_INFORMATION, true,
                    XMLResource.OPTION_XML_MAP, new SvgXmlMap()
            ));
        } catch (IOException e) {
            return StringUtils.EMPTY;
        }

        return writer.toString();
    }

    public Document eObjectToDocument(EObject eObject) {
        return stringToDocument(eObjectToXmlString(eObject));
    }

    public void clearChildren(Element element) {
        while (element.getFirstChild() != null) {
            element.removeChild(element.getFirstChild());
        }
    }
}
