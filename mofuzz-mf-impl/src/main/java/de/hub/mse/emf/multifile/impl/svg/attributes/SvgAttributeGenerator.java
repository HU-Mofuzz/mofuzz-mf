package de.hub.mse.emf.multifile.impl.svg.attributes;

import com.pholser.junit.quickcheck.random.SourceOfRandomness;

@FunctionalInterface
public interface SvgAttributeGenerator {

    String generateRandom(SourceOfRandomness source);
}
