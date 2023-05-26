package de.hub.mse.server.service.execution;

import java.io.File;
import java.io.IOException;

public interface FilePersistence {

    void persistFile(String key, File file) throws IOException;

    void deleteFile(String key);
}
