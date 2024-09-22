package com.starryassociates.core.config;

import com.starryassociates.core.util.KmsEncryptionHelper;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;

import java.io.InputStream;
import java.util.Properties;

public class ConfigManager {
    private static ConfigManager instance;
    private final Properties properties = new Properties();
    private final KmsEncryptionHelper encryptionHelper;
    private final SsmClient ssmClient;

    private ConfigManager(String environment) {
        try {
            // Load the appropriate properties file based on the environment
            String propertiesFile = String.format("application-%s.properties", environment);
            InputStream input = getClass().getClassLoader().getResourceAsStream(propertiesFile);

            if (input == null) {
                throw new IllegalArgumentException("Properties file not found: " + propertiesFile);
            }

            properties.load(input);

            // Initialize KMS Client
            KmsClient kmsClient = KmsClient.builder()
                    .region(Region.of(properties.getProperty("aws.region")))
                    .build();

            // Initialize KmsEncryptionHelper
            this.encryptionHelper = new KmsEncryptionHelper(kmsClient, properties.getProperty("crypto.key.id"));

            // Initialize SSM Client
            this.ssmClient = SsmClient.builder().build();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load configuration for environment: " + environment, e);
        }
    }

    // Singleton pattern for ConfigManager
    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            String environment = System.getenv("ENVIRONMENT");
            if (environment == null || environment.isEmpty()) {
                environment = "dev"; // Default to 'dev' if not specified
            }
            instance = new ConfigManager(environment);
        }
        return instance;
    }

    // Method to retrieve properties from properties file
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getS3BucketNameDefault() {
        return properties.getProperty("s3.bucket.name.default");
    }

    public String getS3BucketNameSaveToS3() {
        return properties.getProperty("s3.bucket.name.saveToS3");
    }

    public KmsEncryptionHelper getEncryptionHelper() {
        return encryptionHelper;
    }

    // New method to retrieve any parameter from SSM Parameter Store
    public String getParameterFromSSM(String parameterName, boolean withDecryption) {
        GetParameterRequest parameterRequest = GetParameterRequest.builder()
                .name(parameterName)
                .withDecryption(withDecryption)  // true if parameter is a SecureString
                .build();

        GetParameterResponse parameterResponse = ssmClient.getParameter(parameterRequest);
        return parameterResponse.parameter().value();
    }

    // Specific method to get the SQS Transaction Queue URL
    public String getTransactionQueueUrl() {
        return getParameterFromSSM("/app/transactionQueueUrl", false);  // false as this is not a SecureString
    }
}
