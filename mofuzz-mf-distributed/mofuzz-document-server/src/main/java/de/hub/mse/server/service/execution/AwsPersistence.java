package de.hub.mse.server.service.execution;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import de.hub.mse.server.config.ServiceConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

@Slf4j
public class AwsPersistence implements FilePersistence {

    private final String bucketId;
    final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.EU_CENTRAL_1).build();

    public AwsPersistence(ServiceConfig config) {
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
}
