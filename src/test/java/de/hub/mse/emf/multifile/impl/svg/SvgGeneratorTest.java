package de.hub.mse.emf.multifile.impl.svg;

import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import de.hub.mse.emf.multifile.base.GeneratorConfig;
import de.hub.mse.emf.multifile.base.PreparationMode;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

public class SvgGeneratorTest {

    @Test
    public void testConstruct() {
        new SvgGenerator(GeneratorConfig.builder().build());
    }

    @Test
    public void testFileGeneration() {
        var generator = new SvgGenerator(GeneratorConfig.builder()
                .workingDirectory("C:\\Users\\juene\\Desktop\\Test")
                .preparationMode(PreparationMode.GENERATE_FILES)
                .objectsToGenerate(1)
                .build());
        generator.generate(new SourceOfRandomness(new Random()), null);
    }

}