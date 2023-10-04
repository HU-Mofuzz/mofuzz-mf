package de.hub.mse.server.service.execution;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import de.hub.mse.server.config.ServiceConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

@Slf4j
public class AwsFilePersistence implements FilePersistence {

    private final String bucketId;
    final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.EU_CENTRAL_1).build();

    public AwsFilePersistence(ServiceConfig config) {
        this.bucketId = config.getAwsBucketId();
    }
    @Override
    public void persistFile(String key, File file) throws IOException {
        try {
            s3.putObject(bucketId, key, file);
        } catch (AmazonServiceException e) {
            log.error("Error persisting file!", e);
            throw e;
        }
    }

    @Override
    public void deleteFile(String key) {
        try {
            s3.deleteObject(bucketId, key);
        } catch (AmazonServiceException e) {
            log.error("Error deleting file from persistence!", e);
        }
    }

    @Override
    public FileAccess getFile(String key, Function<String, String> filenameMapper) {
        var awsObject = s3.getObject(bucketId, key);
        if(awsObject == null) {
            return new EmptyFileAccess();
        } else {
            return new AwsFileAccess(filenameMapper.apply(key), awsObject.getObjectContent(), awsObject.getObjectMetadata().getContentLength());
        }
    }

    @Data
    @AllArgsConstructor
    private static class AwsFileAccess implements FileAccess {

        private String filename;
        private InputStream content;
        private long contentLength;
    }
}
