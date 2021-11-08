package de.hub.mse.emf.multifile.impl.svg;

import de.hub.mse.emf.multifile.base.GeneratorConfig;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.*;

public class SvgUtilTest {

    @Test
    public void extractLinks() {

        GeneratorConfig config = GeneratorConfig.builder()
                .workingDirectory("C:\\Users\\Laokoon\\Desktop\\Test\\test")
                .existingFiles(Arrays.asList(
                        "a.svg",
                        "b.svg"
                ))
                .build();
        SvgUtil.extractLinks(config);

    }
}