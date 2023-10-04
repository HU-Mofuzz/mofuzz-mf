package de.hub.mse.server.service.execution;

import java.io.InputStream;

public class EmptyFileAccess implements FileAccess {

    @Override
    public String getFilename() {
        return "";
    }

    @Override
    public InputStream getContent() {
        return InputStream.nullInputStream();
    }

    @Override
    public long getContentLength() {
        return 0;
    }
}
