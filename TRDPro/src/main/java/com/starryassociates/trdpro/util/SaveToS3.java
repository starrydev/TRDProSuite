package com.starryassociates.trdpro.util;

import com.starryassociates.core.config.ConfigManager;
import com.starryassociates.core.config.CustomLogger;
import com.starryassociates.trdpro.config.ServiceLocator;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class SaveToS3 {
    private final ServiceLocator serviceLocator;
    private final ConfigManager config;
    private final CustomLogger log;
    private final S3Client s3Client;
    private final String bucketName;

    public SaveToS3() {
        this.serviceLocator = ServiceLocator.getInstance();
        this.log = serviceLocator.getLogger(this.getClass());
        this.s3Client = serviceLocator.getS3Client();
        this.config = serviceLocator.getConfigManager();
        this.bucketName = config.getS3BucketNameSaveToS3();
    }

    public String upload(Path filePath, String outputFileName) {
        return uploadFile(filePath, bucketName, outputFileName);
    }

    public String upload(String payload, String outputFileName) {
        InputStream payloadStream = new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8));
        return uploadStream(payloadStream, payload.length(), bucketName, outputFileName);
    }

    private String uploadFile(Path filePath, String bucketName, String outputFileName) {
        try {
            PutObjectRequest putObjectRequest = createPutObjectRequest(bucketName, outputFileName);
            s3Client.putObject(putObjectRequest, filePath);
            return outputFileName;
        } catch (AwsServiceException | SdkClientException e) {
            logError(e);
            throw new RuntimeException(e);
        }
    }

    private String uploadStream(InputStream stream, long contentLength, String bucketName, String outputFileName) {
        try {
            PutObjectRequest putObjectRequest = createPutObjectRequest(bucketName, outputFileName);
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(stream, contentLength));
            return outputFileName;
        } catch (AwsServiceException | SdkClientException e) {
            logError(e);
            throw new RuntimeException(e);
        }
    }

    private PutObjectRequest createPutObjectRequest(String bucketName, String outputFileName) {
        return PutObjectRequest.builder()
                .bucket(bucketName)
                .key(outputFileName)
                .build();
    }

    private void logError(Exception e) {
        log.error("SaveToS3 Error: " + e.toString());
    }
}
