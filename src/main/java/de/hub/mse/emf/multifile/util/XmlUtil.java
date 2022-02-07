package de.hub.mse.emf.multifile.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

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

    @SneakyThrows
    public Document newDocument() {
        return DOCUMENT_BUILDER_FACTORY.newDocumentBuilder().newDocument();
    }

    @SneakyThrows
    public Document documentFromFile(File file) {
        return DOCUMENT_BUILDER_FACTORY.newDocumentBuilder().parse(file);
    }
}
