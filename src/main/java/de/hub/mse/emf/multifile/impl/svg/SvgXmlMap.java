package de.hub.mse.emf.multifile.impl.svg;

import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMLInfoImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLMapImpl;

class SvgXmlMap extends XMLMapImpl {

    public SvgXmlMap() {
        super();
    }

    @Override
    public XMLResource.XMLInfo getInfo(ENamedElement element) {
        var info = super.getInfo(element);
        String search;
        if (info == null) {
            info = new XMLInfoImpl();
            search = element.getName();
        } else {
            search = info.getName();
        }
        info.setName(SvgUtil.TYPE_NAME_MAPPING.getOrDefault(search, search));
        return info;
    }
}
