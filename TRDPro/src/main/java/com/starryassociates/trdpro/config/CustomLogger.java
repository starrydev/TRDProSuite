package com.starryassociates.trdpro.config;

import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CustomLogger {

    private static final ConcurrentMap<String, CustomLogger> instances = new ConcurrentHashMap<>();

    private final SnsClient snsClient;
    private final String infoTopicArn;
    private final String warnTopicArn;
    private final String debugTopicArn;
    private final String errorTopicArn;
    private final Set<String> enabledLogLevels;
    private final Logger logger;

    // Private constructor to enforce Singleton pattern
    private CustomLogger(Class<?> clazz, String infoTopicArn, String warnTopicArn, String debugTopicArn, String errorTopicArn, Set<String> enabledLogLevels) {
        this.logger = LoggerFactory.getLogger(clazz);
        this.snsClient = SnsClient.builder().build();  // Initialize SnsClient
        this.infoTopicArn = infoTopicArn;
        this.warnTopicArn = warnTopicArn;
        this.debugTopicArn = debugTopicArn;
        this.errorTopicArn = errorTopicArn;
        this.enabledLogLevels = enabledLogLevels;
    }

    // Factory method to create or retrieve the singleton instance
    public static CustomLogger getInstance(Class<?> clazz, String infoTopicArn, String warnTopicArn, String debugTopicArn, String errorTopicArn, Set<String> enabledLogLevels) {
        return instances.computeIfAbsent(clazz.getName(),
                key -> new CustomLogger(clazz, infoTopicArn, warnTopicArn, debugTopicArn, errorTopicArn, enabledLogLevels));
    }
    public void info(String message, Object... args) {
        logger.info(message, args);
        if (enabledLogLevels.contains("INFO")) {
            sendSNSNotification(message, infoTopicArn, args);
        }
    }

    public void warn(String message, Object... args) {
        logger.warn(message, args);
        if (enabledLogLevels.contains("WARN")) {
            sendSNSNotification(message, warnTopicArn, args);
        }
    }

    public void debug(String message, Object... args) {
        logger.debug(message, args);
        if (enabledLogLevels.contains("DEBUG")) {
            sendSNSNotification(message, debugTopicArn, args);
        }
    }

    public void error(String message, Object... args) {
        logger.error(message, args);
        if (enabledLogLevels.contains("ERROR")) {
            sendSNSNotification(message, errorTopicArn, args);
        }
    }

    private void sendSNSNotification(String message, String topicArn, Object... args) {
        if (topicArn == null || topicArn.isEmpty()) {
            return;  // Do nothing if the topic ARN is not set
        }

        try {
            String formattedMessage = org.slf4j.helpers.MessageFormatter.arrayFormat(message, args).getMessage();
            PublishRequest publishRequest = PublishRequest.builder()
                    .topicArn(topicArn)
                    .message(formattedMessage)
                    .subject("Log Notification")
                    .build();

            snsClient.publish(publishRequest);
            logger.info("SNS notification sent successfully for topic: {}", topicArn);
        } catch (Exception e) {
            logger.error("Failed to send SNS notification for topic: {}", topicArn, e);
        }
    }
}
