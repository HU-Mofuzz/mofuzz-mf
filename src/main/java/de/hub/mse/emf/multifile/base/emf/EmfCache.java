package de.hub.mse.emf.multifile.base.emf;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import lombok.experimental.UtilityClass;
import org.checkerframework.checker.units.qual.C;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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

    private final Map<EClass, LoadingCache<String, EReference>> CONTAINMENT_MAPPING = new HashMap<>();

    private final Map<String, String> nameMap = new HashMap<>();

    public Set<EAttribute> getAttributes(EClass clazz) {
        try {
            return ATTRIBUTE_CACHE.get(clazz);
        } catch (ExecutionException e) {
            return Collections.emptySet();
        }
    }

    public EReference getContainmentReference(EClass container, String typeName) {
        LoadingCache<String, EReference> cache;
        if (CONTAINMENT_MAPPING.containsKey(container)) {
            cache = CONTAINMENT_MAPPING.get(container);
        } else {
            cache = CacheBuilder.newBuilder()
                    .softValues()
                    .build(new CacheLoader<>() {
                        @Override
                        public EReference load(String type) {
                            for(var ref : container.getEAllContainments()) {
                                if (ref.getEType().getName().equals(type)) {
                                    return  ref;
                                }
                            }
                            return null;
                        }
                    });
        }
        try {
            return cache.get(typeName);
        } catch (ExecutionException e) {
            return null;
        }
    }
}
