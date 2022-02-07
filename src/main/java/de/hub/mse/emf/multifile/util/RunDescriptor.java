package de.hub.mse.emf.multifile.util;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;

import java.text.SimpleDateFormat;
import java.util.*;

@Data
public class RunDescriptor {

    public static final String ELEMENT_DESCRIPTOR = "descriptor";
    public static final String ELEMENT_FILES = "files";
    public static final String ELEMENT_FILE = "file";
    public static final String ELEMENT_PROPERTIES = "properties";
    public static final String ELEMENT_PROPERTY = "property";

    public static final String ATTRIBUTE_MAIN_FILE = "mainFile";
    public static final String ATTRIBUTE_NAME = "name";

    private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String PROPERTY_TIMESTAMP = "timestamp";

    private Set<String> files = new HashSet<>();
    private String mainFile = StringUtils.EMPTY;
    private final Map<String,  String> properties = new HashMap<>();

    public void setMainFile(String mainFile) {
        this.mainFile = mainFile;
        this.files.add(mainFile);
    }

    public void addProperty(String name, String value) {
        properties.put(name, value);
    }

    public Document toXML() {
        var doc = XmlUtil.newDocument();

        var descriptorElement = doc.createElement(ELEMENT_DESCRIPTOR);

        // files descriptor
        var filesElement = doc.createElement(ELEMENT_FILES);
        filesElement.setAttribute(ATTRIBUTE_MAIN_FILE, mainFile);
        files.stream()
                .map(file -> {
                    var fileElement = doc.createElement(ELEMENT_FILE);
                    fileElement.setTextContent(file);
                    return fileElement;
                })
                .forEach(filesElement::appendChild);
        descriptorElement.appendChild(filesElement);

        // properties
        var propertiesCopy = new HashMap<>(properties);
        propertiesCopy.put(PROPERTY_TIMESTAMP, new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Date()));

        var propertiesElement = doc.createElement(ELEMENT_PROPERTIES);
        propertiesCopy.entrySet().stream()
                .map(entry -> {
                    var propertyElement = doc.createElement(ELEMENT_PROPERTY);
                    propertyElement.setAttribute(ATTRIBUTE_NAME, entry.getKey());
                    propertyElement.setTextContent(entry.getValue());
                    return propertyElement;
                })
                .forEach(propertiesElement::appendChild);
        descriptorElement.appendChild(propertiesElement);

        doc.appendChild(descriptorElement);
        return doc;
    }

}
