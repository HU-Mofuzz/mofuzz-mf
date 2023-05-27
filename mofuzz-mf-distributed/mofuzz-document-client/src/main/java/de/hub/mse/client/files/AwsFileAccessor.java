package de.hub.mse.client.files;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static de.hub.mse.client.MofuzzDocumentClientApplication.CONFIG;

public class AwsFileAccessor implements FileAccessor{

    private final String bucketId;
    final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.EU_CENTRAL_1).build();

    public AwsFileAccessor() {
        this.bucketId = CONFIG.getBucketId();
    }

    public void obtainFile(String id, File targetFile) throws IOException {
        var awsObject = s3.getObject(bucketId, id);
        try(InputStream is = awsObject.getObjectContent();
            FileOutputStream fos = new FileOutputStream(targetFile)) {
            IOUtils.copy(is, fos);
        }
    }
}
