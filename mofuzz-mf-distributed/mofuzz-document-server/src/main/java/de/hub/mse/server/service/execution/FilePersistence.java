package de.hub.mse.server.service.execution;

import java.io.File;

public interface FilePersistence {

    void persistFile(String key, File file) throws Exception;

    void deleteFile(String key);
}
