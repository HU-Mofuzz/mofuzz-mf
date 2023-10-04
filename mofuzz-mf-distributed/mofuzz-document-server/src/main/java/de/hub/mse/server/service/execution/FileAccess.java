package de.hub.mse.server.service.execution;

import java.io.InputStream;

public interface FileAccess extends AutoCloseable {

    String getFilename();

    InputStream getContent();

    /**
     * @return Content length in byte
     */
    long getContentLength();

    @Override
    default void close() throws Exception {
        getContent().close();
    }
}
