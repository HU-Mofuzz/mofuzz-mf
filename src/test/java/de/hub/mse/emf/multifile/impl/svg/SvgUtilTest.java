package de.hub.mse.emf.multifile.impl.svg;

import de.hub.mse.emf.multifile.base.GeneratorConfig;
import org.eclipse.emf.ecore.EClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static de.hub.mse.emf.multifile.impl.svg.SvgUtil.SVG_PACKAGE;

public class SvgUtilTest {

    @Test
    public void extractLinks() {
        GeneratorConfig config = GeneratorConfig.getInstance();
        config.setWorkingDirectory("C:\\Users\\Laokoon\\Desktop\\Test\\test");
        config.setExistingFiles(Arrays.asList(
                "a.svg",
                "b.svg"
        ));
        SvgUtil.extractLinkables(config);

    }

    @Test
    public void extractTypeNameMapping() {
        Map<String,String> TYPE_NAME_MAPPING = new HashMap<>();

        var lst = SVG_PACKAGE.getEClassifiers().stream()
                .filter(f -> f instanceof EClass).map(EClass.class::cast)
                .collect(Collectors.toList());

        lst.forEach(clazz -> clazz.getEAllReferences().forEach(ref -> TYPE_NAME_MAPPING.putIfAbsent(  ref.getEType().getName(), ref.getName())));

        lst.forEach(clazz -> clazz.getEStructuralFeatures().forEach(feature -> {
            var attribName = feature.getName();
            String targetName = null;
            for (var annotation : feature.getEAnnotations()) {
                for (var detail : annotation.getDetails()) {
                    if (detail.getKey().equals("name")) {
                        targetName = detail.getValue();
                    }
                }
            }
            if (targetName != null) {
                TYPE_NAME_MAPPING.putIfAbsent(attribName, targetName);
            }
        }));


        System.out.println(TYPE_NAME_MAPPING.toString());
    }
}