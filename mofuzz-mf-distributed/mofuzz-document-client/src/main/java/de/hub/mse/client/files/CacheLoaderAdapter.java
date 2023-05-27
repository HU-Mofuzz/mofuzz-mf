package de.hub.mse.client.files;

import com.google.common.cache.CacheLoader;

public class CacheLoaderAdapter<K, V> extends CacheLoader<K, V> {

    private final Loader<K, V> mapper;

    public CacheLoaderAdapter(Loader<K, V> mapper) {
        this.mapper = mapper;
    }

    @Override
    public V load(K key) throws Exception {
        return mapper.load(key);
    }

    @FunctionalInterface
    public interface Loader<K, V> {
        V load(K key) throws Exception;
    }
}
