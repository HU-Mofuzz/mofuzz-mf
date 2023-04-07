package de.hub.mse.emf.multifile.impl.svg;

import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMLInfoImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLMapImpl;

import java.util.Objects;

class SvgXmlMap extends XMLMapImpl {

    public SvgXmlMap() {
        super();
    }

    @Override
    public XMLResource.XMLInfo getInfo(ENamedElement element) {
        var info = super.getInfo(element);
        if (info == null) {
            info = new XMLInfoImpl();
        }
        var name = SvgUtil.TYPE_NAME_MAPPING.getOrDefault(element, element.getName());

        //find namespace and put before name
        var namespacePredicate = element.getEAnnotations().stream()
                .filter(a -> a.getDetails() != null)
                .map(a -> a.getDetails().get("namespace"))
                .filter(Objects::nonNull)
                .filter(ns -> !ns.startsWith("#"))
                .findFirst();
        if (namespacePredicate.isPresent()) {
            String namespace = namespacePredicate.get().substring(namespacePredicate.get().lastIndexOf("/")+1);
            name = namespace + ":" + name;
        }

        info.setName(name);

        if (name.equals("content")) {
            info.setXMLRepresentation(XMLResource.XMLInfo.CONTENT);
        }
        return info;
    }
}
