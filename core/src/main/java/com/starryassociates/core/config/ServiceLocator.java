package com.starryassociates.core.config;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ServiceLocator {

    private static ServiceLocator instance;
    private final DynamoDBConnectionPool dynamoDBConnectionPool;
    private final S3ConnectionPool s3ConnectionPool;
    private final ConfigManager configManager;
    private final String infoArn;
    private final String warnArn;
    private final String debugArn;
    private final String errorArn;
    private final Set<String> enabledLogLevels;
    // Single instance for DynamoDbEnhancedClient
    private final DynamoDbEnhancedClient dynamoDbEnhancedClient;

    private ServiceLocator() {
        // Initialize the service pools, logger, and config manager
        dynamoDBConnectionPool = DynamoDBConnectionPool.getInstance();
        s3ConnectionPool = S3ConnectionPool.getInstance();
        configManager = ConfigManager.getInstance();


        // Load SNS ARNs from properties file
        infoArn = configManager.getProperty("sns.info.arn");
        warnArn = configManager.getProperty("sns.warn.arn");
        debugArn = configManager.getProperty("sns.debug.arn");
        errorArn = configManager.getProperty("sns.error.arn");

        // Load enabled log levels from properties
        String enabledLogLevelsString = configManager.getProperty("log.sns.enabled.levels");
        this.enabledLogLevels = new HashSet<>(Arrays.asList(enabledLogLevelsString.split(",")));

        // Initialize DynamoDbEnhancedClient once during construction
        dynamoDbEnhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDBConnectionPool.getClient())
                .build();
    }

    public static synchronized ServiceLocator getInstance() {
        if (instance == null) {
            instance = new ServiceLocator();
        }
        return instance;
    }

    public DynamoDbClient getDynamoDBClient() {
        return dynamoDBConnectionPool.getClient();
    }

    public DynamoDbEnhancedClient getDynamoDBEnhancedClient() {
        return dynamoDbEnhancedClient;
    }

    public S3Client getS3Client() {
        return s3ConnectionPool.getClient();
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public CustomLogger getLogger(Class<?> clazz) {
        return CustomLogger.getInstance(clazz, infoArn, warnArn, debugArn, errorArn, enabledLogLevels);
    }
    // Add other services as needed
}
