package com.starryassociates.core.config;

import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.retry.RetryMode;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.time.Duration;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DynamoDBConnectionPool {

    private static volatile DynamoDBConnectionPool instance;
    private DynamoDbClient dynamoDBClient;
    private static final Lock lock = new ReentrantLock();
    private long lastRefreshTime = 0L;
    private static final long REFRESH_INTERVAL = 3600000L; // 1 hour

    private DynamoDBConnectionPool() {
        this.dynamoDBClient = createDynamoDBClient();
        this.lastRefreshTime = System.currentTimeMillis();
    }

    public static DynamoDBConnectionPool getInstance() {
        if (instance == null) {
            synchronized (DynamoDBConnectionPool.class) {
                if (instance == null) {
                    instance = new DynamoDBConnectionPool();
                }
            }
        }
        return instance;
    }

    public DynamoDbClient getClient() {
        refreshClientIfNeeded();
        return dynamoDBClient;
    }

    private void refreshClientIfNeeded() {
        lock.lock();
        try {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastRefreshTime > REFRESH_INTERVAL) {
                if (dynamoDBClient != null) {
                    dynamoDBClient.close();
                }
                dynamoDBClient = createDynamoDBClient();
                lastRefreshTime = currentTime;
            }
        } finally {
            lock.unlock();
        }
    }

    private DynamoDbClient createDynamoDBClient() {
        // Configure HTTP Client
        ApacheHttpClient httpClient = (ApacheHttpClient) ApacheHttpClient.builder()
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(10))
                .socketTimeout(Duration.ofSeconds(30))
                .build();

        // Configure Retry Policy
        ClientOverrideConfiguration overrideConfig = ClientOverrideConfiguration.builder()
                .retryPolicy(RetryMode.STANDARD)  // Use the standard retry mode
                .apiCallTimeout(Duration.ofSeconds(30))
                .apiCallAttemptTimeout(Duration.ofSeconds(10))
                .build();

        // Build DynamoDbClient
        return DynamoDbClient.builder()
                .httpClient(httpClient)
                .overrideConfiguration(overrideConfig)
                .region(Region.US_EAST_1) // Replace with your region
                .build();
    }
}
