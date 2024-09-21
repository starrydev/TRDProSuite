package com.starryassociates.trdpro.jobs.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.time.LocalDateTime;

@DynamoDbBean
public class JobExecutionState {
    private String executionId;
    private String status;
    private LocalDateTime lastUpdateTime;

    public JobExecutionState() {
        // Default constructor for DynamoDB mapper
    }

    public JobExecutionState(String executionId, String status) {
        this.executionId = executionId;
        this.status = status;
        this.lastUpdateTime = LocalDateTime.now();
    }

    @DynamoDbPartitionKey
    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(LocalDateTime lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public void updateStatus(String newStatus) {
        this.status = newStatus;
        this.lastUpdateTime = LocalDateTime.now();
    }
}
