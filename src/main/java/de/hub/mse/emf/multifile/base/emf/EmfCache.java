package de.hub.mse.emf.multifile.base.emf;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import lombok.experimental.UtilityClass;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@UtilityClass
public class EmfCache {

    private final LoadingCache<EClass, Set<EAttribute>> ATTRIBUTE_CACHE = CacheBuilder.newBuilder()
            .softValues()
            .build(new CacheLoader<>() {
                @Override
                public Set<EAttribute> load(EClass key) throws Exception {
                    return ImmutableSet.copyOf(key.getEAllAttributes());
                }
            });

    public Set<EAttribute> getAttributes(EClass clazz) {
        try {
            return ATTRIBUTE_CACHE.get(clazz);
        } catch (ExecutionException e) {
            return Collections.emptySet();
        }
    }
}
