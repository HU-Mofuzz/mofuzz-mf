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

import java.util.*;
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

    private final LoadingCache<EClass, Set<EReference>> REQUIRED_CONTAINMENT_REFERENCES = CacheBuilder.newBuilder()
            .softValues()
            .build(new CacheLoader<>() {
                @Override
                public Set<EReference> load(EClass eClass) throws Exception {
                    return ImmutableSet.copyOf(eClass.getEAllContainments().stream()
                            .filter(EReference::isRequired).toList());
                }
            });

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
    
    public Optional<EAttribute> getAttributeForClass(EClass eClass, String attribute) {
        return getAttributes(eClass).stream()
                .filter(attrib -> attrib.getName().equals(attribute))
                .findFirst();
    }

    public Set<EReference> getRequiredContainmentReferences(EClass eClass) {
        try {
            return REQUIRED_CONTAINMENT_REFERENCES.get(eClass);
        } catch (ExecutionException e) {
            return Collections.emptySet();
        }
    }
}
