package com.starryassociates.trdpro.jobs.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.time.LocalDateTime;

@DynamoDbBean
public class JobInfo {
    public enum Status {
        SUCCESS,
        FAILURE,
        DISABLED,
        RUNNING
    }

    private String name;
    private LocalDateTime lastRunTime;
    private LocalDateTime nextRunTime;
    private int defaultInterval; // In minutes
    private Status lastRunStatus;
    private boolean enabled;
    private int multiplier;
    private String lambdaFunctionName;
    private String snsArn;
    private boolean isSnsEnabled;

    public static final int MAX_DELAY_IN_MINUTES = 240; // 4 hours

    public JobInfo() {
        this.multiplier = 1;
        this.lastRunStatus = Status.SUCCESS; // Default status
    }

    public JobInfo(String name, int defaultInterval, boolean enabled, String lambdaFunctionName, String snsArn, boolean isSnsEnabled) {
        this.name = name;
        this.defaultInterval = defaultInterval;
        this.enabled = enabled;
        this.lambdaFunctionName = lambdaFunctionName;
        this.snsArn = snsArn;
        this.isSnsEnabled = isSnsEnabled;
        this.multiplier = 1;
        this.lastRunTime = null;
        this.nextRunTime = LocalDateTime.now().plusMinutes(defaultInterval);
        this.lastRunStatus = enabled ? Status.SUCCESS : Status.DISABLED;
    }

    @DynamoDbPartitionKey
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getLastRunTime() {
        return lastRunTime;
    }

    public void setLastRunTime(LocalDateTime lastRunTime) {
        this.lastRunTime = lastRunTime;
    }

    public LocalDateTime getNextRunTime() {
        return nextRunTime;
    }

    public void setNextRunTime(LocalDateTime nextRunTime) {
        this.nextRunTime = nextRunTime;
    }

    public int getDefaultInterval() {
        return defaultInterval;
    }

    public void setDefaultInterval(int defaultInterval) {
        this.defaultInterval = defaultInterval;
    }

    public Status getLastRunStatus() {
        return lastRunStatus;
    }

    public void setLastRunStatus(Status lastRunStatus) {
        this.lastRunStatus = lastRunStatus;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            this.lastRunStatus = Status.DISABLED;
        }
    }

    public int getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(int multiplier) {
        this.multiplier = multiplier;
    }

    public String getLambdaFunctionName() {
        return lambdaFunctionName;
    }

    public void setLambdaFunctionName(String lambdaFunctionName) {
        this.lambdaFunctionName = lambdaFunctionName;
    }

    public String getSnsArn() {
        return snsArn;
    }

    public void setSnsArn(String snsArn) {
        this.snsArn = snsArn;
    }

    public boolean isSnsEnabled() {
        return isSnsEnabled;
    }

    public void setSnsEnabled(boolean snsEnabled) {
        isSnsEnabled = snsEnabled;
    }

    public void startJob() {
        if (this.lastRunStatus != Status.RUNNING) {
            this.lastRunStatus = Status.RUNNING;
            this.lastRunTime = LocalDateTime.now();
        } else {
            throw new IllegalStateException("Job is already running");
        }
    }

    public boolean isDueForRun() {
        return enabled && this.lastRunStatus != Status.RUNNING && LocalDateTime.now().isAfter(nextRunTime);
    }
}
