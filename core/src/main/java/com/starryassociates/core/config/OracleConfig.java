package com.starryassociates.core.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.json.JSONObject;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

public class OracleConfig {

    private static HikariDataSource dataSource;
    private static ConfigManager configManager;

    // Static block to initialize HikariCP DataSource
    static {
        // Retrieve credentials from Secrets Manager
        String secret = getSecret();
        JSONObject secretJson = new JSONObject(secret);
        String username = secretJson.getString("username");
        String password = secretJson.getString("password");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(configManager.getProperty("oracle.url"));
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(100);
        config.setDriverClassName("oracle.jdbc.OracleDriver");

        dataSource = new HikariDataSource(config);
    }

    public OracleConfig() {
        this.configManager = ConfigManager.getInstance();
    }

    // Method to get the DataSource instance
    public static HikariDataSource getDataSource() {
        return dataSource;
    }

    // Method to close the DataSource when the app is done (Optional)
    public static void closeDataSource() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    public static String getSecret() {
        // Create a Secrets Manager client using the AWS SDK v2.x
        SecretsManagerClient client = SecretsManagerClient.builder().build();

        // Create a request to get the secret
        GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
                .secretId("oracleDBSecret")  // The secret name in AWS Secrets Manager
                .build();

        // Get the secret value response
        GetSecretValueResponse getSecretValueResult = client.getSecretValue(getSecretValueRequest);

        // Return the secret string
        return getSecretValueResult.secretString();
    }
}
