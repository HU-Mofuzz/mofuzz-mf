package de.hub.mse.emf.multifile.impl.svg.attributes;

import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

public class AttributeGeneratorMapTest {
    @Test
    public void pathTest() {

        AttributeGeneratorMap attributeGeneratorMap = new AttributeGeneratorMap();

        String result = attributeGeneratorMap.get("d").generateRandom(new SourceOfRandomness(new Random()));

        Assert.assertTrue(result != null);
        System.out.println(result);

    }
}
