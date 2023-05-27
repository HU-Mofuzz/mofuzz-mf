package de.hub.mse.client.files;

import java.io.File;
import java.io.IOException;

public interface FileAccessor {
    void obtainFile(String id, File targetFile) throws IOException;
}
