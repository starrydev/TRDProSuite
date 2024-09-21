package com.starryassociates.trdpro.config;

import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.retry.RetryMode;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.time.Duration;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class S3ConnectionPool {

    private static S3ConnectionPool instance;
    private S3Client s3Client;
    private static final Lock lock = new ReentrantLock();
    private long lastRefreshTime = 0L;
    private static final long REFRESH_INTERVAL = 3600000L; // 1 hour

    private S3ConnectionPool() {
        // Initialize the S3 client with retry policies
        s3Client = createS3Client();
        lastRefreshTime = System.currentTimeMillis();
    }

    public static S3ConnectionPool getInstance() {
        if (instance == null) {
            synchronized (S3ConnectionPool.class) {
                if (instance == null) {
                    instance = new S3ConnectionPool();
                }
            }
        }
        return instance;
    }

    public S3Client getClient() {
        refreshClientIfNeeded();
        return s3Client;
    }

    private void refreshClientIfNeeded() {
        lock.lock();
        try {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastRefreshTime > REFRESH_INTERVAL) {
                s3Client = createS3Client();
                lastRefreshTime = currentTime;
            }
        } finally {
            lock.unlock();
        }
    }

    private S3Client createS3Client() {
        // Configure HTTP Client
        ApacheHttpClient httpClient = (ApacheHttpClient) ApacheHttpClient.builder()
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(10))
                .socketTimeout(Duration.ofSeconds(30))
                .build();

        // Configure Client Override Configuration
        ClientOverrideConfiguration overrideConfig = ClientOverrideConfiguration.builder()
                .retryPolicy(RetryMode.STANDARD)  // Use the standard retry mode
                .apiCallTimeout(Duration.ofSeconds(30))
                .apiCallAttemptTimeout(Duration.ofSeconds(10))
                .build();

        // Build S3Client
        return S3Client.builder()
                .httpClient(httpClient)
                .overrideConfiguration(overrideConfig)
                .region(Region.US_EAST_1) // Set your AWS region
                .build();
    }
}
