package de.hub.mse.client.config;

import com.beust.jcommander.Parameter;
import lombok.Data;

import java.io.File;
import java.nio.file.Paths;

@Data
public class Config {

    @Parameter(names = "--backend", description = "Backend Host, e.g. http://localhost:8080 or https://1.1.1.1")
    private String backendHost;

    @Parameter(names = "--type", description = "Type of the client either <LibreOffice> or <MSOffice>")
    private String clientType;

    @Parameter(names = {"--workingDirectory"}, description = "Working directory where the test files will be generated")
    private String workingDirectory;

    @Parameter(names = {"--bucket-id"}, description = "Working directory where the test files will be generated")
    private String bucketId = "mofuzz-bucket1";

    @Parameter(names = {"--cache-size"}, description = "Amount of files in the local file cache")
    private int cacheSize = 10000;

    @Parameter(names = {"--help", "/h", "-h", "/?"}, description = "Print this argument description", help = true)
    private boolean help;

    @Parameter(description = "client id")
    private String clientId;

    private boolean isStringSet(String s) {
        return s!= null && !s.isEmpty();
    }

    private boolean isWorkingDirectorySet() {
        return isStringSet(workingDirectory);
    }
    public boolean isBackendHostSet() {
        return isStringSet(backendHost);
    }
    private boolean isClientTypeSet() {
        return isStringSet(clientType);
    }

    private boolean isBucketIdSet() {
        return isStringSet(bucketId);
    }

    private boolean isClientIdSet() {
        return isStringSet(clientId);
    }

    private boolean cacheSizeValid() {
        return cacheSize > 0;
    }

    public void validate() {
        if(!isWorkingDirectorySet()) {
            throw new IllegalArgumentException("Working directory must be set!");
        }
        if(!isBackendHostSet()) {
            throw new IllegalArgumentException("Backend host must be set!");
        }
        if(!isBucketIdSet()) {
            throw new IllegalArgumentException("Bucket Id must be set!");
        }
        if(!isClientTypeSet()) {
            throw new IllegalArgumentException("Client Type must be set!");
        }
        if(!isClientIdSet()) {
            throw new IllegalArgumentException("Client Id must be set!");
        }
        if(!cacheSizeValid()) {
            throw new IllegalArgumentException("Invalid cache size!");
        }
    }

    public File getWorkingDirAsFile() {
        if(workingDirectory == null) {
            return new File(System.getProperty("user.home"));
        } else {
            return Paths.get(workingDirectory).toFile();
        }
    }

}
