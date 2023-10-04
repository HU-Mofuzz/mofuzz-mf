package de.hub.mse.server.service.execution;

import java.io.File;
import java.util.function.Function;

public interface FilePersistence {

    void persistFile(String key, File file) throws Exception;

    void deleteFile(String key);

    FileAccess getFile(String key, Function<String, String> filenameMapper);
}
