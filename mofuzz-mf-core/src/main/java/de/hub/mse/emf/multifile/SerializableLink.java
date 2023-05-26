package de.hub.mse.emf.multifile;

public interface SerializableLink<T> {

    T serializeLink();

    void deserializeLink(String workingDirectory, T serialized);
}
