package de.hub.mse.client.files;

import com.google.common.cache.*;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

import static de.hub.mse.client.MofuzzDocumentClientApplication.CONFIG;

@Slf4j
public class FileCache implements RemovalListener<String, File> {

    private static final String XLSX_FILE_ENDING = ".xlsx";

    private final File cacheDir;
    private final FileAccessor fileAccessor;

    private final LoadingCache<String, File> fileCache;

    public FileCache(File cacheDir) {
        this.cacheDir = cacheDir;
        this.fileAccessor = new AwsFileAccessor();
        this.fileCache = CacheBuilder.newBuilder()
                .maximumSize(CONFIG.getCacheSize())
                .removalListener(this)
                .build(new CacheLoaderAdapter<>(this::load));
    }

    private File load(String key) throws Exception {
        var filename = key + XLSX_FILE_ENDING;
        var targetFile = Paths.get(cacheDir.getAbsolutePath(), filename).toFile();
        fileAccessor.obtainFile(key, targetFile);
        return targetFile;
    }

    public File getOrLoad(String id) {
        try {
            return fileCache.get(id);
        } catch (ExecutionException e) {
            log.error("Error loading file \""+id+"\"", e);
            return null;
        }
    }

    @Override
    public void onRemoval(RemovalNotification<String, File> notification) {
        var file = notification.getValue();
        if(file != null && file.exists() && !file.delete()) {
            log.error("Error deleting file \""+file.getName()+"\" from cache dir: "+cacheDir.getAbsolutePath());
        }
    }
}
