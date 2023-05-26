package de.hub.mse.emf.multifile;

public interface PoolBasedGeneratorConfig<L> extends IGeneratorConfig {

    LinkPool<L> getLinkPool();
}
