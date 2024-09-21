package com.starryassociates.trdpro.jobs;

import com.starryassociates.trdpro.jobs.model.JobExecutionState;
import com.starryassociates.trdpro.jobs.model.JobInfo;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

import java.util.ArrayList;
import java.util.List;

public class JobService {

    private final DynamoDbTable<JobExecutionState> stateTable;
    private final DynamoDbTable<JobInfo> jobTable;

    public JobService() {
        // Initialize the DynamoDB enhanced client
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder().build();
        this.stateTable = enhancedClient.table("JobExecutionState", TableSchema.fromBean(JobExecutionState.class));
        this.jobTable = enhancedClient.table("JobInfo", TableSchema.fromBean(JobInfo.class));
    }

    public boolean acquireGlobalLock() {
        try {
            JobExecutionState state = new JobExecutionState("GlobalExecutionState", "RUNNING");
            stateTable.putItem(state);
            return true;
        } catch (ConditionalCheckFailedException e) {
            // Lock is already held
            return false;
        }
    }

    public void releaseGlobalLock() {
        stateTable.deleteItem(Key.builder().partitionValue("GlobalExecutionState").build());
    }

    public boolean isGlobalLockActive() {
        JobExecutionState state = stateTable.getItem(Key.builder().partitionValue("GlobalExecutionState").build());
        return state != null && "RUNNING".equals(state.getStatus());
    }

    public List<JobInfo> getAllJobs() {
        List<JobInfo> jobs = new ArrayList<>();
        jobTable.scan(ScanEnhancedRequest.builder().build()).items().forEach(jobs::add);
        return jobs;
    }

    public void updateJobStatus(JobInfo job, String status){
        if ("Success".equalsIgnoreCase(status)) {
            job.setMultiplier(1); // Reset on success
            job.setLastRunStatus(JobInfo.Status.SUCCESS);
        } else {
            int multiplier = job.getMultiplier();
            job.setMultiplier(multiplier++);
            job.setLastRunStatus(JobInfo.Status.FAILURE);
        }
        int nextInterval = job.getDefaultInterval() * job.getMultiplier();
        if (nextInterval > JobInfo.MAX_DELAY_IN_MINUTES) {
            nextInterval = JobInfo.MAX_DELAY_IN_MINUTES;
        }
        job.setNextRunTime(job.getLastRunTime().plusMinutes(nextInterval));

        jobTable.putItem(job);
    }
}
