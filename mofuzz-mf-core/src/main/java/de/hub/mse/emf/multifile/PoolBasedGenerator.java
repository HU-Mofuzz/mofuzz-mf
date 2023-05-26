package de.hub.mse.emf.multifile;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import lombok.Getter;

import java.io.File;
import java.util.Collection;

public abstract class PoolBasedGenerator<D, S, L extends SerializableLink<S>, C extends PoolBasedGeneratorConfig<L>> extends Generator<D> {

    @Getter
    protected final C config;

    protected PoolBasedGenerator(Class<D> type, C config) {
        super(type);
        this.config = config;
    }

    public abstract Collection<D> prepareLinkPool(SourceOfRandomness random);

    protected abstract D internalExecute(SourceOfRandomness random) throws Exception;

    public abstract File getWorkingDirFileForId(String name);

    public abstract void addLinkFromSerialized(S serialized);

    @Override
    public D generate(SourceOfRandomness random, GenerationStatus status) {
        try {
            return internalExecute(random);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
